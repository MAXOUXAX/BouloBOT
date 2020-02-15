package net.maxx.boulobot.commands.register.discord;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.maxx.boulobot.BOT;
import net.maxx.boulobot.commands.Command;
import net.maxx.boulobot.commands.CommandMap;
import net.maxx.boulobot.util.Reference;

import java.util.List;
import java.util.Random;

public class CommandOctogone {

    private final BOT botDiscord;
    private final CommandMap commandMap;

    public CommandOctogone(BOT botDiscord, CommandMap commandMap){
        this.botDiscord = botDiscord;
        this.commandMap = commandMap;
    }

    @Command(name = "octogone", description = "Vous voulez vous battre ? Bah battez vous !", help = ".octogone @<participants...>", example = ".octogone @LYORINE @MAXOUXAX @DJOXX", type = Command.ExecutorType.USER)
    public void octogone(User user, TextChannel textChannel, Message message, String[] args){
        textChannel.sendTyping().queue();
        List<Member> participants = message.getMentionedMembers();
        if(participants.isEmpty()){
            EmbedBuilder em = commandMap.getHelpEmbed("octogone");
            textChannel.sendMessage(em.build()).queue();
        }else{
            if(participants.size() == 1 && participants.get(0).getId().equalsIgnoreCase(user.getId())){
                EmbedBuilder em = commandMap.getHelpEmbed("octogone");
                textChannel.sendMessage(em.build()).queue();
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

                EmbedBuilder embedBuilder = new EmbedBuilder();
                embedBuilder.setTitle("Octogone", "https://lyorine.com");
                embedBuilder.setAuthor(user.getName(), null, user.getAvatarUrl()+"?size=256");
                embedBuilder.addField(participants.size()+" participants", participantsStr.toString(), true);
                embedBuilder.addField("Vainqueur", winner.getAsMention(), true);
                embedBuilder.setDescription("Bien joué à "+winner.getAsMention()+" qui a écrabouillé(e) ses adversaires. Quel(le) malade !");
                embedBuilder.setColor(15528177);
                embedBuilder.setFooter(Reference.EmbedFooter.asDate(), Reference.EmbedIcon.getString());
            }
        }
    }

}
