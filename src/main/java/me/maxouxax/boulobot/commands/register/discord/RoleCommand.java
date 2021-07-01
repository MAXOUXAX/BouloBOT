package me.maxouxax.boulobot.commands.register.discord;

import me.maxouxax.boulobot.BOT;
import me.maxouxax.boulobot.commands.Command;
import me.maxouxax.boulobot.commands.CommandMap;
import me.maxouxax.boulobot.commands.slashannotations.Option;
import me.maxouxax.boulobot.roles.Grade;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;

import java.util.Optional;

public class RoleCommand {

    private final BOT bot;
    private final CommandMap commandMap;

    public RoleCommand(CommandMap commandMap){
        this.commandMap = commandMap;
        this.bot = BOT.getInstance();
    }

    @Option(name = "Nom du rôle", description = "Nom du rôle affichée dans l'embed des rôles", type = OptionType.STRING, isRequired = true)
    @Option(name = "Description du rôle", description = "Description du rôle affichée dans l'embed des rôles", type = OptionType.STRING, isRequired = true)
    @Option(name = "Rôle", description = "Rôle à ajouter dans l'embed des rôles", type = OptionType.ROLE, isRequired = true)
    @Option(name = "Réaction", description = "Réaction à ajouter sur l'embed des rôles pour recevoir le rôle", type = OptionType.STRING, isRequired = true)
    @Command(name = "addrole", description = "Permet d'ajouter un rôle dans la liste des gradochats", help = ".addrole Nom du rôle ²² Description du rôle ²² @Role ²² réaction à ajouter", example = ".addrole Mangeur de pâtes ²² Pour tout les mangeur de pâtes ²² @Mangeur de pâtes ²² :clap: ", power = 100)
    public void addrole(User user, TextChannel textChannel, Message message, SlashCommandEvent slashCommandEvent) {
        String name = slashCommandEvent.getOption("Nom du rôle").getAsString();
        String description = slashCommandEvent.getOption("Description du rôle").getAsString();
        Emote emote = textChannel.getGuild().getEmoteById(slashCommandEvent.getOption("Réaction").getAsString());
        Role role = slashCommandEvent.getOption("Rôle").getAsRole();
        Grade grade = new Grade(role, name, description, emote.getIdLong());
        bot.getRolesManager().registerGrade(grade);
        slashCommandEvent.reply("Rôle ajouté !\nNom: " + grade.getDisplayName() + "\nDescription: " + grade.getDescription() + "\nRôle: " + grade.getRole().getAsMention() + "\nRéaction: " + emote.getAsMention()).queue();
        bot.getRolesManager().reloadRoles();
    }

    @Option(name = "Rôle", description = "Rôle à supprimer de l'embed des rôles", isRequired = true, type = OptionType.ROLE)
    @Command(name = "removerole", description = "Permet de retirer un rôle dans la liste des gradochats", help = ".removerole @Role", example = ".removerole @Mangeur de pâtes", power = 100)
    public void removerole(User user, Message message, TextChannel textChannel, SlashCommandEvent slashCommandEvent) {
        Role role = slashCommandEvent.getOption("Rôle").getAsRole();
        Optional<Grade> grade = bot.getRolesManager().getGrades().stream().filter(grade1 -> grade1.getRole() == role).findFirst();
        if (grade.isPresent()) {
            bot.getRolesManager().getGrades().remove(grade.get());
            slashCommandEvent.reply("» Le gradochat " + role.getAsMention() + " a bien été suprimé !").queue();
            bot.getRolesManager().reloadRoles();
        } else {
            slashCommandEvent.reply("» Aucun gradochat avec le nom " + role.getAsMention()).queue();
        }
    }

}
