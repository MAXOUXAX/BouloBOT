package me.maxouxax.boulobot.music;

import me.maxouxax.boulobot.BOT;
import me.maxouxax.boulobot.util.EmbedCrafter;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.interactions.InteractionHook;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class SkipDemand {

    private final VoiceChannel voiceChannel;
    private InteractionHook interactionHook;
    private Member requester;
    private List<Member> listenersWishingToSkip;
    private final String musique;
    private final DecimalFormat format = new DecimalFormat("##.##");
    private boolean completed = false;

    public SkipDemand(VoiceChannel voiceChannel, InteractionHook interactionHook, Member requester) {
        this.voiceChannel = voiceChannel;
        this.interactionHook = interactionHook;
        this.requester = requester;
        this.listenersWishingToSkip = new ArrayList<>();
        this.musique = MusicCommand.getManager().getPlayer(voiceChannel.getGuild()).getAudioPlayer().getPlayingTrack().getInfo().title;
    }

    public void updateDemand(){
        int numberOfListenersWishingToSkip = listenersWishingToSkip.size();
        double percent = (double) numberOfListenersWishingToSkip / getNumberOfMembersInChannel() * 100;
        User user = requester.getUser();
        if (percent >= 50) {
            completed = true;
            StringBuilder members = new StringBuilder();
            listenersWishingToSkip.forEach(member -> members.append(" • ").append(member.getUser().getName()));
            EmbedCrafter embedCrafter = new EmbedCrafter()
                    .setTitle("✔ • Demande de skip")
                    .setAuthor(user.getName(), BOT.getInstance().getConfigurationManager().getStringValue("websiteUrl"), user.getAvatarUrl() + "?size=256")
                    .setColor(3066993)
                    .setDescription("La demande pour skip la musique `" + musique + "` a été acceptée !\n\n" +
                            members+" ont accepté la demande !")
                    .addField("Avancée de la demande", numberOfListenersWishingToSkip + "/" + getNumberOfMembersInChannel() + " - " + format.format(percent) + "%", true);
            interactionHook.editOriginalEmbeds(embedCrafter.build()).queue();
            MusicCommand.getManager().getPlayer(requester.getGuild()).skipTrack();
        }else{
            EmbedCrafter embedCrafter = new EmbedCrafter()
                    .setTitle("⏳ • Demande de skip")
                    .setColor(15844367)
                    .setAuthor(user.getName(), BOT.getInstance().getConfigurationManager().getStringValue("websiteUrl"), user.getAvatarUrl() + "?size=256")
                    .setDescription("Une demande pour skip la musique `" + musique + "` a été lancée !\n" +
                            "Si vous aussi vous souhaitez skip la musique, effectuez /skip\n\n" +
                            "Il faut qu'au moins 50% des personnes présentes dans le salon souhaitent passer la musique pour que la demande soit acceptée")
                    .addField("Avancée de la demande", "1/" + getNumberOfMembersInChannel() + " - " + format.format(percent) + "%", true);
            interactionHook.editOriginalEmbeds(embedCrafter.build()).queue();
        }
    }

    public void cancelDemand(){
        if(completed)return;
        interactionHook.deleteOriginal().queue();
        int numberOfListenersWishingToSkip = listenersWishingToSkip.size();
        double percent = (double) numberOfListenersWishingToSkip / getNumberOfMembersInChannel() * 100;
        User user = requester.getUser();
        EmbedCrafter embedCrafter = new EmbedCrafter()
                .setTitle("❌ • Demande de skip")
                .setColor(15158332)
                .setAuthor(user.getName(), BOT.getInstance().getConfigurationManager().getStringValue("websiteUrl"), user.getAvatarUrl() + "?size=256")
                .setDescription("La demande pour skip la musique `" + musique + "` n'a pas aboutie !\n")
                .addField("Avancée de la demande", numberOfListenersWishingToSkip + "/" + getNumberOfMembersInChannel() + " - " + format.format(percent) + "%", true);
        interactionHook.getInteraction().getTextChannel().sendMessageEmbeds(embedCrafter.build()).queue();
    }

    public int getNumberOfMembersInChannel(){
        return voiceChannel.getMembers().size()-1;
    }

    public InteractionHook getInteractionHook() {
        return interactionHook;
    }

    public void setInteractionHook(InteractionHook interactionHook) {
        this.interactionHook = interactionHook;
    }

    public Member getRequester() {
        return requester;
    }

    public void setRequester(Member requester) {
        this.requester = requester;
    }

    public List<Member> getListenersWishingToSkip() {
        return listenersWishingToSkip;
    }

    public void setListenersWishingToSkip(List<Member> listenersWishingToSkip) {
        this.listenersWishingToSkip = listenersWishingToSkip;
    }
}
