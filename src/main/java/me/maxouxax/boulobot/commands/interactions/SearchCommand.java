package me.maxouxax.boulobot.commands.interactions;

import me.maxouxax.boulobot.commands.CommandMap;
import me.maxouxax.boulobot.commands.slashannotations.InteractionListener;
import me.maxouxax.boulobot.music.MusicCommand;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;

public class SearchCommand {

    private final CommandMap commandMap;

    public SearchCommand(CommandMap commandMap) {
        this.commandMap = commandMap;
    }

    @InteractionListener(id = "music-search-choose")
    private void choose(ButtonClickEvent buttonClickEvent){
        int index = Integer.parseInt(buttonClickEvent.getButton().getLabel());
        MessageEmbed messageEmbed = buttonClickEvent.getMessage().getEmbeds().get(0);
        String url = messageEmbed.getFields().get(index-1).getValue();
        MusicCommand.getManager().loadTrack(buttonClickEvent, url, buttonClickEvent.getUser());
    }

    @InteractionListener(id = "music-search-cancel")
    private void cancel(ButtonClickEvent buttonClickEvent){
        buttonClickEvent.editMessage("Recherche annulée !").queue();
    }

}
