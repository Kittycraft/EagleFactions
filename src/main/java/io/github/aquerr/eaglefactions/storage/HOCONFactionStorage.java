package io.github.aquerr.eaglefactions.storage;

import com.google.common.reflect.TypeToken;
import io.github.aquerr.eaglefactions.caching.FactionsCache;
import io.github.aquerr.eaglefactions.entities.Faction;
import io.github.aquerr.eaglefactions.entities.FactionFlagTypes;
import io.github.aquerr.eaglefactions.entities.FactionHome;
import io.github.aquerr.eaglefactions.entities.FactionMemberType;
import io.github.aquerr.eaglefactions.permissions.Group;
import io.github.aquerr.eaglefactions.permissions.Player;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.api.text.Text;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class HOCONFactionStorage implements IStorage {
    private ConfigurationLoader<CommentedConfigurationNode> configLoader;
    private CommentedConfigurationNode configNode;

    public HOCONFactionStorage(Path configDir) {
        try {
            Path dataPath = configDir.resolve("data");

            if (!Files.exists(dataPath)) {
                Files.createDirectory(dataPath);
            }

            Path filePath = dataPath.resolve("factions.conf");

            if (!Files.exists(filePath)) {
                Files.createFile(filePath);

                configLoader = HoconConfigurationLoader.builder().setPath(filePath).build();
                precreate();
            } else {
                configLoader = HoconConfigurationLoader.builder().setPath(filePath).build();
                load();
            }
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    private void precreate() {
        load();
        getStorage().getNode("factions").setComment("This file stores all data about factions");

        getStorage().getNode("factions", "WarZone", "claims").setValue(new ArrayList<>());
        getStorage().getNode("factions", "WarZone", "members").setValue(new ArrayList<>());
        getStorage().getNode("factions", "WarZone", "power").setValue(9999);

        getStorage().getNode("factions", "SafeZone", "claims").setValue(new ArrayList<>());
        getStorage().getNode("factions", "SafeZone", "members").setValue(new ArrayList<>());
        getStorage().getNode("factions", "SafeZone", "power").setValue(9999);

        saveChanges();
    }

    @Override
    public boolean addOrUpdateFaction(Faction faction) {
        try {
            configNode.getNode("factions", faction.Name, "tag").setValue(TypeToken.of(Text.class), faction.Tag);
            configNode.getNode("factions", faction.Name, "leader").setValue(faction.Leader.name);
            //configNode.getNode("factions", faction.Name, "members").setValue(faction.Members);
            configNode.getNode("factions", faction.Name, "enemies").setValue(faction.Enemies);
            configNode.getNode("factions", faction.Name, "alliances").setValue(faction.Alliances);
            configNode.getNode("factions", faction.Name, "claims").setValue(faction.Claims);
            configNode.getNode("factions", faction.Name, "flags").setValue(faction.Flags);
            //configNode.getNode("factions", faction.Name, "groups").setValue(faction.groups);
            List<String> groups = new ArrayList<>();
            faction.groups.forEach((a, b) -> {
                groups.add(a);
                configNode.getNode("factions", faction.Name, "group", a, "inherit").setValue(b.perms.inherit);
                configNode.getNode("factions", faction.Name, "group", a, "nodes").setValue(b.perms.nodes);
                configNode.getNode("factions", faction.Name, "group", a, "priority").setValue(b.priority);
                configNode.getNode("factions", faction.Name, "group", a, "prefix").setValue(b.prefix);
            });
            configNode.getNode("factions", faction.Name, "groups").setValue(groups);

            List<String> members = new ArrayList<>();
            for(Player p : faction.Members){
                members.add(p.name);
                configNode.getNode("factions", faction.Name, "member", p.name, "inherit").setValue(p.inherit);
                configNode.getNode("factions", faction.Name, "member", p.name, "nodes").setValue(p.nodes);
            }
            configNode.getNode("factions", faction.Name, "members").setValue(members);

            if (faction.Home == null) {
                configNode.getNode("factions", faction.Name, "home").setValue(faction.Home);
            } else {
                configNode.getNode("factions", faction.Name, "home").setValue(faction.Home.WorldUUID.toString() + '|' + faction.Home.BlockPosition.toString());
            }

            FactionsCache.addOrUpdateFactionCache(faction);

            return saveChanges();
        } catch (Exception exception) {
            exception.printStackTrace();
        }

        return false;
    }

    @Override
    public boolean removeFaction(String factionName) {
        try {
            configNode.getNode("factions").removeChild(factionName);
            FactionsCache.removeFactionCache(factionName);
            return true;
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return false;
    }

    @Override
    public @Nullable Faction getFaction(String factionName) {
        try {
            Faction factionCache = FactionsCache.getFactionCache(factionName);
            if (factionCache != null) return factionCache;

            if (configNode.getNode("factions", factionName).getValue() == null) {
                return null;
            }

            Faction faction = createFactionObject(factionName);

            FactionsCache.addOrUpdateFactionCache(faction);

            return faction;
        } catch (Exception exception) {
            exception.printStackTrace();
        }

        //If it was not possible to get a faction then return null.
        return null;
    }

    private Faction createFactionObject(String factionName) {
        Text tag = getFactionTag(factionName);
        Player leader = getFactionLeader(factionName);
        FactionHome home = getFactionHome(factionName);
        List<Player> members = getFactionMembers(factionName);
        List<String> alliances = getFactionAlliances(factionName);
        List<String> enemies = getFactionEnemies(factionName);
        List<String> claims = getFactionClaims(factionName);
        Map<FactionMemberType, Map<FactionFlagTypes, Boolean>> flags = getFactionFlags(factionName);
        Map<String, Group> groups = getFactionGroups(factionName);

        Faction faction = new Faction(factionName, tag, leader, members, claims, alliances, enemies, home, flags, groups);

        //TODO: Refactor this code so that the power can be sent to the faction constructor like other parameters.
        //faction.Power = PowerManager.getFactionPower(faction); //Get power from all players in faction.

        return faction;
    }

    private Map<FactionMemberType, Map<FactionFlagTypes, Boolean>> getFactionFlags(String factionName) {
        Map<FactionMemberType, Map<FactionFlagTypes, Boolean>> flagMap = new LinkedHashMap<>();

        //Use TreeMap instead of LinkedHashMap to sort the map if needed.

        //TODO: Add map for recruit rank.

        Map<FactionFlagTypes, Boolean> leaderMap = new LinkedHashMap<>();
        Map<FactionFlagTypes, Boolean> officerMap = new LinkedHashMap<>();
        Map<FactionFlagTypes, Boolean> membersMap = new LinkedHashMap<>();
        Map<FactionFlagTypes, Boolean> recruitMap = new LinkedHashMap<>();
        Map<FactionFlagTypes, Boolean> allyMap = new LinkedHashMap<>();

        //Get leader flags
        boolean leaderUSE = configNode.getNode("factions", factionName, "flags", "LEADER", "USE").getBoolean(true);
        boolean leaderPLACE = configNode.getNode("factions", factionName, "flags", "LEADER", "PLACE").getBoolean(true);
        boolean leaderDESTROY = configNode.getNode("factions", factionName, "flags", "LEADER", "DESTROY").getBoolean(true);
        boolean leaderCLAIM = configNode.getNode("factions", factionName, "flags", "LEADER", "CLAIM").getBoolean(true);
        boolean leaderATTACK = configNode.getNode("factions", factionName, "flags", "LEADER", "ATTACK").getBoolean(true);
        boolean leaderINVITE = configNode.getNode("factions", factionName, "flags", "LEADER", "INVITE").getBoolean(true);

        //Get officer flags
        boolean officerUSE = configNode.getNode("factions", factionName, "flags", "OFFICER", "USE").getBoolean(true);
        boolean officerPLACE = configNode.getNode("factions", factionName, "flags", "OFFICER", "PLACE").getBoolean(true);
        boolean officerDESTROY = configNode.getNode("factions", factionName, "flags", "OFFICER", "DESTROY").getBoolean(true);
        boolean officerCLAIM = configNode.getNode("factions", factionName, "flags", "OFFICER", "CLAIM").getBoolean(true);
        boolean officerATTACK = configNode.getNode("factions", factionName, "flags", "LEADER", "ATTACK").getBoolean(true);
        boolean officerINVITE = configNode.getNode("factions", factionName, "flags", "OFFICER", "INVITE").getBoolean(true);

        //Get member flags
        boolean memberUSE = configNode.getNode("factions", factionName, "flags", "MEMBER", "USE").getBoolean(true);
        boolean memberPLACE = configNode.getNode("factions", factionName, "flags", "MEMBER", "PLACE").getBoolean(true);
        boolean memberDESTROY = configNode.getNode("factions", factionName, "flags", "MEMBER", "DESTROY").getBoolean(true);
        boolean memberCLAIM = configNode.getNode("factions", factionName, "flags", "MEMBER", "CLAIM").getBoolean(false);
        boolean memberATTACK = configNode.getNode("factions", factionName, "flags", "LEADER", "ATTACK").getBoolean(false);
        boolean memberINVITE = configNode.getNode("factions", factionName, "flags", "MEMBER", "INVITE").getBoolean(true);

        //Get recruit flags
        boolean recruitUSE = configNode.getNode("factions", factionName, "flags", "RECRUIT", "USE").getBoolean(true);
        boolean recruitPLACE = configNode.getNode("factions", factionName, "flags", "RECRUIT", "PLACE").getBoolean(true);
        boolean recruitDESTROY = configNode.getNode("factions", factionName, "flags", "RECRUIT", "DESTROY").getBoolean(true);
        boolean recruitCLAIM = configNode.getNode("factions", factionName, "flags", "RECRUIT", "CLAIM").getBoolean(false);
        boolean recruitATTACK = configNode.getNode("factions", factionName, "flags", "RECRUIT", "ATTACK").getBoolean(false);
        boolean recruitINVITE = configNode.getNode("factions", factionName, "flags", "RECRUIT", "INVITE").getBoolean(false);

        //Get ally flags
        boolean allyUSE = configNode.getNode("factions", factionName, "flags", "ALLY", "USE").getBoolean(true);
        boolean allyPLACE = configNode.getNode("factions", factionName, "flags", "ALLY", "PLACE").getBoolean(false);
        boolean allyDESTROY = configNode.getNode("factions", factionName, "flags", "ALLY", "DESTROY").getBoolean(false);

        leaderMap.put(FactionFlagTypes.USE, leaderUSE);
        leaderMap.put(FactionFlagTypes.PLACE, leaderPLACE);
        leaderMap.put(FactionFlagTypes.DESTROY, leaderDESTROY);
        leaderMap.put(FactionFlagTypes.CLAIM, leaderCLAIM);
        leaderMap.put(FactionFlagTypes.ATTACK, leaderATTACK);
        leaderMap.put(FactionFlagTypes.INVITE, leaderINVITE);

        officerMap.put(FactionFlagTypes.USE, officerUSE);
        officerMap.put(FactionFlagTypes.PLACE, officerPLACE);
        officerMap.put(FactionFlagTypes.DESTROY, officerDESTROY);
        officerMap.put(FactionFlagTypes.CLAIM, officerCLAIM);
        officerMap.put(FactionFlagTypes.ATTACK, officerATTACK);
        officerMap.put(FactionFlagTypes.INVITE, officerINVITE);

        membersMap.put(FactionFlagTypes.USE, memberUSE);
        membersMap.put(FactionFlagTypes.PLACE, memberPLACE);
        membersMap.put(FactionFlagTypes.DESTROY, memberDESTROY);
        membersMap.put(FactionFlagTypes.CLAIM, memberCLAIM);
        membersMap.put(FactionFlagTypes.ATTACK, memberATTACK);
        membersMap.put(FactionFlagTypes.INVITE, memberINVITE);

        recruitMap.put(FactionFlagTypes.USE, recruitUSE);
        recruitMap.put(FactionFlagTypes.PLACE, recruitPLACE);
        recruitMap.put(FactionFlagTypes.DESTROY, recruitDESTROY);
        recruitMap.put(FactionFlagTypes.CLAIM, recruitCLAIM);
        recruitMap.put(FactionFlagTypes.ATTACK, recruitATTACK);
        recruitMap.put(FactionFlagTypes.INVITE, recruitINVITE);

        allyMap.put(FactionFlagTypes.USE, allyUSE);
        allyMap.put(FactionFlagTypes.PLACE, allyPLACE);
        allyMap.put(FactionFlagTypes.DESTROY, allyDESTROY);

        flagMap.put(FactionMemberType.LEADER, leaderMap);
        flagMap.put(FactionMemberType.OFFICER, officerMap);
        flagMap.put(FactionMemberType.MEMBER, membersMap);
        flagMap.put(FactionMemberType.RECRUIT, recruitMap);
        flagMap.put(FactionMemberType.ALLY, allyMap);

        return flagMap;
    }

    private List<String> getFactionClaims(String factionName) {
        Object claimsObject = configNode.getNode("factions", factionName, "claims").getValue();

        if (claimsObject != null) {
            return (List<String>) claimsObject;
        } else {
            configNode.getNode("factions", factionName, "claims").setValue(new ArrayList<>());
            saveChanges();
            return new ArrayList<>();
        }
    }

    private List<String> getFactionEnemies(String factionName) {
        Object enemiesObject = configNode.getNode("factions", factionName, "enemies").getValue();

        if (enemiesObject != null) {
            return (List<String>) enemiesObject;
        } else {
            configNode.getNode("factions", factionName, "enemies").setValue(new ArrayList<>());
            saveChanges();
            return new ArrayList<>();
        }
    }

    private List<String> getFactionAlliances(String factionName) {
        Object alliancesObject = configNode.getNode("factions", factionName, "alliances").getValue();

        if (alliancesObject != null) {
            return (List<String>) alliancesObject;
        } else {
            configNode.getNode("factions", factionName, "alliances").setValue(new ArrayList<String>());
            saveChanges();
            return new ArrayList<>();
        }
    }

    private List<Player> getFactionMembers(String factionName) {
        Object membersObject = configNode.getNode("factions", factionName, "members").getValue();
        List<String> members;
        if (membersObject != null) {
            members = (List<String>) membersObject;
        } else {
            configNode.getNode("factions", factionName, "members").setValue(new ArrayList<String>());
            saveChanges();
            members = new ArrayList<>();
        }

        List<Player> memberList = new ArrayList<>();
        for (String player : members) {
            Object inherit = configNode.getNode("factions", factionName, "member", player, "inherit").getValue();
            Object nodes = configNode.getNode("factions", factionName, "member", player, "nodes").getValue();
            if (inherit == null) {
                configNode.getNode("factions", factionName, "member", player, "inherit").setValue(new ArrayList<String>());
                saveChanges();
                inherit = new ArrayList<String>();
            }
            if (nodes == null) {
                configNode.getNode("factions", factionName, "member", player, "nodes").setValue(new ArrayList<String>());
                saveChanges();
                nodes = new ArrayList<String>();
            }
            memberList.add(new Player(player, (List<String>) inherit, (List<String>) nodes));
        }

        return memberList;
    }

    private Map<String, Group> getFactionGroups(String factionName) {
        Object groupNameList = configNode.getNode("factions", factionName, "groups").getValue();
        List<String> groups;
        if (groupNameList != null) {
            groups = (List<String>) groupNameList;
        } else {
            configNode.getNode("factions", factionName, "groups").setValue(new ArrayList<String>());
            saveChanges();
            groups = new ArrayList<>();
        }

        Map<String, Group> groupMap = new HashMap<>();
        for (String group : groups) {
            Object inherit = configNode.getNode("factions", factionName, "group", group, "inherit").getValue();
            Object nodes = configNode.getNode("factions", factionName, "group", group, "nodes").getValue();
            Object priority = configNode.getNode("factions", factionName, "group", group, "priority").getValue();
            Object prefix = configNode.getNode("factions", factionName, "group", group, "prefix").getValue();
            if (inherit == null) {
                configNode.getNode("factions", factionName, "group", group, "inherit").setValue(new ArrayList<String>());
                saveChanges();
                inherit = new ArrayList<String>();
            }
            if (nodes == null) {
                configNode.getNode("factions", factionName, "group", group, "nodes").setValue(new ArrayList<String>());
                saveChanges();
                nodes = new ArrayList<String>();
            }
            if (priority == null) {
                configNode.getNode("factions", factionName, "group", group, "priority").setValue(0);
                saveChanges();
                priority = 0;
            }
            if (prefix == null) {
                configNode.getNode("factions", factionName, "group", group, "prefix").setValue("SAVE-ERROR");
                saveChanges();
                prefix = "SAVE-ERROR";
            }
            groupMap.put(group, new Group(group, (String) prefix, (int) priority, (List<String>) inherit, (List<String>) nodes));
        }

        return groupMap;
    }

    private FactionHome getFactionHome(String factionName) {
        Object homeObject = configNode.getNode("factions", factionName, "home").getValue();

        if (homeObject != null) {
            if (String.valueOf(homeObject).equals("")) {
                return null;
            } else return new FactionHome(String.valueOf(homeObject));

        } else {
            configNode.getNode("factions", factionName, "home").setValue("");
            saveChanges();
            return null;
        }
    }

    private Player getFactionLeader(String factionName) {
        Object name = configNode.getNode("factions", factionName, "leader").getValue();

        if (name != null) {
            List<Player> members = getFactionMembers(factionName);
            for (Player p : members) {
                if (p.name.equals(name)) {
                    return p;
                }
            }
        }
        configNode.getNode("factions", factionName, "leader").setValue("");
        saveChanges();
        return null;

    }

    private Text getFactionTag(String factionName) {
        Object tagObject = null;
        try {
            tagObject = configNode.getNode("factions", factionName, "tag").getValue(TypeToken.of(Text.class));
        } catch (ObjectMappingException e) {
            e.printStackTrace();
        }

        if (tagObject != null) {
            return (Text) tagObject;
        } else {
            try {
                configNode.getNode("factions", factionName, "tag").setValue(TypeToken.of(Text.class), Text.of(""));
            } catch (ObjectMappingException e) {
                e.printStackTrace();
            }
            saveChanges();
            return Text.of("");
        }
    }

    @Override
    public List<Faction> getFactions() {
        List<Faction> factionList = FactionsCache.getFactionsList();

        final Set<Object> keySet = getStorage().getNode("factions").getChildrenMap().keySet();

        for (Object object : keySet) {
            if (object instanceof String) {
                if (factionList.stream().noneMatch(x -> x.Name.equals(String.valueOf(object)))) {
                    Faction faction = createFactionObject(String.valueOf(object));
                    FactionsCache.addOrUpdateFactionCache(faction);
                }
            }
        }

        return FactionsCache.getFactionsList();
    }

    @Override
    public void load() {
        try {
            configNode = configLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean saveChanges() {
        try {
            configLoader.save(configNode);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    private CommentedConfigurationNode getStorage() {
        return configNode;
    }
}
