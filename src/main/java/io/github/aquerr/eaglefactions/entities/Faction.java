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
    //public BigDecimal Power;
//    public List<String> Recruits;
    public List<Player> Members;
    public List<String> Alliances;
    public List<String> Enemies;
    public Player Leader;
    //    public List<String> Officers;
    public List<String> Claims;
    public FactionHome Home;
    public Map<String, Group> groups;

    //Constructor used while creating a new faction.
    public Faction(String factionName, String factionTag, Player factionLeader) {
        this.Name = factionName;
        this.Tag = Text.of(TextColors.GREEN, factionTag);
        this.Leader = factionLeader;
        //this.Power = new BigDecimal("0.0");
//        this.Recruits = new ArrayList<>();
        this.Members = new ArrayList<>();
        this.Members.add(factionLeader);
        this.Claims = new ArrayList<>();
//        this.Officers = new ArrayList<>();
        this.Alliances = new ArrayList<>();
        this.Enemies = new ArrayList<>();
        this.groups = new HashMap<>();
        this.Home = null;

        //TODO: Setup basic perms
        groups.put("leader", new Group("leader", 1, "*"));
        groups.put("officer", new Group("officer", 10, "eaglefactions.player.ally*",
                "eaglefactions.player.enemy*", "eaglefactions.player.kick", "eaglefactions.player.invite",
                "eaglefactions.player.*claim", "eaglefactions.player.member", "eaglefactions.player.sethome",
                "eaglefactions.player.attack"));
        groups.put("member", new Group("member", 20, "eaglefactions.player.home"));
        groups.put("recruit", new Group("recruit", 30, "eaglefactions.player.chat",
                "eaglefactions.player.top", "eaglefactions.player.list", "eaglefactions.player.help",
                "eaglefactions.player.info*", "eaglefactions.player.*map"));

        groups.get("leader").perms.addGroup("officer");
        groups.get("officer").perms.addGroup("member");
        groups.get("member").perms.addGroup("recruit");

        factionLeader.addGroup("leader");
    }

    //Constructor used while getting a faction from storage.
    public Faction(String factionName, Text factionTag, Player factionLeader, List<Player> members, List<String> claims, List<String> alliances, List<String> enemies, FactionHome home, Map<String, Group> groups) {
        this.Name = factionName;
        this.Tag = factionTag;
        this.Leader = factionLeader;
        //this.Power = new BigDecimal("0.0");
        //this.Recruits = recruits;
        this.Members = members;
        this.Claims = claims;
        // this.Officers = officers;
        this.Alliances = alliances;
        this.Enemies = enemies;
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
