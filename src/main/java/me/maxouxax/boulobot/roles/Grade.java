package me.maxouxax.boulobot.roles;

import net.dv8tion.jda.api.entities.Role;

public class Grade {

    private final String name;
    private final String displayName;
    private final String description;
    private final String emoteId;
    private final Role role;

    public Grade(Role role, String displayName, String description, String emoteId) {
        this.role = role;
        this.name = role.getName();
        this.displayName = displayName;
        this.emoteId = emoteId;
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public String getName() {
        return name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getEmoteId() {
        return emoteId;
    }

    public Role getRole() {
        return role;
    }
}
