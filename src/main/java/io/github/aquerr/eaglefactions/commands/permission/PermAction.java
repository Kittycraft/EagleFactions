package io.github.aquerr.eaglefactions.commands.permission;

import java.util.HashMap;
import java.util.Map;

public enum PermAction {
    add, remove, rem, del, delete, create, parents, group;
    public static final Map<String, PermAction> choices;

    static {
        choices = new HashMap<>();
        for(PermAction action : PermAction.values()){
            choices.put(action.name(), action);
        }
    }
}
