package io.github.aquerr.eaglefactions.caching;

import com.flowpowered.math.vector.Vector3i;
import io.github.aquerr.eaglefactions.entities.Faction;
import io.github.aquerr.eaglefactions.entities.FactionClaim;
import io.github.aquerr.eaglefactions.entities.FactionPlayer;
import io.github.aquerr.eaglefactions.entities.FactionRelation;

import java.util.*;

public class FactionsCache {

    private List<Faction> factionsList = new LinkedList<>();
    private Map<String, Faction> factionNameMap = new HashMap<>();
    private Map<String, Faction> playerUUIDMap = new HashMap<>();
    private Map<UUID, Map<Vector3i, FactionClaim>> claims = new HashMap<>();
    private List<FactionRelation> relations = new LinkedList<>();
    private boolean requireSave = false;

    private static FactionsCache instance;


    private FactionsCache() {
        //Request DataLoader
    }

    public static FactionsCache getInstance() {
        if (instance == null) {
            instance = new FactionsCache();
        }
        return instance;
    }

    public List<Faction> getFactions() {
        return factionsList;
    }

    public List<FactionRelation> getRelations() {
        return relations;
    }

    public void addOrSetClaim(FactionClaim claim){
        if(!claims.containsKey(claim.world)){
            claims.put(claim.world, new HashMap<>());
        }
        claims.get(claim.world).put(claim.chunk, claim);
    }

    public Optional<FactionClaim> removeClaim(UUID world, Vector3i chunk){
        if(claims.containsKey(world)){
            return Optional.ofNullable(claims.get(world).remove(chunk));
        }
        return Optional.empty();
    }

    public Optional<FactionClaim> getClaim(UUID world, Vector3i chunk) {
        return Optional.ofNullable(claims.getOrDefault(world, new HashMap<>()).get(chunk));
    }

    public void addFaction(Faction faction) {
        factionsList.add(faction);
        factionNameMap.put(faction.name.toLowerCase(), faction);
    }

    public void removePlayer(UUID uuid){
        playerUUIDMap.remove(uuid);
    }

    public void updatePlayer(String player, String newFaction){
        playerUUIDMap.put(player, getFaction(newFaction).get());
    }

    public void removeFaction(String factionName) {
        final String faction = factionName.toLowerCase();
        Optional<Faction> optionalFaction = factionsList.stream().filter(x -> x.name.equals(faction)).findFirst();
        if (optionalFaction.isPresent()) {
            factionsList.remove(optionalFaction.get());
            factionNameMap.remove(faction);
            for(FactionPlayer player : optionalFaction.get().members){
                playerUUIDMap.remove(player.uuid);
            }
        }
    }

    public Optional<Faction> getFaction(String factionName) {
        if(!factionNameMap.containsKey(factionName.toLowerCase())){
            return Optional.empty();
        }
        return Optional.of(factionNameMap.get(factionName.toLowerCase()));
    }

    public Optional<Faction> getFactionByPlayer(String uuid) {
        if(!playerUUIDMap.containsKey(uuid)){
            return Optional.empty();
        }
        return Optional.of(playerUUIDMap.get(uuid));
    }

    public Optional<Faction> getFactionByPlayer(UUID uuid) {
        return getFactionByPlayer(uuid.toString());
    }

    public Set<String> getFactionNames(){
        return factionNameMap.keySet();
    }

    public Optional<Faction> getFactionByChunk(UUID world, Vector3i chunk){
        Optional<FactionClaim> claim = getClaim(world, chunk);
        if(claim.isPresent()){
            return getFaction(claim.get().faction);
        }
        return Optional.empty();
    }

}
