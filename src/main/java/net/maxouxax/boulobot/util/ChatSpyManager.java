package net.maxouxax.boulobot.util;

import net.dv8tion.jda.api.entities.TextChannel;
import net.maxouxax.boulobot.BOT;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class ChatSpyManager {

    private final BOT bot;
    private final ArrayList<String> stringsToPost = new ArrayList<>();
    private final ArrayList<String> ignoredUsers = new ArrayList<>();
    private final ScheduledFuture scheduleChatSpy;

    public ChatSpyManager() {
        this.bot = BOT.getInstance();
        this.scheduleChatSpy = bot.getScheduler().scheduleAtFixedRate(this::postMessages, 30, 30, TimeUnit.SECONDS);
        String ignoredUsersString = bot.getConfigurationManager().getStringValue("ignoredUsers");
        ignoredUsers.addAll(Arrays.asList(ignoredUsersString.split(" ²² ")));
    }

    private void postMessages() {
        if(!stringsToPost.isEmpty()) {
            ArrayList<StringBuilder> stringBuilderArrayList = new ArrayList<>();
            stringBuilderArrayList.add(new StringBuilder());
            AtomicReference<Integer> listIndex = new AtomicReference<>(0);
            stringsToPost.forEach(s -> {
                if (stringBuilderArrayList.get(listIndex.get()).length() + s.length() > 2000) {
                    stringBuilderArrayList.add(new StringBuilder());
                    listIndex.set(listIndex.get() + 1);
                }
                stringBuilderArrayList.get(listIndex.get()).append(s).append("\n");
            });

            stringBuilderArrayList.forEach(stringBuilder -> {
                TextChannel channel = Objects.requireNonNull(bot.getJda().getGuildById(bot.getConfigurationManager().getStringValue("guildId"))).getTextChannelById(bot.getConfigurationManager().getStringValue("chatSpyTextChannelId"));
                if (channel != null) {
                    channel.sendMessage(stringBuilder.toString()).queue();
                }
            });
        }
        stringsToPost.clear();
    }

    public void endSpy(){
        scheduleChatSpy.cancel(false);
    }

    public void addMessage(String usernameId, String string) {
        if (!isIgnored(usernameId)) {
            String date = new SimpleDateFormat("HH:mm:ss").format(new Date());
            stringsToPost.add("`" + date + "` • " + string);
        }
    }

    public boolean isIgnored(String usernameId){
        for (String s : ignoredUsers) {
            if (s.equalsIgnoreCase(usernameId)) {
                return true;
            }
        }
        return false;
    }

    public ArrayList<String> getIgnoredUsers() {
        return ignoredUsers;
    }

    public void saveIgnoredUsers(){
        StringBuilder stringBuilder = new StringBuilder();
        ignoredUsers.forEach(s -> stringBuilder.append(s).append(" ²² "));
        bot.getConfigurationManager().setValue("ignoredUsers", stringBuilder.toString(), true);
    }
}
