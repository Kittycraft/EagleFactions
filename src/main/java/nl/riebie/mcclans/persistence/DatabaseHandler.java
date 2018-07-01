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

package nl.riebie.mcclans.persistence;

import io.github.aquerr.eaglefactions.EagleFactions;
import io.github.aquerr.eaglefactions.caching.FactionsCache;
import io.github.aquerr.eaglefactions.config.Config;
import io.github.aquerr.eaglefactions.entities.Faction;
import io.github.aquerr.eaglefactions.entities.FactionRelation;
import nl.riebie.mcclans.persistence.exceptions.WrappedDataException;
import nl.riebie.mcclans.persistence.implementations.*;
import nl.riebie.mcclans.persistence.interfaces.DataLoader;
import nl.riebie.mcclans.persistence.interfaces.DataSaver;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.scheduler.Task;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHandler {

    public static final int CURRENT_DATA_VERSION = 1;
    private static final String COUNT_DATAVERSION_QUERY = "SELECT COUNT(*) FROM `ef_dataversion`";
    private static final String INSERT_DATAVERSION_QUERY = "INSERT INTO `ef_dataversion` VALUES (" + CURRENT_DATA_VERSION + ")";
    private static DatabaseHandler instance;
    private final String CREATE_TABLE_DATAVERSION_QUERY = "CREATE TABLE IF NOT EXISTS `ef_dataversion` " + "( "
            + "`dataversion` INT(11) NOT NULL, " + "PRIMARY KEY (`dataversion`) " + ") ENGINE=InnoDB;";
    private final String CREATE_TABLE_FACTIONS_QUERY = "CREATE TABLE IF NOT EXISTS `ef_factions` "
            + "( `faction_name` VARCHAR(255) NOT NULL, `faction_owner` VARCHAR(255) NULL, `faction_home` VARCHAR(255) NULL, `creation_time` BIGINT NOT NULL," +
            "PRIMARY KEY (`faction_name`) " + ") ENGINE=InnoDB;";
    private final String CREATE_TABLE_FACTION_RELATIONS_QUERY = "CREATE TABLE IF NOT EXISTS `ef_relations` " + "( "
            + "`factionA` VARCHAR(255) NOT NULL,`factionB` VARCHAR(255) NOT NULL,`relation` INT(11) NOT NULL, " + "PRIMARY KEY (`factionA`, `factionB`) " + ") ENGINE=InnoDB;";
    private final String CREATE_TABLE_PLAYERS_QUERY = "CREATE TABLE IF NOT EXISTS `ef_players` "
            + "( `player_uuid` VARCHAR(255) NOT NULL,`player_name` VARCHAR(255) NOT NULL, "
            + "`player_faction` VARCHAR(255) NOT NULL,`player_groups` TEXT NOT NULL,`player_nodes` TEXT NOT NULL,`last_online_time` BIGINT NOT NULL, "
            + "PRIMARY KEY (`player_uuid`) " + ") ENGINE=InnoDB;";
    private final String CREATE_TABLE_GROUPS_QUERY = "CREATE TABLE IF NOT EXISTS `ef_groups` " + "( "
            + "`faction` VARCHAR(255) NOT NULL, " + "`group_name` VARCHAR(255) NOT NULL, `group_nodes` STRING NOT NULL," +
            " `group_parents` STRING NOT NULL, `priority` INT(11) NOT NULL, " + "PRIMARY KEY (`faction`, `group_name`) " + ") ENGINE=InnoDB;";
    private final String CREATE_TABLE_CLAIMS_QUERY = "CREATE TABLE IF NOT EXISTS `ef_claims` " + "( "
            + "`claim_x` INT(11) NOT NULL,`claim_z` INT(11) NOT NULL, " + "`faction_name` VARCHAR(255) NOT NULL, PRIMARY KEY (`claim_x`, `claim_z`) " + ") ENGINE=InnoDB;";

    protected DatabaseHandler() {
    }

    public static DatabaseHandler getInstance() {
        if (instance == null) {
            instance = new DatabaseHandler();
        }
        return instance;
    }

    public boolean setupConnection() {
        return DatabaseConnectionOwner.getInstance().setupConnection();
    }

    public void setupDatabase() {
        DatabaseConnectionOwner databaseConnectionOwner = DatabaseConnectionOwner.getInstance();
        databaseConnectionOwner.executeStatement(CREATE_TABLE_DATAVERSION_QUERY);

        ResultSet resultSet = databaseConnectionOwner.executeQuery(COUNT_DATAVERSION_QUERY);

        try {
            if (resultSet.next()) {
                int count = resultSet.getInt(1);
                if (count == 0) {
                    databaseConnectionOwner.executeStatement(INSERT_DATAVERSION_QUERY);
                    EagleFactions.getLogger().info("Inserted dataversion in database", false);
                }
            } else {
                EagleFactions.getLogger().warn("Could not read result of count dataversion query", true);
            }
        } catch (SQLException e) {
            throw new WrappedDataException(e);
        }

        databaseConnectionOwner.executeStatement(CREATE_TABLE_FACTIONS_QUERY);
        databaseConnectionOwner.executeStatement(CREATE_TABLE_FACTION_RELATIONS_QUERY);
        databaseConnectionOwner.executeStatement(CREATE_TABLE_PLAYERS_QUERY);
        databaseConnectionOwner.executeStatement(CREATE_TABLE_GROUPS_QUERY);
        databaseConnectionOwner.executeStatement(CREATE_TABLE_CLAIMS_QUERY);
    }

    public void clearDatabase() {
        DatabaseConnectionOwner databaseConnectionOwner = DatabaseConnectionOwner.getInstance();
        databaseConnectionOwner.executeStatement("DROP TABLE ef_dataversion");
        databaseConnectionOwner.executeStatement("DROP TABLE ef_factions");
        databaseConnectionOwner.executeStatement("DROP TABLE ef_relations");
        databaseConnectionOwner.executeStatement("DROP TABLE ef_players");
        databaseConnectionOwner.executeStatement("DROP TABLE ef_groups");
        databaseConnectionOwner.executeStatement("DROP TABLE ef_claims");
    }

    public void truncateDatabase() {
        DatabaseConnectionOwner databaseConnectionOwner = DatabaseConnectionOwner.getInstance();
        databaseConnectionOwner.executeStatement("DELETE FROM ef_dataversion");
        databaseConnectionOwner.executeStatement("DELETE FROM ef_factions");
        databaseConnectionOwner.executeStatement("DELETE FROM ef_relations");
        databaseConnectionOwner.executeStatement("DELETE FROM ef_players");
        databaseConnectionOwner.executeStatement("DELETE FROM ef_groups");
        databaseConnectionOwner.executeStatement("DELETE FROM ef_claims");
    }

    public boolean save() {
        DataSaver dataSaver;
        if (Config.getBoolean(Config.DATABASE_QUICK_SAVE)) {
            dataSaver = new BadSaver();
        } else if (Config.getBoolean(Config.USE_DATABASE)) {
            dataSaver = new DatabaseSaver();
        } else {
            dataSaver = new JsonSaver();
        }
        return dataSaver.save();
    }

    public boolean load() {
        DataLoader dataLoader;
        if (Config.getBoolean(Config.DATABASE_QUICK_SAVE)) {
            dataLoader = new BadLoader();
        } else if (Config.getBoolean(Config.USE_DATABASE) && !JsonLoader.loadFilesPresent()) {
            dataLoader = new DatabaseLoader();
        } else {
            dataLoader = new JsonLoader();
        }
        if (dataLoader.load()) {
            return true;
        }
        return false;
    }

    public void backup() {
        EagleFactions.getLogger().info("System backup commencing...", false);

        List<Faction> retrievedFactions = FactionsCache.getInstance().getFactions();

        final List<Faction> factions = new ArrayList<>();
        final List<FactionRelation> relations = (List) ((ArrayList) FactionsCache.getInstance().getRelations()).clone();

        for (Faction retrievedFaction : retrievedFactions) {
            factions.add(retrievedFaction.clone());
        }

        Task.Builder taskBuilder = Sponge.getScheduler().createTaskBuilder();
        taskBuilder.execute(() -> {
            JsonSaver jsonSaver = new JsonSaver();
            jsonSaver.useBackupLocation();
            jsonSaver.save(factions, relations);
            EagleFactions.getLogger().info("System backup finished", false);
        });
        taskBuilder.async().submit(EagleFactions.getPlugin());
    }
}
