package me.maxouxax.boulobot.roles;

import me.maxouxax.boulobot.BOT;
import me.maxouxax.boulobot.database.DatabaseManager;
import me.maxouxax.boulobot.util.EmbedCrafter;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
        grades.clear();
        loadRoles();
    }

    public void loadRoles(){
        try {
            Connection connection = DatabaseManager.getDatabaseAccess().getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM roles");

            final ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                String displayName = resultSet.getString("displayname");
                String description = resultSet.getString("description");
                String roleId = resultSet.getString("role_id");
                String emoteId = resultSet.getString("emote_id");
                Grade gradeToAdd = new Grade(textChannelRoles.getGuild().getRoleById(roleId), displayName, description, emoteId);
                grades.add(gradeToAdd);
            }
            connection.close();
        }catch (SQLException e){
            bot.getErrorHandler().handleException(e);
        }
        loadMessage();
    }

    private void loadMessage() {
        long messageId = bot.getConfigurationManager().getLongValue("messageRolesID");
        try{
            this.messageForRoles = textChannelRoles.retrieveMessageById(messageId).complete();
        }catch (Exception e){
            bot.getErrorHandler().handleException(e);
        }
        sendMessage();
    }

    private void sendMessage() {
        if(messageForRoles == null){
            EmbedCrafter embedCrafter = new EmbedCrafter()
                    .setTitle("Grades")
                    .setDescription("?? Afin d'obtenir le grade choisi, veuillez ajouter la r??action correspondante. Veuillez noter que ces grades sont purement visuels, ils ne vous apporteront aucune permission suppl??mentaire.")
                    .setColor(3447003);
            EmbedCrafter embedCrafter2 = generateRoleEmbed();
            textChannelRoles.sendMessage(embedCrafter.build()).queue();
            textChannelRoles.sendMessage(embedCrafter2.build()).queue(message -> {
                messageForRoles = message;
                bot.getConfigurationManager().setValue("messageRolesID", message.getId());
                grades.forEach(grade -> {
                    Emote emote = textChannelRoles.getGuild().getEmoteById(grade.getEmoteId());
                    messageForRoles.addReaction(emote).queue();
                });
            });
        }else{
            EmbedCrafter embedCrafter = generateRoleEmbed();
            messageForRoles.editMessage(embedCrafter.build()).queue(message -> {
                messageForRoles = message;
                grades.forEach(grade -> {
                    Emote emote = textChannelRoles.getGuild().getEmoteById(grade.getEmoteId());
                    messageForRoles.addReaction(emote).queue();
                });
            });
        }
    }

    public EmbedCrafter generateRoleEmbed(){
        EmbedCrafter embedCrafter = new EmbedCrafter()
                .setTitle("Liste des grades", bot.getConfigurationManager().getStringValue("websiteUrl"))
                .setThumbnailUrl(bot.getConfigurationManager().getStringValue("rolesThumbnailUrl") + "?size=256")
                .setColor(3066993);
        grades.forEach(grade -> {
            Emote emote = textChannelRoles.getGuild().getEmoteById(grade.getEmoteId());
            if(emote != null) {
                embedCrafter.addField(grade.getDisplayName(), grade.getDescription() + "\nR??action a ajouter ?? " + emote.getAsMention(), true);
            }else{
                embedCrafter.addField(grade.getDisplayName(), grade.getDescription() + "\nR??action a ajouter ?? Inconnue (contacter un administrateur !)", true);
                bot.getErrorHandler().handleException(new Exception("Unknown emote"));
            }
        });
        return embedCrafter;
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
        try {
            this.grades.add(grade);
            Connection connection = DatabaseManager.getDatabaseAccess().getConnection();
            PreparedStatement insertPreparedStatement = connection.prepareStatement("INSERT INTO roles (displayname, description, role_id, emote_id) VALUES (?, ?, ?, ?)");
            insertPreparedStatement.setString(1, grade.getDisplayName());
            insertPreparedStatement.setString(2, grade.getDescription());
            insertPreparedStatement.setString(3, grade.getRole().getId());
            insertPreparedStatement.setString(4, grade.getEmoteId());
            insertPreparedStatement.execute();
        }catch (SQLException e) {
            bot.getErrorHandler().handleException(e);
        }
    }

    public void unregisterGrade(Grade grade) {
        try {
            this.grades.remove(grade);
            Connection connection = DatabaseManager.getDatabaseAccess().getConnection();
            PreparedStatement insertPreparedStatement = connection.prepareStatement("DELETE FROM roles WHERE role_id = ?");
            insertPreparedStatement.setString(1, grade.getRole().getId());
            insertPreparedStatement.execute();
        }catch (SQLException e) {
            bot.getErrorHandler().handleException(e);
        }
    }

}
