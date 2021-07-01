package me.maxouxax.boulobot.commands.register.discord;

import com.github.twitch4j.helix.domain.UserList;
import me.maxouxax.boulobot.BOT;
import me.maxouxax.boulobot.commands.Command;
import me.maxouxax.boulobot.commands.CommandMap;
import me.maxouxax.boulobot.commands.slashannotations.Option;
import me.maxouxax.boulobot.commands.slashannotations.Subcommand;
import me.maxouxax.boulobot.util.ChatSpyManager;
import me.maxouxax.boulobot.util.EmbedCrafter;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;

import java.util.Collections;
import java.util.concurrent.atomic.AtomicReference;

public class CommandIgnore {

    private final BOT bot;
    private final CommandMap commandMap;

    public CommandIgnore(CommandMap commandMap) {
        this.bot = BOT.getInstance();
        this.commandMap = commandMap;
    }

    @Subcommand(name = "add", description = "Ajouter un utilisateur ignoré")
    @Subcommand(name = "remove", description = "Supprimer un utilisateur ignoré")
    @Subcommand(name = "list", description = "Afficher la liste des utilisateurs ignorés")
    @Option(name = "nom-de-lutilisateur", description = "Nom de l'utilisateur à ignoré", type = OptionType.STRING, isRequired = false)
    @Command(name = "ignore", description = "Permet d'ajouter ou supprimer des utilisateurs Twitch dans la liste des utilisateurs ignorés", help = "ignore add|remove|list [<username>]", example = "ignore add BouloBOT", power = 100)
    public void ignore(User user, TextChannel textChannel, SlashCommandEvent slashCommandEvent) {
        ChatSpyManager chatSpyManager = bot.getTwitchListener().getChatSpyManager();
        if (slashCommandEvent.getSubcommandName().equalsIgnoreCase("list")) {
            EmbedCrafter embedCrafter = new EmbedCrafter();
            embedCrafter.setTitle("Ignore", bot.getConfigurationManager().getStringValue("websiteUrl"))
                    .setAuthor(user.getName(), bot.getConfigurationManager().getStringValue("websiteUrl"), user.getAvatarUrl() + "?size=256")
                    .setColor(15158332);
            StringBuilder stringBuilder = new StringBuilder("Voici la liste des utilisateurs ignorés: ");
            chatSpyManager.getIgnoredUsers().forEach(s -> stringBuilder.append(s).append(" - "));
            embedCrafter.setDescription(stringBuilder.toString());
            slashCommandEvent.replyEmbeds(embedCrafter.build()).queue();
        } else {
            String username = slashCommandEvent.getOption("nom-de-lutilisateur").getAsString();
            if (username.equalsIgnoreCase("")) {
                slashCommandEvent.reply("Vous devez spécifier un nom d'utilisateur").setEphemeral(true).queue();
            } else {
                if (slashCommandEvent.getSubcommandName().equalsIgnoreCase("add")) {
                    UserList resultList = bot.getTwitchClient().getHelix().getUsers(bot.getConfigurationManager().getStringValue("oauth2Token"), null, Collections.singletonList(username)).execute();
                    if (resultList.getUsers().isEmpty()) {
                        slashCommandEvent.reply("Aucun utilisateur avec le nom \"" + username + "\" trouvé").setEphemeral(true).queue();
                    } else {
                        AtomicReference<com.github.twitch4j.helix.domain.User> twitchUser = new AtomicReference<>();
                        resultList.getUsers().stream().findFirst().ifPresent(twitchUser::set);
                        if (chatSpyManager.getIgnoredUsers().contains(twitchUser.get().getDisplayName())) {
                            slashCommandEvent.reply("Cet utilisateur est déjà ignoré").setEphemeral(true).queue();
                        } else {
                            chatSpyManager.editIgnoredUsers(twitchUser.get().getDisplayName(), true);
                            slashCommandEvent.reply(username + " est désormais ignoré").queue();
                        }
                    }
                } else if (slashCommandEvent.getSubcommandName().equalsIgnoreCase("remove")) {
                    UserList resultList = bot.getTwitchClient().getHelix().getUsers(bot.getConfigurationManager().getStringValue("oauth2Token"), null, Collections.singletonList(username)).execute();
                    if (resultList.getUsers().isEmpty()) {
                        slashCommandEvent.reply("Aucun utilisateur avec le nom \"" + username + "\" trouvé").setEphemeral(true).queue();
                    } else {
                        AtomicReference<com.github.twitch4j.helix.domain.User> twitchUser = new AtomicReference<>();
                        resultList.getUsers().stream().findFirst().ifPresent(twitchUser::set);
                        if (chatSpyManager.getIgnoredUsers().contains(twitchUser.get().getDisplayName())) {
                            chatSpyManager.editIgnoredUsers(twitchUser.get().getDisplayName(), false);
                            slashCommandEvent.reply(username + " n'est désormais plus ignoré").queue();
                        } else {
                            slashCommandEvent.reply("Cet utilisateur n'est pas ignoré").setEphemeral(true).queue();
                        }
                    }
                }
            }
        }
    }
}