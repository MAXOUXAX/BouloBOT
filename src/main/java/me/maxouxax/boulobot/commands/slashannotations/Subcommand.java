package me.maxouxax.boulobot.commands.slashannotations;

import java.lang.annotation.*;

@Target(value= ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(Subcommands.class)
public @interface Subcommand {

    String name();
    String description();

}
