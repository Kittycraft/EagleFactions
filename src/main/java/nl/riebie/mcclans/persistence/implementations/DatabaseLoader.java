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

import nl.riebie.mcclans.persistence.DatabaseConnectionOwner;
import nl.riebie.mcclans.persistence.exceptions.DataVersionNotFoundException;
import nl.riebie.mcclans.persistence.exceptions.GetDataVersionFailedException;
import nl.riebie.mcclans.persistence.exceptions.WrappedDataException;
import nl.riebie.mcclans.persistence.interfaces.DataLoader;
import nl.riebie.mcclans.persistence.upgrade.interfaces.DataUpgrade;
import nl.riebie.mcclans.persistence.upgrade.versions.DatabaseUpgrade2;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class DatabaseLoader extends DataLoader {

    private static final String GET_DATAVERSION_QUERY = "SELECT * FROM mcc_dataversion";
    private static final String GET_CLANS_QUERY = "SELECT * FROM mcc_clans";
    private static final String GET_CLANS_ALLIES_QUERY = "SELECT * FROM mcc_clans_allies";
    private static final String GET_CLANPLAYERS_QUERY = "SELECT * FROM mcc_clanplayers";
    private static final String GET_RANKS_QUERY = "SELECT * FROM mcc_ranks";

    private final DatabaseConnectionOwner databaseConnectionOwner = DatabaseConnectionOwner.getInstance();

    @Override
    protected boolean initialize() {
        return true;
    }

    @Override
    protected int getDataVersion() {
        ResultSet dataVersionResultSet = databaseConnectionOwner.executeQuery(GET_DATAVERSION_QUERY);
        if (dataVersionResultSet != null) {
            try {
                if (dataVersionResultSet.next()) {
                    return dataVersionResultSet.getInt("dataversion");
                } else {
                    throw new GetDataVersionFailedException("No 'dataversion' present in database");
                }
            } catch (SQLException e) {
                throw new WrappedDataException(e);
            }
        }

        throw new DataVersionNotFoundException();
    }

    @Override
    protected List<DataUpgrade> getDataUpgrades(List<DataUpgrade> dataUpgrades) {
        dataUpgrades.add(new DatabaseUpgrade2());
        return dataUpgrades;
    }

    @Override
    protected void loadClans() {
        ResultSet clansResultSet = databaseConnectionOwner.executeQuery(GET_CLANS_QUERY);
        if (clansResultSet != null) {
            try {
                while (clansResultSet.next()) {
                    int clanID = clansResultSet.getInt("clan_id");
                    String clanTag = clansResultSet.getString("clantag");
                    String clanName = clansResultSet.getString("clanname");
                    int ownerID = clansResultSet.getInt("clanplayer_id_owner");
                    String tagColorId = clansResultSet.getString("tagcolor");
                    boolean allowAllyInvites = clansResultSet.getBoolean("allow_ally_invites");
                    boolean ffProtection = clansResultSet.getBoolean("ff_protection");
                    long creationTime = clansResultSet.getLong("creation_time");

                    String homeWorld = clansResultSet.getString("clanhome_world");
                    double homeX = clansResultSet.getDouble("clanhome_x");
                    double homeY = clansResultSet.getDouble("clanhome_y");
                    double homeZ = clansResultSet.getDouble("clanhome_z");
                    float homeYaw = clansResultSet.getFloat("clanhome_yaw");
                    float homePitch = clansResultSet.getFloat("clanhome_pitch");

                    int homeSetTimes = clansResultSet.getInt("clanhome_set_times");
                    long homeLastSetTimeStamp = clansResultSet.getLong("clanhome_set_timestamp");

                    String bankId = clansResultSet.getString("bank_id");

                    super.loadedClan(clanID, clanTag, clanName, ownerID, tagColorId, allowAllyInvites, ffProtection, creationTime, homeWorld, homeX,
                            homeY, homeZ, homeYaw, homePitch, homeSetTimes, homeLastSetTimeStamp, bankId);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void loadRanks() {
        ResultSet ranksResultSet = databaseConnectionOwner.executeQuery(GET_RANKS_QUERY);
        if (ranksResultSet != null) {
            try {
                while (ranksResultSet.next()) {
                    int rankID = ranksResultSet.getInt("rank_id");
                    int clanID = ranksResultSet.getInt("clan_id");
                    String rankName = ranksResultSet.getString("rankname");
                    String permissions = ranksResultSet.getString("permissions");
                    boolean changeable = ranksResultSet.getBoolean("changeable");

                    super.loadedRank(rankID, clanID, rankName, permissions, changeable);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void loadClanPlayers() {
        ResultSet clanPlayersResultSet = databaseConnectionOwner.executeQuery(GET_CLANPLAYERS_QUERY);
        if (clanPlayersResultSet != null) {
            try {
                while (clanPlayersResultSet.next()) {
                    String playerName = clanPlayersResultSet.getString("playername");
                    int clanPlayerID = clanPlayersResultSet.getInt("clanplayer_id");
                    long uuidMostSigBits = clanPlayersResultSet.getLong("uuid_most_sig_bits");
                    long uuidLeastSigBits = clanPlayersResultSet.getLong("uuid_least_sig_bits");
                    int rankID = clanPlayersResultSet.getInt("rank_id");
                    int clanID = clanPlayersResultSet.getInt("clan_id");
                    int killsHigh = clanPlayersResultSet.getInt("kills_high");
                    int killsMedium = clanPlayersResultSet.getInt("kills_medium");
                    int killsLow = clanPlayersResultSet.getInt("kills_low");
                    int deathsHigh = clanPlayersResultSet.getInt("deaths_high");
                    int deathsMedium = clanPlayersResultSet.getInt("deaths_medium");
                    int deathsLow = clanPlayersResultSet.getInt("deaths_low");
                    boolean ffProtection = clanPlayersResultSet.getBoolean("ff_protection");
                    long lastOnlineTime = clanPlayersResultSet.getLong("last_online_time");

                    super.loadedClanPlayer(clanPlayerID, uuidMostSigBits, uuidLeastSigBits, playerName, clanID, rankID, killsHigh, killsMedium,
                            killsLow, deathsHigh, deathsMedium, deathsLow, ffProtection, lastOnlineTime);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void loadAllies() {
        ResultSet clansResultSet = databaseConnectionOwner.executeQuery(GET_CLANS_ALLIES_QUERY);
        if (clansResultSet != null) {
            try {
                while (clansResultSet.next()) {
                    int clanID = clansResultSet.getInt("clan_id");
                    int clanIDAlly = clansResultSet.getInt("clan_id_ally");

                    super.loadedAlly(clanID, clanIDAlly);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

    }
}