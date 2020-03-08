package net.maxouxax.boulobot.commands.register.discord;

import com.mrpowergamerbr.temmiewebhook.DiscordEmbed;
import com.mrpowergamerbr.temmiewebhook.DiscordMessage;
import com.mrpowergamerbr.temmiewebhook.TemmieWebhook;
import com.mrpowergamerbr.temmiewebhook.embed.FooterEmbed;
import com.mrpowergamerbr.temmiewebhook.embed.ImageEmbed;
import com.mrpowergamerbr.temmiewebhook.embed.ThumbnailEmbed;
import me.legrange.haveibeenpwned.Breach;
import me.legrange.haveibeenpwned.HaveIBeenPwndApi;
import me.legrange.haveibeenpwned.HaveIBeenPwndException;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.maxouxax.boulobot.BOT;
import net.maxouxax.boulobot.commands.Command;
import net.maxouxax.boulobot.commands.Command.ExecutorType;
import net.maxouxax.boulobot.commands.CommandMap;
import net.maxouxax.boulobot.util.Reference;

import java.awt.*;
import java.text.SimpleDateFormat;
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

    @Command(name="embed",type = ExecutorType.ALL,power = 100,help = ".embed <titre>-²<description>-²<image (url)>",example = ".embed Ceci est une annonce-²Juste pour vous dire que les bananes c'est assez bon mais que la raclette reste au dessus.-²https://lien-de-l-image.fr/image32.png")
    public void embed(User user, TextChannel textChannel, String[] args) {
        try {
            EmbedBuilder em = commandMap.getHelpEmbed("embed");
            if (args.length == 0) {
                textChannel.sendMessage(em.build()).queue();
            } else {
                StringBuilder str = new StringBuilder();

                for (String arg : args) {
                    str.append(arg).append(" ");
                }

                if (!str.toString().contains("-²")) {
                    textChannel.sendMessage(em.build()).queue();
                    return;
                }

                String[] argsReal = str.toString().split("-²");

                String title = argsReal[0];

                String description = "Aucune description n'a été fournie !";
                if (argsReal.length >= 2) {
                    description = argsReal[1];
                }

                String imageURL = null;
                if (argsReal.length == 3) {
                    imageURL = argsReal[2];
                }

                EmbedBuilder embedBuilder = new EmbedBuilder();
                embedBuilder.setTitle(title, "https://twitch.lyorine.com");
                embedBuilder.setFooter(Reference.EmbedFooter.asDate(), Reference.EmbedIcon.getString());
                embedBuilder.setColor(15844367);
                embedBuilder.setDescription(description);
                if (imageURL != null) {
                    embedBuilder.setImage(imageURL);
                }
                textChannel.sendMessage(embedBuilder.build()).queue();
            }
        }catch (Exception e){
            botDiscord.getErrorHandler().handleException(e);
        }
    }

    @Command(name="pwned",type = ExecutorType.ALL, description = "Permet de récupérer les brèches trouvées via votre adresse e-mail", example = ".pwned example@test.com", help = ".pwned <adresse email>")
    private void pwned(TextChannel textChannel, User user, String[] args){
        EmbedBuilder helperEmbed = commandMap.getHelpEmbed("pwned");
        if(args.length == 0){
            textChannel.sendMessage(helperEmbed.build()).queue();
        }else if(args.length == 1){
            String email = args[0];
            textChannel.sendMessage("Fetching data...").queue();
            textChannel.sendTyping().queue();

            try {
                HaveIBeenPwndApi hibp = new HaveIBeenPwndApi("PwnedDiscordBotCommand");
                List<Breach> breaches = hibp.getAllBreachesForAccount(email);

                EmbedBuilder reply = new EmbedBuilder();
                reply.setTitle("';--have i been pwned?", "https://haveibeenpwned.com/")
                        .setFooter(Reference.EmbedFooter.asDate(), Reference.EmbedIcon.getString());
                if(breaches.size() <= 0){
                    reply.setDescription("Houra !\n» Aucune brèche n'a été trouvée !")
                         .setColor(3066993);
                }else{
                    reply.setDescription("Aie !\n» "+breaches.size()+(breaches.size() == 1 ? " brèche a été trouvée !" : " brèches ont été trouvées !"))
                         .setColor(15158332);
                    breaches.forEach(breach -> {
                        reply.addField(new MessageEmbed.Field(
                                breach.getName(),
                                breach.getDomain()+"\n\n» "
                                        +breach.getDescription().replaceAll("\\<[^>]*>"
                                        ,"")+"\n\n» "
                                        +new SimpleDateFormat("EEE, d MMM yyyy")
                                        .format(breach.getBreachDate()), true));
                    });
                }

                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        textChannel.sendMessage(reply.build()).queue();
                    }
                }, 3000);
            } catch (HaveIBeenPwndException | NoSuchMethodError e) {
                textChannel.sendMessage("Failure!\nAPI returned error code: " + e.getMessage()).queue();
                e.printStackTrace();
            }
        }else{
            textChannel.sendMessage("Veuillez spécifier votre email.\n» `.pwned example@test.com`").queue();
        }
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

        String NAME = member.getEffectiveName();
        String TAG = member.getUser().getName() + "#" + member.getUser().getDiscriminator();
        String GUILD_JOIN_DATE = member.getTimeJoined().format(DateTimeFormatter.RFC_1123_DATE_TIME);
        String DISCORD_JOINED_DATE = member.getUser().getTimeCreated().format(DateTimeFormatter.RFC_1123_DATE_TIME);
        String ID = member.getUser().getId();
        String STATUS = member.getOnlineStatus().getKey();
        String ROLES = "";
        String GAME;
        String AVATAR = member.getUser().getAvatarUrl();

        try {
            GAME = member.getActivities().get(0).getName();
        } catch (Exception e) {
            GAME = "-/-";
        }

        for ( Role r : member.getRoles() ) {
            ROLES += r.getName() + ", ";
        }
        if (ROLES.length() > 0)
            ROLES = ROLES.substring(0, ROLES.length()-2);
        else
            ROLES = "Aucun rôle.";

        if (AVATAR == null) {
            AVATAR = "Pas d'avatar";
        }

        EmbedBuilder em = new EmbedBuilder().setColor(Color.GREEN);
        em.setDescription(":spy:   **Informations sur " + member.getUser().getName() + ":**")
                .addField("Nom", NAME, true)
                .addField("Tag", TAG, true)
                .addField("ID", ID, true)
                .addField("Statut", STATUS, true)
                .addField("Joue à", GAME, true)
                .addField("Rôles", ROLES, true)
                .addField("Rejoint le", GUILD_JOIN_DATE, true)
                .addField("Rejoint discord le", DISCORD_JOINED_DATE, true)
                .addField("URL de l'avatar", AVATAR, true);
        em.setFooter(Reference.EmbedFooter.getString(), Reference.EmbedIcon.getString());
        if (AVATAR != "Pas d'avatar") {
            em.setThumbnail(AVATAR);
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
            builder.setDescription("Plus ou moins bon ping");
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
        }, 1000*5);

    }
}