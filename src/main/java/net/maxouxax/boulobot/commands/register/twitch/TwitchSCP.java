package net.maxouxax.boulobot.commands.register.twitch;

import com.github.twitch4j.helix.domain.User;
import net.maxouxax.boulobot.BOT;
import net.maxouxax.boulobot.commands.CommandMap;
import net.maxouxax.boulobot.commands.TwitchCommand;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class TwitchSCP {

    private final BOT botDiscord;
    private final CommandMap commandMap;

    public TwitchSCP(BOT botDiscord, CommandMap commandMap){
        this.botDiscord = botDiscord;
        this.commandMap = commandMap;
    }

    @TwitchCommand(name = "scp", example = "&scp", help = "&scp", description = "Récupérer les informations d'un SPC", rank = TwitchCommand.ExecutorRank.EVERYONE)
    private void scp(User user, String broadcaster, String[] args){
        if(args.length == 0){
            botDiscord.getTwitchClient().getChat().sendMessage(broadcaster, "Pour récupérer la description d'un SCP: &scp <numéro>");
        }else if(args.length == 1){
            String scp = args[0];

            try {
                Document doc = Jsoup.connect("http://fondationscp.wikidot.com/scp-"+scp).get();
                Elements newsHeadlines = doc.select("p:contains(Description)");
                String description = "Introuvable";
                for (Element headline : newsHeadlines) {
                    description = headline.text();
                }

                int msgLength = description.length()+70;
                if(msgLength >= 500){
                    description = description.substring(0, 420)+"...";
                    botDiscord.getTwitchClient().getChat().sendMessage(broadcaster, description + " | Pour en savoir plus: http://fondationscp.wikidot.com/scp-" + scp);
                }else {
                    if (description.equalsIgnoreCase("Introuvable")) {
                        botDiscord.getTwitchClient().getChat().sendMessage(broadcaster, "Description: " + description);
                    } else {
                        botDiscord.getTwitchClient().getChat().sendMessage(broadcaster, description + " | Pour en savoir plus: http://fondationscp.wikidot.com/scp-" + scp);
                    }
                }
            }catch (Exception e){
                botDiscord.getErrorHandler().handleException(e);
                botDiscord.getTwitchClient().getChat().sendMessage(broadcaster, "Une erreur est survenue: le SCP n'existe peut-être pas. Erreur » "+e.getMessage());
            }

        }else{
            botDiscord.getTwitchClient().getChat().sendMessage(broadcaster, "Trop d'arguments ! Pour récupérer la description d'un SCP: &scp <numéro>");
        }
    }

}
