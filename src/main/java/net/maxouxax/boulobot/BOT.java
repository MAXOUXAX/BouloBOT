package net.maxouxax.boulobot;

import com.github.philippheuer.credentialmanager.CredentialManager;
import com.github.philippheuer.credentialmanager.CredentialManagerBuilder;
import com.github.philippheuer.credentialmanager.domain.OAuth2Credential;
import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.TwitchClientBuilder;
import com.github.twitch4j.auth.providers.TwitchIdentityProvider;
import com.github.twitch4j.chat.events.channel.IRCMessageEvent;
import com.github.twitch4j.common.events.channel.ChannelGoLiveEvent;
import com.github.twitch4j.common.events.channel.ChannelGoOfflineEvent;
import com.github.twitch4j.helix.domain.GameList;
import com.github.twitch4j.helix.domain.Stream;
import com.github.twitch4j.helix.domain.StreamList;
import net.dv8tion.jda.api.*;
import net.dv8tion.jda.api.entities.*;
import net.maxouxax.boulobot.commands.CommandMap;
import net.maxouxax.boulobot.event.BotListener;
import net.maxouxax.boulobot.event.TwitchListener;
import net.maxouxax.boulobot.roles.RolesManager;
import net.maxouxax.boulobot.util.*;

import javax.security.auth.login.LoginException;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
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
    private String channelName;

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

        channelName = configurationManager.getStringValue("channelName");

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
        twitchClient.getChat().joinChannel(channelName);
        logger.log(Level.INFO, "> "+channelName+"'s channel joined!");

        //Registering SessionManager and loading all passed sessions
        this.sessionManager = new SessionManager(this);
        sessionManager.loadSessions();

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
                            event.getTwitchChat().sendMessage(channelName, "Coucou @" + username + " ! Passe un bon moment sur le stream, et pose toi avec ton PopCorn !");
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
        twitchClient.getClientHelper().enableStreamEventListener(channelName);
        twitchClient.getEventManager().onEvent(ChannelGoLiveEvent.class).subscribe(channelGoLiveEvent -> {
            sendGoLiveNotif(channelGoLiveEvent.getTitle(), channelGoLiveEvent.getGameId(), channelGoLiveEvent.getChannel().getId());
        });
        twitchClient.getEventManager().onEvent(ChannelGoOfflineEvent.class).subscribe(channelGoOfflineEvent -> {
            sendGoOfflineNotif();
        });
    }

    public void sendGoLiveNotif(String title, String gameId, String channelId){
        Session session = sessionManager.startNewSession(channelId);
        session.newGame(gameId);
        session.setTitle(title);

        Guild discord = jda.getGuildById(Reference.GuildID.getString());
        Member lyorine = discord.getMemberById(Reference.LyorineClientID.getString());
        Role notif = discord.getRoleById(Reference.NotifRoleID.getString());
        logger.log(Level.INFO, "> Le stream est ONLINE!");
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle("Notification \uD83D\uDD14", "https://twitch.lyorine.com");
        embedBuilder.setFooter(Reference.EmbedFooter.asDate(), Reference.EmbedIcon.getString());
        embedBuilder.setColor(3066993);
        embedBuilder.setDescription("Oyé oyé les "+notif.getAsMention()+" !\n**"+lyorine.getAsMention()+"** part en stream !\n» https://twitch.lyorine.com");
        embedBuilder.addField(new MessageEmbed.Field("Titre", title, true));
        GameList resultList = twitchClient.getHelix().getGames(Collections.singletonList(gameId), null).execute();
        final String[] gameName = {"Aucun jeu"};
        resultList.getGames().forEach(game -> {
            gameName[0] = game.getName();
            String boxUrl = game.getBoxArtUrl();
            boxUrl = boxUrl.replace("{width}", "600");
            boxUrl = boxUrl.replace("{height}", "800");
            embedBuilder.setThumbnail(boxUrl);
        });
        embedBuilder.addField(new MessageEmbed.Field("Jeu", gameName[0], true));

        StreamList streamResultList = twitchClient.getHelix().getStreams(configurationManager.getStringValue("oauth2Token"), "", "", null, null, null, null, Collections.singletonList(channelId), null).execute();
        final Stream[] currentStream = new Stream[1];
        streamResultList.getStreams().forEach(stream -> {
            currentStream[0] = stream;
        });

        embedBuilder.setImage(currentStream[0].getThumbnailUrl());
        TextChannel toSend = discord.getTextChannelById(Reference.NotifTextChannelID.getString());
        Message message = new MessageBuilder(notif.getAsMention()).setEmbed(embedBuilder.build()).build();
        toSend.sendMessage(message).queue(session::setSessionMessage);
        twitchClient.getChat().sendMessage(channelName, "Coucou imGlitch ! \n» Je viens d'envoyer la notification à tous les chats ! Bon live ! LUL");
        jda.getPresence().setActivity(Activity.streaming("avec sa reine à "+gameName[0], "https://twitch.tv/"+channelName.toUpperCase()));
    }

    public void sendGoOfflineNotif(){
        logger.log(Level.INFO, "> Le stream est OFFLINE!");
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle("Notification \uD83D\uDD14", "https://twitch.tv/"+channelName.toUpperCase());
        embedBuilder.setFooter(Reference.EmbedFooter.asDate(), Reference.EmbedIcon.getString());
        embedBuilder.setColor(15158332);
        embedBuilder.setDescription("Coucou !\nLe live est désormais terminé, merci à tous de l'avoir suivi !\nVous pourrez me retrouver une prochaine fois, à l'adresse suivante !\n» https://twitch.lyorine.com");
        jda.getGuildById(Reference.GuildID.getString()).getTextChannelById(Reference.NotifTextChannelID.getString()).sendMessage(embedBuilder.build()).queue();
        jda.getPresence().setActivity(Activity.playing("attendre sa reine"));

        Message newMessage = new MessageBuilder(embedBuilder.build()).build();
        sessionManager.getCurrentSession().getSessionMessage().editMessage(newMessage).queue();
        logger.log(Level.INFO, "> Updated!");
        sessionManager.endSession();
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
        sessionManager.saveSessions();
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
