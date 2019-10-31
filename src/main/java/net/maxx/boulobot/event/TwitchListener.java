package net.maxx.boulobot.event;

import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import com.github.twitch4j.common.events.domain.EventUser;
import com.github.twitch4j.helix.domain.User;
import net.maxx.boulobot.BOT;
import net.maxx.boulobot.commands.CommandMap;
import net.maxx.boulobot.commands.TwitchCommand;

import java.util.Collections;
import java.util.logging.Level;

public class TwitchListener {

    private final CommandMap commandMap;
    private final BOT botDiscord;

    public TwitchListener(CommandMap commandMap, BOT botDiscord){
        this.commandMap = commandMap;
        this.botDiscord = botDiscord;

        botDiscord.getTwitchClient().getEventManager().onEvent(ChannelMessageEvent.class).subscribe(this::onMessageEvent);
    }

    private void onMessageEvent(ChannelMessageEvent channelMessageEvent) {
        String message = channelMessageEvent.getMessage();
        botDiscord.getLogger().log(Level.INFO, "onMessage");
        if (message.startsWith(commandMap.getTwitchTag())) {
            botDiscord.getLogger().log(Level.INFO, "onCommand");
            EventUser user = channelMessageEvent.getUser();
            User userUser = botDiscord.getTwitchClient().getHelix().getUsers(null, Collections.singletonList(user.getId()), null).execute().getUsers().get(0);
            botDiscord.getLogger().log(Level.INFO, "User getted");
            String userName = user.getName();
            String broadcaster = channelMessageEvent.getChannel().getName();
            TwitchCommand.ExecutorRank executorRank = commandMap.getRank(channelMessageEvent.getPermissions());
            botDiscord.getLogger().log(Level.INFO, "rank getted");
            message = message.replaceFirst(commandMap.getTwitchTag(), "");
            commandMap.twitchCommandUser(userUser, broadcaster, executorRank, message);
            botDiscord.getLogger().log(Level.INFO, "command executed");
        }
    }


}
