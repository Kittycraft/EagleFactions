package io.github.aquerr.eaglefactions.entities;

import io.github.aquerr.eaglefactions.managers.PlayerManager;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.service.user.UserStorageService;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FactionPlayer extends PermissionsObject {

    public final String uuid, name, faction;
    public long lastOnline;

    public FactionPlayer(String uuid, String name, String faction) {
        this(uuid, name, faction, new String[]{});
    }

    public FactionPlayer(String uuid, String name, String faction, String... groups) {
        this.uuid = uuid;
        this.name = name;
        this.faction = faction;
        for (String group : groups) {
            addGroup(group);
        }
    }

    public FactionPlayer(String uuid, String name, String faction, List<String> inherit, List<String> nodes, long lastOnline) {
        super(inherit, nodes);
        this.uuid = uuid;
        this.name = name;
        this.faction = faction;
        this.lastOnline = lastOnline;
    }

    public int getPriority(Faction context) {
        int best = Integer.MAX_VALUE;
        for (String s : parents) {
            if (context.groups.getOrDefault(s, empty).priority < best) {
                best = context.groups.getOrDefault(s, empty).priority;
            }
        }
        return best;
    }

    public String getPrimaryGroup() {
        if (parents.size() > 0) {
            return parents.get(parents.size() - 1);
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
        return new FactionPlayer(uuid, name, faction, (List<String>) ((ArrayList) parents).clone(), (List<String>) ((ArrayList) nodes).clone(), lastOnline);
    }

    public long getLastOnline() {
        if (Sponge.getServiceManager().provideUnchecked(UserStorageService.class).get(UUID.fromString(uuid)).isPresent()) {
            lastOnline = System.currentTimeMillis();
        }
        return lastOnline;
    }

}
