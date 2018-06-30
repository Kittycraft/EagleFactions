/*
 * Copyright (c) 2016 riebie, Kippers <https://bitbucket.org/Kippers/mcclans-core-sponge>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package nl.riebie.mcclans.persistence.interfaces;

import com.flowpowered.math.vector.Vector3i;
import io.github.aquerr.eaglefactions.EagleFactions;
import io.github.aquerr.eaglefactions.caching.FactionsCache;
import io.github.aquerr.eaglefactions.entities.*;
import nl.riebie.mcclans.persistence.DatabaseHandler;
import nl.riebie.mcclans.persistence.exceptions.DataVersionTooHighException;
import nl.riebie.mcclans.persistence.upgrade.DataUpgradeComparator;
import nl.riebie.mcclans.persistence.upgrade.interfaces.DataUpgrade;

import java.util.*;

public abstract class DataLoader {

    private FactionsCache cache = FactionsCache.getInstance();

    public boolean load() {
        if (initialize()) {
            upgradeIfNeeded();

            loadFactions();
            loadGroups();
            loadPlayers();
            loadRelations();
            loadClaims();

            return true;
        } else {
            return false;
        }
    }

    protected abstract boolean initialize();

    protected abstract int getDataVersion();

    protected abstract List<DataUpgrade> getDataUpgrades(List<DataUpgrade> dataUpgrades);

    private void upgradeIfNeeded() {
        int dataVersion = getDataVersion();
        EagleFactions.getPlugin().getLogger().info("Detected data version " + dataVersion, true);
        if (DatabaseHandler.CURRENT_DATA_VERSION > dataVersion) {
            EagleFactions.getPlugin().getLogger().info("Starting data upgrade from version " + dataVersion + " to " + DatabaseHandler.CURRENT_DATA_VERSION + "...", true);

            List<DataUpgrade> dataUpgrades = new ArrayList<>();
            for (DataUpgrade dataUpgrade : getDataUpgrades(new ArrayList<>())) {
                if (dataUpgrade.getVersion() > dataVersion && dataUpgrade.getVersion() <= DatabaseHandler.CURRENT_DATA_VERSION) {
                    dataUpgrades.add(dataUpgrade);
                }
            }
            // Perform upgrades in order
            Collections.sort(dataUpgrades, new DataUpgradeComparator());
            for (DataUpgrade dataUpgrade : dataUpgrades) {
                dataUpgrade.upgrade();
                EagleFactions.getPlugin().getLogger().info("Finished data upgrade to version " + dataUpgrade.getVersion(), true);
            }
        } else if (DatabaseHandler.CURRENT_DATA_VERSION < dataVersion) {
            throw new DataVersionTooHighException(dataVersion, DatabaseHandler.CURRENT_DATA_VERSION);
        }
    }

    protected abstract void loadFactions();

    protected abstract void loadGroups();

    protected abstract void loadPlayers();

    protected abstract void loadRelations();

    protected abstract void loadClaims();

    protected void loadedFaction(String name, String owner, FactionHome home, long creationTime) {
        cache.addFaction(new Faction(name, owner, new ArrayList<>(), new ArrayList<>(), home, new HashMap<>(), creationTime));
    }

    protected void loadedGroup(String factionName, String group, int priority, List<String> parents, List<String> nodes) {
        Optional<Faction> faction = cache.getFaction(factionName);
        if(faction.isPresent()){
            faction.get().groups.put(group, new Group(group, priority, parents, nodes));
        }
    }

    protected void loadedPlayer(String name, String uuid, String factionName, List<String> parents, List<String> nodes, long lastOnline) {
        Optional<Faction> faction = cache.getFaction(factionName);
        if(faction.isPresent()){
            faction.get().members.add(new FactionPlayer(uuid, name, factionName, parents, nodes, lastOnline));
            cache.updatePlayer(uuid, factionName);
        }
    }

    protected void loadedRelation(String factionA, String factionB, RelationType type) {
        cache.getRelations().add(new FactionRelation(factionA, factionB, type));
    }

    protected void loadedClaim(Vector3i chunk, UUID world, String faction) {
        cache.addOrSetClaim(new FactionClaim(chunk, world, faction));
    }

    //For json loader
    protected void loadedClaim(FactionClaim claim) {
        cache.addOrSetClaim(claim);
    }

}