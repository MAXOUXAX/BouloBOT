package net.maxouxax.boulobot.music;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.maxouxax.boulobot.BOT;

import java.awt.*;
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
                EmbedBuilder builder = new EmbedBuilder();

                Long totalMillis = info.length;
                Duration duration = Duration.ofMillis(totalMillis);
                String durationfinal = duration.toString();
                durationfinal = durationfinal.replace("PT", "");
                durationfinal = durationfinal.replace("M", " minute(s) ");
                durationfinal = durationfinal.replace("S", " seconde(s) ");

                builder.setColor(Color.GREEN);
                builder.setTitle("Play command requested by "+user.getName());
                builder.setThumbnail(user.getAvatarUrl()+"?size=256");
                builder.setDescription("Titre: **"+info.title+"**\nAuteur: **"+info.author+"**\nDurée: **"+durationfinal+"**\nURL: **"+info.uri+"**");
                builder.setFooter(bot.getConfigurationManager().getStringValue("embedFooter"), bot.getConfigurationManager().getStringValue("embedIconUrl"));

                channel.sendMessage(builder.build()).queue();
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                StringBuilder sbuilder = new StringBuilder();
                sbuilder.append("Ajout de la playlist **").append(playlist.getName()).append("**\n");

                EmbedBuilder builder = new EmbedBuilder();


                builder.setColor(Color.GREEN);
                builder.setTitle("Play(list) command requested by "+user.getName());
                builder.setThumbnail(user.getAvatarUrl()+"?size=256");
                builder.setFooter(bot.getConfigurationManager().getStringValue("embedFooter"), bot.getConfigurationManager().getStringValue("embedIconUrl"));

                for(int i = 0; i < playlist.getTracks().size(); i++) {
                    AudioTrack track = playlist.getTracks().get(i);
                    player.playTrack(track);
                }

                for(int i = 0; i < playlist.getTracks().size() && i < 5; i++){
                    AudioTrackInfo info = playlist.getTracks().get(i).getInfo();
                    Long totalMillis = info.length;
                    Duration duration = Duration.ofMillis(totalMillis);
                    String durationfinal = duration.toString();
                    durationfinal = durationfinal.replace("PT", "");
                    durationfinal = durationfinal.replace("M", " minute(s) ");
                    durationfinal = durationfinal.replace("S", " seconde(s) ");
                    sbuilder.append("\n-----------------\n").append("Titre: **" + info.title + "**\nAuteur: **" + info.author + "**\nDurée: **" + durationfinal + "**\nURL: **" + info.uri+"**");
                }
                if(playlist.getTracks().size() > 5){
                    final Integer b = (playlist.getTracks().size()-5);
                    sbuilder.append("\n\n*et "+b+" autre(s) !*");
                }
                builder.setDescription(sbuilder.toString());
                channel.sendMessage(builder.build()).queue();

            }


            @Override
            public void noMatches() {
                EmbedBuilder builder = new EmbedBuilder();
                builder.setColor(Color.RED);
                builder.setTitle("Play command requested by "+user.getName());
                builder.setDescription("La piste " + source + " n'a pas été trouvé.");
                builder.setThumbnail(user.getAvatarUrl()+"?size=256");
                builder.setFooter(bot.getConfigurationManager().getStringValue("embedFooter"), bot.getConfigurationManager().getStringValue("embedIconUrl"));
                channel.sendMessage(builder.build()).queue();
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                EmbedBuilder builder = new EmbedBuilder();
                builder.setColor(Color.RED);
                builder.setTitle("Play command requested by "+user.getName());
                builder.setDescription("Impossible de jouer la piste (raison:" + exception.getMessage()+")");
                builder.setThumbnail(user.getAvatarUrl()+"?size=256");
                builder.setFooter(bot.getConfigurationManager().getStringValue("embedFooter"), bot.getConfigurationManager().getStringValue("embedIconUrl"));
                channel.sendMessage(builder.build()).queue();
            }
        });
    }
}

