package net.maxouxax.boulobot.util;

public class Modifications {

    private String name;
    private String description;
    private State state;

    public Modifications(String name, String description, State state) {
        this.name = name;
        this.description = description;
        this.state = state;
    }

    public String getName() {
        return name;
    }

    public State getState() {
        return state;
    }

    public String getDescription() {
        return description;
    }
}
