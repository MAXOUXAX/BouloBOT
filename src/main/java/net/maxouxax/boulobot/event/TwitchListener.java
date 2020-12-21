package net.maxouxax.boulobot.event;

import com.github.philippheuer.events4j.simple.SimpleEventHandler;
import com.github.twitch4j.chat.events.channel.*;
import com.github.twitch4j.common.events.channel.ChannelChangeGameEvent;
import com.github.twitch4j.common.events.domain.EventUser;
import com.github.twitch4j.helix.domain.User;
import net.maxouxax.boulobot.BOT;
import net.maxouxax.boulobot.commands.CommandMap;
import net.maxouxax.boulobot.commands.TwitchCommand;
import net.maxouxax.boulobot.util.ChatSpyManager;

import java.util.Collections;
import java.util.logging.Level;

public class TwitchListener {

    private final CommandMap commandMap;
    private final ChatSpyManager chatSpyManager;
    private final BOT bot;

    public TwitchListener(CommandMap commandMap) {
        this.commandMap = commandMap;
        this.bot = BOT.getInstance();
        this.chatSpyManager = new ChatSpyManager();

        SimpleEventHandler eventHandler = bot.getTwitchClient().getEventManager().getEventHandler(SimpleEventHandler.class);
        eventHandler.onEvent(ChannelMessageEvent.class, this::onMessageEvent);
        eventHandler.onEvent(ChannelChangeGameEvent.class, this::onGameUpdate);
        eventHandler.onEvent(UserTimeoutEvent.class, this::onTimeOut);
        eventHandler.onEvent(UserBanEvent.class, this::onBan);
        eventHandler.onEvent(IRCMessageEvent.class, this::ircMessage);
        eventHandler.onEvent(FollowEvent.class, this::followEvent);
        eventHandler.onEvent(ChannelJoinEvent.class, this::channelJoinEvent);
        eventHandler.onEvent(ChannelLeaveEvent.class, this::channelLeaveEvent);
    }

    private void channelJoinEvent(ChannelJoinEvent event) {
        chatSpyManager.addMessage(event.getUser().getName(), "✅ `" + event.getUser().getName() + "` a rejoint le chat");
    }

    private void channelLeaveEvent(ChannelLeaveEvent event) {
        chatSpyManager.addMessage(event.getUser().getName(), "❌ `" + event.getUser().getName() + "` a quitté le chat");
    }

    private void followEvent(FollowEvent event) {
        if (bot.getSessionManager().isSessionStarted()) {
            bot.getSessionManager().getCurrentSession().addFollower();
        }
        chatSpyManager.addMessage(event.getUser().getName(), "\uD83C\uDD95 `" + event.getUser().getName() + "` vient de follow");
        bot.getLogger().log(Level.INFO, event.getUser().getName() + " vient de follow !");
    }

    private void ircMessage(IRCMessageEvent event) {
        if (!event.getMessage().isPresent()) {
            bot.getLogger().log(Level.INFO, event.getRawMessage());
        }
    }

    private void onBan(UserBanEvent event) {
        if (bot.getSessionManager().isSessionStarted()) {
            bot.getSessionManager().getCurrentSession().addBanOrTimeout();
        }
        chatSpyManager.addMessage(event.getUser().getName(), "⛔ `" + event.getUser().getName() + "` vient d'être banni pour " + event.getReason());
        bot.getLogger().log(Level.INFO, event.getUser().getName() + " vient d'être banni");
    }

    private void onTimeOut(UserTimeoutEvent event) {
        if (bot.getSessionManager().isSessionStarted()) {
            bot.getSessionManager().getCurrentSession().addBanOrTimeout();
        }
        chatSpyManager.addMessage(event.getUser().getName(), "\uD83D\uDCDB `" + event.getUser().getName() + "` vient d'être timeout pour " + event.getReason());
        bot.getLogger().log(Level.INFO, event.getUser().getName() + " vient d'être timeout");
    }

    private void onGameUpdate(ChannelChangeGameEvent event) {
        if(bot.getSessionManager().isSessionStarted()) {
            bot.getSessionManager().updateGame(event.getGameId());
        }
    }

    private void onMessageEvent(ChannelMessageEvent event) {
        String message = event.getMessage();
        String username = event.getUser().getName();
        String id = event.getUser().getId();
        bot.getLogger().log(Level.INFO, "LiveChat > " + username + " > " + message);
        chatSpyManager.addMessage(event.getUser().getName(), "\uD83D\uDCAC `" + username + "` » " + message);
        if (!commandMap.isKnown(id)) {
            commandMap.addKnownUser(id);
            event.getTwitchChat().sendMessage(bot.getChannelName().toLowerCase(), "Coucou @" + username + " ! Passe un bon moment sur le stream, et pose toi avec ton PopCorn !");
            if (bot.getSessionManager().isSessionStarted()) {
                bot.getSessionManager().getCurrentSession().addNewViewer();
            }
        }
        if (bot.getSessionManager().isSessionStarted()) {
            bot.getSessionManager().getCurrentSession().addMessage();
        }
        if (message.startsWith(commandMap.getTwitchTag())) {
            EventUser user = event.getUser();
            User userUser = bot.getTwitchClient().getHelix().getUsers(bot.getConfigurationManager().getStringValue("oauth2Token"), Collections.singletonList(user.getId()), null).execute().getUsers().get(0);
            String broadcaster = event.getChannel().getName();
            String broadcasterId = event.getChannel().getId();
            TwitchCommand.ExecutorRank executorRank = commandMap.getRank(event.getPermissions());
            message = message.replaceFirst(commandMap.getTwitchTag(), "");
            if (bot.getSessionManager().isSessionStarted()) {
                bot.getSessionManager().getCurrentSession().addCommandUse(message.split(" ")[0]);
            }
            commandMap.twitchCommandUser(userUser, broadcaster, broadcasterId, executorRank, message, event.getPermissions());
        }
    }

    public void closeListener() {
        chatSpyManager.stopSpying();
    }

    public ChatSpyManager getChatSpyManager() {
        return chatSpyManager;
    }
}
