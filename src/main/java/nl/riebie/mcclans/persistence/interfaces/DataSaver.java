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

import io.github.aquerr.eaglefactions.caching.FactionsCache;
import io.github.aquerr.eaglefactions.config.Config;
import io.github.aquerr.eaglefactions.entities.*;

import java.util.List;

public abstract class DataSaver {

    private List<Faction> retrievedFactions;
    private List<FactionRelation> retrievedRelations;

    public boolean save() {
        retrievedFactions = FactionsCache.getInstance().getFactions();
        retrievedRelations = FactionsCache.getInstance().getRelations();
        return continueSave();
    }

    public boolean save(List<Faction> factions, List<FactionRelation> relations) {
        retrievedFactions = factions;
        retrievedRelations = relations;

        return continueSave();
    }

    public boolean continueSave() {
        if (!Config.getBoolean(Config.SKIP_SAVE)) {
            return true;
        }
        try {
            saveStarted();
            storeFactions();
            storeFactionRelations();
            saveFinished();
            return true;
        } catch (Exception e) {
            saveCancelled();
            if (Config.getBoolean(Config.DEBUGGING)) {
                e.printStackTrace();
            }
            return false;
        }
    }

    private void storeFactionRelations() throws Exception {
        for (FactionRelation relation : retrievedRelations) {
            saveFactionRelation(relation);
        }
    }

    private void storeFactions() throws Exception {
        for (Faction faction : retrievedFactions) {
            saveFaction(faction);
            for (Group group : faction.groups.values()) {
                saveGroup(faction.name, group);
            }
            for (FactionPlayer player : faction.members) {
                savePlayer(player);
            }
            for (FactionClaim claim : faction.claims) {
                saveFactionClaim(claim);
            }
        }
    }


    protected abstract void saveFaction(Faction faction) throws Exception;

    protected abstract void savePlayer(FactionPlayer player) throws Exception;

    protected abstract void saveGroup(String factionName, Group group) throws Exception;

    protected abstract void saveFactionRelation(FactionRelation relation) throws Exception;

    protected abstract void saveFactionClaim(FactionClaim claim) throws Exception;

    protected abstract void saveStarted() throws Exception;

    protected abstract void saveFinished() throws Exception;

    protected abstract void saveCancelled();
}
