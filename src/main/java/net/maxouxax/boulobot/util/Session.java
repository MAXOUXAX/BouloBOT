package net.maxouxax.boulobot.util;

import com.github.twitch4j.helix.domain.GameList;
import com.github.twitch4j.helix.domain.Stream;
import com.github.twitch4j.helix.domain.StreamList;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.*;
import net.maxouxax.boulobot.BOT;

import java.util.*;
import java.util.logging.Level;

public class Session {

    private final BOT bot;
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
    private final ArrayList<Integer> viewerCountList = new ArrayList<>();

    private String title;
    private String currentGameId;
    private ArrayList<String> gameIds = new ArrayList<>();

    public Session(long currentTimeMillis, String channelId) {
        this(currentTimeMillis, UUID.randomUUID(), channelId);
    }

    public Session(long currentTimeMillis, UUID uuid, String channelId) {
        this.bot = BOT.getInstance();
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
        Guild discord = bot.getJda().getGuildById(bot.getConfigurationManager().getStringValue("guildId"));
        Member lyorine = Objects.requireNonNull(discord).getMemberById(bot.getConfigurationManager().getStringValue("lyorineClientId"));
        Role notif = discord.getRoleById(bot.getConfigurationManager().getStringValue("notificationRoleId"));
        String channelName = bot.getChannelName();
        bot.getLogger().log(Level.INFO, "> Updating session message!");
        EmbedCrafter embedCrafter = new EmbedCrafter();
        embedCrafter.setTitle("Notification \uD83D\uDD14", "https://twitch.tv/"+channelName.toUpperCase())
                    .setColor(3066993)
                    .setDescription("Coucou les "+ Objects.requireNonNull(notif).getAsMention()+" !\n**"+ Objects.requireNonNull(lyorine).getAsMention()+"** vient de démarrer son live, v'nez voir !\n» https://twitch.tv/"+channelName.toUpperCase())
                    .addField(new MessageEmbed.Field("Titre", title, true));
        GameList resultList = bot.getTwitchClient().getHelix().getGames(bot.getConfigurationManager().getStringValue("oauth2Token"), Collections.singletonList(currentGameId), null).execute();
        final String[] gameName = {"Aucun jeu"};
        resultList.getGames().forEach(game -> {
            gameName[0] = game.getName();
            String boxUrl = game.getBoxArtUrl(600, 800);
            embedCrafter.setThumbnailUrl(boxUrl);
        });
        embedCrafter.addField(new MessageEmbed.Field("Jeu", gameName[0], true));

        StreamList streamResultList = bot.getTwitchClient().getHelix().getStreams(bot.getConfigurationManager().getStringValue("oauth2Token"), "", "", null, null, null, null, Collections.singletonList(channelId), null).execute();
        final Stream[] currentStream = new Stream[1];
        streamResultList.getStreams().forEach(stream -> {
            currentStream[0] = stream;
        });

        embedCrafter.setImageUrl(currentStream[0].getThumbnailUrl(1280, 720));
        Message message = new MessageBuilder(notif.getAsMention()).setEmbed(embedCrafter.build()).build();
        bot.getJda().getPresence().setActivity(Activity.streaming("avec sa reine à "+gameName[0], "https://twitch.tv/"+channelName.toUpperCase()));
        if(sessionMessage == null){
            TextChannel toSend = discord.getTextChannelById(bot.getConfigurationManager().getStringValue("noticationTextChannelId"));
            Objects.requireNonNull(toSend).sendMessage(message).queue(message1 -> this.sessionMessage = message1);
        }else{
            this.sessionMessage.editMessage(message).queue();
        }
    }

    public void endSession() {
        endDate = System.currentTimeMillis();
        if(viewerCountList.size() != 0) {
            int sum = viewerCountList.stream().mapToInt(integer -> integer).sum();
            avgViewers = sum / viewerCountList.size();
        }
    }

    public ArrayList<Integer> getViewerCountList() {
        return viewerCountList;
    }
}