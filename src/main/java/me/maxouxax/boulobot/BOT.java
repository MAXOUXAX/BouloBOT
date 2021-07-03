package me.maxouxax.boulobot;

import com.github.philippheuer.credentialmanager.CredentialManager;
import com.github.philippheuer.credentialmanager.CredentialManagerBuilder;
import com.github.philippheuer.credentialmanager.domain.OAuth2Credential;
import com.github.philippheuer.events4j.simple.SimpleEventHandler;
import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.TwitchClientBuilder;
import com.github.twitch4j.auth.providers.TwitchIdentityProvider;
import com.github.twitch4j.helix.domain.UserList;
import io.sentry.Sentry;
import me.maxouxax.boulobot.commands.CommandMap;
import me.maxouxax.boulobot.database.DatabaseManager;
import me.maxouxax.boulobot.event.DiscordListener;
import me.maxouxax.boulobot.event.TwitchListener;
import me.maxouxax.boulobot.roles.RolesManager;
import me.maxouxax.boulobot.sessions.SessionManager;
import me.maxouxax.boulobot.util.ConfigurationManager;
import me.maxouxax.boulobot.util.ErrorHandler;
import me.maxouxax.boulobot.util.Logger;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.requests.GatewayIntent;

import javax.security.auth.login.LoginException;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Collections;
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
    private final ConfigurationManager configurationManager;
    private SessionManager sessionManager;

    private boolean running;
    private final String version;
    private final String channelName;

    public BOT() throws LoginException, IllegalArgumentException, NullPointerException, IOException, InterruptedException, SQLException {
        instance = this;

        this.logger = new Logger();
        this.errorHandler = new ErrorHandler();

        DatabaseManager.initDatabaseConnection();

        String string = new File(BOT.class.getProtectionDomain().getCodeSource().getLocation().getPath()).getName();
        string = string.replaceAll("BouloBOT-", "")
                .replaceAll("-jar-with-dependencies", "")
                .replaceAll(".jar", "");
        this.version = string;

        this.configurationManager = new ConfigurationManager();
        channelName = configurationManager.getStringValue("channelName");

        logger.log(Level.INFO, "--------------- STARTING ---------------");

        logger.log(Level.INFO, "> Initializing Sentry...");
        Sentry.init(options -> {
            options.setEnableExternalConfiguration(true);
        });
        logger.log(Level.INFO, "> Sentry initialized !");

        logger.log(Level.INFO, "> Generated new BOT instance");
        logger.log(Level.INFO, "> BOT thread started, loading libraries and joining DiscordAPI channel");
        this.commandMap = new CommandMap();
        logger.log(Level.INFO, "> Libraries loaded and DiscordAPI channel joined.");

        loadDiscord();
        commandMap.updateCommands();
        logger.log(Level.INFO, "> DiscordBOT loaded, launching Twitch's modules!.");

        loadTwitch();

        logger.log(Level.INFO, "> The BOT is now good to go !");
        logger.log(Level.INFO, "--------------- STARTING ---------------");
    }

    private void loadTwitch() {
        CredentialManager credentialManager = CredentialManagerBuilder.builder().build();
        credentialManager.registerIdentityProvider(new TwitchIdentityProvider(configurationManager.getStringValue("twitchClientId"), configurationManager.getStringValue("twitchClientSecret"), ""));
        logger.log(Level.INFO, "> Credentials registered!");

        OAuth2Credential oAuth2Credential = new OAuth2Credential("twitch", configurationManager.getStringValue("oauth2Token"));
        twitchClient = TwitchClientBuilder.builder()
                .withCredentialManager(credentialManager)
                .withEnableHelix(true)
                .withDefaultAuthToken(oAuth2Credential)
                .withChatAccount(oAuth2Credential)
                .withEnableChat(true)
                .withEnableTMI(true)
                .withEnablePubSub(true)
                .build();
        logger.log(Level.INFO, "> TwitchAPI launched.");

        twitchClient.getChat().connect();
        twitchClient.getChat().joinChannel(channelName);
        twitchClient.getClientHelper().enableFollowEventListener(channelName);
        UserList resultList = twitchClient.getHelix().getUsers(null, null, Collections.singletonList(channelName)).execute();
        AtomicReference<String> channelId = new AtomicReference<>("");
        resultList.getUsers().stream().findFirst().ifPresent(user -> channelId.set(user.getId()));
        twitchClient.getPubSub().listenForChannelPointsRedemptionEvents(new OAuth2Credential("twitch", configurationManager.getStringValue("lyorineChannelToken")), channelId.get());

        logger.log(Level.INFO, "> "+channelName+"'s channel joined!");

        this.sessionManager = new SessionManager();
        sessionManager.loadSessions();

        this.twitchListener = new TwitchListener(commandMap);
        twitchClient.getEventManager().getEventHandler(SimpleEventHandler.class).registerListener(twitchListener);
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
                commandMap.consoleCommand(nextLine);
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
        logger.save();
        logger.log(Level.INFO, "> Logger saved");
        logger.log(Level.INFO, "--------------- STOPPING ---------------");
        logger.log(Level.INFO, "Arrêt du BOT réussi");
        System.exit(0);
    }

    public static void main(String[] args) {
        try {
            BOT bot = new BOT();
            new Thread(bot, "bot").start();
        } catch (LoginException | IllegalArgumentException | NullPointerException | IOException | InterruptedException | SQLException e) {
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
