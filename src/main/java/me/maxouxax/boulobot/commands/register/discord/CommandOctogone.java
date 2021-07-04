package me.maxouxax.boulobot.commands.register.discord;

import me.maxouxax.boulobot.BOT;
import me.maxouxax.boulobot.commands.Command;
import me.maxouxax.boulobot.commands.CommandMap;
import me.maxouxax.boulobot.util.EmbedCrafter;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;

import java.util.List;
import java.util.Random;

public class CommandOctogone {

    private final BOT bot;
    private final CommandMap commandMap;

    public CommandOctogone(CommandMap commandMap){
        this.commandMap = commandMap;
        this.bot = BOT.getInstance();
    }

    @Command(name = "octogone", description = "Vous voulez vous battre ? Bah battez vous !", help = ".octogone @<participants...>", example = ".octogone @LYORINE @MAXOUXAX @DJOXX")
    public void octogone(User user, TextChannel textChannel, Message message, String[] args){
        textChannel.sendTyping().queue();
        List<Member> participants = message.getMentionedMembers();
        if(participants.isEmpty()){
            textChannel.sendMessage(commandMap.getHelpEmbed("octogone")).queue();
        }else{
            if(participants.size() == 1 && participants.get(0).getId().equalsIgnoreCase(user.getId())){
                textChannel.sendMessage(commandMap.getHelpEmbed("octogone")).queue();
            }else{
                int random = new Random().nextInt(participants.size());
                Member winner = participants.get(random);
                StringBuilder participantsStr = new StringBuilder();

                for (int i = 0; i < participants.size(); i++) {
                    if ((i == participants.size() - 1)) {
                        participantsStr.append(participants.get(i).getAsMention());
                    } else {
                        participantsStr.append(participants.get(i).getAsMention()).append(" vs. ");
                    }
                }

                EmbedCrafter embedCrafter = new EmbedCrafter();
                embedCrafter.setTitle("Octogone", bot.getConfigurationManager().getStringValue("websiteUrl"))
                    .setAuthor(user.getName(), bot.getConfigurationManager().getStringValue("websiteUrl"), user.getAvatarUrl()+"?size=256")
                    .addField(participants.size()+" participants", participantsStr.toString(), true)
                    .addField("Vainqueur", winner.getAsMention(), true)
                    .setDescription("Bien joué à "+winner.getAsMention()+" qui a écrabouillé(e) ses adversaires. Quel(le) malade !")
                    .setColor(15528177);
                textChannel.sendMessage(embedCrafter.build()).queue();
            }
        }
    }

}
