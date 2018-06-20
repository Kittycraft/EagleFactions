package io.github.aquerr.eaglefactions.entities;

import java.util.ArrayList;
import java.util.List;

public class FactionPlayer extends PermObject {

    public final String uuid, name;

    public FactionPlayer(String uuid, String name) {
        this(uuid, name, new String[]{});
    }

    public FactionPlayer(String uuid, String name, String... groups) {
        this.uuid = uuid;
        this.name = name;
        for (String group : groups) {
            addGroup(group);
        }
    }

    public FactionPlayer(String uuid, String name, List<String> inherit, List<String> nodes) {
        super(inherit, nodes);
        this.uuid = uuid;
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

    public String getPrimaryGroup() {
        if (inherit.size() > 0) {
            return inherit.get(inherit.size() - 1);
        }
        return "NO-GROUP";
    }

    @Override
    public String toString() {
        return uuid;
    }

    @Override
    public boolean equals(Object obj) {
        return uuid.equals(obj);
    }

    @Override
    public FactionPlayer clone() {
        return new FactionPlayer(uuid, name, (List<String>) ((ArrayList) inherit).clone(), (List<String>) ((ArrayList) nodes).clone());
    }
}
