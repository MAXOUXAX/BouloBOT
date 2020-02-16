package net.maxouxax.boulobot.commands.register.discord;

import com.mrpowergamerbr.temmiewebhook.DiscordEmbed;
import com.mrpowergamerbr.temmiewebhook.DiscordMessage;
import com.mrpowergamerbr.temmiewebhook.TemmieWebhook;
import com.mrpowergamerbr.temmiewebhook.embed.FooterEmbed;
import com.mrpowergamerbr.temmiewebhook.embed.ImageEmbed;
import com.mrpowergamerbr.temmiewebhook.embed.ThumbnailEmbed;
import com.samuelmaddock.strawpollwrapper.DupCheckType;
import com.samuelmaddock.strawpollwrapper.StrawPoll;
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
import org.apache.commons.lang3.ArrayUtils;

import java.awt.*;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.*;

public class CommandDefault {

    private final BOT botDiscord;
    private final CommandMap commandMap;

    public CommandDefault(BOT botDiscord, CommandMap commandMap){
        this.botDiscord = botDiscord;
        this.commandMap = commandMap;
    }

    private static HashMap<String, StrawPoll> strawPollMap = new HashMap<>();

    public static HashMap<String, StrawPoll> getStrawPollMap() {
        return strawPollMap;
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

    /*@Command(name="anniv",type = ExecutorType.ALL)
    private void anniv(Guild guild, TextChannel textChannel, User user) {
        if (user.getId().equalsIgnoreCase(Reference.MaxClientID.getString())) {
            EmbedBuilder embedBuilder = new EmbedBuilder();
            embedBuilder.setTitle("\uD83C\uDF89 Bon anniversaire", "https://twitch.tv/Lyorine")
                    .setFooter(Reference.EmbedFooter.asDate(), Reference.EmbedIcon.getString())
                    .setThumbnail(Reference.BirthdayThumbnail.getString() + "?size=256")
                    .setColor(2600544)
                    .setDescription("Eh ouais !\n\nAujourd'hui, c'est l'anniversaire de notre reine.\nCela fait maintenant 23 ans qu'elle est apparue sur Terre. Dès son plus jeune âge, elle commençait déjà à s'occuper des chats. Elle est née potauphile également.\n\nEnfin bref, cessons cette biographie nulle. Ce message est là, simplement pour te dire que, nous, ta communauté, entière, nous t'aimons. Nous avons rejoint ton royaume, nous tous, car tu fournis un travail incroyable. Tu nous remontes le moral quand on a un coup de mou, tu animes nos soirées, enfin bref, merci quoi. Tu te rends pas forcément compte de tout ce que tu apportes, alors on te le dit, merci.\n • Donc voilà, merci.\n\n**Amoureusement,**\nta communauté.");
            textChannel.sendMessage(embedBuilder.build()).queue();

            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    Message message = textChannel.getMessageById(textChannel.getLatestMessageId()).complete();
                    message.addReaction(guild.getEmoteById("530411080380317696")).queue();
                }
            }, 5000);

            EmbedBuilder embedBuilderLyorine = new EmbedBuilder();
            embedBuilderLyorine.setTitle("Message personnel", "https://message-personnel.maxx.com")
                    .setFooter(Reference.EmbedFooter.asDate(), Reference.EmbedIcon.getString())
                    .setThumbnail(Reference.ConffetiImage.getString() + "?size=256")
                    .setColor(3447003)
                    .setDescription("Coucou !\nDéjà, bon anniversaire ! LUL\nBon, ne lit pas ce message sur ton stream, ça serait un peu con. LUL.\n\n»Je t'écris ce message, simplement pour te remercier de tout ce que tu apportes, ta bonne humeur, etc. Vraiment, ça peut paraître con, mais ça me fait beaucoup de bien. J'ai pas de problèmes particuliers, mais rien que le fait d'être sur ton stream, de parler avec ta communauté, et toi, c'est un pur plaisir. Du coup, voilà, 23 ans, tu es vieille maintenant... lmao\n\n Tu as encore beaucoup de belles choses à accomplir, alors je ne peux que te souhaiter du bien. Vraiment. Pleins de bonnes choses pour tes projets, je suis sûr qu'ils se réaliseront !\n\nAvec beaucoup, mais **beaucoup d'amour**,\nMaxx_.");

            User user1 = botDiscord.getJda().getUserById(Reference.LyorineClientID.getString());
            if(!user1.hasPrivateChannel())user1.openPrivateChannel().queue();
            ((UserImpl)user1).getPrivateChannel().sendMessage(embedBuilderLyorine.build()).queue();

            guild.getTextChannels().forEach(textChannel1 -> {
                textChannel1.sendMessage("**Envoyez plein de love les amis !** ❤️️ ❤️️ ❤️️ ").queue();
            });
        }
    }*/

    /*@Command(name = "senddm", type = ExecutorType.ALL)
    private void sendDM(Guild guild){
        EmbedBuilder embedBuilderLyorine = new EmbedBuilder();
        embedBuilderLyorine.setTitle("Message personnel", "https://message-personnel.maxx.com")
                .setFooter(Reference.EmbedFooter.asDate(), Reference.EmbedIcon.getString())
                .setThumbnail(Reference.ConffetiImage.getString() + "?size=256")
                .setColor(3447003)
                .setDescription("Coucou !\nDéjà, bon anniversaire ! LUL\nBon, ne lit pas ce message sur ton stream, ça serait un peu con. LUL.\n\n»Je t'écris ce message, simplement pour te remercier de tout ce que tu apportes, ta bonne humeur, etc. Vraiment, ça peut paraître con, mais ça me fait beaucoup de bien. J'ai pas de problèmes particuliers, mais rien que le fait d'être sur ton stream, de parler avec ta communauté, et toi, c'est un pur plaisir. Du coup, voilà, 23 ans, tu es vieille maintenant... lmao\n\n Tu as encore beaucoup de belles choses à accomplir, alors je ne peux que te souhaiter du bien. Vraiment. Pleins de bonnes choses pour tes projets, je suis sûr qu'ils se réaliseront !\n\nAvec beaucoup, mais **beaucoup d'amour**,\nMaxx_.");

        User user1 = null;
        Member member = guild.getMemberById(Reference.MaxClientID.getString());
        user1 = member.getUser();
        botDiscord.getLogger().log(Level.INFO, "test");
        if(!user1.hasPrivateChannel())user1.openPrivateChannel().complete();
        botDiscord.getLogger().log(Level.INFO, "test1");
        PrivateChannel privateChannel = ((UserImpl)user1).getPrivateChannel();
        botDiscord.getLogger().log(Level.INFO, "test5");
        privateChannel
                .sendMessage(embedBuilderLyorine.build()).queue();
        botDiscord.getLogger().log(Level.INFO, "test2");

        /*guild.getTextChannels().forEach(textChannel1 -> {
            textChannel1.sendMessage("**Envoyez plein de love les amis !** ❤️️ ❤️️ ❤️️ ").queue();
        });
    }*/

    @Command(name="stop",type=ExecutorType.CONSOLE)
    private void stop(){
        botDiscord.setRunning(false);
    }

    /*@Command(name="vote",power = 100,type = ExecutorType.USER)
    private void vote(TextChannel textChannel, Message message, Guild guild, User user, String[] args){
        if(args.length == 0 || message.getMentionedUsers().size() == 0){
            textChannel.sendMessage("Veuillez vous référer à la page d'aide: `.vote @CHOIX1 @CHOIX2 @CHOIX3 @CHOIX...`").queue();
            return;
        }
        if(message.getMentionedUsers().size() == 1){
            textChannel.sendMessage("Vous devez spécifier plusieurs choix, sinon c'est PAS COOL !").queue();
            return;
        }

        List<String> choices = new ArrayList<>();

        message.getMentionedUsers().forEach(user1 -> choices.add(user1.getName()));

        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle("Nouveau vote !");
        builder.setDescription("Veuillez voter pour la personne que vous souhaitez !");
        builder.setColor(Color.PINK);
        builder.setAuthor("Créateur du vote >> "+user.getName());
        builder.setFooter(Reference.EmbedFooter.getString(), Reference.EmbedIcon.getString());
        builder.setDescription("**Afin de voter, envoyez un MP __au bot__ avec le numéro du candidat pour lequel vous voulez voter :)**");
        BotListener.votes.clear();
        BotListener.intvotes.clear();
        BotListener.hasalreadyvoted.clear();
        BotListener.isVoting = true;

        for (int i = 0; i < message.getMentionedUsers().size(); i++) {
            builder.addField(message.getMentionedUsers().get(i).getName(), "Numéro: "+i, true);
            BotListener.votes.put(i, message.getMentionedUsers().get(i));
            BotListener.intvotes.put(message.getMentionedUsers().get(i), 0);
        }

        textChannel.sendMessage(builder.build()).queue();
    }

    @Command(name = "results",power = 100,description = "Permet d'arrêter le vote et d'obtenir les résultats")
    private void results(User user, Guild guild, TextChannel textChannel, Message message, String[] args){
        if(BotListener.isVoting){
            textChannel.sendMessage("Arrêt du vote...").queue();
            BotListener.isVoting = false;
            textChannel.sendMessage("Vote arrêté, récupération des résultats...").queue();

            EmbedBuilder builder = new EmbedBuilder();
            builder.setTitle("Résultats du vote !");
            builder.setDescription("Voici les résultats du vote !");
            builder.setColor(Color.GREEN);
            builder.setAuthor("Résultats donnés par >> "+user.getName());
            builder.setFooter(Reference.EmbedFooter.getString(), Reference.EmbedIcon.getString());

            for(User user1 : BotListener.intvotes.keySet()){
                builder.addField(user1.getName(), "Nombre de votes: "+BotListener.intvotes.get(user1), true);
            }

            textChannel.sendMessage(builder.build()).queue();
            BotListener.intvotes.clear();
            BotListener.votes.clear();
            BotListener.hasalreadyvoted.clear();

        }else{
            textChannel.sendMessage("Aucun vote en cours.").queue();
        }
    }*/

    @Command(name = "createvote",power = 50,description = "Permet de créer un strawpoll avec autant de choix que vous voulez", help = ".createvote <question> ²² <choix 1> ²² <choix 2> ²² <choix ...>", example = ".createvote Quel est mon prénom ? ²² Je ne sais pas ²² Je crois savoir ²² Lily")
    private void createVote(User user, MessageChannel messageChannel, String[] args){
        EmbedBuilder helperEmbed = commandMap.getHelpEmbed("createvote");
        if(args.length == 0){
            messageChannel.sendMessage(helperEmbed.build()).queue();
        }else {
            StringBuilder str = new StringBuilder();

            for (String arg : args) {
                str.append(arg).append(" ");
            }

            if(!str.toString().contains(" ²² ")){
                messageChannel.sendMessage(helperEmbed.build()).queue();
                return;
            }

            String[] argsReal = str.toString().split(" ²² ");

            String title = argsReal[0];

            String[] options = ArrayUtils.remove(argsReal, 0);

            StrawPoll newStrawpoll = new StrawPoll();
            newStrawpoll.setTitle(title);

            EmbedBuilder embedBuilder = new EmbedBuilder();

            for (String option1 : options) {
                newStrawpoll.addOptions(option1);
            }


            newStrawpoll.setDupCheck(DupCheckType.NORMAL);
            newStrawpoll.create();

            strawPollMap.put(newStrawpoll.getId(), newStrawpoll);
            commandMap.addStrawpoll(newStrawpoll);


            embedBuilder
                    .setTitle("Nouveau vote !", newStrawpoll.getPollURL())
                    .setFooter(Reference.EmbedFooter.asDate(), Reference.EmbedIcon.getString())
                    .setThumbnail(Reference.StrawpollThumbnail.getString()+"?size=256")
                    .setColor(3066993)
            ;


            messageChannel.sendMessage(embedBuilder.build()).queue();
        }
    }

    @Command(name = "retrievepolls", power = 50, description = "Permet de récupérer les résultats des strawpolls", example = ".retrievepolls", help = ".retrievepolls")
    private void retrievePolls(TextChannel textChannel) {
        if (strawPollMap.size() >= 1) {


            EmbedBuilder embedBuilder = new EmbedBuilder();
            embedBuilder
                    .setTitle("Résultats des derniers strawpolls")
                    .setFooter(Reference.EmbedFooter.asDate(), Reference.AttentionWebhookFooterIconURL.getString())
                    .setThumbnail(Reference.StrawpollThumbnail.getString() + "?size=256")
                    .setColor(3447003)
            ;
            textChannel.sendMessage(embedBuilder.build()).queue();
            List<EmbedBuilder> embedBuilders = new ArrayList<>();
            Integer[] colors = new Integer[]{1752220, 1482885, 15844367, 15965202, 15105570, 13849600, 2719929, 10181046, 9323693, 15528177, 12436423};
            strawPollMap.forEach((s, strawPoll) -> {
                strawPoll.update();
                HashMap<String, Integer> results = new HashMap<>();
                for (int i = 0; i < strawPoll.getOptions().size(); i++) {
                    results.put(strawPoll.getOptions().get(i), strawPoll.getVotes().get(i));
                }
                EmbedBuilder em = new EmbedBuilder();
                int random = new Random().nextInt(colors.length);
                em.setTitle(strawPoll.getTitle(), strawPoll.getPollURL());
                results.forEach((s1, integer) -> {
                    em.addField(new MessageEmbed.Field(s1, integer + " vote" + (integer >= 2 ? "s" : ""), true));
                });
                em.setColor(colors[random]);
                embedBuilders.add(em);
            });
            embedBuilders.forEach(embedBuilder1 -> textChannel.sendMessage(embedBuilder1.build()).queue());
        }else{
            EmbedBuilder embedBuilder = new EmbedBuilder();
            embedBuilder
                    .setTitle("Résultats des derniers strawpolls")
                    .setFooter(Reference.EmbedFooter.asDate(), Reference.EmbedIcon.getString())
                    .setThumbnail(Reference.StrawpollThumbnail.getString() + "?size=256")
                    .setColor(15158332)
                    .setDescription("» Aucun StrawPoll n'a été crée !")
            ;
            textChannel.sendMessage(embedBuilder.build()).queue();
        }
    }

    @Command(name = "removepoll",power = 50, description = "Permet de supprimer un poll !",help = ".removepoll <poll link>", example = ".removepoll https://strawpoll.me/5aez41f85sed4f")
    private void removePoll(User user, TextChannel textChannel, String[] args){
        EmbedBuilder helperEmbed = commandMap.getHelpEmbed("removepoll");
        if(args.length == 0){
            textChannel.sendMessage(helperEmbed.build()).queue();
        }else{
            StrawPoll strawPollToRemove = new StrawPoll(args[0]);
            strawPollMap.remove(strawPollToRemove.getId());
            commandMap.removeStrawpoll(strawPollToRemove);
            EmbedBuilder embedBuilder = new EmbedBuilder();
            embedBuilder
                    .setTitle("Suppression de poll")
                    .setFooter(Reference.EmbedFooter.asDate(), Reference.EmbedIcon.getString())
                    .setThumbnail(Reference.StrawpollThumbnail.getString() + "?size=256")
                    .setColor(3066993)
                    .setDescription("» Le poll avec l'ID "+strawPollToRemove.getId()+" a bien été supprimé !")
            ;
            textChannel.sendMessage(embedBuilder.build()).queue();
        }
    }

    @Command(name = "hookpoll",power = 50,description = "Permet d'ajouter un poll !",help = ".hookpoll <poll link>", example = ".hookpoll https://strawpoll.me/5aez41f85sed4f")
    private void hookPoll(User user, TextChannel textChannel, String[] args){
        EmbedBuilder helperEmbed = commandMap.getHelpEmbed("hookpoll");
        if(args.length == 0){
            textChannel.sendMessage(helperEmbed.build()).queue();
        }else{
            StrawPoll strawPollToAdd = new StrawPoll(args[0]);
            strawPollMap.put(strawPollToAdd.getId(), strawPollToAdd);
            commandMap.addStrawpoll(strawPollToAdd);
            EmbedBuilder embedBuilder = new EmbedBuilder();
            embedBuilder
                    .setTitle("Ajout de poll")
                    .setFooter(Reference.EmbedFooter.asDate(), Reference.EmbedIcon.getString())
                    .setThumbnail(Reference.StrawpollThumbnail.getString() + "?size=256")
                    .setColor(3066993)
                    .setDescription("» Le poll avec l'ID "+strawPollToAdd.getId()+" a bien été ajouté !")
            ;
            textChannel.sendMessage(embedBuilder.build()).queue();
        }
    }

    @Command(name = "listpoll", power = 50, description = "Permet de voir tout les polls link !",help = ".listpoll", example = ".listpoll")
    private void listPoll(User user, TextChannel textChannel){
        if(strawPollMap.size() <= 0) {
            EmbedBuilder em = new EmbedBuilder();
            em.setTitle("Liste des StrawPolls linkés")
                    .setFooter(Reference.EmbedFooter.getString(), Reference.EmbedIcon.getString())
                    .setThumbnail(Reference.StrawpollThumbnail.getString() + "?size=256")
                    .setColor(15158332)
                    .setDescription("» Aucun StrawPoll n'a été crée !");
            textChannel.sendMessage(em.build()).queue();
        }else{
            EmbedBuilder em = new EmbedBuilder();
            em.setTitle("Liste des StrawPolls linkés")
                    .setFooter(Reference.EmbedFooter.getString(), Reference.EmbedIcon.getString())
                    .setThumbnail(Reference.StrawpollThumbnail.getString() + "?size=256")
                    .setColor(3447003);
            strawPollMap.forEach((s, strawPoll) -> {
                em.addField(new MessageEmbed.Field(strawPoll.getTitle(), strawPoll.getPollURL(), true));
            });
            textChannel.sendMessage(em.build()).queue();
        }
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