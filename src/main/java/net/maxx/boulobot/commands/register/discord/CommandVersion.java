package net.maxx.boulobot.commands.register.discord;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.maxx.boulobot.BOT;
import net.maxx.boulobot.commands.Command;
import net.maxx.boulobot.commands.CommandMap;
import net.maxx.boulobot.util.Reference;

public class CommandVersion {

    private final BOT botDiscord;
    private final CommandMap commandMap;

    public CommandVersion(BOT botDiscord, CommandMap commandMap) {
        this.botDiscord = botDiscord;
        this.commandMap = commandMap;
    }

    @Command(name="version",type= Command.ExecutorType.USER,description="Affiche les informations sur la version du BOT", help = ".version", example = ".version")
    private void version(MessageChannel channel){
        try{
            EmbedBuilder builder = new EmbedBuilder();
            builder.setTitle("BouloBOT • A bot developped by Maxx_", Reference.WebsiteURL.getString());
            builder.setColor(3447003);
            builder.addField("Je suis en version", botDiscord.getVersion(), true);
            builder.addField("Je gère", commandMap.getDiscordCommands().size()+" commandes Discord", true);
            builder.addField("Je gère", commandMap.getTwitchCommands().size()+" commandes Twitch", true);
            builder.addField("J'ai souhaité la bienvenue à", commandMap.getUserIds().size()+" viewers", true);
            channel.sendMessage(builder.build()).queue();
        }catch (Exception e) {
            e.printStackTrace();
            channel.sendMessage("An error occured. > " + e.getMessage()).queue();
        }
    }

}
