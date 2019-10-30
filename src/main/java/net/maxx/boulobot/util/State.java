package net.maxx.boulobot.util;

public enum  State {

    PATCH("PATCH", "Patch de bug"),
    NEW("NEW", "Nouveauté"),
    OPT("OPT", "Optimisation"),
    MOD("MOD", "Modification")
    ;

    private String findName;
    private String name;

    State(String findName, String name) {
        this.findName = findName;
        this.name = name;
    }

    public String getFindName() {
        return findName;
    }

    public String getName() {
        return name;
    }

    public static State getByName(String name){
        for (State value : State.values()) {
            if(name.contains(value.getFindName())){
                return value;
            }
        }
        return null;
    }
}
