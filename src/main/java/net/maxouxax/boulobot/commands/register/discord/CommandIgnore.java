package net.maxouxax.boulobot.commands.register.discord;

import com.github.twitch4j.helix.domain.UserList;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.maxouxax.boulobot.BOT;
import net.maxouxax.boulobot.commands.Command;
import net.maxouxax.boulobot.commands.CommandMap;
import net.maxouxax.boulobot.util.ChatSpyManager;
import net.maxouxax.boulobot.util.TextFormatter;

import java.util.Collections;
import java.util.concurrent.atomic.AtomicReference;

public class CommandIgnore {

    private final BOT bot;
    private final CommandMap commandMap;

    public CommandIgnore(CommandMap commandMap){
        this.bot = BOT.getInstance();
        this.commandMap = commandMap;
    }

    @Command(name = "ignore", description = "Permet d'ajouter ou supprimer des utilisateurs Twitch dans la liste des utilisateurs ignorés", help = "ignore add|remove|list [<username>]", example = "ignore add BouloBOT", power = 100, type = Command.ExecutorType.USER)
    public void ignore(User user, TextChannel textChannel, String[] args) {
        ChatSpyManager chatSpyManager = bot.getTwitchListener().getChatSpyManager();
        if (args.length == 0) {
            textChannel.sendMessage(commandMap.getHelpEmbed("ignore").build()).queue();
        } else if (args.length == 1) {
            if (args[0].equalsIgnoreCase("list")) {
                EmbedBuilder embedBuilder = new EmbedBuilder();
                embedBuilder.setTitle("Ignore", bot.getConfigurationManager().getStringValue("websiteUrl"));
                embedBuilder.setAuthor(user.getName(), bot.getConfigurationManager().getStringValue("websiteUrl"), user.getAvatarUrl() + "?size=256");
                StringBuilder stringBuilder = new StringBuilder("Voici la liste des utilisateurs ignorés: ");
                chatSpyManager.getIgnoredUsers().forEach(s -> stringBuilder.append(s).append(" - "));
                embedBuilder.setDescription(stringBuilder.toString());
                embedBuilder.setColor(15158332);
                embedBuilder.setFooter(TextFormatter.asDate(TextFormatter.asDate(bot.getConfigurationManager().getStringValue("embedFooter"))), bot.getConfigurationManager().getStringValue("embedIconUrl"));
                textChannel.sendMessage(embedBuilder.build()).queue();
            } else {
                textChannel.sendMessage(commandMap.getHelpEmbed("ignore").build()).queue();
            }
        }else if(args.length == 2){
            if(args[0].equalsIgnoreCase("add")){
                UserList resultList = bot.getTwitchClient().getHelix().getUsers(bot.getConfigurationManager().getStringValue("oauth2Token"), null, Collections.singletonList(args[1])).execute();
                if(resultList.getUsers().isEmpty()){
                    textChannel.sendMessage("Aucun utilisateur avec le nom \""+args[1]+"\" trouvé").queue();
                }else{
                    AtomicReference<com.github.twitch4j.helix.domain.User> twitchUser = new AtomicReference<>();
                    resultList.getUsers().stream().findFirst().ifPresent(twitchUser::set);
                    if(chatSpyManager.getIgnoredUsers().contains(twitchUser.get().getDisplayName())){
                        textChannel.sendMessage("Cet utilisateur est déjà ignoré").queue();
                    }else{
                        chatSpyManager.getIgnoredUsers().add(twitchUser.get().getDisplayName());
                        chatSpyManager.saveIgnoredUsers();
                        textChannel.sendMessage("Cet utilisateur est désormais ignoré").queue();
                    }
                }
            }else if(args[0].equalsIgnoreCase("remove")){
                UserList resultList = bot.getTwitchClient().getHelix().getUsers(bot.getConfigurationManager().getStringValue("oauth2Token"), null, Collections.singletonList(args[1])).execute();
                if(resultList.getUsers().isEmpty()){
                    textChannel.sendMessage("Aucun utilisateur avec le nom \""+args[1]+"\" trouvé").queue();
                }else{
                    AtomicReference<com.github.twitch4j.helix.domain.User> twitchUser = new AtomicReference<>();
                    resultList.getUsers().stream().findFirst().ifPresent(twitchUser::set);
                    if(chatSpyManager.getIgnoredUsers().contains(twitchUser.get().getDisplayName())){
                        chatSpyManager.getIgnoredUsers().remove(twitchUser.get().getDisplayName());
                        chatSpyManager.saveIgnoredUsers();
                        textChannel.sendMessage("Cet utilisateur n'est désormais plus ignoré").queue();
                    }else{
                       textChannel.sendMessage("Cet utilisateur n'est pas ignoré").queue();
                    }
                }
            }
        }else{
            textChannel.sendMessage(commandMap.getHelpEmbed("ignore").build()).queue();
        }
    }

}