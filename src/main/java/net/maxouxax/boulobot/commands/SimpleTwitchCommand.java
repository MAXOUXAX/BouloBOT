package net.maxouxax.boulobot.commands;

import java.lang.reflect.Method;

public class SimpleTwitchCommand {

    private final String name, description, help, exemple;
    private final TwitchCommand.ExecutorRank executorRank;
    private final Object object;
    private final Method method;

    public SimpleTwitchCommand(String name, String description, String help, String exemple, TwitchCommand.ExecutorRank executorRank, Object object, Method method){
        super();
        this.name = name;
        this.description = description;
        this.help = help;
        this.exemple = exemple;
        this.executorRank = executorRank;
        this.object = object;
        this.method = method;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getHelp() {
        return help;
    }

    public String getExemple() {
        return exemple;
    }

    public TwitchCommand.ExecutorRank getExecutorRank() {
        return executorRank;
    }

    public Object getObject() {
        return object;
    }

    public Method getMethod() {
        return method;
    }

}
