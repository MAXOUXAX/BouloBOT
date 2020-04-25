package net.maxouxax.boulobot.roles;

import com.mrpowergamerbr.temmiewebhook.DiscordEmbed;
import com.mrpowergamerbr.temmiewebhook.DiscordMessage;
import com.mrpowergamerbr.temmiewebhook.TemmieWebhook;
import com.mrpowergamerbr.temmiewebhook.embed.FooterEmbed;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.maxouxax.boulobot.BOT;
import net.maxouxax.boulobot.util.JSONReader;
import net.maxouxax.boulobot.util.JSONWriter;
import net.maxouxax.boulobot.util.Reference;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;

public class RolesManager {

    private Message messageForRoles;
    private final TextChannel textChannelRoles;
    private final ArrayList<Grade> grades = new ArrayList<>();
    private final BOT bot;

    public RolesManager(TextChannel textChannelRoles, BOT bot) {
        this.textChannelRoles = textChannelRoles;
        this.bot = bot;
    }

    public void reloadRoles(){
        saveRoles();
        grades.clear();
        loadRoles();
    }

    public void loadRoles(){
        bot.getLogger().log(Level.INFO, "> LoadRoles");
        File file = new File("roles.json");
        bot.getLogger().log(Level.INFO,"> file roles.json");
        if(!file.exists()) return;

        try{
            JSONReader reader = new JSONReader(file, this.bot);
            JSONArray array = reader.toJSONArray();

            for(int i = 0; i < array.length(); i++)
            {
                JSONObject object = array.getJSONObject(i);
                Grade gradeToAdd = new Grade(textChannelRoles.getGuild().getRoleById(object.getLong("id")), object.getString("displayname"), object.getString("description"), object.getLong("emoteId"));
                bot.getLogger().log(Level.INFO,"> Created grade");
                bot.getLogger().log(Level.INFO,"> "+gradeToAdd.toString());
                grades.add(gradeToAdd);
                bot.getLogger().log(Level.INFO,"> Grade added to list");
            }

        }catch(IOException e){
            bot.getErrorHandler().handleException(e);
        }
        loadMessage();
    }

    private void loadMessage() {
        bot.getLogger().log(Level.INFO,"> LoadMessage");
        long messageId = bot.getConfigurationManager().getLongValue("messageRolesID");
        this.messageForRoles = textChannelRoles.retrieveMessageById(messageId).complete();
        bot.getLogger().log(Level.INFO,"> TextChannel getted!");

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                sendMessage();
            }
        }, 5000);
    }

    private void sendMessage() {
        bot.getLogger().log(Level.INFO,"> SendMessage");
        if(messageForRoles == null){
            bot.getLogger().log(Level.INFO,"> Message do not exist, let's send it!");
            TemmieWebhook temmieWebhook = new TemmieWebhook(Reference.GradesWebhookURL.getString());
            DiscordEmbed discordEmbed = DiscordEmbed.builder()
                    .title("Grades")
                    .description("» Afin d'obtenir le grade choisi, veuillez ajouter la réaction correspondante. Veuillez noter, que ces grades sont purement visuels, ils ne vous apporteront aucune permission supplémentaire.")
                    .footer(new FooterEmbed(Reference.EmbedFooter.getString(), Reference.EmbedIcon.getString(), Reference.EmbedIcon.getString()))
                    .color(3447003)
                    .build();
            DiscordMessage discordMessage = DiscordMessage.builder().embed(discordEmbed).build();
            temmieWebhook.sendMessage(discordMessage);
            bot.getLogger().log(Level.INFO,"> Webhook message sended!");
            EmbedBuilder embedBuilder = new EmbedBuilder();
            embedBuilder.setTitle("Liste des grades", "https://lyorine.com")
                    .setFooter(Reference.EmbedFooter.getString(), Reference.EmbedIcon.getString())
                    .setThumbnail(Reference.RankThumbnail.getString() + "?size=256")
                    .setColor(3066993);
            grades.forEach(grade -> {
                Emote emote = textChannelRoles.getGuild().getEmoteById(grade.getEmoteId());
                embedBuilder.addField("Grade: "+grade.getDisplayName(), grade.getDescription()+"\nRéaction a ajouter » "+emote.getAsMention(), true);
            });
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    textChannelRoles.sendMessage(embedBuilder.build()).queue();
                    bot.getLogger().log(Level.INFO,"> EmbedRoles sended!");
                }
            }, 5000);
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    messageForRoles = textChannelRoles.getHistory().getMessageById(textChannelRoles.getLatestMessageId());
                    bot.getLogger().log(Level.INFO,"> MessageForRoles correctly setted to "+messageForRoles.getId()+" !");
                }
            }, 15000);
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    grades.forEach(grade -> {
                        Emote emote = textChannelRoles.getGuild().getEmoteById(grade.getEmoteId());
                        messageForRoles.addReaction(emote).queue();
                    });
                }
            }, 20000);
        }else{
            bot.getLogger().log(Level.INFO,"> Message already existing, refreshing it!");
            EmbedBuilder embedBuilder = new EmbedBuilder();
            embedBuilder.setTitle("Liste des grades", "https://lyorine.com")
                    .setFooter(Reference.EmbedFooter.getString(), Reference.EmbedIcon.getString())
                    .setThumbnail(Reference.RankThumbnail.getString() + "?size=256")
                    .setColor(3066993);
            grades.forEach(grade -> {
                Emote emote = textChannelRoles.getGuild().getEmoteById(grade.getEmoteId());
                if(emote != null) {
                    embedBuilder.addField("Grade: " + grade.getDisplayName(), grade.getDescription() + "\nRéaction a ajouter » " + emote.getAsMention(), true);
                }else{
                    embedBuilder.addField("Grade: " + grade.getDisplayName(), grade.getDescription() + "\nRéaction a ajouter » Inconnue (contacter un administrateur !)", true);
                    bot.getErrorHandler().handleException(new Exception("Unknown emote"));
                }
            });
            messageForRoles.editMessage(embedBuilder.build()).queue();
            grades.forEach(grade -> {
                Emote emote = textChannelRoles.getGuild().getEmoteById(grade.getEmoteId());
                messageForRoles.addReaction(emote).queue();
            });
            bot.getLogger().log(Level.INFO,"> Message refreshed!");
        }
    }

    public void saveRoles(){
        bot.getLogger().log(Level.INFO,"> saveRoles");
        JSONArray array = new JSONArray();
        for(Grade grade : grades)
        {
            bot.getLogger().log(Level.INFO,"> Saving grade: "+grade.getDisplayName());
            JSONObject object = new JSONObject();
            object.accumulate("id", grade.getRole().getIdLong());
            object.accumulate("displayname", grade.getDisplayName());
            object.accumulate("description", grade.getDescription());
            object.accumulate("emoteId", grade.getEmoteId());
            array.put(object);
        }
        try(JSONWriter writter = new JSONWriter("roles.json")){
            writter.write(array);
            writter.flush();
        }catch(IOException e){
            bot.getErrorHandler().handleException(e);
        }


        JSONArray arrayConfig = new JSONArray();
        JSONObject object = new JSONObject();
        object.accumulate("messageRolesID", messageForRoles.getIdLong());
        arrayConfig.put(object);
        try(JSONWriter writter = new JSONWriter("config.json")){
            writter.write(arrayConfig);
            writter.flush();
        }catch(IOException ioe){
            ioe.printStackTrace();
        }
    }

    public Message getMessageForRoles() {
        return messageForRoles;
    }

    public TextChannel getTextChannelRoles() {
        return textChannelRoles;
    }

    public ArrayList<Grade> getGrades() {
        return grades;
    }

    public void registerGrade(Grade grade) {
        this.grades.add(grade);
        bot.getLogger().log(Level.INFO,"> Grades size: "+grades.size());
    }
}
