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

import nl.riebie.mcclans.persistence.DatabaseConnectionOwner;
import nl.riebie.mcclans.persistence.exceptions.WrappedDataException;
import nl.riebie.mcclans.persistence.query.AlterQuery;
import nl.riebie.mcclans.persistence.query.CreateQuery;
import nl.riebie.mcclans.persistence.query.TableQuery;
import nl.riebie.mcclans.persistence.upgrade.interfaces.DataUpgrade;

public abstract class DatabaseUpgrade extends DataUpgrade {

    private List<TableQuery> queries = new ArrayList<TableQuery>();

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
    }

    @Override
    public void upgrade() {
        try {
            DatabaseConnectionOwner.getInstance().startTransaction();
            upgradeDatabase();
            DatabaseConnectionOwner.getInstance().commitTransaction();
            execute();
        } catch (SQLException e) {
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
    }
}
