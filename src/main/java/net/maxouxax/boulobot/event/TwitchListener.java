package net.maxouxax.boulobot.event;

import com.github.philippheuer.events4j.simple.SimpleEventHandler;
import com.github.twitch4j.chat.events.channel.*;
import com.github.twitch4j.common.events.channel.ChannelChangeGameEvent;
import com.github.twitch4j.common.events.domain.EventUser;
import com.github.twitch4j.helix.domain.User;
import net.maxouxax.boulobot.BOT;
import net.maxouxax.boulobot.commands.CommandMap;
import net.maxouxax.boulobot.commands.TwitchCommand;

import java.util.Collections;
import java.util.logging.Level;

public class TwitchListener {

    private final CommandMap commandMap;
    private final BOT botDiscord;

    public TwitchListener(CommandMap commandMap, BOT botDiscord){
        this.commandMap = commandMap;
        this.botDiscord = botDiscord;

        botDiscord.getTwitchClient().getEventManager().getEventHandler(SimpleEventHandler.class).onEvent(ChannelMessageEvent.class, this::onMessageEvent);
        botDiscord.getTwitchClient().getEventManager().getEventHandler(SimpleEventHandler.class).onEvent(ChannelChangeGameEvent.class, this::onGameUpdate);
        botDiscord.getTwitchClient().getEventManager().getEventHandler(SimpleEventHandler.class).onEvent(UserTimeoutEvent.class, this::onTimeOut);
        botDiscord.getTwitchClient().getEventManager().getEventHandler(SimpleEventHandler.class).onEvent(UserBanEvent.class, this::onBan);
        botDiscord.getTwitchClient().getEventManager().getEventHandler(SimpleEventHandler.class).onEvent(IRCMessageEvent.class, this::ircMessage);
        botDiscord.getTwitchClient().getEventManager().getEventHandler(SimpleEventHandler.class).onEvent(FollowEvent.class, this::followEvent);
    }

    private void followEvent(FollowEvent followEvent) {
        if(botDiscord.getSessionManager().isSessionStarted()){
            botDiscord.getSessionManager().getCurrentSession().addFollower();
        }
        botDiscord.getLogger().log(Level.INFO, followEvent.getUser().getName()+" vient de follow !");
    }

    private void ircMessage(IRCMessageEvent event) {
        if(event.getMessage().isPresent()) {
            String message = event.getMessage().get();
            if(event.getUser() != null) {
                String username = event.getUser().getName();
                botDiscord.getLogger().log(Level.INFO, "LiveChat > " + username + " > " + message);
                String id = event.getUser().getId();
                if (id == null) return;
                if (!commandMap.isKnown(id)) {
                    commandMap.addKnownUser(id);
                    event.getTwitchChat().sendMessage(botDiscord.getChannelName().toLowerCase(), "Coucou @" + username + " ! Passe un bon moment sur le stream, et pose toi avec ton PopCorn !");
                    if (botDiscord.getSessionManager().isSessionStarted()) {
                        botDiscord.getSessionManager().getCurrentSession().addNewViewer();
                    }
                }
            }
        }else{
            botDiscord.getLogger().log(Level.WARNING, event.getRawMessage());
        }
    }

    private void onBan(UserBanEvent userBanEvent) {
        if(botDiscord.getSessionManager().isSessionStarted()){
            botDiscord.getSessionManager().getCurrentSession().addBanOrTimeout();
        }
        botDiscord.getLogger().log(Level.INFO, userBanEvent.getUser().getName()+" vient d'être banni");
    }

    private void onTimeOut(UserTimeoutEvent userTimeoutEvent) {
        if(botDiscord.getSessionManager().isSessionStarted()){
            botDiscord.getSessionManager().getCurrentSession().addBanOrTimeout();
        }
        botDiscord.getLogger().log(Level.INFO, userTimeoutEvent.getUser().getName()+" vient d'être timeout");
    }

    private void onGameUpdate(ChannelChangeGameEvent channelChangeGameEvent) {
        botDiscord.getSessionManager().updateGame(channelChangeGameEvent.getGameId());
    }

    private void onMessageEvent(ChannelMessageEvent channelMessageEvent) {
        String message = channelMessageEvent.getMessage();
        if(botDiscord.getSessionManager().isSessionStarted()){
            botDiscord.getSessionManager().getCurrentSession().addMessage();
        }
        if (message.startsWith(commandMap.getTwitchTag())) {
            EventUser user = channelMessageEvent.getUser();
            User userUser = botDiscord.getTwitchClient().getHelix().getUsers(null, Collections.singletonList(user.getId()), null).execute().getUsers().get(0);
            String broadcaster = channelMessageEvent.getChannel().getName();
            String broadcasterId = channelMessageEvent.getChannel().getId();
            TwitchCommand.ExecutorRank executorRank = commandMap.getRank(channelMessageEvent.getPermissions());
            message = message.replaceFirst(commandMap.getTwitchTag(), "");
            if(botDiscord.getSessionManager().isSessionStarted()) {
                botDiscord.getSessionManager().getCurrentSession().addCommandUse(message.split(" ")[0]);
            }
            commandMap.twitchCommandUser(userUser, broadcaster, broadcasterId, executorRank, message, channelMessageEvent.getPermissions());
        }
    }


}
