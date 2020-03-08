package net.maxouxax.boulobot.commands.register.discord;

import com.mrpowergamerbr.temmiewebhook.DiscordEmbed;
import com.mrpowergamerbr.temmiewebhook.DiscordMessage;
import com.mrpowergamerbr.temmiewebhook.TemmieWebhook;
import com.mrpowergamerbr.temmiewebhook.embed.FooterEmbed;
import com.mrpowergamerbr.temmiewebhook.embed.ImageEmbed;
import com.mrpowergamerbr.temmiewebhook.embed.ThumbnailEmbed;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.maxouxax.boulobot.BOT;
import net.maxouxax.boulobot.commands.Command;
import net.maxouxax.boulobot.commands.Command.ExecutorType;
import net.maxouxax.boulobot.commands.CommandMap;
import net.maxouxax.boulobot.util.Reference;

import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class CommandDefault {

    private final BOT botDiscord;
    private final CommandMap commandMap;

    public CommandDefault(BOT botDiscord, CommandMap commandMap){
        this.botDiscord = botDiscord;
        this.commandMap = commandMap;
    }

    @Command(name="stop",type=ExecutorType.CONSOLE)
    private void stop(){
        botDiscord.setRunning(false);
    }

    @Command(name="power",power=150, description = "Permet de définir le power d'un utilisateur", example = ".power 150 @Maxx_#2233", help = ".power <power> <@user>")
    private void power(User user, MessageChannel channel, Message message, String[] args){
        EmbedBuilder helperEmbed = commandMap.getHelpEmbed("power");
        if(args.length == 0 || message.getMentionedUsers().size() == 0){
            channel.sendMessage(helperEmbed.build()).queue();
        }
        int power = 0;
        try{
            power = Integer.parseInt(args[0]);
        }catch(NumberFormatException nfe){
            channel.sendMessage(helperEmbed.build()).queue();
        }

        User target = message.getMentionedUsers().get(0);
        commandMap.addUserPower(target, power);
        channel.sendMessage("Le power de "+target.getAsMention()+" est maintenant de "+power).queue();
    }

    @Command(name="game",power=100,description = "Permet de modifier le jeu du BOT.", help = ".game <jeu>", example = ".game planter des tomates")
    private void game(TextChannel textChannel, JDA jda, String[] args){
        EmbedBuilder helperEmbed = commandMap.getHelpEmbed("game");
        if(args.length == 0){
            textChannel.sendMessage(helperEmbed.build()).queue();
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

        EmbedBuilder em = new EmbedBuilder().setColor(Color.GREEN);
        em.setDescription(":spy:   **Informations sur " + member.getUser().getName() + ":**")
                .addField("Nom", name, true)
                .addField("Tag", tag, true)
                .addField("ID", id, true)
                .addField("Statut", status, true)
                .addField("Joue à", game, true)
                .addField("Rôles", roles, true)
                .addField("A rejoint le serveur le", guildJoinDate, true)
                .addField("A rejoint discord le", discordJoinDate, true)
                .addField("URL de l'avatar", avatar, true);
        em.setFooter(Reference.EmbedFooter.getString(), Reference.EmbedIcon.getString());
        if (!avatar.equals("Pas d'avatar")) {
            em.setThumbnail(avatar);
        }

        textChannel.sendMessage(
                em.build()
        ).queue();
    }

    @Command(name = "ping", description = "Permet de récupérer le ping du bot :)", type = Command.ExecutorType.USER, example = ".ping", help = ".ping")
    private void ping(TextChannel textChannel, User user, Guild guild){
        long ping = guild.getJDA().getGatewayPing();
        EmbedBuilder builder = new EmbedBuilder();
        if(ping > 300) {
            builder.setColor(Color.RED);
            builder.setDescription("Mauvais ping");
        }else{
            builder.setColor(Color.GREEN);
            builder.setDescription("Bon ping");
        }
        builder.setTitle("Ping command requested by "+user.getName());
        builder.setThumbnail(user.getAvatarUrl()+"?size=256");
        builder.addField(new MessageEmbed.Field("Ping", ping+"ms", true));
        builder.setFooter(Reference.EmbedFooter.getString(), Reference.EmbedIcon.getString());
        textChannel.sendMessage(builder.build()).queue();
    }

    @Command(name = "sendrules", description = "Permet d'envoyer les règles dans le salon destiné.", type = ExecutorType.CONSOLE)
    private void sendRules(){
        TemmieWebhook temmieWebhook = new TemmieWebhook(Reference.RulesWebhookURL.getString());
        DiscordEmbed discordEmbed = DiscordEmbed.builder()
                .image(ImageEmbed.builder()
                        .url(Reference.DiscordBanner.getString())
                        .width(1000)
                        .build())
                .color(2895667)
                .build();

        DiscordEmbed discordEmbed1 = DiscordEmbed.builder()
                .title("Bienvenue !")
                .color(3447003)
                .description("Afin d'obtenir la meilleure expérience utilisateur possible, veuillez lire attentivement les règles qui suivent.")
                .build();

        DiscordEmbed discordEmbed2 = DiscordEmbed.builder()
                .title("Règles")
                .color(15105570)
                .thumbnail(ThumbnailEmbed.builder().url(Reference.RulesEmbedThumbnail.getString()).height(256).build())
                .description(":eight_pointed_black_star:️ 1 - Pas d'insulte (normal).\n:eight_pointed_black_star:️ 2 - Pas de MAJ Abusive ou de SPAM/FLOOD.\n:eight_pointed_black_star:️ 3 - Pas de propos raciste, sexiste, homophobe...\n:eight_pointed_black_star:️ 4 - Aucune image à caractère raciste, sexuelle ou dans un but de faire peur n'est toléré sur le Discord (profil ou en message).\n:eight_pointed_black_star:️ 5 - Pas d'appellation de jeux discord à caractère sexuel, violent, raciste...\n:eight_pointed_black_star:️ 6 - Pas de pseudos incorrects ou remplis d'émoticônes ou de caractères spéciaux empêchant de vous mentionner.")
                .build();

        DiscordEmbed discordEmbed3 = DiscordEmbed.builder()
                .title("Fonctionnalités")
                .thumbnail(ThumbnailEmbed.builder().url(Reference.RulesEmbedThumbnailF.getString()).height(256).build())
                .color(3066993)
                .description("Avant de pouvoir accèder à la totalité des fonctionnalités du Discord, vous devrez valider nos règles. Une fois celle-ci validées, vous aurez accès à un second channel, vous demandant une confirmation. Une fois que vous avez confirmer que vous acceptiez nos règles, notre équipe de modérateur peut vous sanctionner à tout moment si vous les enfreinez.\n\nSi vous souhaitez partager des clips, cela se passe dans le salon <#529321314574532628>\n\nPour discuter de toute chose, le channel <#529320863737315345> est fait pour ça ! (en plus des croquettes sont offertes !)")
                .build();

        DiscordMessage dm = DiscordMessage.builder()
                .embeds(Arrays.asList(discordEmbed, discordEmbed1, discordEmbed2, discordEmbed3))
                .build();

        temmieWebhook.sendMessage(dm);

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                TemmieWebhook temmieWebhook1 = new TemmieWebhook(Reference.AttentionWebhookURL.getString());
                DiscordEmbed discordEmbed4 = DiscordEmbed.builder()
                        .title("Attention !")
                        .color(15158332)
                        .thumbnail(ThumbnailEmbed.builder()
                                .url(Reference.AttentionWebhookIconURL.getString())
                                .height(256)
                                .build())
                        .description("Avant de pouvoir accèder au Discord, nous souhaitons nous assurer que vous acceptiez nos règles.\n\nÊtes-vous sûr d'accepter notre règlement ?\n\nSi vous ne respectez pas une règle, vous recevrez un avertissement.\n\nAu bout de 3 avertissements, vous serez banni définitivement du serveur.\n\n**DE PLUS**, si vous commetez une sanction très grave, l'équipe de modération se réserve le droit de vous bannir directement, sans avertissement.")
                        .footer(FooterEmbed.builder()
                                .icon_url(Reference.AttentionWebhookFooterIconURL.getString()+"?size=256")
                                .text("Ajoutez une réaction afin de rejoindre le serveur")
                                .build())
                        .build();

                DiscordMessage dm1 = DiscordMessage.builder()
                        .embed(discordEmbed4)
                        .build();

                temmieWebhook1.sendMessage(dm1);
            }
        }, 1000*2);

    }
}