package me.maxouxax.boulobot.sessions;

import com.github.philippheuer.events4j.simple.SimpleEventHandler;
import com.github.twitch4j.common.events.channel.ChannelGoLiveEvent;
import com.github.twitch4j.common.events.channel.ChannelGoOfflineEvent;
import me.maxouxax.boulobot.BOT;
import me.maxouxax.boulobot.tasks.TaskUpdateSessionMessage;
import me.maxouxax.boulobot.util.JSONReader;
import me.maxouxax.boulobot.util.JSONWriter;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class SessionManager {

    private final BOT bot;
    private final File SESSIONS_FOLDER;
    private final ArrayList<Session> sessions = new ArrayList<>();
    private Session currentSession;
    private ScheduledFuture scheduleViewerCheck;

    public SessionManager() {
        this.bot = BOT.getInstance();
        SESSIONS_FOLDER = new File("sessions" + File.separator);
        loadNotifications();
    }

    private void loadNotifications() {
        bot.getTwitchClient().getClientHelper().enableStreamEventListener(bot.getChannelName());
        bot.getTwitchClient().getEventManager().getEventHandler(SimpleEventHandler.class).onEvent(ChannelGoLiveEvent.class, channelGoLiveEvent -> {
            streamStarted(channelGoLiveEvent.getTitle(), channelGoLiveEvent.getGameId(), channelGoLiveEvent.getChannel().getId());
        });
        bot.getTwitchClient().getEventManager().getEventHandler(SimpleEventHandler.class).onEvent(ChannelGoOfflineEvent.class, channelGoOfflineEvent -> {
            streamEnded();
        });
    }

    public void streamStarted(String title, String gameId, String channelId) {
        try {
            if (getCurrentSession() != null) {
                bot.getErrorHandler().handleException(new Exception("A session is already running! Aborting!"));
            } else {
                bot.getLogger().log(Level.INFO, "> Stream started!");
                startNewSession(channelId, gameId, title);
            }
        } catch (Exception e) {
            bot.getErrorHandler().handleException(e);
        }
    }

    public void streamEnded() {
        try {
            if (getCurrentSession() == null) {
                bot.getErrorHandler().handleException(new Exception("No sessions were running! Aborting!"));
            } else {
                bot.getLogger().log(Level.INFO, "> Stream ended!");
                endSession();
            }
        } catch (Exception e) {
            bot.getErrorHandler().handleException(e);
        }
    }

    public void startNewSession(String channelId, String gameId, String title) {
        currentSession = new Session(System.currentTimeMillis(), channelId);
        sessions.add(currentSession);
        taskUpdateSessionMessage();
        currentSession.newGame(gameId);
        currentSession.setTitle(title);
        if (!currentSession.updateMessage()) {
            startRetrying();
        }
    }

    public void taskUpdateSessionMessage() {
        scheduleViewerCheck = bot.getScheduler().scheduleAtFixedRate(new TaskUpdateSessionMessage(currentSession.getChannelId()), 2, 2, TimeUnit.MINUTES);
    }

    private void startRetrying() {
        RetryTask retryTask = new RetryTask(currentSession);
        retryTask.setCallbackOnEachTry(session -> {
            retryTask.success(session.updateMessage());
        });
        retryTask.retryIn(10, TimeUnit.SECONDS);
    }

    public void endSession() {
        scheduleViewerCheck.cancel(false);
        if ((new Date().getTime() - currentSession.getStartDate()) > 1000 * 60 * 15) {
            //Si la session a duré plus de 15 minutes, alors on l'arrête normalement
            currentSession.endSession();
            saveSession(currentSession);
            deleteCurrentSession();
        } else {
            //Si la session a duré moins de 15 minutes, alors on essaie de la relancer pendant 10 minutes
            startRetrying();
        }
    }

    public void deleteCurrentSession() {
        this.currentSession = null;
    }

    public ArrayList<Session> getSessions() {
        return sessions;
    }

    public Session getCurrentSession() {
        return currentSession;
    }

    public Optional<Session> getSession(String uuid) {
        return sessions.stream().filter(session -> session.getUuid().toString().equalsIgnoreCase(uuid)).findFirst();
    }

    public boolean isSessionStarted() {
        return currentSession != null;
    }

    public void loadSessions() {
        if (!SESSIONS_FOLDER.exists()) {
            SESSIONS_FOLDER.mkdirs();
        }

        File[] allSessions = SESSIONS_FOLDER.listFiles();

        if (allSessions == null) return;

        for (File file : allSessions) {
            if (file.getName().endsWith(".json")) {
                try {
                    JSONReader reader = new JSONReader(file);
                    JSONArray array = reader.toJSONArray();

                    for (int i = 0; i < array.length(); i++) {
                        JSONObject object = array.getJSONObject(i);
                        long startTime = object.getLong("startDate");
                        String channelId = object.getString("channelId");
                        UUID uuid = UUID.fromString(object.getString("uuid"));
                        Session loadingSession = new Session(startTime, uuid, channelId);

                        loadingSession.setMaxViewers(object.getInt("maxViewers"));
                        loadingSession.setAvgViewers(object.getInt("avgViewers"));
                        loadingSession.setBansAndTimeouts(object.getInt("bansAndTimeouts"));
                        loadingSession.setCommandUsed(object.getInt("commandUsed"));
                        loadingSession.setEndDate(object.getLong("endDate"));
                        loadingSession.setMessageSended(object.getInt("messageSended"));
                        loadingSession.setNewViewers(object.getInt("newViewers"));
                        loadingSession.setNewFollowers(object.getInt("newFollowers"));
                        loadingSession.setCommandsUsed(decrushMap(object.getString("commandsUsed")));
                        loadingSession.setGameIds(decrushList(object.getString("gameIds")));
                        loadingSession.setTitle(object.getString("title"));

                        sessions.add(loadingSession);
                    }

                } catch (IOException e) {
                    bot.getErrorHandler().handleException(e);
                }
            }
        }
    }


    public void saveSession(Session session) {
        if (!SESSIONS_FOLDER.exists()) {
            SESSIONS_FOLDER.mkdirs();
        }

        File file = new File(SESSIONS_FOLDER, session.getUuid().toString() + ".json");
        JSONArray array = new JSONArray();

        JSONObject object = new JSONObject();
        object.accumulate("uuid", session.getUuid().toString());
        object.accumulate("channelId", session.getChannelId());
        object.accumulate("maxViewers", session.getMaxViewers());
        object.accumulate("avgViewers", session.getAvgViewers());
        object.accumulate("bansAndTimeouts", session.getBansAndTimeouts());
        object.accumulate("commandUsed", session.getCommandUsed());
        object.accumulate("startDate", session.getStartDate());
        object.accumulate("endDate", session.getEndDate());
        object.accumulate("messageSended", session.getMessageSended());
        object.accumulate("newViewers", session.getNewViewers());
        object.accumulate("newFollowers", session.getNewFollowers());
        object.accumulate("commandsUsed", crushMap(session.getCommandsUsed()));
        object.accumulate("gameIds", crushList(session.getGameIds()));
        object.accumulate("title", session.getTitle());

        array.put(object);

        try (JSONWriter writter = new JSONWriter(file)) {

            writter.write(array);
            writter.flush();

        } catch (IOException e) {
            bot.getErrorHandler().handleException(e);
        }
    }

    private String crushList(ArrayList<String> decrushedList) {
        StringBuilder str = new StringBuilder();
        decrushedList.forEach(s -> {
            str.append(s).append("&&&");
        });
        return str.toString();
    }

    private ArrayList<String> decrushList(String crushedList) {
        try {
            ArrayList<String> decrushedList = new ArrayList<>(Arrays.asList(crushedList.split("&&&")));
            return decrushedList;
        } catch (Exception e) {
            bot.getErrorHandler().handleException(e);
        }
        return new ArrayList<>();
    }

    private String crushMap(HashMap<String, Integer> decrushedMap) {
        StringBuilder str = new StringBuilder();
        decrushedMap.forEach((s, integer) -> {
            str.append(s).append("::").append(integer).append("&&&");
        });
        return str.toString();
    }

    private HashMap<String, Integer> decrushMap(String crushedMap) {
        try {
            HashMap<String, Integer> decrushedMap = new HashMap<>();
            List<String> cuttedStrings = Arrays.asList(crushedMap.split("&&&"));
            cuttedStrings.forEach(s -> {
                String[] values = s.split("::");
                if (values.length > 1) {
                    decrushedMap.put(values[0], Integer.valueOf(values[1]));
                }
            });
            return decrushedMap;
        } catch (Exception e) {
            bot.getErrorHandler().handleException(e);
        }
        return new HashMap<>();
    }

    public void updateGame(String newGameId) {
        this.currentSession.newGame(newGameId);
        this.currentSession.updateMessage();
    }

    public void addViewerCount(int viewerCount) {
        currentSession.getViewerCountList().add(viewerCount);
    }

    public void cancelCurrentSession() {
        sessions.remove(currentSession);
        deleteCurrentSession();
    }

    public void retryStartingSession() {

    }

}
