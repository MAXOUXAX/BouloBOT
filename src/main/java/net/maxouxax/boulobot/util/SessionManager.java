package net.maxouxax.boulobot.util;

import net.maxouxax.boulobot.BOT;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class SessionManager {

    private BOT bot;
    private Session currentSession;
    private File SESSIONS_FOLDER;
    private ArrayList<Session> sessions = new ArrayList<>();

    public SessionManager(BOT bot) {
        this.bot = bot;
        SESSIONS_FOLDER = new File("sessions" + File.separator);
    }

    public Session startNewSession(String channelId) {
        currentSession = new Session(System.currentTimeMillis(), channelId, bot);
        sessions.add(currentSession);
        return currentSession;
    }

    public void endSession() {
        currentSession.setEndDateMillis(System.currentTimeMillis());
    }

    public Session getCurrentSession() {
        return currentSession;
    }

    public Optional<Session> getSession(String uuid) {
        return sessions.stream().filter(session -> session.getUuid().toString().equalsIgnoreCase(uuid)).findFirst();
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
                    JSONReader reader = new JSONReader(file, bot);
                    JSONArray array = reader.toJSONArray();

                    for (int i = 0; i < array.length(); i++) {
                        JSONObject object = array.getJSONObject(i);
                        long startTime = object.getLong("startDate");
                        String channelId = object.getString("channelId");
                        Session loadingSession = new Session(startTime, channelId, bot);
                        loadingSession.setEndDateMillis(object.getLong("endDate"));
                        loadingSession.setAvgViewers(object.getInt("avgViewers"));
                        loadingSession.setMaxViewers(object.getInt("maxViewers"));
                        loadingSession.setBansAndTimeouts(object.getInt("bansAndTimeouts"));
                        loadingSession.setNewViewers(object.getInt("newViewers"));
                        loadingSession.setNewFollowers(object.getInt("newFollowers"));
                        loadingSession.setCommandUsed(object.getInt("commandUsed"));
                        loadingSession.setMessageSended(object.getInt("messageSended"));
                        loadingSession.setCommandsUsed(decrushMap(object.getString("commandsUsed")));
                        loadingSession.setUsedEmotes(decrushMap(object.getString("usedEmotes")));

                        sessions.add(loadingSession);
                    }

                } catch (IOException e) {
                    bot.getErrorHandler().handleException(e);
                }
            }
        }
    }



    public void saveSessions() {
        if (!SESSIONS_FOLDER.exists()) {
            SESSIONS_FOLDER.mkdirs();
        }

        for (Session session : sessions) {
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
            object.accumulate("usedEmotes", crushMap(session.getUsedEmotes()));

            array.put(object);

            try(JSONWriter writter = new JSONWriter(file)){

                writter.write(array);
                writter.flush();

            }catch(IOException ioe){
                ioe.printStackTrace();
            }
        }
    }

    private String crushMap(HashMap<String, Integer> decrushedMap) {
        StringBuilder str = new StringBuilder();
        decrushedMap.forEach((s, integer) -> {
            str.append(s+"::"+integer+"&&&");
        });
        return str.toString();
    }

    private HashMap<String, Integer> decrushMap(String crushedMap) {
        HashMap<String, Integer> decrushedMap = new HashMap<>();
        List<String> cuttedStrings = Arrays.asList(crushedMap.split("&&&"));
        cuttedStrings.forEach(s -> {
            String[] values = s.split("::");
            decrushedMap.put(values[0], Integer.valueOf(values[1]));
        });
        return decrushedMap;
    }

    public void updateGame(String newGameId) {
        this.currentSession.newGame(newGameId);
        this.currentSession.updateMessage();
    }

}
