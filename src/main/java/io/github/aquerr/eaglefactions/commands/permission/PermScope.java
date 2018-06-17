package io.github.aquerr.eaglefactions.commands.permission;

import java.util.HashMap;
import java.util.Map;

public enum PermScope {
    USER, GROUP;
    public static final Map<String, PermScope> choices;

    static {
        choices = new HashMap<>();
        for(PermScope scope : PermScope.values()){
            choices.put(scope.name(), scope);
        }
    }
}
