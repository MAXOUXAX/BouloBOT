package me.maxouxax.boulobot.commands.register.discord;

import me.maxouxax.boulobot.BOT;
import me.maxouxax.boulobot.commands.Command;
import me.maxouxax.boulobot.commands.Command.ExecutorType;
import me.maxouxax.boulobot.commands.CommandMap;
import me.maxouxax.boulobot.util.EmbedCrafter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;

import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

public class CommandDefault {

    private final BOT bot;
    private final CommandMap commandMap;

    public CommandDefault(CommandMap commandMap){
        this.commandMap = commandMap;
        this.bot = BOT.getInstance();
    }

    @Command(name="stop",type=ExecutorType.CONSOLE)
    private void stop(){
        bot.setRunning(false);
    }

    @Command(name="power",power=150, description = "Permet de définir le power d'un utilisateur", example = ".power 150 @MAXOUXAX", help = ".power <power> <@user>")
    private void power(User user, MessageChannel channel, Message message, String[] args){
        if(args.length == 0 || message.getMentionedUsers().size() == 0){
            channel.sendMessage(commandMap.getHelpEmbed("power")).queue();
        }
        int power = 0;
        try{
            power = Integer.parseInt(args[0]);
        }catch(NumberFormatException nfe){
            channel.sendMessage(commandMap.getHelpEmbed("power")).queue();
        }

        User target = message.getMentionedUsers().get(0);
        commandMap.addUserPower(target, power);
        channel.sendMessage("Le power de "+target.getAsMention()+" est maintenant de "+power).queue();
    }

    @Command(name="game",power=100,description = "Permet de modifier le jeu du BOT.", help = ".game <jeu>", example = ".game planter des tomates")
    private void game(TextChannel textChannel, JDA jda, String[] args){
        if(args.length == 0){
            textChannel.sendMessage(commandMap.getHelpEmbed("game")).queue();
        }else {
            StringBuilder builder = new StringBuilder();
            for (String str : args) {
                if (builder.length() > 0) builder.append(" ");
                builder.append(str);
            }

            jda.getPresence().setActivity(Activity.playing(builder.toString()));
        }
    }

    @Command(name="delete",power=50,description = "Permet de nettoyer un nombre x de message du salon", example = ".delete 50", help = ".delete <nombre de message>")
    private void delete(TextChannel textChannel, JDA jda, String[] args){
        if (getInt(args[0]) <= 100) {
            List<Message> msgs;
            MessageHistory history = new MessageHistory(textChannel);
            msgs = history.retrievePast(getInt(args[0])).complete();
            textChannel.deleteMessages(msgs).queue();
            textChannel.sendMessage("Suppression de " + args[0] + " messages terminée !").queue();
        }
    }

    private int getInt(String arg) {
        try {
            return Integer.parseInt(arg);
        } catch (Exception e) {
            return 0;
        }

    }

    @Command(name = "info",description = "Permet d'obtenir des informations sur un membre",type = Command.ExecutorType.USER, help = ".info <@user>", example = ".info @Maxx_#2233")
    private void info(User user, Guild guild, TextChannel textChannel, String[] args, Message message){
        Member member;

        if (args.length > 0) {
            member = guild.getMember(message.getMentionedUsers().get(0));
        } else {
            member = message.getMember();
        }

        String name = member.getEffectiveName();
        String tag = member.getUser().getName() + "#" + member.getUser().getDiscriminator();
        String guildJoinDate = member.getTimeJoined().format(DateTimeFormatter.RFC_1123_DATE_TIME);
        String discordJoinDate = member.getUser().getTimeCreated().format(DateTimeFormatter.RFC_1123_DATE_TIME);
        String id = member.getUser().getId();
        String status = member.getOnlineStatus().getKey();
        String roles = "";
        String game;
        String avatar = member.getUser().getAvatarUrl();

        try {
            game = member.getActivities().get(0).getName();
        } catch (Exception e) {
            game = "-/-";
        }

        for ( Role r : member.getRoles() ) {
            roles += r.getName() + ", ";
        }
        if (roles.length() > 0)
            roles = roles.substring(0, roles.length()-2);
        else
            roles = "Aucun rôle.";

        if (avatar == null) {
            avatar = "Pas d'avatar";
        }

        EmbedCrafter embedCrafter = new EmbedCrafter().setColor(Color.GREEN);
        embedCrafter.setDescription(":spy:   **Informations sur " + member.getUser().getName() + ":**")
                .addField("Nom", name, true)
                .addField("Tag", tag, true)
                .addField("ID", id, true)
                .addField("Statut", status, true)
                .addField("Joue à", game, true)
                .addField("Rôles", roles, true)
                .addField("A rejoint le serveur le", guildJoinDate, true)
                .addField("A rejoint discord le", discordJoinDate, true)
                .addField("URL de l'avatar", avatar, true);
        if (!avatar.equals("Pas d'avatar")) {
            embedCrafter.setThumbnailUrl(avatar);
        }

        textChannel.sendMessage(
                embedCrafter.build()
        ).queue();
    }

    @Command(name = "ping", description = "Permet de récupérer le ping du bot", type = Command.ExecutorType.USER, example = ".ping", help = ".ping")
    private void ping(TextChannel textChannel, User user, Guild guild){
        long ping = guild.getJDA().getGatewayPing();
        EmbedCrafter embedCrafter = new EmbedCrafter()
                .setTitle("DiscordAPI ping", bot.getConfigurationManager().getStringValue("websiteUrl"))
                .setThumbnailUrl(user.getAvatarUrl()+"?size=256")
                .addField(new MessageEmbed.Field("Ping", ping+"ms", true));
        if(ping > 300) {
            embedCrafter.setColor(Color.RED);
            embedCrafter.setDescription("Mauvais ping");
        }else{
            embedCrafter.setColor(Color.GREEN);
            embedCrafter.setDescription("Bon ping");
        }
        textChannel.sendMessage(embedCrafter.build()).queue();
    }

    @Command(name = "sendrules", description = "Permet d'envoyer les règles dans le salon destiné.", type = ExecutorType.CONSOLE)
    private void sendRules(){
        EmbedCrafter embedCrafter = new EmbedCrafter()
                .setImageUrl(bot.getConfigurationManager().getStringValue("rulesBanner")+"?size=1000")
                .setColor(2895667)
                .noFooter();
        EmbedCrafter embedCrafterRules = new EmbedCrafter()
                .setTitle("Règles")
                .setColor(15105570)
                .setThumbnailUrl(bot.getConfigurationManager().getStringValue("rulesEmbedThumbnail"))
                .setDescription(":small_orange_diamond: 1. **Traitez tout le monde avec respect**. " +
                        "Aucun harcèlement, sexisme, racisme ou discours de haine ne sera toléré. " +
                        "**Cette règle s'applique aux pseudonymes ainsi qu'aux statuts personnalisés !**\n" +

                        ":small_orange_diamond: 2. **Pas de spam ni d'autopromotion** (invitations de serveurs" +
                        ", publicités, etc.) sans l'autorisation d'un modérateur du serveur, y compris via les messages " +
                        "privés envoyés aux autres membres.\n" +

                        ":small_orange_diamond: 3. **Pas de contenu violent, obscène ou NSFW**, qu'il s'agisse " +
                        "de texte, d'images ou de liens mettant en scène de la nudité, du sexe, de l'hyperviolence " +
                        "ou un quelconque contenu dérangeant, et ce, **y compris dans les photos de profil**.\n" +

                        ":small_orange_diamond: 4. **Pas de majuscules abusives ou de spam/flood**\n" +

                        ":small_orange_diamond: 5. **Pas de pseudos incorrects ou remplis d'émoticônes ou de " +
                        "caractères spéciaux** empêchant de vous mentionner\n" +

                        ":small_orange_diamond: 6. Si tu remarques quelque chose de contraire aux règles ou qui " +
                        "te met dans un sentiment d'insécurité, informe-en les modérateurs. Nous voulons que ce " +
                        "serveur soit accueillant pour tout le monde !")
                .noFooter();
        EmbedCrafter embedCrafterModeration = new EmbedCrafter()
                .setTitle("Modération")
                .setThumbnailUrl(bot.getConfigurationManager().getStringValue("rulesEmbedThumbnailModeration"))
                .setColor(3066993)
                .setDescription("En rejoignant ce serveur Discord, vous acceptez que notre équipe de modération puisse vous sanctionner à tout moment si vous enfreignez une des règles précédemment citées")
                .noFooter();
        EmbedCrafter embedCrafterWarning = new EmbedCrafter()
                .setTitle("Attention !")
                .setColor(15158332)
                .setThumbnailUrl(bot.getConfigurationManager().getStringValue("rulesAttentionThumbnailUrl"))
                .setDescription("Si vous ne respectez pas une des règles précédemment citées, vous recevrez un avertissement.\n\nAu bout de 3 avertissements, vous serez banni définitivement du serveur par notre équipe de modération.\n\n**DE PLUS**, si vous commettez une sanction très grave, l'équipe de modération se réserve le droit de vous bannir sans avertissement.")
                .forceFooter("Dernière mise à jour des règles");
        TextChannel textChannel = Objects.requireNonNull(bot.getJda()
                .getGuildById(bot.getConfigurationManager().getStringValue("guildId")))
                .getTextChannelById(bot.getConfigurationManager().getStringValue("rulesTextChannelId"));
        if(textChannel == null){
            bot.getErrorHandler().handleException(new Exception("textChannel == null (the textchannel id or the guildid (or both) may not have been set in the config file)"));
        }else {
            textChannel.sendMessage(embedCrafter.build()).queue();
            textChannel.sendMessage(embedCrafterRules.build()).queue();
            textChannel.sendMessage(embedCrafterModeration.build()).queue();
            textChannel.sendMessage(embedCrafterWarning.build()).queue();
        }
    }
}