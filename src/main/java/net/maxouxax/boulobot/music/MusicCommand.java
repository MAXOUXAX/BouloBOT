package net.maxouxax.boulobot.music;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.maxouxax.boulobot.BOT;
import net.maxouxax.boulobot.commands.Command;
import net.maxouxax.boulobot.commands.CommandMap;
import net.maxouxax.boulobot.util.EmbedCrafter;

import java.time.Duration;

public class MusicCommand {

    private final MusicManager manager = new MusicManager();
    private final BOT bot;
    private final CommandMap commandMap;

    public MusicCommand(CommandMap commandMap) {
        this.bot = BOT.getInstance();
        this.commandMap = commandMap;
    }

    @Command(name="play",type= Command.ExecutorType.USER,description = "Permet de jouer de la musique en indiquant un lien YouTube ou SoundCloud (autres formats acceptés)", example = "play https://www.youtube.com/watch?v=GS3GYQQUS3o", help = "play <lien>")
    private void play(Guild guild, TextChannel textChannel, User user, String command, String[] args){
        if(args.length == 0){
            textChannel.sendMessage(commandMap.getHelpEmbed("play")).queue();
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

    @Command(name="skip",type= Command.ExecutorType.USER,description = "Permet de passer la musique actuellement en lecture", help = "skip", example = "skip")
    private void skip(Guild guild, TextChannel textChannel){
        if(!guild.getAudioManager().isConnected() && !guild.getAudioManager().isAttemptingToConnect()){
            textChannel.sendMessage("Aucune piste n'est en cours de lecture").queue();
            return;
        }

        manager.getPlayer(guild).skipTrack();
        textChannel.sendMessage("La piste actuelle a bien été passée.").queue();
    }

    @Command(name="clear",type= Command.ExecutorType.USER,description = "Permet de vider la liste d'attente", example = "clear", help = "clear")
    private void clear(TextChannel textChannel){
        MusicPlayer player = manager.getPlayer(textChannel.getGuild());

        if(player.getListener().getTracks().isEmpty()){
            textChannel.sendMessage("Aucune piste n'est dans la liste d'attente").queue();
            return;
        }

        player.getListener().getTracks().clear();
        textChannel.sendMessage("La liste d'attente a été vidée.").queue();
    }

    @Command(name = "queue",type = Command.ExecutorType.USER,description = "Permet de visualier les musiques en file d'attente", help = "queue", example = "queue")
    private void queue(TextChannel textChannel, User user){
        MusicPlayer player = manager.getPlayer(textChannel.getGuild());
        if(player.getListener().getTracks().isEmpty()){
            textChannel.sendMessage("Aucune piste n'est dans la liste d'attente").queue();
        }else {
            StringBuilder sb = new StringBuilder();
            EmbedCrafter embedCrafter = new EmbedCrafter();

            embedCrafter.setColor(15105570)
                .setTitle("Musique")
                .setAuthor(user.getName(), bot.getConfigurationManager().getStringValue("websiteUrl"), user.getAvatarUrl()+"?size=256");

            int position = 1;

            if(player.getListener().getTracks().size() > 5) {
                for (AudioTrack at : player.getListener().getTracks()) {
                    if(position > 5){
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
            }else{
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
            embedCrafter.setDescription("**File d'attente:** \n\n" + sb.toString());
            textChannel.sendMessage(embedCrafter.build()).queue();
        }
    }

    @Command(name = "goto",type = Command.ExecutorType.USER,description = "Permet, en indiquant un nombre de secondes, se rendre au moment donné de la musique (ex: Si je fais .goto 10, je vais me rendre à la 10ème seconde de la musique)", example = ".goto 200", help = ".goto <temps en secondes>")
    private void gototime(TextChannel textChannel, String command, String[] args){
        if(args.length == 0) {
            textChannel.sendMessage(commandMap.getHelpEmbed("goto")).queue();
        }else {
            MusicPlayer player = manager.getPlayer(textChannel.getGuild());
            long time = Long.parseLong(command.replaceFirst("goto ", "")) * 1000;
            player.getAudioPlayer().getPlayingTrack().setPosition(time);
        }
    }

    @Command(name = "track",type = Command.ExecutorType.USER,description = "Permet d'obtenir les informations sur la musique en cours de lecture",help = "track", example = "track")
    private void track(TextChannel textChannel, User user){
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
            .setAuthor(user.getName(), bot.getConfigurationManager().getStringValue("websiteUrl"), user.getAvatarUrl()+"?size=256")
            .setDescription("Titre: **"+track.title+"**\nAuteur: "+track.author+"\nDurée: "+position+" / "+duration+"\nURL: "+track.uri);
        textChannel.sendMessage(embedCrafter.build()).queue();
    }

    @Command(name = "pause", description = "Permet d'arrêter ou de jouer la musique (un play/pause quoi)", type = Command.ExecutorType.USER, example = "pause", help = "pause")
    private void pause(TextChannel textChannel, User user){
        MusicPlayer player = manager.getPlayer(textChannel.getGuild());
        player.getAudioPlayer().setPaused(!player.getAudioPlayer().isPaused());
        textChannel.sendMessage("La piste en cours a correctement été "+(player.getAudioPlayer().isPaused() ? "mise en pause" : "résumée")).queue();
    }

    @Command(name = "volume",description = "Permet de modifier le volume de la musique", type = Command.ExecutorType.USER, help = "volume <volume>", example = "volume 50")
    private void volume(TextChannel textChannel, User user, Guild guild, String[] args) {
        if (args.length == 0) {
            textChannel.sendMessage(commandMap.getHelpEmbed("volume")).queue();
        } else {
            int volume = Integer.parseInt(args[0]);
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
