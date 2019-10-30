package net.maxx.boulobot;

import com.github.philippheuer.credentialmanager.CredentialManager;
import com.github.philippheuer.credentialmanager.CredentialManagerBuilder;
import com.github.philippheuer.credentialmanager.domain.OAuth2Credential;
import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.TwitchClientBuilder;
import com.github.twitch4j.auth.providers.TwitchIdentityProvider;
import com.github.twitch4j.chat.events.channel.IRCMessageEvent;
import com.github.twitch4j.common.events.channel.ChannelGoLiveEvent;
import com.github.twitch4j.common.events.channel.ChannelGoOfflineEvent;
import net.dv8tion.jda.api.*;
import net.dv8tion.jda.api.entities.*;
import net.maxx.boulobot.commands.CommandMap;
import net.maxx.boulobot.event.BotListener;
import net.maxx.boulobot.event.TwitchListener;
import net.maxx.boulobot.roles.RolesManager;
import net.maxx.boulobot.util.*;

import javax.security.auth.login.LoginException;
import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;

public class BOT implements Runnable{

    private static JDA jda;
    private final CommandMap commandMap;
    private final Scanner scanner = new Scanner(System.in);
    private final Logger logger;
    private final ErrorHandler errorHandler;
    private TwitchClient twitchClient;

    private RolesManager rolesManager;
    private ConfigurationManager configurationManager;
    private SessionManager sessionManager;

    private boolean running;
    private String version;

    public BOT() throws LoginException, IllegalArgumentException, NullPointerException, IOException, InterruptedException {
        //Loading the log system
        this.logger = new Logger(this);

        //Loading the error handler system
        this.errorHandler = new ErrorHandler(this);

        String string = new File(BOT.class.getProtectionDomain().getCodeSource().getLocation().getPath()).getName();
        string = string.replaceAll("BouloBOT-", "");
        string = string.replaceAll("-jar-with-dependencies", "");
        string = string.replaceAll(".jar", "");
        this.version = string;

        loadConfig();

        //Log the startup messages
        logger.log(Level.INFO, "--------------- STARTING ---------------");
        logger.log(Level.INFO, "> Generated new BOT instance");
        logger.log(Level.INFO, "> BOT thread started, loading libraries and joining DiscordAPI channel");
        this.commandMap = new CommandMap(this);
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
            this.configurationManager = new ConfigurationManager(this, "config.json");
            configurationManager.loadData();
        } catch (IOException e) {
            e.printStackTrace();
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
                .withChatAccount(oAuth2Credential)
                .withEnableChat(true)
                .withEnableTMI(true)
                .build();
        logger.log(Level.INFO, "> TwitchAPI launched.");

        //Connecting to the BOT's tchats
        twitchClient.getChat().connect();
        twitchClient.getChat().joinChannel("lyorine");
        logger.log(Level.INFO, "> Lyorine's channel joined!");

        //Registering SessionManager and loading all passed sessions
        //this.sessionManager = new SessionManager(this);
        //sessionManager.loadSessions();

        //Registering the events
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                twitchClient.getEventManager().onEvent(IRCMessageEvent.class).subscribe(event -> {
                    if(event.getMessage().isPresent()) {
                        String message = event.getMessage().get();
                        String username = event.getUser().getName();
                        logger.log(Level.INFO, "LiveTchat > "+username + " > " + message);
                        String id = event.getUser().getId();
                        if(id == null)return;
                        if(!commandMap.isKnown(id)) {
                            //logger.log(Level.WARNING, commandMap.getUsersOnLive().toString());
                            commandMap.addKnownUser(id);
                            event.getTwitchChat().sendMessage("lyorine", "Coucou @" + username + " ! Passe un bon moment sur le stream, et pose toi avec ton PopCorn !");
                        }
                    }else{
                        logger.log(Level.WARNING, event.getRawMessage());
                    }
                });
            }
        }, 5000);

        //Notification system
        loadNotifications();

        //Registering the listener in order to make the events work
        twitchClient.getEventManager().registerListener(new TwitchListener(commandMap, this));
    }

    private void loadNotifications() {
        twitchClient.getClientHelper().enableStreamEventListener("lyorine");
        twitchClient.getEventManager().onEvent(ChannelGoLiveEvent.class).subscribe((channelGoLiveEvent) -> {
            sendGoLiveNotif();
        });
        twitchClient.getEventManager().onEvent(ChannelGoOfflineEvent.class).subscribe((channelGoOfflineEvent) -> {
            sendGoOfflineNotif();
        });
    }

    public void sendGoLiveNotif(){
        Member lyorine = jda.getGuildById(Reference.GuildID.getString()).getMemberById(Reference.LyorineClientID.getString());
        Role notif = lyorine.getGuild().getRoleById(Reference.NotifRoleID.getString());
        logger.log(Level.INFO, "> Le stream est ONLINE!");
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle("Notification \uD83D\uDD14", "https://twitch.lyorine.com");
        embedBuilder.setFooter(Reference.EmbedFooter.asDate(), Reference.EmbedIcon.getString());
        embedBuilder.setColor(3066993);
        embedBuilder.setDescription("Oyé oyé les "+notif.getAsMention()+" !\n**"+lyorine.getAsMention()+"** part en stream !\n» https://twitch.lyorine.com");
        embedBuilder.setThumbnail(Reference.BellImage.getString()+"?size=256");
        TextChannel toSend = jda.getGuildById(Reference.GuildID.getString()).getTextChannelById(Reference.NotifTextChannelID.getString());
        Message message = new MessageBuilder(notif.getAsMention()).setEmbed(embedBuilder.build()).build();
        toSend.sendMessage(message).queue();
        twitchClient.getChat().sendMessage("lyorine", "Coucou imGlitch ! \n» Je viens d'envoyer la notification à tous les chats ! Bon live ! LUL");
        jda.getPresence().setActivity(Activity.streaming("avec sa reine", "https://twitch.tv/LYORINE"));
    }

    public void sendGoOfflineNotif(){
        logger.log(Level.INFO, "> Le stream est OFFLINE!");
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle("Notification \uD83D\uDD14", "https://twitch.tv/Lyorine");
        embedBuilder.setFooter(Reference.EmbedFooter.asDate(), Reference.EmbedIcon.getString());
        embedBuilder.setColor(15158332);
        embedBuilder.setDescription("Coucou !\nLe live est désormais terminé, merci à tous de l'avoir suivi !\nVous pourrez me retrouver une prochaine fois, à l'adresse suivante !\n» https://twitch.lyorine.com");
        jda.getGuildById(Reference.GuildID.getString()).getTextChannelById(Reference.NotifTextChannelID.getString()).sendMessage(embedBuilder.build()).queue();
        jda.getPresence().setActivity(Activity.playing("attendre sa reine"));
    }

    private void loadDiscord() throws LoginException, InterruptedException {
        //Creating the credentials, adding the listeners, and load the roles
        jda = new JDABuilder(AccountType.BOT).setToken(configurationManager.getStringValue("botToken")).build();
        jda.addEventListener(new BotListener(commandMap, this));
        jda.getPresence().setActivity(Activity.playing("attendre sa reine"));
        jda.awaitReady();
        loadRolesManager();
    }

    private void loadRolesManager() {
        //Loading the RoleManager system
        TextChannel textChannel = jda.getGuildById(Reference.GuildID.getString()).getTextChannelById(Reference.RankTextChannelID.getString());
        rolesManager = new RolesManager(textChannel, this);
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
        //Startup of the BOT
        running = true;

        while (running) {
            if (scanner.hasNextLine()) {
                //Scanning for console commands
                String nextLine = scanner.nextLine();
                commandMap.discordCommandConsole(nextLine);
            }
        }

        //Shutdowning the BOT
        jda.getPresence().setActivity(Activity.playing("Arrêt en cours..."));
        logger.log(Level.INFO, "--------------- STOPPING ---------------");
        logger.log(Level.INFO, "> Shutdowning...");
        scanner.close();
        logger.log(Level.INFO, "> Scanner closed");
        jda.shutdown();
        logger.log(Level.INFO, "> JDA shutdowned");
        rolesManager.saveRoles();
        logger.log(Level.INFO, "> Roles saved");
        commandMap.save();
        logger.log(Level.INFO, "> CommandMap saved");
        logger.save();
        logger.log(Level.INFO, "> Logger saved");
        configurationManager.saveData();
        logger.log(Level.INFO, "> Configuration saved");
        //sessionManager.saveSessions();
        logger.log(Level.INFO, "> Sessions saved");
        logger.log(Level.INFO, "--------------- STOPPING ---------------");
        logger.log(Level.INFO, "Arrêt du BOT réussi");
        System.exit(0);
    }

    public static void main(String[] args) {
        try {
            BOT botDiscord = new BOT();
            new Thread(botDiscord, "bot").start();
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
}
