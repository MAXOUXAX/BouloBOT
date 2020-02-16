package net.maxx.boulobot.util;

import com.github.twitch4j.helix.domain.GameList;
import com.github.twitch4j.helix.domain.Stream;
import com.github.twitch4j.helix.domain.StreamList;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.*;
import net.maxx.boulobot.BOT;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.UUID;
import java.util.logging.Level;

public class Session {

    private BOT botDiscord;
    private UUID uuid;
    private String channelId;
    private long startDateMillis;
    private long endDateMillis;
    private int maxViewers;
    private int avgViewers;
    private int commandUsed;
    private int messageSended;
    private int newViewers;
    private int newFollowers;
    private HashMap<String, Integer> commandsUsed = new HashMap<>();
    private int bansAndTimeouts;
    private HashMap<String, Integer> usedEmotes = new HashMap<>();
    private Message sessionMessage;

    private String currentTitle;
    private String currentGameId;
    private ArrayList<String> gameIds;

    public Session(long currentTimeMillis, String channelId, BOT botDiscord) {
        this.botDiscord = botDiscord;
        this.channelId = channelId;
        this.startDateMillis = currentTimeMillis;
        this.uuid = UUID.randomUUID();
        this.maxViewers = 0;
        this.avgViewers = 0;
        this.commandUsed = 0;
        this.messageSended = 0;
        this.newViewers = 0;
        this.newFollowers = 0;
        this.bansAndTimeouts = 0;
    }

    public Session(long currentTimeMillis, UUID uuid, String channelId, BOT botDiscord) {
        this.botDiscord = botDiscord;
        this.channelId = channelId;
        this.startDateMillis = currentTimeMillis;
        this.uuid = uuid;
        this.maxViewers = 0;
        this.avgViewers = 0;
        this.commandUsed = 0;
        this.messageSended = 0;
        this.newViewers = 0;
        this.newFollowers = 0;
        this.bansAndTimeouts = 0;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getChannelId() {
        return channelId;
    }

    public long getStartDate() {
        return startDateMillis;
    }

    public long getEndDate() {
        return endDateMillis;
    }

    public int getMaxViewers() {
        return maxViewers;
    }

    public int getAvgViewers() {
        return avgViewers;
    }

    public int getCommandUsed() {
        return commandUsed;
    }

    public int getMessageSended() {
        return messageSended;
    }

    public int getNewViewers() {
        return newViewers;
    }

    public long getStartDateMillis() {
        return startDateMillis;
    }

    public long getEndDateMillis() {
        return endDateMillis;
    }

    public HashMap<String, Integer> getCommandsUsed() {
        return commandsUsed;
    }

    public int getBansAndTimeouts() {
        return bansAndTimeouts;
    }

    public HashMap<String, Integer> getUsedEmotes() {
        return usedEmotes;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public void setStartDateMillis(long startDateMillis) {
        this.startDateMillis = startDateMillis;
    }

    public void setEndDateMillis(long endDateMillis) {
        this.endDateMillis = endDateMillis;
    }

    public void setMaxViewers(int maxViewers) {
        this.maxViewers = maxViewers;
    }

    public void setAvgViewers(int avgViewers) {
        this.avgViewers = avgViewers;
    }

    public void setCommandUsed(int commandUsed) {
        this.commandUsed = commandUsed;
    }

    public void setMessageSended(int messageSended) {
        this.messageSended = messageSended;
    }

    public void setNewViewers(int newViewers) {
        this.newViewers = newViewers;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public void setCommandsUsed(HashMap<String, Integer> commandsUsed) {
        this.commandsUsed = commandsUsed;
    }

    public void setBansAndTimeouts(int bansAndTimeouts) {
        this.bansAndTimeouts = bansAndTimeouts;
    }

    public void setUsedEmotes(HashMap<String, Integer> usedEmotes) {
        this.usedEmotes = usedEmotes;
    }

    public int getNewFollowers() {
        return newFollowers;
    }

    public void setNewFollowers(int newFollowers) {
        this.newFollowers = newFollowers;
    }

    public Message getSessionMessage() {
        return sessionMessage;
    }

    public void setSessionMessage(Message sessionMessage) {
        this.sessionMessage = sessionMessage;
    }

    public void newGame(String newGameId){
        this.gameIds.add(newGameId);
        this.currentGameId = newGameId;
    }

    public String getCurrentTitle() {
        return currentTitle;
    }

    public void setCurrentTitle(String currentTitle) {
        this.currentTitle = currentTitle;
    }

    public String getCurrentGameId() {
        return currentGameId;
    }

    public void setCurrentGameId(String currentGameId) {
        this.currentGameId = currentGameId;
    }

    public ArrayList<String> getGameIds() {
        return gameIds;
    }

    public void setGameIds(ArrayList<String> gameIds) {
        this.gameIds = gameIds;
    }

    public void addCommand() {
        commandUsed++;
    }

    public void addMessage() {
        messageSended++;
    }

    public void addNewViewer() {
        newViewers++;
    }

    public void addCommandUse(String command) {
        int current = commandsUsed.get(command);
        current++;
        commandsUsed.put(command, current);
    }

    public void addBanOrTimeout() {
        bansAndTimeouts++;
    }

    public void addUsedEmote(String emoteUsed) {
        int current = usedEmotes.get(emoteUsed);
        current++;
        usedEmotes.put(emoteUsed, current);
    }

    public void addFollower() {
        newFollowers++;
    }

    public void updateMessage() {
        Guild discord = botDiscord.getJda().getGuildById(Reference.GuildID.getString());
        Member lyorine = discord.getMemberById(Reference.LyorineClientID.getString());
        Role notif = discord.getRoleById(Reference.NotifRoleID.getString());
        botDiscord.getLogger().log(Level.INFO, "> Le stream est ONLINE!");
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle("Notification \uD83D\uDD14", "https://twitch.lyorine.com");
        embedBuilder.setFooter(Reference.EmbedFooter.asDate(), Reference.EmbedIcon.getString());
        embedBuilder.setColor(3066993);
        embedBuilder.setDescription("Oyé oyé les "+notif.getAsMention()+" !\n**"+lyorine.getAsMention()+"** part en stream !\n» https://twitch.lyorine.com");
        embedBuilder.addField(new MessageEmbed.Field("Titre", currentTitle, true));
        GameList resultList = botDiscord.getTwitchClient().getHelix().getGames(Collections.singletonList(currentGameId), null).execute();
        final String[] gameName = {"Aucun jeu"};
        resultList.getGames().forEach(game -> {
            gameName[0] = game.getName();
            String boxUrl = game.getBoxArtUrl();
            boxUrl = boxUrl.replace("{width}", "600");
            boxUrl = boxUrl.replace("{height}", "800");
            embedBuilder.setThumbnail(boxUrl);
        });
        embedBuilder.addField(new MessageEmbed.Field("Jeu", gameName[0], true));

        StreamList streamResultList = botDiscord.getTwitchClient().getHelix().getStreams(botDiscord.getConfigurationManager().getStringValue("oauth2Token"), "", "", null, null, null, null, Collections.singletonList(channelId), null).execute();
        final Stream[] currentStream = new Stream[1];
        streamResultList.getStreams().forEach(stream -> {
            currentStream[0] = stream;
        });

        embedBuilder.setImage(currentStream[0].getThumbnailUrl());
        Message newMessage = new MessageBuilder(notif.getAsMention()).setEmbed(embedBuilder.build()).build();
        this.sessionMessage.editMessage(newMessage).queue();
    }
}
