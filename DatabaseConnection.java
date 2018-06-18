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

import com.google.common.util.concurrent.UncheckedExecutionException;
import nl.riebie.mcclans.MCClans;
import nl.riebie.mcclans.config.Config;
import nl.riebie.mcclans.enums.DBMSType;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.service.sql.SqlService;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String MYSQL_DRIVER_NAME = "mysql";
    private static final String SQLITE_DRIVER_NAME = "sqlite";
    private static final String H2_DRIVER_NAME = "h2";

    private Connection con = null;

    public boolean setupConnection(String server, int port, String database, String user, String password) {
        String url;
        DBMSType dbmsType = DBMSType.getType(Config.getString(Config.DBMS_TYPE));
        if (dbmsType.equals(DBMSType.MYSQL)) {
            url = "jdbc:" + MYSQL_DRIVER_NAME + "://" + user + ":" + password + "@" + server + ":" + String.valueOf(port) + "/" + database;
        } else if (dbmsType.equals(DBMSType.SQLITE)) {
            url = "jdbc:" + SQLITE_DRIVER_NAME + ":" + database;
        } else if (dbmsType.equals(DBMSType.H2)) {
            url = "jdbc:" + H2_DRIVER_NAME + ":" + database;
        } else {
            return false;
        }

        SqlService sql = Sponge.getGame().getServiceManager().provide(SqlService.class).get();
        try {
            DataSource dataSource = sql.getDataSource(MCClans.getPlugin(), url);
            con = dataSource.getConnection();
            return true;
        } catch (SQLException e) {
            MCClans.getPlugin().getLogger().error("Failed to retrieve sql data source! ", e, true);
            return false;
        } catch (UncheckedExecutionException e) {
            MCClans.getPlugin().getLogger().error("Please check your database settings!", e, true);
            return false;
        }
    }

    public void executeTransactionStatement(PreparedStatement statement) throws SQLException {
        statement.execute();
    }

    public void executeStatement(String statement) {
        try {
            PreparedStatement preparedStatement = con.prepareStatement(statement);
            preparedStatement.execute();
        } catch (SQLException e) {
            if (Config.getBoolean(Config.DEBUGGING)) {
                e.printStackTrace();
            }
        }
    }

    public ResultSet executeQuery(String query) {
        try {
            PreparedStatement preparedStatement = con.prepareStatement(query);
            return preparedStatement.executeQuery();
        } catch (SQLException e) {
            if (Config.getBoolean(Config.DEBUGGING)) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public void startTransaction() throws SQLException {
        con.setAutoCommit(false);
    }

    public void commitTransaction() throws SQLException {
        con.commit();
        con.setAutoCommit(true);
    }

    public void cancelTransaction() {
        try {
            con.rollback();
            con.setAutoCommit(true);
        } catch (SQLException e) {
            if (Config.getBoolean(Config.DEBUGGING)) {
                e.printStackTrace();
            }
        }
    }

    public void close() {
        try {
            con.close();
        } catch (SQLException e) {
            if (Config.getBoolean(Config.DEBUGGING)) {
                e.printStackTrace();
            }
        }
    }

    public boolean isValid() {
        try {
            return con.isValid(5);
        } catch (SQLException e) {
            if (Config.getBoolean(Config.DEBUGGING)) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public Connection connection() {
        return con;
    }
}
