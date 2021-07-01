package me.maxouxax.boulobot.commands.register.discord;

import me.maxouxax.boulobot.BOT;
import me.maxouxax.boulobot.commands.Command;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;

import java.util.HashMap;
import java.util.List;

public class CommandLock {

    private final BOT bot;
    private final HashMap<GuildChannel, List<PermissionOverride>> channelsHashMap = new HashMap<>();

    public CommandLock(){
        this.bot = BOT.getInstance();
    }

    @Command(name="lock", power = 150, help = "lock", example = "lock")
    public void lock(User user, TextChannel textChannel, String[] args) {
        Guild guild = textChannel.getGuild();
        List<GuildChannel> channelsList = guild.getChannels();
        channelsList.forEach(channel -> {
            channelsHashMap.put(channel, channel.getPermissionOverrides());
            channel.getPermissionOverrides().forEach(permissionOverride -> {
                permissionOverride.delete().queue();
            });
            channel.putPermissionOverride(guild.getRoleById("529310963816595467")).deny(Permission.MESSAGE_READ).deny(Permission.MESSAGE_WRITE).deny(Permission.ALL_VOICE_PERMISSIONS).queue();
        });
    }

    @Command(name="unlock", power = 150, help = "unlock", example = "unlock")
    public void unlock(User user, TextChannel textChannel, String[] args) {
        Guild guild = textChannel.getGuild();
        List<GuildChannel> channelsList = guild.getChannels();
        channelsList.forEach(channel -> {
            List<PermissionOverride> permissions = channelsHashMap.get(channel);
            permissions.forEach(permissionOverride -> {
                channel.putPermissionOverride(permissionOverride.getPermissionHolder())
                        .setAllow(permissionOverride.getAllowedRaw())
                        .setDeny(permissionOverride.getDeniedRaw())
                        .queue();
            });
        });
        channelsHashMap.clear();
    }

}
