package me.maxouxax.boulobot.commands.register.twitch;

import com.github.twitch4j.common.enums.CommandPermission;
import com.github.twitch4j.common.util.CryptoUtils;
import com.github.twitch4j.helix.domain.User;
import com.github.twitch4j.helix.domain.UserList;
import me.maxouxax.boulobot.BOT;
import me.maxouxax.boulobot.commands.CommandMap;
import me.maxouxax.boulobot.commands.TwitchCommand;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class TwitchJeparticipe {

    private final BOT bot;
    private final CommandMap commandMap;
    private boolean started;

    public TwitchJeparticipe(CommandMap commandMap){
        this.commandMap = commandMap;
        this.bot = BOT.getInstance();
    }

    @TwitchCommand(name = "jeparticipe", example = "jeparticipe", help = "jeparticipe", description = "Permet de participer au giveaway", rank = TwitchCommand.ExecutorRank.EVERYONE)
    private void jeparticipe(User user, Long broadcasterIdLong, Set<CommandPermission> permissions, String[] args){
        if(started) {
            if (!commandMap.isGiveawayKnown(user.getId())) {
                if(permissions.contains(CommandPermission.SUBSCRIBER)) {
                    commandMap.addGiveawayUser(user.getId());
                    bot.getTwitchClient().getChat().sendMessage(bot.getChannelName(), user.getDisplayName() + ", votre participation a été prise en compte ! (" + user.getId() + ")");
                }else{
                    bot.getTwitchClient().getChat().sendMessage(bot.getChannelName(), user.getDisplayName() + ", vous n'êtes pas autorisé à participer à ce giveaway puisque vous n'êtes pas abonné ! Rendez-vous sur https://lyor.in/subscribe pour en savoir plus ! (" + user.getId() + ")");
                }
            } else {
                bot.getTwitchClient().getChat().sendMessage(bot.getChannelName(), user.getDisplayName() + ", vous avez déjà participé ! (" + user.getId() + ")");
            }
        }else{
            bot.getTwitchClient().getChat().sendMessage(bot.getChannelName(), user.getDisplayName() + ", aucun giveaway n'est en cours");
        }
    }

    @TwitchCommand(name = "giveaway", description = "Permet de gérer les giveaways", example = "giveaway", help = "giveaway", rank = TwitchCommand.ExecutorRank.MOD)
    private void giveaway(User user, String broadcaster, String[] args){
        if(args.length == 0){
            bot.getTwitchClient().getChat().sendMessage(broadcaster, "Système de giveaway • "+commandMap.getTwitchTag()+"giveaway start | "+commandMap.getTwitchTag()+"giveaway stop | "+commandMap.getTwitchTag()+"giveaway tirage");
        }else{
            String arg1 = args[0];
            if(arg1.equalsIgnoreCase("start")){
                if(!started) {
                    commandMap.clearGiveaway();
                    started = true;
                    bot.getTwitchClient().getChat().sendMessage(broadcaster, "Le giveaway est démarré !");
                }else{
                    bot.getTwitchClient().getChat().sendMessage(broadcaster, "Le giveaway est déjà lancé !");
                }
            }else if(arg1.equalsIgnoreCase("stop")){
                if(started) {
                    started = false;
                    bot.getTwitchClient().getChat().sendMessage(broadcaster, "Le giveaway est arrêté ! Les participants ont été supprimés !");
                }else{
                    bot.getTwitchClient().getChat().sendMessage(broadcaster, "Le giveaway n'est pas lancé !");
                }
            }else if(arg1.equalsIgnoreCase("tirage")){
                if(started) {
                    List<String> giveawayIds = commandMap.getGiveawayUsersIds();
                    bot.getTwitchClient().getChat().sendMessage(broadcaster, "Tirage au sort... Nous avons " + giveawayIds.size() + " participants !");
                    if (commandMap.getGiveawayUsersIds().isEmpty()) {
                        bot.getTwitchClient().getChat().sendMessage(broadcaster, "Impossible de tirer au sort, aucun participant !");
                    } else {
                        Timer timer = new Timer();
                        String winningId = giveawayIds.get(new Random().nextInt(giveawayIds.size()));
                        UserList resultList = bot.getTwitchClient().getHelix().getUsers(bot.getConfigurationManager().getStringValue("oauth2Token"), Collections.singletonList(winningId), null).execute();
                        AtomicReference<User> winningUser = new AtomicReference<>();
                        resultList.getUsers().stream().findFirst().ifPresent(winningUser::set);
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy à HH:mm");
                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                bot.getTwitchClient().getChat().sendMessage(broadcaster, "Nous avons un gagnant !");
                            }
                        }, 2*1000);
                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                bot.getTwitchClient().getChat().sendMessage(broadcaster, "Il s'agit de... " + winningUser.get().getDisplayName() + " !");
                                bot.getTwitchClient().getChat().sendMessage(broadcaster, "Bien joué à lui ! LUL ");
                            }
                        }, 4*1000);
                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                bot.getTwitchClient().getChat().sendMessage(broadcaster, "Giveaway " + CryptoUtils.generateNonce(4) + " • "
                                        +giveawayIds.size()+" participant(s) • "
                                        +"Tirage lancé par "+user.getDisplayName()+" • "
                                        +"Identifiant du gagnant » "+winningUser.get().getId()+" • "
                                        +simpleDateFormat.format(new Date()));
                                commandMap.clearGiveaway();
                                started = false;
                            }
                        }, 6*1000);
                    }
                }else{
                    bot.getTwitchClient().getChat().sendMessage(broadcaster, user.getDisplayName() + ", aucun giveaway n'est en cours");
                }
            }
        }
    }

}
