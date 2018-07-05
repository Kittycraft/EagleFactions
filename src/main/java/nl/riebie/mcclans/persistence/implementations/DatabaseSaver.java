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

import io.github.aquerr.eaglefactions.entities.*;
import nl.riebie.mcclans.persistence.DatabaseConnectionOwner;
import nl.riebie.mcclans.persistence.DatabaseHandler;
import nl.riebie.mcclans.persistence.QueryGenerator;
import nl.riebie.mcclans.persistence.interfaces.DataSaver;

import java.sql.PreparedStatement;

public class DatabaseSaver extends DataSaver {

    private final static DatabaseConnectionOwner databaseConnectionOwner = DatabaseConnectionOwner.getInstance();

    public static PreparedStatement getInsertDataVersionQuery(int dataVersion) {
        return QueryGenerator.createInsertQuery(
                "mcc_dataversion",
                databaseConnectionOwner.getConnection()
        ).value(
                "dataversion",
                dataVersion
        ).create();
    }

    public static PreparedStatement getInsertClaimQuery(FactionClaim claim) {
        return QueryGenerator.createInsertQuery("ef_claims", databaseConnectionOwner.getConnection()).value("claim_x", claim.chunk.getX())
                .value("claim_z", claim.chunk.getZ()).value("faction_name", claim.faction).create();
    }

    public static PreparedStatement getDeleteClaimQuery(FactionClaim claim) {
        return QueryGenerator.createDeleteQuery("ef_claims", databaseConnectionOwner.getConnection()).where("claim_x", claim.chunk.getX())
                .and("claim_z", claim.chunk.getZ()).create();
    }

    public static PreparedStatement getInsertPlayerQuery(FactionPlayer player) {
        String playerGroups = "";
        for (int i = 0; i < player.parents.size(); i++) {
            playerGroups += player.parents.get(i) + (i != player.parents.size() - 1 ? "," : "");
        }
        String playerNodes = "";
        for (int i = 0; i < player.nodes.size(); i++) {
            playerGroups += player.nodes.get(i) + (i != player.nodes.size() - 1 ? "\n" : "");
        }
        return QueryGenerator.createInsertQuery("ef_players", databaseConnectionOwner.getConnection()).value("player_uuid", player.uuid)
                .value("player_name", player.name).value("player_faction", player.faction).value("player_groups", playerGroups)
                .value("player_nodes", playerNodes).value("last_online_time", player.getLastOnline()).create();
    }

    public static PreparedStatement getUpdatePlayerQuery(FactionPlayer player) {
        String playerGroups = "";
        for (int i = 0; i < player.parents.size(); i++) {
            playerGroups += player.parents.get(i) + (i != player.parents.size() - 1 ? "," : "");
        }
        String playerNodes = "";
        for (int i = 0; i < player.nodes.size(); i++) {
            playerGroups += player.nodes.get(i) + (i != player.nodes.size() - 1 ? "\n" : "");
        }
        return QueryGenerator.createUpdateQuery("ef_players", databaseConnectionOwner.getConnection())
                .value("player_faction", player.faction).value("player_groups", playerGroups)
                .value("player_nodes", playerNodes).value("last_online_time", player.getLastOnline())
                .where("player_uuid", player.uuid).create();
    }

    public static PreparedStatement getDeletePlayerQuery(String uuid) {
        return QueryGenerator.createDeleteQuery("ef_players", databaseConnectionOwner.getConnection()).where("player_uuid", uuid).create();
    }

    public static PreparedStatement getUpdateFactionQuery(Faction faction) {
        return QueryGenerator.createUpdateQuery("ef_factions", databaseConnectionOwner.getConnection())
                .value("faction_owner", faction.owner == null ? null : faction.owner).value("faction_home", faction.Home == null ? null : faction.Home.toString())
                .where("faction_name", faction.name).create();
    }

    public static PreparedStatement getDeleteFactionQuery(String factionName) {
        return QueryGenerator.createDeleteQuery("ef_factions", databaseConnectionOwner.getConnection()).where("faction_name", factionName).create();
    }

    public static PreparedStatement getInsertGroupQuery(String factionName, Group group) {
        String parents = "";
        for (int i = 0; i < group.perms.parents.size(); i++) {
            parents += group.perms.parents.get(i) + (i != group.perms.parents.size() - 1 ? "," : "");
        }
        String nodes = "";
        for (int i = 0; i < group.perms.nodes.size(); i++) {
            parents += group.perms.nodes.get(i) + (i != group.perms.nodes.size() - 1 ? "\n" : "");
        }
        return QueryGenerator.createInsertQuery("ef_groups", databaseConnectionOwner.getConnection()).value("faction", factionName)
                .value("group_name", group.name).value("group_parents", parents).value("group_nodes", nodes)
                .value("priority", group.priority).create();
    }

    public static PreparedStatement getUpdateGroupQuery(Group group, String factionName) {
        String parents = "";
        for (int i = 0; i < group.perms.parents.size(); i++) {
            parents += group.perms.parents.get(i) + (i != group.perms.parents.size() - 1 ? "," : "");
        }
        String nodes = "";
        for (int i = 0; i < group.perms.nodes.size(); i++) {
            parents += group.perms.nodes.get(i) + (i != group.perms.nodes.size() - 1 ? "\n" : "");
        }
        return QueryGenerator.createUpdateQuery("ef_groups", databaseConnectionOwner.getConnection())
                .value("group_name", group.name).value("group_parents", parents).value("group_nodes", nodes)
                .value("priority", group.priority).where("faction", factionName).and("group_name", group.name).create();
    }

    public static PreparedStatement getDeleteGroupQuery(String factionName, String groupName) {
        return QueryGenerator.createDeleteQuery("ef_groups", databaseConnectionOwner.getConnection()).where("faction", factionName).and("group_name", groupName).create();
    }

    public static PreparedStatement getInsertFactionRelationQuery(FactionRelation relation) {
        return QueryGenerator.createInsertQuery("ef_relations", databaseConnectionOwner.getConnection()).value("factionA", relation.factionA.toString())
                .value("factionB", relation.factionB.toString()).value("relation", relation.type.identifier).create();
    }

    public static PreparedStatement getDeleteFactionRelationQuery(String factionA, String factionB) {
        return QueryGenerator.createDeleteQuery("ef_relations", databaseConnectionOwner.getConnection()).value("factionA", factionA)
                .value("factionB", factionB).create();
    }

    protected void saveFaction(Faction faction) throws Exception {
        databaseConnectionOwner.executeTransactionStatement(QueryGenerator.createInsertQuery("ef_factions",
                databaseConnectionOwner.getConnection()).value("faction_name", faction.name)
                .value("faction_owner", faction.owner == null ? null : faction.owner).value("faction_name", faction.name)
                .value("faction_home", faction.Home == null ? null : faction.Home.toString()).value("creation_time", faction.creationTime)
                .create());
    }

    protected void savePlayer(FactionPlayer player) throws Exception {
        PreparedStatement query = getInsertPlayerQuery(player);
        databaseConnectionOwner.executeTransactionStatement(query);
    }

    protected void saveGroup(String factionName, Group group) throws Exception {
        PreparedStatement query = getInsertGroupQuery(factionName, group);
        databaseConnectionOwner.executeTransactionStatement(query);
    }

    protected void saveFactionRelation(FactionRelation relation) throws Exception {
        PreparedStatement query = getInsertFactionRelationQuery(relation);
        databaseConnectionOwner.executeTransactionStatement(query);
    }

    @Override
    protected void saveFactionClaim(FactionClaim claim) throws Exception {
        PreparedStatement query = getInsertClaimQuery(claim);
        databaseConnectionOwner.executeTransactionStatement(query);
    }

    @Override
    protected void saveStarted() throws Exception {
        databaseConnectionOwner.startTransaction();
        DatabaseHandler.getInstance().truncateDatabase();

        // Store data version
        PreparedStatement query = getInsertDataVersionQuery(DatabaseHandler.CURRENT_DATA_VERSION);
        databaseConnectionOwner.executeTransactionStatement(query);
    }

    @Override
    protected void saveFinished() throws Exception {
        databaseConnectionOwner.commitTransaction();
    }

    @Override
    protected void saveCancelled() {
        databaseConnectionOwner.cancelTransaction();
    }
}
