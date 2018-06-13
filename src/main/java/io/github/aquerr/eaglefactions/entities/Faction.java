package io.github.aquerr.eaglefactions.entities;

import io.github.aquerr.eaglefactions.managers.FlagManager;
import io.github.aquerr.eaglefactions.permissions.PermObject;
import io.github.aquerr.eaglefactions.permissions.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.ArrayList;
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
    public Map<FactionMemberType, Map<FactionFlagTypes, Boolean>> Flags;
    public Map<String, PermObject> groups;

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
        this.Home = null;
        this.Flags = FlagManager.getDefaultFactionFlags();

        //TODO: setup basic perms
        this.groups.put("leader", null);
        this.groups.put("officer", null);
        this.groups.put("member", null);
        this.groups.put("recruit", null);

        factionLeader.addGroup("leader");
    }

    //Constructor used while getting a faction from storage.
    public Faction(String factionName, Text factionTag, Player factionLeader, List<Player> members, List<String> claims, List<String> alliances, List<String> enemies, FactionHome home, Map<FactionMemberType, Map<FactionFlagTypes, Boolean>> flags) {
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
        this.Flags = flags;
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

    public boolean isAllowed(String player, String perm){
        for (Player p : Members) {
            if (p.name.equals(player)) {
                return p.hasNode(perm, this);
            }
        }
        return false;
    }
}
