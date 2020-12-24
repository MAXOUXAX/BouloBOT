package me.maxouxax.boulobot.music;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import me.maxouxax.boulobot.BOT;
import me.maxouxax.boulobot.util.EmbedCrafter;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

public class MusicManager {

    private final AudioPlayerManager manager = new DefaultAudioPlayerManager();
    private final Map<String, MusicPlayer> players = new HashMap<>();

    private final BOT bot;

    public MusicManager(){
        this.bot = BOT.getInstance();
        AudioSourceManagers.registerRemoteSources(manager);
        AudioSourceManagers.registerLocalSource(manager);
    }

    public synchronized MusicPlayer getPlayer(Guild guild){
        if(!players.containsKey(guild.getId())) players.put(guild.getId(), new MusicPlayer(manager.createPlayer(), guild));
        return players.get(guild.getId());
    }

    public void loadTrack(final TextChannel channel, final String source, final User user){
        MusicPlayer player = getPlayer(channel.getGuild());

        channel.getGuild().getAudioManager().setSendingHandler(player.getAudioHandler());

        manager.loadItemOrdered(player, source, new AudioLoadResultHandler(){

            @Override
            public void trackLoaded(AudioTrack track) {
                player.playTrack(track);
                AudioTrackInfo info = track.getInfo();
                EmbedCrafter builder = new EmbedCrafter();

                Duration duration = Duration.ofMillis(info.length);
                String durationfinal = duration.toString();
                durationfinal = durationfinal.replace("PT", "");
                durationfinal = durationfinal.replace("M", " minute(s) ");
                durationfinal = durationfinal.replace("S", " seconde(s) ");

                builder.setColor(3066993)
                    .setTitle("Musique")
                    .setAuthor(user.getName(), bot.getConfigurationManager().getStringValue("websiteUrl"), user.getAvatarUrl()+"?size=256")
                    .setDescription("Titre: **"+info.title+"**\nAuteur: "+info.author+"\nDurée: "+durationfinal+"\nURL: "+info.uri+"");

                channel.sendMessage(builder.build()).queue();
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                StringBuilder sbuilder = new StringBuilder();
                sbuilder.append("Ajout de la playlist **").append(playlist.getName()).append("**\n");

                EmbedCrafter embedCrafter = new EmbedCrafter();


                embedCrafter.setColor(3066993)
                    .setTitle("Musique")
                    .setAuthor(user.getName(), bot.getConfigurationManager().getStringValue("websiteUrl"), user.getAvatarUrl()+"?size=256");

                for(int i = 0; i < playlist.getTracks().size(); i++) {
                    AudioTrack track = playlist.getTracks().get(i);
                    player.playTrack(track);
                }

                for(int i = 0; i < playlist.getTracks().size() && i < 5; i++){
                    AudioTrackInfo info = playlist.getTracks().get(i).getInfo();
                    Duration duration = Duration.ofMillis(info.length);
                    String durationfinal = duration.toString();
                    durationfinal = durationfinal.replace("PT", "");
                    durationfinal = durationfinal.replace("M", " minute(s) ");
                    durationfinal = durationfinal.replace("S", " seconde(s) ");
                    sbuilder.append("\n---\n").append("Titre: **").append(info.title).append("**\nAuteur: ").append(info.author).append("\nDurée: ").append(durationfinal).append("\nURL: ").append(info.uri);
                }
                if(playlist.getTracks().size() > 5){
                    final int b = (playlist.getTracks().size()-5);
                    sbuilder.append("\n\n*et ").append(b).append(" autre(s) !*");
                }
                embedCrafter.setDescription(sbuilder.toString());
                channel.sendMessage(embedCrafter.build()).queue();

            }


            @Override
            public void noMatches() {
                EmbedCrafter embedCrafter = new EmbedCrafter();
                embedCrafter.setColor(15158332)
                    .setTitle("Musique")
                    .setAuthor(user.getName(), bot.getConfigurationManager().getStringValue("websiteUrl"), user.getAvatarUrl()+"?size=256")
                    .setDescription("La piste " + source + " n'a pas été trouvée.");
                channel.sendMessage(embedCrafter.build()).queue();
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                EmbedCrafter embedCrafter = new EmbedCrafter();
                embedCrafter.setColor(15158332)
                    .setTitle("Musique")
                    .setAuthor(user.getName(), bot.getConfigurationManager().getStringValue("websiteUrl"), user.getAvatarUrl()+"?size=256")
                    .setDescription("Impossible de jouer la piste (raison:" + exception.getMessage()+")");
                channel.sendMessage(embedCrafter.build()).queue();
            }
        });
    }
}

