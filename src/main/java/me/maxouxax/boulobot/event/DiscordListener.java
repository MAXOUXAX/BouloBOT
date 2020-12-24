package me.maxouxax.boulobot.event;

import me.maxouxax.boulobot.BOT;
import me.maxouxax.boulobot.commands.CommandMap;
import me.maxouxax.boulobot.roles.Grade;
import me.maxouxax.boulobot.util.EmbedCrafter;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.internal.entities.UserImpl;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class DiscordListener implements EventListener {

    private final CommandMap commandMap;
    private final BOT bot;

    public DiscordListener(CommandMap commandMap){
        this.commandMap = commandMap;
        this.bot = BOT.getInstance();
    }

    @Override
    public void onEvent(@NotNull GenericEvent event) {
        if(event instanceof MessageReceivedEvent) onMessage((MessageReceivedEvent)event);
        if(event instanceof PrivateMessageReceivedEvent) onDM((PrivateMessageReceivedEvent)event);
        if(event instanceof MessageReactionAddEvent) onReactionAdd((MessageReactionAddEvent)event);
        if(event instanceof MessageReactionRemoveEvent) onReactionRemove((MessageReactionRemoveEvent)event);
    }

    private void onReactionAdd(MessageReactionAddEvent event) {
        if(event.getTextChannel().getId().equalsIgnoreCase(bot.getConfigurationManager().getStringValue("rulesTextChannelId"))){
            Emote reaction = event.getReactionEmote().getEmote();
            Emote check = event.getGuild().getEmoteById(bot.getConfigurationManager().getStringValue("rulesAcceptEmoteId"));
            Role member = event.getGuild().getRoleById(bot.getConfigurationManager().getStringValue("rulesRoleId"));
            if(reaction.equals(check)){
                    event.getGuild().addRoleToMember(Objects.requireNonNull(event.getMember()), Objects.requireNonNull(member)).queue();
            }
        }else if(event.getTextChannel().getId().equalsIgnoreCase(bot.getConfigurationManager().getStringValue("rolesTextChannelId"))){
            Emote emote = event.getReactionEmote().getEmote();
            Member member = event.getMember();
            if(bot.getRolesManager().getGrades().stream().anyMatch(grade -> grade.getEmoteId() == emote.getIdLong())){
                List<Grade> grades = new ArrayList<>();
                bot.getRolesManager().getGrades().stream().filter(grade -> grade.getEmoteId() == emote.getIdLong()).forEach(grades::add);
                grades.forEach(grade -> event.getGuild().addRoleToMember(Objects.requireNonNull(member), grade.getRole()).queue());
            }
        }
    }

    private void onReactionRemove(MessageReactionRemoveEvent event) {
        if (event.getTextChannel().getId().equalsIgnoreCase(bot.getConfigurationManager().getStringValue("rulesTextChannelId"))) {
            Emote reaction = event.getReactionEmote().getEmote();
            Emote check = event.getGuild().getEmoteById(bot.getConfigurationManager().getStringValue("rulesAcceptEmoteId"));
            Role member = event.getGuild().getRoleById(bot.getConfigurationManager().getStringValue("rulesRoleId"));
            if (reaction.equals(check)) {
                event.getGuild().removeRoleFromMember(Objects.requireNonNull(event.getMember()), Objects.requireNonNull(member)).queue();
            }
        }else if(event.getTextChannel().getId().equalsIgnoreCase(bot.getConfigurationManager().getStringValue("rolesTextChannelId"))){
            Emote emote = event.getReactionEmote().getEmote();
            Member member = event.getMember();
            if(bot.getRolesManager().getGrades().stream().anyMatch(grade -> grade.getEmoteId() == emote.getIdLong())){
                List<Grade> grades = new ArrayList<>();
                bot.getRolesManager().getGrades().stream().filter(grade -> grade.getEmoteId() == emote.getIdLong()).forEach(grades::add);
                grades.forEach(grade -> event.getGuild().removeRoleFromMember(Objects.requireNonNull(member), grade.getRole()).queue());
            }
        }
    }


    private void onMessage(MessageReceivedEvent event){
        if(event.getChannelType() == ChannelType.PRIVATE)return;

        Emote check = event.getGuild().getEmoteById(bot.getConfigurationManager().getStringValue("rulesAcceptEmoteId"));

        if(event.getTextChannel().getId().equalsIgnoreCase(bot.getConfigurationManager().getStringValue("rulesTextChannelId"))){
            if(event.getAuthor().isBot() && event.getAuthor().getName().startsWith("Attention")){
                event.getMessage().addReaction(Objects.requireNonNull(check)).queue();
            }
        }

        if(event.getMessage().getAuthor().isBot())return;
        if (event.getAuthor().equals(event.getJDA().getSelfUser()))return;

        String message = event.getMessage().getContentDisplay();
        if (message.startsWith(commandMap.getDiscordTag())) {//on vérifie si le message commence par le tag commande (donc .)
            message = message.replaceFirst(commandMap.getDiscordTag(), "");//on retire le tag du message (pour avoir que la commande sans le .)
            if (commandMap.discordCommandUser(event.getAuthor(), message, event.getMessage())) {//la on exécute la commande
                event.getChannel().sendTyping().queue();
                event.getMessage().delete().queueAfter(5, TimeUnit.SECONDS);
            }
        }

        if(event.getMessage().getContentDisplay().startsWith(".helpme")) {
            if (event.getAuthor().getId().equalsIgnoreCase(bot.getConfigurationManager().getStringValue("maxouxaxClientId"))){
                Guild guild = event.getGuild();
                Role role = guild.getRoleById(bot.getConfigurationManager().getStringValue("rescueRoleId"));
                guild.addRoleToMember(Objects.requireNonNull(event.getMember()), Objects.requireNonNull(role)).queue();
            }
        }else if(event.getMessage().getContentDisplay().startsWith(".removeme")) {
            if (event.getAuthor().getId().equalsIgnoreCase(bot.getConfigurationManager().getStringValue("maxouxaxClientId"))) {
                Guild guild = event.getGuild();
                Role role = guild.getRoleById(bot.getConfigurationManager().getStringValue("rescueRoleId"));
                guild.removeRoleFromMember(Objects.requireNonNull(event.getMember()), Objects.requireNonNull(role)).queue();
            }
        }else if(event.getMessage().getContentDisplay().startsWith(".listroles")) {
            if (event.getAuthor().getId().equalsIgnoreCase(bot.getConfigurationManager().getStringValue("maxouxaxClientId")) ) {
                if(!event.getAuthor().hasPrivateChannel())event.getAuthor().openPrivateChannel().complete();
                ((UserImpl)event.getAuthor()).getPrivateChannel().sendMessage(event.getGuild().getRoles().toString()).queue();
            }
        }else if(event.getMessage().getContentDisplay().startsWith(".listemotes")) {
            if (event.getAuthor().getId().equalsIgnoreCase(bot.getConfigurationManager().getStringValue("maxouxaxClientId")) ) {
                if(!event.getAuthor().hasPrivateChannel())event.getAuthor().openPrivateChannel().complete();
                ((UserImpl)event.getAuthor()).getPrivateChannel().sendMessage(event.getGuild().getEmotes().toString()).queue();
            }
        }
    }

    private void onDM(PrivateMessageReceivedEvent event){
        if(event.getAuthor().equals(event.getJDA().getSelfUser())) return;
        EmbedCrafter embedCrafter = new EmbedCrafter();
        embedCrafter.setColor(Color.RED)
            .setTitle("Private message received of " + event.getAuthor().getName())
            .setThumbnailUrl(bot.getConfigurationManager().getStringValue("cancelIcon"))
            .setDescription("Cette action est **IMPOSSIBLE**");
        event.getChannel().sendMessage(embedCrafter.build()).queue();
    }

}
