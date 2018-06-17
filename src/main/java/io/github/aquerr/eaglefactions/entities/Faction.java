package io.github.aquerr.eaglefactions.entities;

import io.github.aquerr.eaglefactions.permissions.Group;
import io.github.aquerr.eaglefactions.permissions.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Aquerr on 2017-07-13.
 */
public class Faction {
    public String Name;
    public Text Tag;
    public List<Player> Members;
    public Player Leader;
    public List<String> Claims;
    public FactionHome Home;
    public Map<String, Group> groups;

    //Constructor used while creating a new faction.
    public Faction(String factionName, String factionTag, Player factionLeader) {
        this.Name = factionName;
        this.Tag = Text.of(TextColors.GREEN, factionTag);
        this.Leader = factionLeader;
        this.Members = new ArrayList<>();
        this.Members.add(factionLeader);
        this.Claims = new ArrayList<>();
        this.groups = new HashMap<>();
        this.Home = null;

        groups.put("leader", new Group("leader", 1, "[^-]"));
        groups.put("officer", new Group("officer", 10, "f ally",
                "f enemy", "f kick", "f invite", "f *claim", "f member", "f sethome"));
        groups.put("member", new Group("member", 20, "f home", "build", "interact"));
        groups.put("recruit", new Group("recruit", 30, "f ((chat)|(c))",
                "f top", "f list", "f help", "f ((info)|(i)|(f)|(faction)|(show))", "f *map", "f"));

        groups.get("leader").perms.addGroup("officer");
        groups.get("officer").perms.addGroup("member");
        groups.get("member").perms.addGroup("recruit");

        factionLeader.addGroup("leader");
    }

    //Constructor used while getting a faction from storage.
    public Faction(String factionName, Text factionTag, Player factionLeader, List<Player> members, List<String> claims, FactionHome home, Map<String, Group> groups) {
        this.Name = factionName;
        this.Tag = factionTag;
        this.Leader = factionLeader;
        this.Members = members;
        this.Claims = claims;
        this.Home = home;
        this.groups = groups;
    }

    public Player getMember(String name) {
        for (Player p : Members) {
            if (p.name.equals(name)) {
                return p;
            }
        }
        return null;
    }

    public boolean containsMember(String name) {
        for (Player p : Members) {
            if (p.name.equals(name)) {
                return true;
            }
        }
        return false;
    }

    public boolean isAllowed(String player, String perm) {
        for (Player p : Members) {
            if (p.name.equals(player)) {
                return p.hasNode(perm, this);
            }
        }
        return false;
    }
}
