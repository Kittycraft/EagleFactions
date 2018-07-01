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

import io.github.aquerr.eaglefactions.entities.FactionHome;
import nl.riebie.mcclans.persistence.DatabaseConnectionOwner;
import nl.riebie.mcclans.persistence.exceptions.DataVersionNotFoundException;
import nl.riebie.mcclans.persistence.exceptions.GetDataVersionFailedException;
import nl.riebie.mcclans.persistence.exceptions.WrappedDataException;
import nl.riebie.mcclans.persistence.interfaces.DataLoader;
import nl.riebie.mcclans.persistence.upgrade.interfaces.DataUpgrade;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

public class DatabaseLoader extends DataLoader {

    private static final String GET_DATAVERSION_QUERY = "SELECT * FROM ef_dataversion";
    private static final String GET_FACTIONS_QUERY = "SELECT * FROM ef_factions";
    private static final String GET_RELATIONS_QUERY = "SELECT * FROM ef_relations";
    private static final String GET_PLAYERS_QUERY = "SELECT * FROM ef_players";
    private static final String GET_GROUPS_QUERY = "SELECT * FROM ef_groups";
    private static final String GET_CLAIMS_QUERY = "SELECT * FROM ef_claims";

    private final DatabaseConnectionOwner databaseConnectionOwner = DatabaseConnectionOwner.getInstance();

    public DatabaseLoader() {
        throw new UnsupportedOperationException("Database support is not completed yet.");
    }

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
        return dataUpgrades;
    }

    @Override
    protected void loadRelations() {

    }

    @Override
    protected void loadClaims() {

    }

    @Override
    protected void loadFactions() {
        ResultSet resultSet = databaseConnectionOwner.executeQuery(GET_FACTIONS_QUERY);
        if (resultSet != null) {
            try {
                while (resultSet.next()) {
                    String name = resultSet.getString("faction_name");
                    String owner = resultSet.getString("faction_owner");
                    String home = resultSet.getString("faction_home");
                    long time = resultSet.getLong("creation_time");
                    super.loadedFaction(name, owner, new FactionHome(home), time);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void loadGroups() {
        ResultSet resultSet = databaseConnectionOwner.executeQuery(GET_GROUPS_QUERY);
        if (resultSet != null) {
            try {
                while (resultSet.next()) {
                    String faction = resultSet.getString("faction");
                    String name = resultSet.getString("group_name");
                    String nodes = resultSet.getString("group_nodes");
                    String parents = resultSet.getString("group_parents");
                    int priority = resultSet.getInt("priority");

                    super.loadedGroup(faction, name, priority, Arrays.asList(parents.split(",")), Arrays.asList(nodes.split("\n")));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void loadPlayers() {
        ResultSet clanPlayersResultSet = databaseConnectionOwner.executeQuery(GET_PLAYERS_QUERY);
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

//                    super.loadedClanPlayer(clanPlayerID, uuidMostSigBits, uuidLeastSigBits, playerName, clanID, rankID, killsHigh, killsMedium,
//                            killsLow, deathsHigh, deathsMedium, deathsLow, ffProtection, lastOnlineTime);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}