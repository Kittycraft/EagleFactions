package io.github.aquerr.eaglefactions.permissions;

import io.github.aquerr.eaglefactions.entities.Faction;

public class Player extends PermObject {

    public final String name;

    public Player(String name) {
        this.name = name;
    }

    public int getPriority(Faction context) {
        int best = Integer.MAX_VALUE;
        for (String s : inherit) {
            if (context.groups.getOrDefault(s, empty).priority < best) {
                best = context.groups.getOrDefault(s, empty).priority;
            }
        }
        return best;
    }

    public String getPrimaryGroup(){
        if(inherit.size() > 0) {
            return inherit.get(inherit.size() - 1);
        }
        return "NO-GROUP";
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object obj) {
        throw new Error("Attempted to call .equals on Player object!");
    }
}
