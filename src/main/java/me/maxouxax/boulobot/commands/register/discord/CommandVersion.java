package me.maxouxax.boulobot.commands.register.discord;

import me.maxouxax.boulobot.BOT;
import me.maxouxax.boulobot.commands.Command;
import me.maxouxax.boulobot.commands.CommandMap;
import me.maxouxax.boulobot.util.EmbedCrafter;
import net.dv8tion.jda.api.entities.MessageChannel;

public class CommandVersion {

    private final BOT bot;
    private final CommandMap commandMap;

    public CommandVersion(CommandMap commandMap) {
        this.commandMap = commandMap;
        this.bot = BOT.getInstance();
    }

    @Command(name="version",type= Command.ExecutorType.USER,description="Affiche les informations sur la version du BOT", help = ".version", example = ".version")
    private void version(MessageChannel channel){
        try{
            EmbedCrafter embedCrafter = new EmbedCrafter();
            embedCrafter.setTitle("BouloBOT by MAXOUXAX • Amazingly powerful.", bot.getConfigurationManager().getStringValue("websiteUrl"))
                .setColor(3447003)
                .addField("Je suis en version", bot.getVersion(), true)
                .addField("Je gère", commandMap.getDiscordCommands().size()+" commandes Discord", true)
                .addField("Je gère", commandMap.getTwitchCommands().size()+" commandes Twitch", true)
                .addField("J'ai souhaité la bienvenue à", commandMap.getUserIds().size()+" viewers", true);
            channel.sendMessage(embedCrafter.build()).queue();
        }catch (Exception e) {
            bot.getErrorHandler().handleException(e);
            channel.sendMessage("An error occured. > " + e.getMessage()).queue();
        }
    }

}
