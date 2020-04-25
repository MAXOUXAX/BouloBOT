package net.maxouxax.boulobot.music;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.maxouxax.boulobot.BOT;
import net.maxouxax.boulobot.commands.Command;
import net.maxouxax.boulobot.commands.CommandMap;
import net.maxouxax.boulobot.util.Reference;

import java.awt.*;
import java.time.Duration;

public class MusicCommand {

    private final MusicManager manager = new MusicManager();
    private final BOT bot;
    private final CommandMap commandMap;

    public MusicCommand(BOT bot, CommandMap commandMap) {
        this.bot = bot;
        this.commandMap = commandMap;
    }

    @Command(name="play",type= Command.ExecutorType.USER,description = "Permet de jouer de la musique en indiquant un lien YouTube ou SoundCloud (autres formats acceptés)", example = ".play https://www.youtube.com/watch?v=GS3GYQQUS3o", help = ".play <lien>")
    private void play(Guild guild, TextChannel textChannel, User user, String command, String[] args){
        EmbedBuilder helperEmbed = commandMap.getHelpEmbed("play");
        if(args.length == 0){
            textChannel.sendMessage(helperEmbed.build()).queue();
        }else {
            if (!guild.getAudioManager().isConnected() && !guild.getAudioManager().isAttemptingToConnect()) {
                VoiceChannel voiceChannel = guild.getMember(user).getVoiceState().getChannel();
                if (voiceChannel == null) {
                    textChannel.sendMessage("Vous devez être connecté à un salon vocal.").queue();
                    return;
                }
                guild.getAudioManager().openAudioConnection(voiceChannel);
            }
            manager.getPlayer(guild).getAudioPlayer().setPaused(false);
            manager.loadTrack(textChannel, command.replaceFirst("play ", ""), user);
            if (manager.getPlayer(guild).getAudioPlayer().isPaused()) {
                manager.getPlayer(guild).getAudioPlayer().setVolume(20);
            }
        }
    }

    @Command(name="skip",type= Command.ExecutorType.USER,description = "Permet de passer la musique actuellement en lecture", help = ".skip", example = ".skip")
    private void skip(Guild guild, TextChannel textChannel){
        if(!guild.getAudioManager().isConnected() && !guild.getAudioManager().isAttemptingToConnect()){
            textChannel.sendMessage("Le player n'as pas de piste en cours.").queue();
            return;
        }

        manager.getPlayer(guild).skipTrack();
        textChannel.sendMessage("La lecture est passée à la piste suivante.").queue();
    }

    @Command(name="clear",type= Command.ExecutorType.USER,description = "Permet de vider la liste d'attente", example = ".clear", help = ".clear")
    private void clear(TextChannel textChannel){
        MusicPlayer player = manager.getPlayer(textChannel.getGuild());

        if(player.getListener().getTracks().isEmpty()){
            textChannel.sendMessage("Il n'y a pas de piste dans la liste d'attente.").queue();
            return;
        }

        player.getListener().getTracks().clear();
        textChannel.sendMessage("La liste d'attente à été vidée.").queue();
    }

    @Command(name = "queue",type = Command.ExecutorType.USER,description = "Permet de visualier les musiques en file d'attente", help = ".queue", example = ".queue")
    private void queue(TextChannel textChannel, User user){
        MusicPlayer player = manager.getPlayer(textChannel.getGuild());
        if(player.getListener().getTracks().isEmpty()){
            textChannel.sendMessage("Aucune musique en attente !").queue();
        }else {
            StringBuilder sb = new StringBuilder();
            EmbedBuilder builder = new EmbedBuilder();

            builder.setColor(Color.YELLOW);
            builder.setTitle("Queue command requested by "+user.getName());
            builder.setThumbnail(user.getAvatarUrl()+"?size=256");
            builder.setFooter(Reference.EmbedFooter.getString(), Reference.EmbedIcon.getString());

            Integer position = 1;

            if(player.getListener().getTracks().size() > 5) {
                for (AudioTrack at : player.getListener().getTracks()) {
                    if(position > 5){
                        break;
                    }
                    Long totalMillis = at.getDuration();
                    Duration duration = Duration.ofMillis(totalMillis);
                    String durationfinal = duration.toString();
                    durationfinal = durationfinal.replace("PT", "");
                    durationfinal = durationfinal.replace("M", " minute(s) ");
                    durationfinal = durationfinal.replace("S", " seconde(s) ");
                    sb.append("-----------------\nPosition: **" + position + "**\n • Titre: **" + at.getInfo().title + "**\n • Chaîne: **" + at.getInfo().author + "**\n • Durée: **" + durationfinal + "**\n • URL: **" + at.getInfo().uri +"**\n");
                    position++;
                }
                sb.append("\n\net "+(player.getListener().getTracks().size()-5)+" autre(s) !");
            }else{
                for (AudioTrack at : player.getListener().getTracks()) {
                    Long totalMillis = at.getDuration();
                    Duration duration = Duration.ofMillis(totalMillis);
                    String durationfinal = duration.toString();
                    durationfinal = durationfinal.replace("PT", "");
                    durationfinal = durationfinal.replace("M", " minute(s) ");
                    durationfinal = durationfinal.replace("S", " seconde(s) ");
                    sb.append("-----------------\nPosition: **" + position + "**\n • Titre: **" + at.getInfo().title + "**\n • Chaîne: **" + at.getInfo().author + "**\n • Durée: **" + durationfinal + "**\n • URL: **" + at.getInfo().uri +"**");
                    position++;
                }
            }
            builder.setDescription("**File d'attente:** \n\n" + sb.toString());
            textChannel.sendMessage(builder.build()).queue();
        }
    }

    @Command(name = "goto",type = Command.ExecutorType.USER,description = "Permet, en indiquant un nombre de secondes, se rendre au moment donné de la musique (ex: Si je fais .goto 10, je vais me rendre à la 10ème seconde de la musique)", example = ".goto 200", help = ".goto <temps en secondes>")
    private void gototime(TextChannel textChannel, String command, String[] args){
        EmbedBuilder helperEmbed = commandMap.getHelpEmbed("goto");
        if(args.length == 0) {
            textChannel.sendMessage(helperEmbed.build()).queue();
        }else {
            MusicPlayer player = manager.getPlayer(textChannel.getGuild());
            Long time = Long.parseLong(command.replaceFirst("goto ", ""));
            time = time * 1000;
            player.getAudioPlayer().getPlayingTrack().setPosition(time);
        }
    }

    @Command(name = "track",type = Command.ExecutorType.USER,description = "Permet d'obtenir les informations sur la musique en cours de lecture",help = ".track", example = ".track")
    private void track(TextChannel textChannel, User user){
        MusicPlayer player = manager.getPlayer(textChannel.getGuild());
        EmbedBuilder builder = new EmbedBuilder();
        AudioTrackInfo track = player.getAudioPlayer().getPlayingTrack().getInfo();

        Long totalMillis = track.length;
        Duration duration = Duration.ofMillis(totalMillis);
        String durationfinal = duration.toString();
        durationfinal = durationfinal.replace("PT", "");
        durationfinal = durationfinal.replace("M", "m");
        durationfinal = durationfinal.replace("S", "s");

        Long timeelapsed = player.getAudioPlayer().getPlayingTrack().getPosition();
        Duration duration1 = Duration.ofMillis(timeelapsed);
        String durationfinal1 = duration1.toString();
        durationfinal1 = durationfinal1.replace("PT", "");
        durationfinal1 = durationfinal1.replace("M", "m");
        durationfinal1 = durationfinal1.replace("S", "s");

        builder.setColor(Color.GREEN);
        builder.setTitle("Track command requested by "+user.getName());
        builder.setThumbnail(user.getAvatarUrl()+"?size=256");
        builder.setDescription("Titre: **"+track.title+"**\nAuteur: **"+track.author+"**\nDurée: **"+durationfinal1+"** / **"+durationfinal+"**\nURL: "+track.uri);
        builder.setFooter(Reference.EmbedFooter.getString(), Reference.EmbedIcon.getString());
        textChannel.sendMessage(builder.build()).queue();
    }

    @Command(name = "pause", description = "Permet d'arrêter ou de jouer la musique (un play/pause quoi)", type = Command.ExecutorType.USER, example = ".pause", help = ".pause")
    private void pause(TextChannel textChannel, User user){
        MusicPlayer player = manager.getPlayer(textChannel.getGuild());
        //si la musique est en pause
        //sinon (donc si la musique est pas en pause donc qu'elle JOUE
        //on pause
        player.getAudioPlayer().setPaused(!player.getAudioPlayer().isPaused());//on retire la pause (donc on fait play)
    }

    @Command(name = "volume",description = "Permet de modifier le volume de la musique", type = Command.ExecutorType.USER, help = ".volume <volume>", example = ".volume 50")
    private void volume(TextChannel textChannel, User user, Guild guild, String[] args) {
        EmbedBuilder helperEmbed = commandMap.getHelpEmbed("volume");
        if (args.length == 0) {
            textChannel.sendMessage(helperEmbed.build()).queue();
        } else {
            Integer volume = Integer.parseInt(args[0]);
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
                textChannel.sendMessage("Erreur...\n" + e.getMessage()).queue();
            }
            textChannel.sendMessage("Le volume a été défini à " + volume + (tooLargeVolume ? " (» Volume trop haut | Diminution de " + args[0] + " à 100)" : "") + (tooLowVolume ? " (» Volume trop bas | Augmentation de " + args[0] + " à 1)" : "")).queue();
        }
    }
}
