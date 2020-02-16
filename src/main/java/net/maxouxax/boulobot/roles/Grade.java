package net.maxouxax.boulobot.roles;

import net.dv8tion.jda.api.entities.Role;

public class Grade {

    private String name;
    private String displayName;
    private String description;
    private Long emoteId;
    private Role role;

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
