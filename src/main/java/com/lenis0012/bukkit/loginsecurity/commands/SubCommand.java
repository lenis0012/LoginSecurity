package com.lenis0012.bukkit.loginsecurity.commands;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface SubCommand {
    String name() default "";

    String description() default "";

    String usage() default "";

    int minArgs() default 0;
}
