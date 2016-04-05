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

import java.util.List;

import nl.riebie.mcclans.ClansImpl;
import nl.riebie.mcclans.clan.ClanImpl;
import nl.riebie.mcclans.clan.RankFactory;
import nl.riebie.mcclans.clan.RankImpl;
import nl.riebie.mcclans.config.Config;
import nl.riebie.mcclans.player.ClanPlayerImpl;

public abstract class DataSaver {

    private List<ClanImpl> retrievedClans;
    private List<ClanPlayerImpl> retrievedClanPlayers;

    public boolean save() {
        retrievedClans = ClansImpl.getInstance().getClanImpls();
        retrievedClanPlayers = ClansImpl.getInstance().getClanPlayerImpls();

        return continueSave();
    }

    public boolean save(List<ClanImpl> clans, List<ClanPlayerImpl> clanPlayers) {
        retrievedClans = clans;
        retrievedClanPlayers = clanPlayers;

        return continueSave();
    }

    public boolean continueSave() {
        try {
            saveStarted();
            storeClans();
            storeClanPlayers();
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

    private void storeClans() throws Exception {
        for (ClanImpl clan : retrievedClans) {
            saveClan(clan);
            int clanID = clan.getID();
            List<RankImpl> ranks = clan.getRankImpls();
            List<ClanImpl> allies = clan.getAlliesImpl();
            for (RankImpl rank : ranks) {
                if (rank.getID() != RankFactory.getOwnerID() && rank.getID() != RankFactory.getRecruitID())
                    saveRank(clanID, rank);
            }
            for (ClanImpl ally : allies) {
                saveClanAlly(clanID, ally.getID());
            }
        }
    }

    private void storeClanPlayers() throws Exception {
        for (ClanPlayerImpl clanPlayer : retrievedClanPlayers) {
            saveClanPlayer(clanPlayer);
        }
    }

    protected abstract void saveClan(ClanImpl clan) throws Exception;

    protected abstract void saveClanPlayer(ClanPlayerImpl cp) throws Exception;

    protected abstract void saveRank(int clanID, RankImpl rank) throws Exception;

    protected abstract void saveClanAlly(int clanID, int clanIDAlly) throws Exception;

    protected abstract void saveStarted() throws Exception;

    protected abstract void saveFinished() throws Exception;

    protected abstract void saveCancelled();
}
