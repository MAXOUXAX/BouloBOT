package me.maxouxax.boulobot.roles;

import net.dv8tion.jda.api.entities.Role;

public class Grade {

    private final String name;
    private final String displayName;
    private final String description;
    private final Long emoteId;
    private final Role role;

    public String getDescription() {
        return description;
    }

    public String getName() {
        return name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Long getEmoteId() {
        return emoteId;
    }

    public Role getRole() {
        return role;
    }

    public Grade(Role role, String displayName, String description, Long emoteId) {
        this.role = role;
        this.name = role.getName();
        this.displayName = displayName;
        this.emoteId = emoteId;
        this.description = description;
    }
}
