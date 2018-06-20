package io.github.aquerr.eaglefactions.storage;

import com.google.common.reflect.TypeToken;
import io.github.aquerr.eaglefactions.caching.FactionsCache;
import io.github.aquerr.eaglefactions.entities.*;
import io.github.aquerr.eaglefactions.entities.FactionPlayer;
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

@Deprecated
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
            configNode.getNode("factions", faction.name, "tag").setValue(TypeToken.of(Text.class), faction.Tag);
            configNode.getNode("factions", faction.name, "leader").setValue(faction.Leader.uuid);
            //configNode.getNode("factions", faction.uuid, "claims").setValue(faction.claims);
            List<String> groups = new ArrayList<>();
            faction.groups.forEach((a, b) -> {
                groups.add(a);
                configNode.getNode("factions", faction.name, "group", a, "inherit").setValue(b.perms.inherit);
                configNode.getNode("factions", faction.name, "group", a, "nodes").setValue(b.perms.nodes);
                configNode.getNode("factions", faction.name, "group", a, "priority").setValue(b.priority);
                configNode.getNode("factions", faction.name, "group", a, "prefix").setValue(b.prefix);
            });
            configNode.getNode("factions", faction.name, "groups").setValue(groups);

            List<String> members = new ArrayList<>();
            for (FactionPlayer p : faction.members) {
                members.add(p.uuid);
                configNode.getNode("factions", faction.name, "member", p.uuid, "inherit").setValue(p.inherit);
                configNode.getNode("factions", faction.name, "member", p.uuid, "nodes").setValue(p.nodes);
            }
            configNode.getNode("factions", faction.name, "members").setValue(members);

            if (faction.Home == null) {
                configNode.getNode("factions", faction.name, "home").setValue(faction.Home);
            } else {
                configNode.getNode("factions", faction.name, "home").setValue(faction.Home.worldUUID.toString() + '|' + faction.Home.blockPosition.toString());
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
        FactionPlayer leader = getFactionLeader(factionName);
        FactionHome home = getFactionHome(factionName);
        List<FactionPlayer> members = getFactionMembers(factionName);
//        List<String> claims = getFactionClaims(factionName);
        Map<String, Group> groups = getFactionGroups(factionName);

        Faction faction = new Faction(factionName, tag, leader, members, new ArrayList<>(), home, groups, 0);

        //TODO: Refactor this code so that the power can be sent to the faction constructor like other parameters.
        //faction.Power = PowerManager.getFactionPower(faction); //Get power from all players in faction.

        return faction;
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

    private List<FactionPlayer> getFactionMembers(String factionName) {
        Object membersObject = configNode.getNode("factions", factionName, "members").getValue();
        List<String> members;
        if (membersObject != null) {
            members = (List<String>) membersObject;
        } else {
            configNode.getNode("factions", factionName, "members").setValue(new ArrayList<String>());
            saveChanges();
            members = new ArrayList<>();
        }

        List<FactionPlayer> memberList = new ArrayList<>();
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
            memberList.add(new FactionPlayer(player, "", factionName, (List<String>) inherit, (List<String>) nodes));
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

    private FactionPlayer getFactionLeader(String factionName) {
        Object name = configNode.getNode("factions", factionName, "leader").getValue();

        if (name != null) {
            List<FactionPlayer> members = getFactionMembers(factionName);
            for (FactionPlayer p : members) {
                if (p.uuid.equals(name)) {
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
                if (factionList.stream().noneMatch(x -> x.name.equals(String.valueOf(object)))) {
                    Faction faction = createFactionObject(String.valueOf(object));
                    FactionsCache.addOrUpdateFactionCache(faction);
                }
            }
        }

        return FactionsCache.getFactionsList();
    }

    @Override
    public void updateRelations(List<FactionRelation> relations) {
        List<String> saveData = new ArrayList<>();
        for (FactionRelation relation : relations) {
            saveData.add(relation.toString());
        }
        configNode.getNode("relations").getValue(saveData);
        saveChanges();
    }

    @Override
    public List<FactionRelation> getFactionRelations() {
        List<FactionRelation> relations = new ArrayList<>();
        Object relationList = configNode.getNode("relations").getValue();
        if (relationList != null) {
            for (String s : (List<String>) relationList) {
                try {
                    relations.add(new FactionRelation(s));
                } catch (Exception ignored) {
                }
            }
            return relations;
        } else {
            configNode.getNode("relations").setValue(new ArrayList<>());
            saveChanges();
            return relations;
        }
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
