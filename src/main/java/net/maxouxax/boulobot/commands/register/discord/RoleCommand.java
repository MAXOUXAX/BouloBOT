package net.maxouxax.boulobot.commands.register.discord;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.maxouxax.boulobot.BOT;
import net.maxouxax.boulobot.commands.Command;
import net.maxouxax.boulobot.commands.CommandMap;
import net.maxouxax.boulobot.roles.Grade;

import java.util.Optional;
import java.util.logging.Level;

public class RoleCommand {

    private final BOT bot;
    private final CommandMap commandMap;

    public RoleCommand(BOT bot, CommandMap commandMap){
        this.bot = bot;
        this.commandMap = commandMap;
    }

    @Command(name = "addrole", description = "Permet d'ajouter un rôle dans la liste des gradochats", help = ".addrole Nom du rôle ²² Description du rôle ²² @Role ²² réaction à ajouter", example = ".addrole Mangeur de pâtes ²² Pour tout les mangeur de pâtes ²² @Mangeur de pâtes ²² :clap: ", power = 100, type = Command.ExecutorType.USER)
    public void addrole(User user, TextChannel textChannel, Message message, String[] args){
        EmbedBuilder em = commandMap.getHelpEmbed("addrole");
        if (args.length == 0) {
            textChannel.sendMessage(em.build()).queue();
            bot.getLogger().log(Level.INFO, "args0");
        } else{
            bot.getLogger().log(Level.INFO, "!args0");
            if (message.getMentionedRoles().size() == 0 || message.getEmotes().size() == 0) {
                bot.getLogger().log(Level.INFO, "no role or no emote");
                textChannel.sendMessage(em.build()).queue();
            } else {
                bot.getLogger().log(Level.INFO, "there is role and emote");
                StringBuilder str = new StringBuilder();

                for (String arg : args) {
                    str.append(arg).append(" ");
                }

                if (!str.toString().contains("²²")) {
                    textChannel.sendMessage(em.build()).queue();
                    return;
                }

                String[] argsReal = str.toString().split(" ²² ");
                if (argsReal.length == 1) {
                    bot.getLogger().log(Level.INFO, "arg1");
                    textChannel.sendMessage(em.build()).queue();
                } else if (argsReal.length == 2) {
                    bot.getLogger().log(Level.INFO, "arg2");
                    textChannel.sendMessage(em.build()).queue();
                } else if (argsReal.length == 3) {
                    bot.getLogger().log(Level.INFO, "arg3");
                    textChannel.sendMessage(em.build()).queue();
                } else {
                    bot.getLogger().log(Level.INFO, "else");
                    String name = argsReal[0];
                    String description = argsReal[1];
                    Emote emote = message.getEmotes().get(0);
                    Role role = message.getMentionedRoles().get(0);
                    Grade grade = new Grade(role, name, description, emote.getIdLong());
                    bot.getRolesManager().registerGrade(grade);
                    textChannel.sendMessage("Rôle ajouté !\nNom: " + grade.getDisplayName() + "\nDescription: " + grade.getDescription() + "\nRôle: " + grade.getRole().getAsMention() + "\nRéaction: " + emote.getAsMention()).queue();
                    bot.getRolesManager().reloadRoles();
                }
            }
        }
    }

    @Command(name = "removerole", description = "Permet de retirer un rôle dans la liste des gradochats", help = ".removerole @Role", example = ".removerole @Mangeur de pâtes", power = 100, type = Command.ExecutorType.USER)
    public void removerole(User user, Message message, TextChannel textChannel, String[] args){
        EmbedBuilder em = commandMap.getHelpEmbed("removerole");
        if (args.length == 0) {
            textChannel.sendMessage(em.build()).queue();
        }else{
            if(message.getMentionedRoles().size() == 0){
                textChannel.sendMessage(em.build()).queue();
            }else{
                Role role = message.getMentionedRoles().get(0);
                Optional<Grade> grade = bot.getRolesManager().getGrades().stream().filter(grade1 -> grade1.getRole() == role).findFirst();
                if(grade.isPresent()){
                    bot.getRolesManager().getGrades().remove(grade.get());
                    textChannel.sendMessage("» Le gradochat "+role.getAsMention()+" a bien été suprimé !").queue();
                    bot.getRolesManager().reloadRoles();
                }else{
                    textChannel.sendMessage("» Aucun gradochat avec le nom "+role.getAsMention()).queue();
                }
            }
        }
    }

}
