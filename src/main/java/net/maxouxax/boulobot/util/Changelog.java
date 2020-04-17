package net.maxouxax.boulobot.util;

import net.maxouxax.boulobot.BOT;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class Changelog {

    private String name;
    private String oldVersion;
    private String version;
    private UUID uuid;
    private BOT botDiscord;
    private HashMap<Platform, ArrayList<Modifications>> hashMap = new HashMap<>();

    public Changelog(BOT botDiscord, String name, String oldVersion) {
        this.botDiscord = botDiscord;
        this.name = name;
        this.oldVersion = oldVersion;
        this.version = botDiscord.getVersion();
        this.uuid = UUID.randomUUID();
    }

    public Changelog(BOT botDiscord, String name, String oldVersion, String version) {
        this.botDiscord = botDiscord;
        this.name = name;
        this.oldVersion = oldVersion;
        this.version = version;
        this.uuid = UUID.randomUUID();
    }

    public String getName() {
        return name;
    }

    public String getOldVersion() {
        return oldVersion;
    }

    public String getVersion() {
        return version;
    }

    public UUID getUuid() {
        return uuid;
    }

    public HashMap<Platform, ArrayList<Modifications>> getHashMap() {
        return hashMap;
    }

    public void addModification(Modifications modif, Platform platform){
        ArrayList<Modifications> arrayList = this.hashMap.getOrDefault(platform, new ArrayList<>());
        arrayList.add(modif);
        this.hashMap.put(platform, arrayList);
    }

    public void removeModification(Modifications modif, Platform platform) {
        ArrayList<Modifications> arrayList = this.hashMap.getOrDefault(platform, new ArrayList<>());
        arrayList.remove(modif);
        if(arrayList.isEmpty()) {
            this.hashMap.remove(platform);
        }else{
            this.hashMap.put(platform, arrayList);
        }
    }

}
