package net.maxouxax.boulobot.commands;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(value=ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface TwitchCommand {

    String name();
    String description() default "Sans description.";
    String help() default "Aucune aide n'a été fournie";
    String example() default "Aucun exemple n'a été fourni";
    ExecutorRank rank() default ExecutorRank.EVERYONE;

    enum ExecutorRank{
        EVERYONE("EVERYONE", 0),
        SUBSCRIBER("SUBSCRIBER", 1),
        VIP("VIP", 2),
        MOD("MOD", 3),
        OWNER("OWNER", 4);

        ExecutorRank(String name, Integer power) {
            this.name = name;
            this.power = power;
        }

        private final String name;
        private final Integer power;

        public String getName() {
            return name;
        }

        public Integer getPower() {
            return power;
        }
    }
}
