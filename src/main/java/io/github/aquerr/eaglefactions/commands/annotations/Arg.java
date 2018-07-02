package io.github.aquerr.eaglefactions.commands.annotations;

import io.github.aquerr.eaglefactions.commands.enums.ArgType;

import java.lang.annotation.*;

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface Arg {
    ArgType type();
    String key();
    boolean optional() default false;
}
