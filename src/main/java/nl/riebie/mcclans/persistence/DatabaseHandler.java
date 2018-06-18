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

import nl.riebie.mcclans.ClansImpl;
import nl.riebie.mcclans.MCClans;
import nl.riebie.mcclans.clan.ClanImpl;
import nl.riebie.mcclans.config.Config;
import nl.riebie.mcclans.persistence.exceptions.WrappedDataException;
import nl.riebie.mcclans.persistence.implementations.DatabaseLoader;
import nl.riebie.mcclans.persistence.implementations.DatabaseSaver;
import nl.riebie.mcclans.persistence.implementations.JsonLoader;
import nl.riebie.mcclans.persistence.implementations.JsonSaver;
import nl.riebie.mcclans.persistence.interfaces.DataLoader;
import nl.riebie.mcclans.persistence.interfaces.DataSaver;
import nl.riebie.mcclans.player.ClanPlayerImpl;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.scheduler.Task;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHandler {

    public static final int CURRENT_DATA_VERSION = 2;

    private List<ClanPlayerImpl> markedClanPlayers = new ArrayList<>();

    private final String CREATE_TABLE_DATAVERSION_QUERY = "CREATE TABLE IF NOT EXISTS `mcc_dataversion` " + "( "
            + "`dataversion` INT(11) NOT NULL, " + "PRIMARY KEY (`dataversion`) " + ") ENGINE=InnoDB;";

    private static final String COUNT_DATAVERSION_QUERY = "SELECT COUNT(*) FROM `mcc_dataversion`";
    private static final String INSERT_DATAVERSION_QUERY = "INSERT INTO `mcc_dataversion` VALUES (" + CURRENT_DATA_VERSION + ")";

    private final String CREATE_TABLE_CLANS_QUERY = "CREATE TABLE IF NOT EXISTS `mcc_clans` "
            + "( "
            + "`clan_id` INT(11) NOT NULL,`clantag` VARCHAR(255) NOT NULL, "
            + "`clanname` VARCHAR(255) NOT NULL,`clanplayer_id_owner` INT(11) NOT NULL, "
            + "`tagcolor` VARCHAR(255) NOT NULL,`allow_ally_invites` TINYINT(1) NOT NULL, "
            + "`clanhome_world` VARCHAR(255) NULL,`clanhome_x` DOUBLE NOT NULL, "
            + "`clanhome_y` DOUBLE NOT NULL,`clanhome_z` DOUBLE NOT NULL,`clanhome_yaw` FLOAT NOT NULL "
            + ",`clanhome_pitch` FLOAT NOT NULL,`clanhome_set_times` INT(11) NOT NULL,`clanhome_set_timestamp` BIGINT NOT NULL,`ff_protection` TINYINT(1) NOT NULL,`creation_time` BIGINT NOT NULL, "
            + "`bank_id` VARCHAR(255) NOT NULL, PRIMARY KEY (`clan_id`) " + ") ENGINE=InnoDB;";

    private final String CREATE_TABLE_CLANS_ALLIES_QUERY = "CREATE TABLE IF NOT EXISTS `mcc_clans_allies` " + "( "
            + "`clan_id` INT(11) NOT NULL,`clan_id_ally` INT(11) NOT NULL, " + "PRIMARY KEY (`clan_id`, `clan_id_ally`) " + ") ENGINE=InnoDB;";

    private final String CREATE_TABLE_CLANPLAYERS_QUERY = "CREATE TABLE IF NOT EXISTS `mcc_clanplayers` "
            + "( "
            + "`clanplayer_id` INT NOT NULL,`uuid_most_sig_bits` BIGINT NOT NULL,`uuid_least_sig_bits` BIGINT NOT NULL,`playername` VARCHAR(255) NOT NULL, "
            + "`clan_id` INT(11) NOT NULL,`rank_id` INT(11) NOT NULL,`kills_high` INT(11) NOT NULL "
            + ",`kills_medium` INT(11) NOT NULL,`kills_low` INT(11) NOT NULL, " + "`deaths_high` INT(11) NOT NULL,`deaths_medium` INT(11) NOT NULL, "
            + "`deaths_low` INT(11) NOT NULL,`ff_protection` TINYINT(1) NOT NULL,`last_online_time` BIGINT NOT NULL, "
            + "PRIMARY KEY (`clanplayer_id`) " + ") ENGINE=InnoDB;";

    private final String CREATE_TABLE_RANKS_QUERY = "CREATE TABLE IF NOT EXISTS `mcc_ranks` " + "( "
            + "`rank_id` INT(11) NOT NULL,`clan_id` INT(11) NOT NULL, " + "`rankname` VARCHAR(255) NOT NULL,`permissions` VARCHAR(255) NULL, "
            + "`changeable` TINYINT(1) NOT NULL, " + "PRIMARY KEY (`rank_id`) " + ") ENGINE=InnoDB;";

    private static DatabaseHandler instance;

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
                    MCClans.getPlugin().getLogger().info("Inserted dataversion in database", false);
                }
            } else {
                MCClans.getPlugin().getLogger().warn("Could not read result of count dataversion query", true);
            }
        } catch (SQLException e) {
            throw new WrappedDataException(e);
        }

        databaseConnectionOwner.executeStatement(CREATE_TABLE_CLANS_QUERY);
        databaseConnectionOwner.executeStatement(CREATE_TABLE_CLANS_ALLIES_QUERY);
        databaseConnectionOwner.executeStatement(CREATE_TABLE_CLANPLAYERS_QUERY);
        databaseConnectionOwner.executeStatement(CREATE_TABLE_RANKS_QUERY);
    }

    public void clearDatabase() {
        DatabaseConnectionOwner databaseConnectionOwner = DatabaseConnectionOwner.getInstance();
        databaseConnectionOwner.executeStatement("DROP TABLE mcc_dataversion");
        databaseConnectionOwner.executeStatement("DROP TABLE mcc_clans");
        databaseConnectionOwner.executeStatement("DROP TABLE mcc_clans_allies");
        databaseConnectionOwner.executeStatement("DROP TABLE mcc_clanplayers");
        databaseConnectionOwner.executeStatement("DROP TABLE mcc_ranks");
    }

    public void truncateDatabase() {
        DatabaseConnectionOwner databaseConnectionOwner = DatabaseConnectionOwner.getInstance();
        databaseConnectionOwner.executeStatement("DELETE FROM mcc_dataversion");
        databaseConnectionOwner.executeStatement("DELETE FROM mcc_clans");
        databaseConnectionOwner.executeStatement("DELETE FROM mcc_clans_allies");
        databaseConnectionOwner.executeStatement("DELETE FROM mcc_clanplayers");
        databaseConnectionOwner.executeStatement("DELETE FROM mcc_ranks");
    }

    public boolean save() {
        DataSaver dataSaver;
        if (Config.getBoolean(Config.USE_DATABASE)) {
            dataSaver = new DatabaseSaver();
        } else {
            dataSaver = new JsonSaver();
        }
        return dataSaver.save();
    }

    public boolean load() {
        DataLoader dataLoader;
        if (Config.getBoolean(Config.USE_DATABASE) && !JsonLoader.loadFilesPresent()) {
            dataLoader = new DatabaseLoader();
        } else {
            dataLoader = new JsonLoader();
        }
        if (dataLoader.load()) {
            markedClanPlayers = dataLoader.getMarkedClanPlayers();
            return true;
        }
        return false;
    }

    public void backup() {
        MCClans.getPlugin().getLogger().info("System backup commencing...", false);

        List<ClanImpl> retrievedClans = ClansImpl.getInstance().getClanImpls();
        List<ClanPlayerImpl> retrievedClanPlayers = ClansImpl.getInstance().getClanPlayerImpls();

        final List<ClanImpl> clans = new ArrayList<ClanImpl>();
        final List<ClanPlayerImpl> clanPlayers = new ArrayList<ClanPlayerImpl>();

        for (ClanImpl retrievedClan : retrievedClans) {
            clans.add(retrievedClan.clone());
        }

        for (ClanPlayerImpl retrievedclanPlayers : retrievedClanPlayers) {
            clanPlayers.add(retrievedclanPlayers.clone());
        }

        Task.Builder taskBuilder = Sponge.getScheduler().createTaskBuilder();
        taskBuilder.execute(new Runnable() {
            @Override
            public void run() {
                JsonSaver jsonSaver = new JsonSaver();
                jsonSaver.useBackupLocation();
                jsonSaver.save(clans, clanPlayers);
                MCClans.getPlugin().getLogger().info("System backup finished", false);
            }
        });
        taskBuilder.async().submit(MCClans.getPlugin());
    }

    public void removeMarkedInactiveClanPlayers() {
        for (ClanPlayerImpl clanPlayer : markedClanPlayers) {
            ClansImpl.getInstance().removeClanPlayer(clanPlayer);
        }
    }
}