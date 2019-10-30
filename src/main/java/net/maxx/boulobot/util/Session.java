package net.maxx.boulobot.util;

import java.util.HashMap;
import java.util.UUID;

public class Session {

    private UUID uuid;
    private long startDateMillis;
    private long endDateMillis;
    private int maxViewers;
    private int avgViewers;
    private int commandUsed;
    private int messageSended;
    private int newViewers;
    private HashMap<String, Integer> commandsUsed = new HashMap<>();
    private int bansAndTimeouts;
    private HashMap<String, Integer> usedEmotes = new HashMap<>();

    public Session(long currentTimeMillis) {
        this.startDateMillis = currentTimeMillis;
        this.uuid = UUID.randomUUID();
        this.maxViewers = 0;
        this.avgViewers = 0;
        this.commandUsed = 0;
        this.messageSended = 0;
        this.newViewers = 0;
        this.bansAndTimeouts = 0;
    }

    public Session(long currentTimeMillis, UUID uuid) {
        this.startDateMillis = currentTimeMillis;
        this.uuid = uuid;
        this.maxViewers = 0;
        this.avgViewers = 0;
        this.commandUsed = 0;
        this.messageSended = 0;
        this.newViewers = 0;
        this.bansAndTimeouts = 0;
    }

    public UUID getUuid() {
        return uuid;
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

    public void setCommandsUsed(HashMap<String, Integer> commandsUsed) {
        this.commandsUsed = commandsUsed;
    }

    public void setBansAndTimeouts(int bansAndTimeouts) {
        this.bansAndTimeouts = bansAndTimeouts;
    }

    public void setUsedEmotes(HashMap<String, Integer> usedEmotes) {
        this.usedEmotes = usedEmotes;
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
}
