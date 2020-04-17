package net.maxouxax.boulobot.util;

import com.github.twitch4j.helix.domain.GameList;
import com.github.twitch4j.helix.domain.Stream;
import com.github.twitch4j.helix.domain.StreamList;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.*;
import net.maxouxax.boulobot.BOT;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.UUID;
import java.util.logging.Level;

public class Session {

    private BOT botDiscord;
    private UUID uuid;
    private String channelId;
    private long startDate;
    private long endDate;
    private int maxViewers;
    private int avgViewers;
    private int commandUsed;
    private int messageSended;
    private int newViewers;
    private int newFollowers;
    private HashMap<String, Integer> commandsUsed = new HashMap<>();
    private int bansAndTimeouts;
    private Message sessionMessage;

    private String title;
    private String currentGameId;
    private ArrayList<String> gameIds = new ArrayList<>();

    public Session(long currentTimeMillis, String channelId, BOT botDiscord) {
        this.botDiscord = botDiscord;
        this.channelId = channelId;
        this.startDate = currentTimeMillis;
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
        this.startDate = currentTimeMillis;
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

    public long getStartDate() {
        return startDate;
    }

    public long getEndDate() {
        return endDate;
    }

    public HashMap<String, Integer> getCommandsUsed() {
        return commandsUsed;
    }

    public int getBansAndTimeouts() {
        return bansAndTimeouts;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public void setStartDate(long startDate) {
        this.startDate = startDate;
    }

    public void setEndDate(long endDate) {
        this.endDate = endDate;
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
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

    private void addCommand() {
        commandUsed++;
    }

    public void addMessage() {
        messageSended++;
    }

    public void addNewViewer() {
        newViewers++;
    }

    public void addCommandUse(String command) {
        int current = commandsUsed.getOrDefault(command, 0);
        current++;
        commandsUsed.put(command, current);
        addCommand();
    }

    public void addBanOrTimeout() {
        bansAndTimeouts++;
    }

    public void addFollower() {
        newFollowers++;
    }

    public void updateMessage() {
        Guild discord = botDiscord.getJda().getGuildById(Reference.GuildID.getString());
        Member lyorine = discord.getMemberById(Reference.LyorineClientID.getString());
        Role notif = discord.getRoleById(Reference.NotifRoleID.getString());
        String channelName = botDiscord.getChannelName();
        botDiscord.getLogger().log(Level.INFO, "> Updating session message!");
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle("Notification \uD83D\uDD14", "https://twitch.tv/"+channelName.toUpperCase());
        embedBuilder.setFooter(Reference.EmbedFooter.asDate(), Reference.EmbedIcon.getString());
        embedBuilder.setColor(3066993);
        embedBuilder.setDescription("Coucou les "+notif.getAsMention()+" !\n**"+lyorine.getAsMention()+"** vient de démarrer son live, v'nez voir !\n» https://twitch.tv/"+channelName.toUpperCase());
        embedBuilder.addField(new MessageEmbed.Field("Titre", title, true));
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

        embedBuilder.setImage(currentStream[0].getThumbnailUrl(1280, 720));
        Message newMessage = new MessageBuilder(notif.getAsMention()).setEmbed(embedBuilder.build()).build();
        this.sessionMessage.editMessage(newMessage).queue();
        botDiscord.getLogger().log(Level.INFO, "> Updated!");
        botDiscord.getJda().getPresence().setActivity(Activity.streaming("avec sa reine à "+gameName[0], "https://twitch.tv/"+channelName.toUpperCase()));
    }
}
