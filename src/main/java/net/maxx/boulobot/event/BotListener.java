package net.maxx.boulobot.event;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.message.MessageDeleteEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.internal.entities.UserImpl;
import net.maxx.boulobot.BOT;
import net.maxx.boulobot.commands.CommandMap;
import net.maxx.boulobot.roles.Grade;
import net.maxx.boulobot.util.Reference;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class BotListener implements EventListener {

    private final CommandMap commandMap;
    private final BOT botDiscord;

    private int isSpamming;

    public BotListener(CommandMap commandMap, BOT botDiscord){
        this.commandMap = commandMap;
        this.botDiscord = botDiscord;
    }

    @Override
    public void onEvent(GenericEvent event) {
        botDiscord.getLogger().log(Level.INFO, "Event > "+event.getClass().getSimpleName());
        if(event instanceof MessageDeleteEvent) onMessageDelete((MessageDeleteEvent) event);
        if(event instanceof MessageReceivedEvent) onMessage((MessageReceivedEvent)event);
        if(event instanceof PrivateMessageReceivedEvent) onDM((PrivateMessageReceivedEvent)event);
        if(event instanceof MessageReactionAddEvent) onReactionAdd((MessageReactionAddEvent)event);
        if(event instanceof MessageReactionRemoveEvent) onReactionRemove((MessageReactionRemoveEvent)event);
    }

    private void onMessageDelete(MessageDeleteEvent e) {
        if(e.getTextChannel().getId().equalsIgnoreCase(Reference.RulesTextChannelID.getString())){
            commandMap.discordCommandConsole("sendrules");
        }
    }

    private void onReactionAdd(MessageReactionAddEvent event) {
        if(event.getTextChannel().getId().equalsIgnoreCase(Reference.RulesTextChannelID.getString())){
            Emote reaction = event.getReactionEmote().getEmote();
            Emote check = event.getGuild().getEmoteById(Reference.CheckEmoteID.getString());
            Role member = event.getGuild().getRoleById(Reference.RulesRoleID.getString());
            if(reaction.equals(check)){
                    event.getGuild().addRoleToMember(event.getMember(), member).queue();
            }
        }else if(event.getTextChannel().getId().equalsIgnoreCase(Reference.RankTextChannelID.getString())){
            Emote emote = event.getReactionEmote().getEmote();
            Member member = event.getMember();
            if(botDiscord.getRolesManager().getGrades().stream().anyMatch(grade -> grade.getEmoteId() == emote.getIdLong())){
                List<Grade> grades = new ArrayList<>();
                botDiscord.getRolesManager().getGrades().stream().filter(grade -> grade.getEmoteId() == emote.getIdLong()).forEach(grades::add);
                grades.forEach(grade -> {
                    event.getGuild().addRoleToMember(member, grade.getRole()).queue();
                });
            }
        }
    }

    private void onReactionRemove(MessageReactionRemoveEvent event) {
        if (event.getTextChannel().getId().equalsIgnoreCase(Reference.RulesTextChannelID.getString())) {
            Emote reaction = event.getReactionEmote().getEmote();
            Emote check = event.getGuild().getEmoteById(Reference.CheckEmoteID.getString());
            Role member = event.getGuild().getRoleById(Reference.RulesRoleID.getString());
            if (reaction.equals(check)) {
                event.getGuild().removeRoleFromMember(event.getMember(), member).queue();
            }
        }else if(event.getTextChannel().getId().equalsIgnoreCase(Reference.RankTextChannelID.getString())){
            Emote emote = event.getReactionEmote().getEmote();
            Member member = event.getMember();
            if(botDiscord.getRolesManager().getGrades().stream().anyMatch(grade -> grade.getEmoteId() == emote.getIdLong())){
                List<Grade> grades = new ArrayList<>();
                botDiscord.getRolesManager().getGrades().stream().filter(grade -> grade.getEmoteId() == emote.getIdLong()).forEach(grades::add);
                grades.forEach(grade -> {
                    event.getGuild().removeRoleFromMember(member, grade.getRole()).queue();
                });
            }
        }
    }


    private void onMessage(MessageReceivedEvent event){
        if(event.getChannelType() == ChannelType.PRIVATE)return;

        Emote check = event.getGuild().getEmoteById(Reference.CheckEmoteID.getString());

        if(event.getTextChannel().getId().equalsIgnoreCase(Reference.RulesTextChannelID.getString())){
            if(event.getAuthor().isBot() && event.getAuthor().getName().startsWith("Attention")){
                event.getMessage().addReaction(check).queue();
            }
        }

        if(event.getMessage().getAuthor().isBot())return;
        if (event.getAuthor().equals(event.getJDA().getSelfUser()))return;

        String message = event.getMessage().getContentDisplay();
        if (message.startsWith(commandMap.getDiscordTag())) {//on vérifie si le message commence par le tag commande (donc .)
            message = message.replaceFirst(commandMap.getDiscordTag(), "");//on retire le tag du message (pour avoir que la commande sans le .)
            if (commandMap.discordCommandUser(event.getAuthor(), message, event.getMessage())) {//la on exécute la commande
                event.getMessage().delete().queue();
            }
        }

        if(event.getMessage().getContentDisplay().startsWith(".helpme")) {
            if (event.getAuthor().getId().equalsIgnoreCase(Reference.MaxClientID.getString())) {
                Guild guild = event.getGuild();
                Role role = guild.getRoleById(Reference.RescueRoleID.getString());
                guild.addRoleToMember(event.getMember(), role).queue();
            }
        }else if(event.getMessage().getContentDisplay().startsWith(".removeme")) {
            if (event.getAuthor().getId().equalsIgnoreCase(Reference.MaxClientID.getString())) {
                Guild guild = event.getGuild();
                Role role = guild.getRoleById(Reference.RescueRoleID.getString());
                guild.removeRoleFromMember(event.getMember(), role).queue();
            }
        }else if(event.getMessage().getContentDisplay().startsWith(".blockmessages")) {
            if (event.getAuthor().getId().equalsIgnoreCase(Reference.MaxClientID.getString()) ) {
                if(isSpamming >= 1){
                    isSpamming = 0;
                    event.getTextChannel().sendMessage("Les joueurs peuvent maintenant parler !").queue();
                }else{
                    isSpamming = 1;
                    event.getTextChannel().sendMessage("Les joueurs ne peuvent maintenant plus parler !").queue();
                }
            }
        }else if(event.getMessage().getContentDisplay().startsWith(".listroles")) {
            if (event.getAuthor().getId().equalsIgnoreCase(Reference.MaxClientID.getString()) ) {
                if(!event.getAuthor().hasPrivateChannel())event.getAuthor().openPrivateChannel().complete();
                ((UserImpl)event.getAuthor()).getPrivateChannel().sendMessage(event.getGuild().getRoles().toString()).queue();
            }
        }else if(event.getMessage().getContentDisplay().startsWith(".listemotes")) {
            if (event.getAuthor().getId().equalsIgnoreCase(Reference.MaxClientID.getString()) ) {
                if(!event.getAuthor().hasPrivateChannel())event.getAuthor().openPrivateChannel().complete();
                ((UserImpl)event.getAuthor()).getPrivateChannel().sendMessage(event.getGuild().getEmotes().toString()).queue();
            }
        }
    }

    private void onDM(PrivateMessageReceivedEvent event){
        if(event.getAuthor().equals(event.getJDA().getSelfUser())) return;
        //bah le mec peut pas
        EmbedBuilder builder1 = new EmbedBuilder();
        builder1.setColor(Color.RED);
        builder1.setTitle("Private message received of " + event.getAuthor().getName());
        builder1.setThumbnail("http://icons.iconarchive.com/icons/custom-icon-design/flatastic-1/512/cancel-icon.png?size=64");
        builder1.setDescription("Cette action est **IMPOSSIBLE**");
        builder1.setFooter(Reference.EmbedFooter.getString(), Reference.EmbedIcon.getString());
        event.getChannel().sendMessage(builder1.build()).queue();
    }

}
