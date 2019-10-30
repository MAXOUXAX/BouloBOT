package net.maxx.boulobot.commands.register.twitch;

import com.github.twitch4j.helix.domain.User;
import com.github.twitch4j.tmi.domain.Chatters;
import net.maxx.boulobot.BOT;
import net.maxx.boulobot.commands.CommandMap;
import net.maxx.boulobot.commands.TwitchCommand;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;

public class TwitchRandom {

    private final BOT botDiscord;
    private final CommandMap commandMap;

    public TwitchRandom(BOT botDiscord, CommandMap commandMap){
        this.botDiscord = botDiscord;
        this.commandMap = commandMap;
    }

    @TwitchCommand(name = "random", description = "Petite commande aléatoire", example = "&random Lyorine", help = "&random <pseudo sans @>", rank = TwitchCommand.ExecutorRank.EVERYONE)
    public void random(User user, String broadcaster, String[] args) {
        Chatters chat = botDiscord.getTwitchClient().getMessagingInterface().getChatters(broadcaster).execute();
        botDiscord.getLogger().log(Level.INFO, "getChatters");
        if (commandMap.getRank(broadcaster, chat, user.getDisplayName().toLowerCase()).getPower() >= TwitchCommand.ExecutorRank.VIP.getPower()) {
            botDiscord.getLogger().log(Level.INFO, "People VIP or more");
            int inte = new Random().nextInt(TrollSentences.values().length);
            botDiscord.getLogger().log(Level.INFO, "Got random number");
            TrollSentences troll = Arrays.asList(TrollSentences.values()).get(inte);
            botDiscord.getLogger().log(Level.INFO, "Got TrollSentence");
            String random = null;
            if (!troll.isRandomAnUsername) {
                botDiscord.getLogger().log(Level.INFO, "Phrase niveau des races");
                int race = new Random().nextInt(Race.values().length);
                Race raceObj = Arrays.asList(Race.values()).get(race);
                random = raceObj.getName();
            } else {
                botDiscord.getLogger().log(Level.INFO, "Phrase pas race");
                if(args.length == 0) {
                    botDiscord.getLogger().log(Level.INFO, "No args so getting a random viewer");
                    List<String> chatters = chat.getAllViewers();
                    botDiscord.getLogger().log(Level.INFO, "Got chatters");
                    int randomInt = new Random().nextInt(chatters.size());
                    botDiscord.getLogger().log(Level.INFO, "Got random number");
                    random = chatters.get(randomInt);
                    botDiscord.getLogger().log(Level.INFO, "Got random people username");
                    while(random.equalsIgnoreCase(user.getDisplayName())){
                        int randomInto = new Random().nextInt(chatters.size());
                        botDiscord.getLogger().log(Level.INFO, "WHILE Got random number");
                        random = chatters.get(randomInto);
                        botDiscord.getLogger().log(Level.INFO, "WHILE Got random people username");
                    }
                    random = botDiscord.getTwitchClient().getHelix().getUsers(null, null, Collections.singletonList(random)).execute().getUsers().get(0).getDisplayName();
                }else{
                    botDiscord.getLogger().log(Level.INFO, "Args so getting given username");
                    List<User> usersList = botDiscord.getTwitchClient().getHelix().getUsers(null, null, Arrays.asList(args[0])).execute().getUsers();
                    botDiscord.getLogger().log(Level.INFO, "Got user list");
                    User finalUser;
                    if(usersList != null && !usersList.isEmpty()) {
                        botDiscord.getLogger().log(Level.INFO, "List isnt null and isnt empty");
                        finalUser = usersList.get(0);
                    }else{
                        botDiscord.getLogger().log(Level.INFO, "List null or empty");
                        botDiscord.getTwitchClient().getChat().sendMessage(broadcaster, "Oops @"+user.getDisplayName()+" ! Je n'ai pas trouvé d'utilisateur correspondant à ce nom");
                        return;
                    }
                    random = finalUser.getDisplayName();
                    botDiscord.getLogger().log(Level.INFO, "Got finalUser getDisplayName");
                }
            }
            String phrase = troll.getSentence(user.getDisplayName(), new Random().nextInt(100), random);
            botDiscord.getLogger().log(Level.INFO, phrase);
            //ENVOI

            botDiscord.getTwitchClient().getChat().sendMessage(broadcaster, phrase);
            botDiscord.getLogger().log(Level.INFO, "Message envoyé (normalement)");
        } else {
            botDiscord.getLogger().log(Level.INFO, "People no VIP");
            int inte = new Random().nextInt(TrollSentences.values().length);
            botDiscord.getLogger().log(Level.INFO, "Got random number");
            TrollSentences troll = Arrays.asList(TrollSentences.values()).get(inte);
            botDiscord.getLogger().log(Level.INFO, "Got TrollSentence");
            while (troll.isVip) {
                botDiscord.getLogger().log(Level.INFO, "Fucking sentence is vip so retrying to get one");
                int randomInteger = new Random().nextInt(TrollSentences.values().length);
                troll = Arrays.asList(TrollSentences.values()).get(randomInteger);
            }
            botDiscord.getLogger().log(Level.INFO, "Finally got one");
            String random = null;
            if (!troll.isRandomAnUsername) {
                botDiscord.getLogger().log(Level.INFO, "Phrase niveau des races");
                int race = new Random().nextInt(Race.values().length);
                Race raceObj = Arrays.asList(Race.values()).get(race);
                random = raceObj.getName();
            } else {
                botDiscord.getLogger().log(Level.INFO, "Phrase pas race");
                if(args.length == 0) {
                    botDiscord.getLogger().log(Level.INFO, "No args so getting a random viewer");
                    List<String> chatters = chat.getAllViewers();
                    botDiscord.getLogger().log(Level.INFO, "Got chatters");
                    int randomInt = new Random().nextInt(chatters.size());
                    botDiscord.getLogger().log(Level.INFO, "Got random number");
                    random = chatters.get(randomInt);
                    botDiscord.getLogger().log(Level.INFO, "Got random people username");
                    while(random.equalsIgnoreCase(user.getDisplayName())){
                        int randomInto = new Random().nextInt(chatters.size());
                        botDiscord.getLogger().log(Level.INFO, "WHILE Got random number");
                        random = chatters.get(randomInto);
                        botDiscord.getLogger().log(Level.INFO, "WHILE Got random people username");
                    }
                    random = botDiscord.getTwitchClient().getHelix().getUsers(null, null, Collections.singletonList(random)).execute().getUsers().get(0).getDisplayName();
                }else{
                    botDiscord.getLogger().log(Level.INFO, "Args so getting given username");
                    List<User> usersList = botDiscord.getTwitchClient().getHelix().getUsers(null, null, Arrays.asList(args[0])).execute().getUsers();
                    botDiscord.getLogger().log(Level.INFO, "Got user list");
                    User finalUser;
                    if(usersList != null && !usersList.isEmpty()) {
                        botDiscord.getLogger().log(Level.INFO, "List isnt null and isnt empty");
                        finalUser = usersList.get(0);
                    }else{
                        botDiscord.getLogger().log(Level.INFO, "List null or empty");
                        botDiscord.getTwitchClient().getChat().sendMessage(broadcaster, "Oops @"+user.getDisplayName()+" ! Je n'ai pas trouvé d'utilisateur correspondant à ce nom");
                        return;
                    }
                    random = finalUser.getDisplayName();
                    botDiscord.getLogger().log(Level.INFO, "Got finalUser getDisplayName");
                }
            }
            String phrase = troll.getSentence(user.getDisplayName(), new Random().nextInt(100), random);
            botDiscord.getLogger().log(Level.INFO, phrase);
            //ENVOI

            botDiscord.getTwitchClient().getChat().sendMessage(broadcaster, phrase);
            botDiscord.getLogger().log(Level.INFO, "Message envoyé (normalement)");
        }
    }

    public enum TrollSentences{

        AMOUR(":sender: aime à :percent:% :random:", false, true),
        HATE(":sender: déteste :random: à :percent:%", false, true),
        RACE(":sender: est un chat de race :random: à :percent:%", true, false),
        FOU(":sender: est fou à :percent:%", false, true),
        CHATO(":sender: est atteint(e) de chatopatithe aïgue à :percent:%", false, true),
        FUN(":sender: fait rire :random: à :percent:%", false, true),
        ;

        private String text;
        private boolean isVip;
        private boolean isRandomAnUsername;

        TrollSentences(String text, boolean isVip, boolean isRandomAnUsername) {
            this.text = text;
            this.isVip = isVip;
            this.isRandomAnUsername = isRandomAnUsername;
        }

        public String getText() {
            return text;
        }

        public boolean isVip() {
            return isVip;
        }

        public boolean isRandomAnUsername() {
            return isRandomAnUsername;
        }

        public String getSentence(String sender, int percent, String random){
            String sentence = getText();
            sentence = sentence.replaceAll(":sender:", sender);
            sentence = sentence.replaceAll(":percent:", ""+percent);
            sentence = sentence.replaceAll(":random:", random);
            return sentence;
        }
    }

    public enum Race{
        ANGORA("angora"),
        ASHERA("ashera"),
        MAINE("maine coon"),
        SPHYNX("sphynx"),
        PERSAN("persan"),
        GRUMPY("grumpy cat"),
        SCOTTISH("scottish fold"),
        SIAMOIS("siamois"),
        SIBÉRIEN("sibérien"),
        TIGRE("tigre du bengale"),
        LILBUB("lil bub (https://bit.ly/2LUxFfJ)"),
        GARFIELD("garfield"),
        GOUTIERE("chat de gouttière"),
        LYKOI("lykoi"),
        ;

        private String name;

        Race(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

}
