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

package nl.riebie.mcclans.persistence.implementations;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import nl.riebie.mcclans.MCClans;
import nl.riebie.mcclans.persistence.exceptions.GetDataVersionFailedException;
import nl.riebie.mcclans.persistence.interfaces.DataLoader;
import nl.riebie.mcclans.persistence.pojo.*;
import nl.riebie.mcclans.persistence.exceptions.FlatFileVersionsNotEqualException;
import nl.riebie.mcclans.persistence.exceptions.WrappedDataException;
import nl.riebie.mcclans.persistence.upgrade.versions.JsonUpgrade2;
import nl.riebie.mcclans.persistence.upgrade.interfaces.DataUpgrade;
import nl.riebie.mcclans.utils.FileUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

public class JsonLoader extends DataLoader {

    private static final File recentDataFolder = new File(MCClans.getPlugin().getDataFolder(), "recent");
    private static final File loadDataFolder = new File(MCClans.getPlugin().getDataFolder(), "load");

    private Gson gson = new Gson();

    private JsonReader clansReader;
    private JsonReader ranksReader;
    private JsonReader clanPlayersReader;
    private JsonReader alliesReader;

    public static boolean recentFilesPresent() {
        File clansFile = new File(recentDataFolder, "clans.json");
        File clanPlayersFile = new File(recentDataFolder, "clanPlayers.json");
        File ranksFile = new File(recentDataFolder, "ranks.json");
        File alliesFile = new File(recentDataFolder, "allies.json");

        if (clansFile.exists() && clanPlayersFile.exists() && ranksFile.exists() && alliesFile.exists()) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean loadFilesPresent() {
        loadDataFolder.mkdirs();
        File loadClansFile = new File(loadDataFolder, "clans.json");
        File loadClanPlayersFile = new File(loadDataFolder, "clanPlayers.json");
        File loadRanksFile = new File(loadDataFolder, "ranks.json");
        File loadAlliesFile = new File(loadDataFolder, "allies.json");

        if (loadClansFile.exists() && loadClanPlayersFile.exists() && loadRanksFile.exists() && loadAlliesFile.exists()) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected boolean initialize() {
        recentDataFolder.mkdirs();
        loadDataFolder.mkdirs();

        File loadClansFile = new File(loadDataFolder, "clans.json");
        File loadClanPlayersFile = new File(loadDataFolder, "clanPlayers.json");
        File loadRanksFile = new File(loadDataFolder, "ranks.json");
        File loadAlliesFile = new File(loadDataFolder, "allies.json");

        File clansFile = new File(recentDataFolder, "clans.json");
        File clanPlayersFile = new File(recentDataFolder, "clanPlayers.json");
        File ranksFile = new File(recentDataFolder, "ranks.json");
        File alliesFile = new File(recentDataFolder, "allies.json");

        if (loadFilesPresent()) {
            MCClans.getPlugin().getLogger().info("Found data in 'load' folder. Loading this data instead", true);
            try {
                FileUtils.moveFile(loadClansFile, clansFile);
                FileUtils.moveFile(loadClanPlayersFile, clanPlayersFile);
                FileUtils.moveFile(loadRanksFile, ranksFile);
                FileUtils.moveFile(loadAlliesFile, alliesFile);

                loadClansFile.delete();
                loadClanPlayersFile.delete();
                loadRanksFile.delete();
                loadAlliesFile.delete();
            } catch (Exception e) {
                throw new WrappedDataException(e);
            }
        }

        boolean recentFilesPresent = recentFilesPresent();
        if (recentFilesPresent) {
            try {
                clansReader = new JsonReader(new FileReader(clansFile));
                ranksReader = new JsonReader(new FileReader(ranksFile));
                clanPlayersReader = new JsonReader(new FileReader(clanPlayersFile));
                alliesReader = new JsonReader(new FileReader(alliesFile));
            } catch (FileNotFoundException e) {
                throw new WrappedDataException(e);
            }
        }

        return recentFilesPresent;
    }

    @Override
    protected int getDataVersion() {
        try {
            File clansFile = new File(recentDataFolder, "clans.json");
            File clanPlayersFile = new File(recentDataFolder, "clanPlayers.json");
            File ranksFile = new File(recentDataFolder, "ranks.json");
            File alliesFile = new File(recentDataFolder, "allies.json");

            JsonReader clansReader = new JsonReader(new FileReader(clansFile));
            JsonReader ranksReader = new JsonReader(new FileReader(ranksFile));
            JsonReader clanPlayersReader = new JsonReader(new FileReader(clanPlayersFile));
            JsonReader alliesReader = new JsonReader(new FileReader(alliesFile));

            VersionPojo clans = gson.fromJson(clansReader, VersionPojo.class);
            VersionPojo ranks = gson.fromJson(ranksReader, VersionPojo.class);
            VersionPojo clanPlayers = gson.fromJson(clanPlayersReader, VersionPojo.class);
            VersionPojo allies = gson.fromJson(alliesReader, VersionPojo.class);

            clansReader.close();
            ranksReader.close();
            clanPlayersReader.close();
            alliesReader.close();

            if (clans.dataVersion == ranks.dataVersion && ranks.dataVersion == clanPlayers.dataVersion && clanPlayers.dataVersion == allies.dataVersion) {
                if (clans.dataVersion == -1) {
                    throw new GetDataVersionFailedException("dataVersion still has the default value of -1");
                }

                return clans.dataVersion;
            } else {
                String versions = "clans: " + clans.dataVersion + ", ranks: " + ranks.dataVersion + ", clanPlayers: " + clanPlayers.dataVersion + ", allies: " + allies.dataVersion;
                throw new FlatFileVersionsNotEqualException(versions);
            }
        } catch (IOException e) {
            throw new WrappedDataException(e);
        }
    }

    @Override
    protected List<DataUpgrade> getDataUpgrades(List<DataUpgrade> dataUpgrades) {
        dataUpgrades.add(new JsonUpgrade2());
        return dataUpgrades;
    }

    @Override
    protected void loadClans() {
        RootPojo<ClanPojo> clans = gson.fromJson(clansReader, new TypeToken<RootPojo<ClanPojo>>() {
        }.getType());

        for (ClanPojo clan : clans.list) {
            super.loadedClan(clan.clanID, clan.clanTag, clan.clanName, clan.ownerID, clan.tagColorId, clan.allowAllyInvites, clan.ffProtection, clan.creationTime, clan.homeWorld,
                    clan.homeX, clan.homeY, clan.homeZ, clan.homeYaw, clan.homePitch, clan.homeSetTimes, clan.homeSetTimeStamp, clan.bankId);
        }

        try {
            clansReader.close();
        } catch (IOException e) {
            e.printStackTrace();
            // No runtime crash needed here
        }
    }

    @Override
    protected void loadRanks() {
        RootPojo<RankPojo> ranks = gson.fromJson(ranksReader, new TypeToken<RootPojo<RankPojo>>() {
        }.getType());

        for (RankPojo rank : ranks.list) {
            super.loadedRank(rank.rankID, rank.clanID, rank.rankName, rank.permissions, rank.changeable);
        }

        try {
            ranksReader.close();
        } catch (IOException e) {
            e.printStackTrace();
            // No runtime crash needed here
        }
    }

    @Override
    protected void loadClanPlayers() {
        RootPojo<ClanPlayerPojo> clanPlayers = gson.fromJson(clanPlayersReader, new TypeToken<RootPojo<ClanPlayerPojo>>() {
        }.getType());

        for (ClanPlayerPojo clanPlayer : clanPlayers.list) {
            super.loadedClanPlayer(clanPlayer.clanPlayerID, clanPlayer.uuidMostSigBits, clanPlayer.uuidLeastSigBits, clanPlayer.playerName, clanPlayer.clanID, clanPlayer.rankID, clanPlayer.killsHigh, clanPlayer.killsMedium,
                    clanPlayer.killsLow, clanPlayer.deathsHigh, clanPlayer.deathsMedium, clanPlayer.deathsLow, clanPlayer.ffProtection, clanPlayer.lastOnlineTime);
        }

        try {
            clanPlayersReader.close();
        } catch (IOException e) {
            e.printStackTrace();
            // No runtime crash needed here
        }
    }

    @Override
    protected void loadAllies() {
        RootPojo<AllyPojo> allies = gson.fromJson(alliesReader, new TypeToken<RootPojo<AllyPojo>>() {
        }.getType());

        for (AllyPojo ally : allies.list) {
            super.loadedAlly(ally.clanID, ally.clanIDAlly);
        }

        try {
            alliesReader.close();
        } catch (IOException e) {
            e.printStackTrace();
            // No runtime crash needed here
        }
    }
}
