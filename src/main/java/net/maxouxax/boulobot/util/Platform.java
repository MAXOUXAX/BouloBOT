package net.maxouxax.boulobot.util;

public enum Platform {

    TWITCH("TWITCH", "Twitch", 6570404),
    DISCORD("DISCORD", "Discord", 7506394),
    GLOBAL("GLOBAL", "Global", 12185538),
    ;

    private String findName;
    private String name;
    private int embedColor;

    Platform(String findName, String name, int embedColor) {
        this.findName = findName;
        this.name = name;
        this.embedColor = embedColor;
    }

    public String getFindName() {
        return findName;
    }

    public String getName() {
        return name;
    }

    public static Platform getByName(String name){
        for (Platform value : Platform.values()) {
            if(name.contains(value.getFindName())){
                return value;
            }
        }
        return null;
    }

    public int getEmbedColor() {
        return embedColor;
    }
}
