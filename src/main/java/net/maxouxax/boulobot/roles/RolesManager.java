package net.maxouxax.boulobot.roles;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.maxouxax.boulobot.BOT;
import net.maxouxax.boulobot.util.JSONReader;
import net.maxouxax.boulobot.util.JSONWriter;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class RolesManager {

    private Message messageForRoles;
    private final TextChannel textChannelRoles;
    private final ArrayList<Grade> grades = new ArrayList<>();
    private final BOT bot;

    public RolesManager(TextChannel textChannelRoles) {
        this.textChannelRoles = textChannelRoles;
        this.bot = BOT.getInstance();
    }

    public void reloadRoles(){
        saveRoles();
        grades.clear();
        loadRoles();
    }

    public void loadRoles(){
        File file = new File("roles.json");
        if(!file.exists()) return;

        try{
            JSONReader reader = new JSONReader(file);
            JSONArray array = reader.toJSONArray();

            for(int i = 0; i < array.length(); i++)
            {
                JSONObject object = array.getJSONObject(i);
                Grade gradeToAdd = new Grade(textChannelRoles.getGuild().getRoleById(object.getLong("id")), object.getString("displayname"), object.getString("description"), object.getLong("emoteId"));
                grades.add(gradeToAdd);
            }

        }catch(IOException e){
            bot.getErrorHandler().handleException(e);
        }
        loadMessage();
    }

    private void loadMessage() {
        long messageId = bot.getConfigurationManager().getLongValue("messageRolesID");
        textChannelRoles.retrieveMessageById(messageId).queue(message -> this.messageForRoles = message);
        sendMessage();
    }

    private void sendMessage() {
        if(messageForRoles == null){
            EmbedBuilder embedBuilder = new EmbedBuilder()
                    .setTitle("Grades")
                    .setDescription("» Afin d'obtenir le grade choisi, veuillez ajouter la réaction correspondante. Veuillez noter, que ces grades sont purement visuels, ils ne vous apporteront aucune permission supplémentaire.")
                    .setFooter(bot.getConfigurationManager().getStringValue("embedFooter"), bot.getConfigurationManager().getStringValue("embedIconUrl"))
                    .setColor(3447003);
            EmbedBuilder embedBuilder1 = new EmbedBuilder()
                    .setTitle("Liste des grades", "https://lyorine.com")
                    .setFooter(bot.getConfigurationManager().getStringValue("embedFooter"), bot.getConfigurationManager().getStringValue("embedIconUrl"))
                    .setThumbnail(bot.getConfigurationManager().getStringValue("rolesThumbnailUrl") + "?size=256")
                    .setColor(3066993);
            grades.forEach(grade -> {
                Emote emote = textChannelRoles.getGuild().getEmoteById(grade.getEmoteId());
                embedBuilder1.addField("Grade: "+grade.getDisplayName(), grade.getDescription()+"\nRéaction a ajouter » "+emote.getAsMention(), true);
            });
            textChannelRoles.sendMessage(embedBuilder.build()).queue();
            textChannelRoles.sendMessage(embedBuilder1.build()).queue(message -> {
                messageForRoles = message;
                bot.getConfigurationManager().setValue("messageRolesID", message.getId(), true);
                grades.forEach(grade -> {
                    Emote emote = textChannelRoles.getGuild().getEmoteById(grade.getEmoteId());
                    messageForRoles.addReaction(emote).queue();
                });
            });
        }else{
            EmbedBuilder embedBuilder = new EmbedBuilder()
                    .setTitle("Liste des grades", "https://lyorine.com")
                    .setFooter(bot.getConfigurationManager().getStringValue("embedFooter"), bot.getConfigurationManager().getStringValue("embedIconUrl"))
                    .setThumbnail(bot.getConfigurationManager().getStringValue("rolesThumbnailUrl") + "?size=256")
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
            messageForRoles.editMessage(embedBuilder.build()).queue(message -> {
                messageForRoles = message;
                grades.forEach(grade -> {
                    Emote emote = textChannelRoles.getGuild().getEmoteById(grade.getEmoteId());
                    messageForRoles.addReaction(emote).queue();
                });
            });
        }
    }

    public void saveRoles(){
        JSONArray array = new JSONArray();
        for(Grade grade : grades)
        {
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
    }
}
