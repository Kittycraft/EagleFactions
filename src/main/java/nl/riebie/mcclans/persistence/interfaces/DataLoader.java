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

import nl.riebie.mcclans.ClansImpl;
import nl.riebie.mcclans.MCClans;
import nl.riebie.mcclans.clan.ClanImpl;
import nl.riebie.mcclans.clan.RankFactory;
import nl.riebie.mcclans.clan.RankImpl;
import nl.riebie.mcclans.config.Config;
import nl.riebie.mcclans.persistence.DatabaseHandler;
import nl.riebie.mcclans.persistence.exceptions.DataVersionTooHighException;
import nl.riebie.mcclans.persistence.upgrade.DataUpgradeComparator;
import nl.riebie.mcclans.persistence.upgrade.interfaces.DataUpgrade;
import nl.riebie.mcclans.player.ClanPlayerImpl;
import nl.riebie.mcclans.player.LastOnlineImpl;
import nl.riebie.mcclans.utils.Utils;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.world.Location;

import java.util.*;
import java.util.Map.Entry;

public abstract class DataLoader {

    private Map<Integer, ClanImpl> clans = new HashMap<Integer, ClanImpl>();
    private Map<Integer, ClanPlayerImpl> clanPlayers = new HashMap<Integer, ClanPlayerImpl>();
    private Map<Integer, RankImpl> ranks = new HashMap<Integer, RankImpl>();

    private Map<Integer, Integer> clanOwners = new HashMap<Integer, Integer>();

    private List<ClanPlayerImpl> markedClanPlayers = new ArrayList<ClanPlayerImpl>();

    private int highestUsedClanID = -1;
    private int highestUsedClanPlayerID = -1;
    private int highestUsedRankID = -1;

    public boolean load() {
        if (initialize()) {
            upgradeIfNeeded();

            loadClans();
            loadRanks();
            loadClanPlayers();
            loadAllies();

            setOwners();

            ClansImpl.getInstance().setHighestUsedClanID(highestUsedClanID);
            ClansImpl.getInstance().setHighestUsedClanPlayerID(highestUsedClanPlayerID);
            ClansImpl.getInstance().setHighestUsedRankID(highestUsedRankID);

            setResults();

            if (markedClanPlayers.size() != 0) {
                MCClans.getPlugin().getLogger().info(
                        "Scheduled " + markedClanPlayers.size() + " ClanPlayer(s) for deletion due to inactivity (>"
                                + Config.getInteger(Config.REMOVE_INACTIVE_CLAN_PLAYERS_AFTER_DAYS) + "d)", false);
            }
            // ClansImpl.getInstance().fillThisDummyDummy();
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
        MCClans.getPlugin().getLogger().info("Detected data version " + dataVersion, true);
        if (DatabaseHandler.CURRENT_DATA_VERSION > dataVersion) {
            MCClans.getPlugin().getLogger().info("Starting data upgrade from version " + dataVersion + " to " + DatabaseHandler.CURRENT_DATA_VERSION + "...", true);

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
                MCClans.getPlugin().getLogger().info("Finished data upgrade to version " + dataUpgrade.getVersion(), true);
            }
        } else if (DatabaseHandler.CURRENT_DATA_VERSION < dataVersion) {
            throw new DataVersionTooHighException(dataVersion, DatabaseHandler.CURRENT_DATA_VERSION);
        }
    }

    protected abstract void loadClans();

    protected abstract void loadRanks();

    protected abstract void loadClanPlayers();

    protected abstract void loadAllies();

    protected void loadedClan(int clanID, String clanTag, String clanName, int ownerID, String tagColorId, boolean allowAllyInvites,
                              boolean ffProtection, long creationTime, String homeWorld, double homeX, double homeY, double homeZ, float homeYaw, float homePitch,
                              int homeSetTimes, long homeLastSetTimeStamp, String bankId) {
        ClanImpl clan = new ClanImpl.Builder(clanID, clanTag, clanName).tagColor(Utils.getTextColorById(tagColorId, Config.getColor(Config.CLAN_TAG_DEFAULT_COLOR))).acceptAllyInvites(allowAllyInvites)
                .ffProtection(ffProtection).creationTime(creationTime).homeSetTimes(homeSetTimes).homeLastSetTimeStamp(homeLastSetTimeStamp).bankId(bankId).build();
        if (homeWorld != null && Sponge.getServer().getWorld(UUID.fromString(homeWorld)).isPresent()) {
            // TODO SPONGE homeYaw, homePitch
            clan.setHomeInternal(new Location<>(Sponge.getServer().getWorld(UUID.fromString(homeWorld)).get(), homeX, homeY, homeZ));
        }

        clan.addRank(RankFactory.getInstance().createOwner());
        clan.addRank(RankFactory.getInstance().createRecruit());

        clanOwners.put(clanID, ownerID);
        clans.put(clanID, clan);

        checkHighestUsedClanID(clanID);
    }

    protected void loadedRank(int rankID, int clanID, String rankName, String permissions, boolean changeable) {

        RankImpl.Builder builder = new RankImpl.Builder(rankID, rankName);
        if (!changeable) {
            builder.unchangeable();
        }
        RankImpl rank = builder.build();

        rank.setPermissions(new ArrayList<>(Arrays.asList(permissions.split(","))));

        clans.get(clanID).addRank(rank);
        ranks.put(rankID, rank);

        checkHighestUsedRankID(rankID);
    }

    protected void loadedClanPlayer(int clanPlayerID, long uuidMostSigBits, long uuidLeastSigBits, String playerName, int clanID, int rankID,
                                    int killsHigh, int killsMedium, int killsLow, int deathsHigh, int deathsMedium, int deathsLow, boolean ffProtection, long lastOnlineTime) {
        ClanImpl clan = null;
        RankImpl rankImpl = null;

        if (clanID != -1 && rankID != -1) {
            clan = clans.get(clanID);
            if (rankID == RankFactory.getOwnerID()) {
                rankImpl = RankFactory.getInstance().createOwner();
            } else if (rankID == RankFactory.getRecruitID()) {
                rankImpl = RankFactory.getInstance().createRecruit();
            } else {
                rankImpl = ranks.get(rankID);
            }
        }

        ClanPlayerImpl cp = new ClanPlayerImpl.Builder(clanPlayerID, new UUID(uuidMostSigBits, uuidLeastSigBits), playerName).clan(clan)
                .rank(rankImpl).kills(killsHigh, killsMedium, killsLow).deaths(deathsHigh, deathsMedium, deathsLow).ffProtection(ffProtection)
                .lastOnline(new LastOnlineImpl(lastOnlineTime)).build();

        clanPlayers.put(clanPlayerID, cp);
        if (clan != null) {
            clan.addMember(cp);
        }

        markInactiveClanPlayer(cp);

        checkHighestUsedClanPlayerID(clanPlayerID);
    }

    protected void loadedAlly(int clanID, int clanIDAlly) {
        ClanImpl clan = clans.get(clanID);
        ClanImpl ally = clans.get(clanIDAlly);
        if (clan != null && ally != null) {
            clan.addAlly(ally);
        } else {
            MCClans.getPlugin().getLogger().warn("Cannot load ally relation, could not find clan. Clan: " + clanID + " ally: " + clanIDAlly, false);
        }
    }

    private void setOwners() {
        for (Entry<Integer, Integer> entry : clanOwners.entrySet()) {
            ClanImpl clan = clans.get(entry.getKey());
            ClanPlayerImpl clanPlayer = clanPlayers.get(entry.getValue());
            clan.setLoadedOwner(clanPlayer);
        }
    }

    private void setResults() {
        Map<UUID, ClanPlayerImpl> finishedClanPlayers = new HashMap<UUID, ClanPlayerImpl>();
        Map<String, ClanImpl> finishedClans = new HashMap<String, ClanImpl>();

        for (ClanImpl clan : clans.values()) {
            finishedClans.put(clan.getTag().toLowerCase(), clan);
        }
        for (ClanPlayerImpl clanPlayer : clanPlayers.values()) {
            finishedClanPlayers.put(clanPlayer.getUUID(), clanPlayer);
        }

        ClansImpl.getInstance().setClanPlayers(finishedClanPlayers);
        ClansImpl.getInstance().setClans(finishedClans);
    }

    private void checkHighestUsedClanID(int clanID) {
        if (clanID > highestUsedClanID) {
            highestUsedClanID = clanID;
        }
    }

    private void checkHighestUsedClanPlayerID(int clanPlayerID) {
        if (clanPlayerID > highestUsedClanPlayerID) {
            highestUsedClanPlayerID = clanPlayerID;
        }
    }

    private void checkHighestUsedRankID(int rankID) {
        if (rankID > highestUsedRankID) {
            highestUsedRankID = rankID;
        }
    }

    public int getHighestUsedClanID() {
        return highestUsedClanID;
    }

    public int getHighestUsedClanPlayerID() {
        return highestUsedClanPlayerID;
    }

    public int getHighestUsedRankID() {
        return highestUsedRankID;
    }

    private void markInactiveClanPlayer(ClanPlayerImpl clanPlayer) {
        int removeInactiveClanPlayersAfterDays = Config.getInteger(Config.REMOVE_INACTIVE_CLAN_PLAYERS_AFTER_DAYS);
        boolean removeInactiveClanOwnersIncludingClan = Config.getBoolean(Config.REMOVE_INACTIVE_CLAN_OWNERS_INCLUDING_CLAN);
        if (removeInactiveClanPlayersAfterDays != 0) {
            long lastOnlineTime = clanPlayer.getLastOnline().getTime();
            if ((System.currentTimeMillis() - lastOnlineTime) / (86400000) > removeInactiveClanPlayersAfterDays) {
                if (clanPlayer.getClan() == null
                        || !(!removeInactiveClanOwnersIncludingClan && clanPlayer.getRank().getName()
                        .equalsIgnoreCase(RankFactory.getOwnerIdentifier()))) {
                    markedClanPlayers.add(clanPlayer);
                }
            }
        }
    }

    public List<ClanPlayerImpl> getMarkedClanPlayers() {
        return markedClanPlayers;
    }
}