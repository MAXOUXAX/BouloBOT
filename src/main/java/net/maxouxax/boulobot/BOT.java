package net.maxouxax.boulobot;

import com.github.philippheuer.credentialmanager.CredentialManager;
import com.github.philippheuer.credentialmanager.CredentialManagerBuilder;
import com.github.philippheuer.credentialmanager.domain.OAuth2Credential;
import com.github.philippheuer.events4j.simple.SimpleEventHandler;
import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.TwitchClientBuilder;
import com.github.twitch4j.auth.providers.TwitchIdentityProvider;
import com.github.twitch4j.common.events.channel.ChannelGoLiveEvent;
import com.github.twitch4j.common.events.channel.ChannelGoOfflineEvent;
import com.github.twitch4j.helix.domain.Stream;
import com.github.twitch4j.helix.domain.StreamList;
import io.sentry.Sentry;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.maxouxax.boulobot.commands.CommandMap;
import net.maxouxax.boulobot.event.DiscordListener;
import net.maxouxax.boulobot.event.TwitchListener;
import net.maxouxax.boulobot.roles.RolesManager;
import net.maxouxax.boulobot.util.*;

import javax.security.auth.login.LoginException;
import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Date;
import java.util.Objects;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;

public class BOT implements Runnable{

    private static BOT instance;
    private static JDA jda;
    private final CommandMap commandMap;
    private final Scanner scanner = new Scanner(System.in);
    private final Logger logger;
    private final ErrorHandler errorHandler;
    private TwitchClient twitchClient;
    private TwitchListener twitchListener;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    private RolesManager rolesManager;
    private ConfigurationManager configurationManager;
    private SessionManager sessionManager;

    private boolean running;
    private final String version;
    private final String channelName;

    public BOT() throws LoginException, IllegalArgumentException, NullPointerException, IOException, InterruptedException {
        instance = this;
        //Loading the log system
        this.logger = new Logger();

        //Loading the error handler system
        this.errorHandler = new ErrorHandler();

        String string = new File(BOT.class.getProtectionDomain().getCodeSource().getLocation().getPath()).getName();
        string = string.replaceAll("BouloBOT-", "")
                .replaceAll("-jar-with-dependencies", "")
                .replaceAll(".jar", "");
        this.version = string;

        loadConfig();

        channelName = configurationManager.getStringValue("channelName");

        //Log the startup messages
        logger.log(Level.INFO, "--------------- STARTING ---------------");

        logger.log(Level.INFO, "> Initializing Sentry...");
        Sentry.init();
        logger.log(Level.INFO, "> Sentry initialized !");

        logger.log(Level.INFO, "> Generated new BOT instance");
        logger.log(Level.INFO, "> BOT thread started, loading libraries and joining DiscordAPI channel");
        this.commandMap = new CommandMap();
        logger.log(Level.INFO, "> Libraries loaded and DiscordAPI channel joined.");

        //Load the Discord modules
        loadDiscord();
        logger.log(Level.INFO, "> DiscordBOT loaded, launching Twitch's modules!.");

        //Load the Twitch modules
        loadTwitch();

        //Log the ending messages
        logger.log(Level.INFO, "> The BOT is now good to go !");
        logger.log(Level.INFO, "--------------- STARTING ---------------");
    }

    private void loadConfig() {
        try {
            this.configurationManager = new ConfigurationManager("config.json");
            configurationManager.loadData();
        } catch (IOException e) {
            getErrorHandler().handleException(e);
        }
    }

    private void loadTwitch() {
        //Registering the CredentialManager
        CredentialManager credentialManager = CredentialManagerBuilder.builder().build();
        credentialManager.registerIdentityProvider(new TwitchIdentityProvider(configurationManager.getStringValue("twitchClientId"), configurationManager.getStringValue("twitchClientSecret"), ""));
        logger.log(Level.INFO, "> Credentials registered!");

        //Connecting to TwitchAPI
        OAuth2Credential oAuth2Credential = new OAuth2Credential("twitch", configurationManager.getStringValue("oauth2Token"));
        twitchClient = TwitchClientBuilder.builder()
                .withCredentialManager(credentialManager)
                .withEnableHelix(true)
                .withDefaultAuthToken(oAuth2Credential)
                .withChatAccount(oAuth2Credential)
                .withEnableChat(true)
                .withEnableTMI(true)
                .build();
        logger.log(Level.INFO, "> TwitchAPI launched.");

        //Connecting to the BOT's chats
        twitchClient.getChat().connect();
        twitchClient.getChat().joinChannel(channelName);
        twitchClient.getClientHelper().enableFollowEventListener(channelName);
        logger.log(Level.INFO, "> "+channelName+"'s channel joined!");

        //Registering SessionManager and loading all passed sessions
        this.sessionManager = new SessionManager();
        sessionManager.loadSessions();

        //Notification system
        loadNotifications();

        //Registering the listener in order to make the events work
        this.twitchListener = new TwitchListener(commandMap);
        twitchClient.getEventManager().getEventHandler(SimpleEventHandler.class).registerListener(twitchListener);
    }

    private void loadNotifications() {
        twitchClient.getClientHelper().enableStreamEventListener(channelName);
        twitchClient.getEventManager().getEventHandler(SimpleEventHandler.class).onEvent(ChannelGoLiveEvent.class, channelGoLiveEvent -> {
            sendGoLiveNotif(channelGoLiveEvent.getTitle(), channelGoLiveEvent.getGameId(), channelGoLiveEvent.getChannel().getId());
        });
        twitchClient.getEventManager().getEventHandler(SimpleEventHandler.class).onEvent(ChannelGoOfflineEvent.class, channelGoOfflineEvent -> {
            sendGoOfflineNotif();
        });
    }

    public void sendGoLiveNotif(String title, String gameId, String channelId){
        try {

            if (sessionManager.getCurrentSession() != null) {
                logger.log(Level.SEVERE, "Gosh! We're in trouble... Session wasn't null, it means that a session was already started! We need to fix that!");
                return;
            }
            StreamList streamResultList = twitchClient.getHelix().getStreams(configurationManager.getStringValue("oauth2Token"), "", "", null, null, null, null, Collections.singletonList(channelId), null).execute();
            AtomicReference<Stream> currentStream = new AtomicReference<>();
            streamResultList.getStreams().stream().findFirst().ifPresent(currentStream::set);
            if(currentStream.get() == null){
                errorHandler.handleException(new Exception("currentStream[0] == null"));
                return;
            }

            Session session = sessionManager.startNewSession(channelId);
            session.newGame(gameId);
            session.setTitle(title);
            session.updateMessage();
        }catch (Exception e){
            getErrorHandler().handleException(e);
        }
    }

    public void sendGoOfflineNotif() {
        try {
            if (sessionManager.getCurrentSession() == null) {
                logger.log(Level.SEVERE, "Hmmm... There's a problem, a GoOffline has been sended, but no session was running... Erhm.");
                return;
            }

            Session session = sessionManager.getCurrentSession();
            sessionManager.endSession();
            logger.log(Level.INFO, "> Le stream est OFFLINE!");
            EmbedBuilder embedBuilder = new EmbedBuilder();
            embedBuilder.setTitle("Live terminé \uD83D\uDD14", "https://twitch.tv/" + channelName.toUpperCase());
            embedBuilder.setFooter(TextFormatter.asDate(configurationManager.getStringValue("embedFooter")), configurationManager.getStringValue("embedIconUrl"));
            embedBuilder.setColor(15158332);
            embedBuilder.setDescription("Oh dommage...\nLe live est désormais terminé !\nVous pourrez retrouver " + channelName.toUpperCase() + " une prochaine fois, à l'adresse suivante !\n» https://twitch.tv/" + channelName.toUpperCase());
            embedBuilder.addField("Nombre de viewer maximum", session.getMaxViewers() + "", true);
            embedBuilder.addField("Nombre de viewer moyen", session.getAvgViewers() + "", true);
            embedBuilder.addField("Titre", session.getTitle(), true);
            embedBuilder.addField("Nombre de ban & timeout", session.getBansAndTimeouts() + "", true);
            embedBuilder.addField("Nombre de commandes utilisées", session.getCommandUsed() + "", true);
            embedBuilder.addField("Nombre de messages envoyés", session.getMessageSended() + "", true);
            embedBuilder.addField("Nombre de followers", session.getNewFollowers() + "", true);
            embedBuilder.addField("Nombre de nouveaux viewers", session.getNewViewers() + "", true);
            LocalDateTime start = new Date(session.getStartDate()).toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
            LocalDateTime end = new Date(session.getEndDate()).toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
            int minutesC = Math.toIntExact(Duration.between(start, end).toMinutes());
            int hours = minutesC / 60;
            int minutes = minutesC % 60;

            embedBuilder.addField("Durée", hours + "h" + (minutes < 10 ? "0" : "") + minutes, true);
            jda.getPresence().setActivity(Activity.playing("Amazingly powerful"));
            session.getSessionMessage().editMessage(" ").embed(embedBuilder.build()).queue();
            logger.log(Level.INFO, "> Updated!");
            sessionManager.deleteCurrentSession();
        } catch (Exception e) {
            getErrorHandler().handleException(e);
        }
    }

    private void loadDiscord() throws LoginException, InterruptedException {
        //Creating the credentials, adding the listeners, and load the roles
        jda = JDABuilder.create(configurationManager.getStringValue("botToken"), GatewayIntent.GUILD_MESSAGES,
                GatewayIntent.DIRECT_MESSAGE_REACTIONS,
                GatewayIntent.DIRECT_MESSAGE_TYPING,
                GatewayIntent.DIRECT_MESSAGES,
                GatewayIntent.GUILD_BANS,
                GatewayIntent.GUILD_EMOJIS,
                GatewayIntent.GUILD_MEMBERS,
                GatewayIntent.GUILD_INVITES,
                GatewayIntent.GUILD_MESSAGE_REACTIONS,
                GatewayIntent.GUILD_MESSAGE_TYPING,
                GatewayIntent.GUILD_PRESENCES,
                GatewayIntent.GUILD_VOICE_STATES)
                .build();
        jda.addEventListener(new DiscordListener(commandMap));
        jda.getPresence().setActivity(Activity.playing("Amazingly powerful"));
        jda.awaitReady();
        loadRolesManager();
    }

    private void loadRolesManager() {
        //Loading the RoleManager system
        TextChannel textChannel = Objects.requireNonNull(jda.getGuildById(configurationManager.getStringValue("guildId"))).getTextChannelById(configurationManager.getStringValue("rolesTextChannelId"));
        rolesManager = new RolesManager(textChannel);
        rolesManager.loadRoles();
    }

    public TwitchClient getTwitchClient() {
        return twitchClient;
    }

    public ErrorHandler getErrorHandler() {
        return errorHandler;
    }

    public JDA getJda() {
        return jda;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    public RolesManager getRolesManager() {
        return rolesManager;
    }

    @Override
    public void run() {
        //Startup
        running = true;

        while (running) {
            if (scanner.hasNextLine()) {
                //Scanning for console commands
                String nextLine = scanner.nextLine();
                commandMap.discordCommandConsole(nextLine);
            }
        }

        //Making the BOT shutdown
        jda.getPresence().setActivity(Activity.playing("Arrêt en cours..."));
        logger.log(Level.INFO, "--------------- STOPPING ---------------");
        logger.log(Level.INFO, "> Shutdowning...");
        scanner.close();
        logger.log(Level.INFO, "> Scanner closed");
        jda.shutdown();
        logger.log(Level.INFO, "> JDA shutdowned");
        twitchListener.closeListener();
        logger.log(Level.INFO, "> TwitchListener closed");
        rolesManager.saveRoles();
        logger.log(Level.INFO, "> Roles saved");
        commandMap.save();
        logger.log(Level.INFO, "> CommandMap saved");
        logger.save();
        logger.log(Level.INFO, "> Logger saved");
        sessionManager.saveSessions();
        logger.log(Level.INFO, "> Sessions saved");
        logger.log(Level.INFO, "--------------- STOPPING ---------------");
        logger.log(Level.INFO, "Arrêt du BOT réussi");
        System.exit(0);
    }

    public static void main(String[] args) {
        try {
            BOT bot = new BOT();
            new Thread(bot, "bot").start();
        } catch (LoginException | IllegalArgumentException | NullPointerException | IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public Logger getLogger() {
        return logger;
    }

    public String getVersion() {
        return version;
    }

    public ConfigurationManager getConfigurationManager() {
        return configurationManager;
    }

    public SessionManager getSessionManager() {
        return sessionManager;
    }

    public String getChannelName() {
        return channelName;
    }

    public ScheduledExecutorService getScheduler() {
        return scheduler;
    }

    public TwitchListener getTwitchListener() {
        return twitchListener;
    }

    public static BOT getInstance(){
        return instance;
    }
}
