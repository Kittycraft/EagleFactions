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

package nl.riebie.mcclans.persistence.upgrade.interfaces;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import nl.riebie.mcclans.persistence.DatabaseConnection;
import nl.riebie.mcclans.persistence.DatabaseConnectionOwner;
import nl.riebie.mcclans.persistence.exceptions.WrappedDataException;
import nl.riebie.mcclans.persistence.query.Query;
import nl.riebie.mcclans.persistence.query.UpdateQuery;
import nl.riebie.mcclans.persistence.query.table.AlterQuery;
import nl.riebie.mcclans.persistence.query.table.CreateQuery;
import nl.riebie.mcclans.persistence.query.table.DropQuery;
import nl.riebie.mcclans.persistence.query.table.TableQuery;

public abstract class DatabaseUpgrade extends DataUpgrade {

    private List<TableQuery> queries = new ArrayList<>();
    private List<Query> updateQueries = new ArrayList<>();

    protected AlterQuery alterTable(String tableName) {
        AlterQuery query = new AlterQuery(tableName, DatabaseConnectionOwner.getInstance().getConnection());
        queries.add(query);
        return query;
    }

    protected CreateQuery createTable(String tableName) {
        CreateQuery query = new CreateQuery(tableName, DatabaseConnectionOwner.getInstance().getConnection());
        queries.add(query);
        return query;
    }

    protected void dropTable(String tableName) {
        DropQuery query = new DropQuery(tableName, DatabaseConnectionOwner.getInstance().getConnection());
        queries.add(query);
    }

    protected Query updateQuery(String tableName) {
        Query query = new UpdateQuery(tableName, DatabaseConnectionOwner.getInstance().getConnection());
        updateQueries.add(query);
        return query;
    }

    @Override
    public void upgrade() {
        try {
            DatabaseConnectionOwner.getInstance().startTransaction();
            upgradeDatabase();
            upgradeVersionTable();
            execute();
            DatabaseConnectionOwner.getInstance().commitTransaction();
        } catch (Exception e) {
            DatabaseConnectionOwner.getInstance().cancelTransaction();
            throw new WrappedDataException(e);
        }
    }

    protected abstract void upgradeDatabase();

    private void execute() throws SQLException {
        for (TableQuery query : queries) {
            PreparedStatement preparedStatement = query.create();
            DatabaseConnectionOwner.getInstance().executeTransactionStatement(preparedStatement);
        }
        for (Query updateQuery : updateQueries) {
            PreparedStatement preparedStatement = updateQuery.create();
            DatabaseConnectionOwner.getInstance().executeTransactionStatement(preparedStatement);
        }
    }

    private void upgradeVersionTable() {
        updateQuery("mcc_dataversion").value("dataversion", getVersion());
    }
}
