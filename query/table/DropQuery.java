package nl.riebie.mcclans.persistence.query.table;

import nl.riebie.mcclans.persistence.query.table.TableQuery;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Created by riebie on 30/04/2016.
 */
public class DropQuery extends TableQuery {

    public DropQuery(String tableName, Connection connection) {
        super(tableName, connection);
    }

    @Override
    public PreparedStatement create() {
        try {
            String query = "DROP TABLE " + getTableName();
             return  getConnection().prepareStatement(query);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
