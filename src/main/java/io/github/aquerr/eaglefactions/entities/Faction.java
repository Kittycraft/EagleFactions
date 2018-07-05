package io.github.aquerr.eaglefactions.entities;

import java.util.*;

/**
 * Created by Aquerr on 2017-07-13.
 */
public class Faction implements Cloneable {
    public final long creationTime;
    public final UUID fid;
    public String name;
    public List<FactionPlayer> members;
    public String owner;
    public List<FactionClaim> claims;
    public FactionHome Home;
    public Map<String, Group> groups;
    public String description;

    //Constructor used while creating a new faction.
    public Faction(String factionName, FactionPlayer factionLeader) {
        this.name = factionName;
        this.owner = factionLeader.uuid;
        this.members = new ArrayList<>();
        this.members.add(factionLeader);
        this.claims = new ArrayList<>();
        this.groups = new HashMap<>();
        this.Home = null;
        this.creationTime = System.currentTimeMillis();
        this.fid = UUID.randomUUID();

        groups.put("leader", new Group("leader", 1, "[^-]"));
        groups.put("officer", new Group("officer", 10, "f ally",
                "f enemy", "f kick", "f invite", "f claim", "f autoclaim", "f member", "f sethome"));
        groups.put("member", new Group("member", 20, "f home", "build", "interact"));
        groups.put("recruit", new Group("recruit", 30, "f ((chat)|(c))",
                "f top", "f list", "f help", "f ((info)|(i)|(f)|(faction)|(show))", "f *map", "f"));

        groups.get("leader").perms.addGroup("officer");
        groups.get("officer").perms.addGroup("member");
        groups.get("member").perms.addGroup("recruit");

        factionLeader.addGroup("leader");
    }

    //TODO: Constructor used while getting a faction from storage.
    public Faction(String factionName, String factionLeader, List<FactionPlayer> members, List<FactionClaim> claims, FactionHome home, Map<String, Group> groups, long creationTime) {
        this.name = factionName;
        this.owner = factionLeader;
        this.members = members;
        this.claims = claims;
        this.Home = home;
        this.groups = groups;
        this.creationTime = creationTime;
        this.fid = UUID.randomUUID();
    }

    //TODO: Temp constructor for FID passing
    public Faction(String factionName, String factionLeader, List<FactionPlayer> members, List<FactionClaim> claims, FactionHome home, Map<String, Group> groups, long creationTime, UUID fid) {
        this.name = factionName;
        this.owner = factionLeader;
        this.members = members;
        this.claims = claims;
        this.Home = home;
        this.groups = groups;
        this.creationTime = creationTime;
        this.fid = fid;
    }

    public FactionPlayer getMember(String name) {
        for (FactionPlayer p : members) {
            if (p.uuid.equals(name)) {
                return p;
            }
        }
        return null;
    }

    public boolean isAllowed(String player, String perm) {
        for (FactionPlayer p : members) {
            if (p.uuid.equals(player)) {
                return p.hasNode(perm, this);
            }
        }
        return false;
    }

    private List<FactionPlayer> cloneMembers() {
        List<FactionPlayer> list2 = new ArrayList<>();
        for (FactionPlayer player : members) {
            list2.add(player.clone());
        }
        return list2;
    }

    private Map<String, Group> cloneGroups() {
        Map<String, Group> map = new HashMap<>();
        groups.forEach((a, b) -> map.put(a, b.clone()));
        return map;
    }

    @Override
    public Faction clone() {
        return new Faction(name, owner, cloneMembers(), (List) ((ArrayList) claims).clone(), Home, cloneGroups(), creationTime);
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Faction){
            return fid.equals(((Faction) obj).fid);
        }
        return false;
    }

    public String getClaimRatio(){

    }
}
