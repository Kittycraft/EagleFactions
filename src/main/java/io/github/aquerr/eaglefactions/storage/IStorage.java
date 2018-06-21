package io.github.aquerr.eaglefactions.storage;

import io.github.aquerr.eaglefactions.entities.Faction;
import io.github.aquerr.eaglefactions.entities.FactionRelation;

import java.util.List;

@Deprecated
public interface IStorage {
    boolean addOrUpdateFaction(Faction faction);

    boolean removeFaction(String factionName);

    Faction getFaction(String factionName);

    List<Faction> getFactions();

    List<FactionRelation> getFactionRelations();

    void updateRelations(List<FactionRelation> relations);

    void load();
}
