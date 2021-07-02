package me.maxouxax.boulobot.music;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import me.maxouxax.boulobot.BOT;
import me.maxouxax.boulobot.commands.Command;
import me.maxouxax.boulobot.commands.CommandMap;
import me.maxouxax.boulobot.commands.slashannotations.Option;
import me.maxouxax.boulobot.util.EmbedCrafter;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;

import java.time.Duration;

public class MusicCommand {

    private final MusicManager manager = new MusicManager();
    private final BOT bot;
    private final CommandMap commandMap;

    public MusicCommand(CommandMap commandMap) {
        this.bot = BOT.getInstance();
        this.commandMap = commandMap;
    }

    @Option(name = "lien-de-la-musique", description = "Lien de la musique que vous souhaitez jouer", type = OptionType.STRING, isRequired = true)
    @Command(name = "play", description = "Permet de jouer de la musique en indiquant un lien YouTube ou SoundCloud (autres formats acceptés)", example = "play https://www.youtube.com/watch?v=GS3GYQQUS3o", help = "play <lien>")
    private void play(Guild guild, TextChannel textChannel, User user, String command, SlashCommandEvent slashCommandEvent) {
        if (!guild.getAudioManager().isConnected() && !guild.getAudioManager().isAttemptingToConnect()) {
            VoiceChannel voiceChannel = guild.getMember(user).getVoiceState().getChannel();
            if (voiceChannel == null) {
                slashCommandEvent.reply("Vous devez être connecté à un salon vocal.").setEphemeral(true).queue();
                return;
            }
            guild.getAudioManager().openAudioConnection(voiceChannel);
        }
        manager.getPlayer(guild).getAudioPlayer().setPaused(false);
        manager.loadTrack(slashCommandEvent, slashCommandEvent.getOption("lien-de-la-musique").getAsString(), user);
        if (manager.getPlayer(guild).getAudioPlayer().isPaused()) {
            manager.getPlayer(guild).getAudioPlayer().setVolume(20);
        }
    }

    @Command(name = "skip", description = "Permet de passer la musique actuellement en lecture", help = "skip", example = "skip")
    private void skip(Guild guild, TextChannel textChannel, SlashCommandEvent slashCommandEvent) {
        if (!guild.getAudioManager().isConnected() && !guild.getAudioManager().isAttemptingToConnect()) {
            slashCommandEvent.reply("Aucune piste n'est en cours de lecture").queue();
            return;
        }

        manager.getPlayer(guild).skipTrack();
        slashCommandEvent.reply("La piste actuelle a bien été passée.").queue();
    }

    @Command(name = "clear", description = "Permet de vider la liste d'attente", example = "clear", help = "clear")
    private void clear(TextChannel textChannel, SlashCommandEvent slashCommandEvent) {
        MusicPlayer player = manager.getPlayer(textChannel.getGuild());

        if (player.getListener().getTracks().isEmpty()) {
            slashCommandEvent.reply("Aucune piste n'est dans la liste d'attente").queue();
            return;
        }

        player.getListener().getTracks().clear();
        slashCommandEvent.reply("La liste d'attente a été vidée.").queue();
    }

    @Command(name = "queue", description = "Permet de visualier les musiques en file d'attente", help = "queue", example = "queue")
    private void queue(TextChannel textChannel, User user, SlashCommandEvent slashCommandEvent) {
        MusicPlayer player = manager.getPlayer(textChannel.getGuild());
        if (player.getListener().getTracks().isEmpty()) {
            slashCommandEvent.reply("Aucune piste n'est dans la liste d'attente").queue();
        } else {
            StringBuilder sb = new StringBuilder();
            EmbedCrafter embedCrafter = new EmbedCrafter();

            embedCrafter.setColor(15105570)
                    .setTitle("Musique")
                    .setAuthor(user.getName(), bot.getConfigurationManager().getStringValue("websiteUrl"), user.getAvatarUrl() + "?size=256");

            int position = 1;

            if (player.getListener().getTracks().size() > 5) {
                for (AudioTrack at : player.getListener().getTracks()) {
                    if (position > 5) {
                        break;
                    }
                    Duration duration = Duration.ofMillis(at.getDuration());
                    String durationfinal = duration.toString();
                    durationfinal = durationfinal.replace("PT", "");
                    durationfinal = durationfinal.replace("M", " minute(s) ");
                    durationfinal = durationfinal.replace("S", " seconde(s) ");
                    sb.append("---\nPosition: ").append(position).append("\n • Titre: **").append(at.getInfo().title).append("**\n • Chaîne: ").append(at.getInfo().author).append("\n • Durée: ").append(durationfinal).append("\n • URL: ").append(at.getInfo().uri).append("\n");
                    position++;
                }
                sb.append("\n\net ").append(player.getListener().getTracks().size() - 5).append(" autre(s) !");
            } else {
                for (AudioTrack at : player.getListener().getTracks()) {
                    Long totalMillis = at.getDuration();
                    Duration duration = Duration.ofMillis(totalMillis);
                    String durationfinal = duration.toString();
                    durationfinal = durationfinal.replace("PT", "");
                    durationfinal = durationfinal.replace("M", " minute(s) ");
                    durationfinal = durationfinal.replace("S", " seconde(s) ");
                    sb.append("---\nPosition: ").append(position).append("\n • Titre: **").append(at.getInfo().title).append("**\n • Chaîne: ").append(at.getInfo().author).append("\n • Durée: ").append(durationfinal).append("\n • URL: ").append(at.getInfo().uri);
                    position++;
                }
            }
            embedCrafter.setDescription("**File d'attente:** \n\n" + sb);
            slashCommandEvent.replyEmbeds(embedCrafter.build()).queue();
        }
    }

    @Option(name = "timecode", description = "Timecode en secondes auquel vous voulez aller (90 pour à 1m30)", type = OptionType.INTEGER, isRequired = true)
    @Command(name = "goto", description = "Permet de déplacer la musique au moment donné", example = ".goto 200", help = ".goto <temps en secondes>")
    private void gototime(TextChannel textChannel, String command, SlashCommandEvent slashCommandEvent) {
        MusicPlayer player = manager.getPlayer(textChannel.getGuild());
        long time = slashCommandEvent.getOption("timecode").getAsLong() * 1000;
        player.getAudioPlayer().getPlayingTrack().setPosition(time);
        slashCommandEvent.reply("La musique est désormais à " + slashCommandEvent.getOption("timecode").getAsLong() + " seconde(s)").queue();
    }

    @Command(name = "track", description = "Permet d'obtenir les informations sur la musique en cours de lecture", help = "track", example = "track")
    private void track(TextChannel textChannel, User user, SlashCommandEvent slashCommandEvent) {
        MusicPlayer player = manager.getPlayer(textChannel.getGuild());
        EmbedCrafter embedCrafter = new EmbedCrafter();
        AudioTrackInfo track = player.getAudioPlayer().getPlayingTrack().getInfo();

        String duration = Duration.ofMillis(track.length).toString();
        duration = duration.replace("PT", "");
        duration = duration.replace("M", "m");
        duration = duration.replace("S", "s");

        String position = Duration.ofMillis(player.getAudioPlayer().getPlayingTrack().getPosition()).toString();
        position = position.replace("PT", "");
        position = position.replace("M", "m");
        position = position.replace("S", "s");

        embedCrafter.setColor(3066993)
                .setTitle("Musique")
                .setAuthor(user.getName(), bot.getConfigurationManager().getStringValue("websiteUrl"), user.getAvatarUrl() + "?size=256")
                .setDescription("Titre: **" + track.title + "**\nAuteur: " + track.author + "\nDurée: " + position + " / " + duration + "\nURL: " + track.uri);
        slashCommandEvent.replyEmbeds(embedCrafter.build()).queue();
    }

    @Command(name = "pause", description = "Permet d'arrêter ou de jouer la musique (un play/pause quoi)", example = "pause", help = "pause")
    private void pause(TextChannel textChannel, User user, SlashCommandEvent slashCommandEvent) {
        MusicPlayer player = manager.getPlayer(textChannel.getGuild());
        player.getAudioPlayer().setPaused(!player.getAudioPlayer().isPaused());
        slashCommandEvent.reply("La piste en cours a correctement été " + (player.getAudioPlayer().isPaused() ? "mise en pause" : "résumée")).queue();
    }

    @Option(name = "volume", description = "Volume en pourcentage (0 à 100)", type = OptionType.INTEGER, isRequired = true)
    @Command(name = "volume", description = "Permet de modifier le volume de la musique", help = "volume <volume>", example = "volume 50")
    private void volume(TextChannel textChannel, User user, Guild guild, SlashCommandEvent slashCommandEvent) {
        int volume = Math.toIntExact(slashCommandEvent.getOption("volume").getAsLong());
        MusicPlayer player = manager.getPlayer(textChannel.getGuild());
        boolean tooLargeVolume = false;
        boolean tooLowVolume = false;
        if (volume > 100) {
            volume = 100;
            tooLargeVolume = true;
        }
        if (volume <= 0) {
            volume = 1;
            tooLowVolume = true;
        }
        try {
            player.getAudioPlayer().setVolume(volume);
        } catch (Exception e) {
            slashCommandEvent.reply("Erreur...\n" + e.getMessage()).queue();
        }
        slashCommandEvent.reply("Le volume a été défini à " + volume + (tooLargeVolume ? " (» Volume trop haut | Diminution de " + slashCommandEvent.getOption("volume").getAsLong() + " à 100)" : "") + (tooLowVolume ? " (» Volume trop bas | Augmentation de " + slashCommandEvent.getOption("Volume").getAsLong() + " à 1)" : "")).queue();
    }
}
