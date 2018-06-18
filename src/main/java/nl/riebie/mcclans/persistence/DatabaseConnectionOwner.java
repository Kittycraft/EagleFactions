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

import nl.riebie.mcclans.config.Config;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DatabaseConnectionOwner {

    private static DatabaseConnectionOwner instance;

    private DatabaseConnection databaseConnection = new DatabaseConnection();

    protected DatabaseConnectionOwner() {
    }

    public static DatabaseConnectionOwner getInstance() {
        if (instance == null) {
            instance = new DatabaseConnectionOwner();
        }
        return instance;
    }

    public boolean setupConnection() {
        return databaseConnection.setupConnection(
                Config.getString(Config.DATABASE_SERVER),
                Config.getInteger(Config.DATABASE_SERVER_PORT),
                Config.getString(Config.DATABASE_NAME),
                Config.getString(Config.DATABASE_SERVER_USER),
                Config.getString(Config.DATABASE_SERVER_PASSWORD)
        );
    }

    public void executeStatement(String statement) {
        databaseConnection.executeStatement(statement);
    }

    public void executeTransactionStatement(PreparedStatement statement) throws SQLException {
        databaseConnection.executeTransactionStatement(statement);
    }

    public ResultSet executeQuery(String query) {
        return databaseConnection.executeQuery(query);
    }

    public void startTransaction() throws SQLException {
        databaseConnection.startTransaction();
    }

    public void commitTransaction() throws SQLException {
        databaseConnection.commitTransaction();
    }

    public void cancelTransaction() {
        databaseConnection.cancelTransaction();
    }

    public boolean isValid() {
        return databaseConnection.isValid();
    }

    public void close() {
        databaseConnection.close();
    }

    public Connection getConnection() {
        return databaseConnection.connection();
    }
}
