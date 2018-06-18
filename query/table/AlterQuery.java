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

package nl.riebie.mcclans.persistence.query.table;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import nl.riebie.mcclans.persistence.query.DataType;

public class AlterQuery extends TableQuery {

    private Map<String, DataType> addedColumns = new HashMap<>();
    private List<String> droppedColumns = new ArrayList<>();
    private Map<String, DataType> alteredColumns = new HashMap<>();

    public AlterQuery(String tableName, Connection connection) {
        super(tableName, connection);
    }

    public AlterQuery addColumn(String key, DataType dataType) {
        addedColumns.put(key, dataType);
        return this;
    }

    public AlterQuery dropColumn(String key) {
        droppedColumns.add(key);
        return this;
    }

    public AlterQuery alterColumn(String key, DataType dataType) {
        alteredColumns.put(key, dataType);
        return this;
    }

    @Override
    public PreparedStatement create() {
        try {
            String query = String.format("ALTER TABLE `%s` %s %s %s", getTableName(), getAddedColumnQueryPart(),
                    getDroppedColumnQueryPart(), getAlteredColumnQueryPart());

            return getConnection().prepareStatement(query);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String getAddedColumnQueryPart() {
        StringBuilder stringBuilder = new StringBuilder();
        for (Entry<String, DataType> keyValuePair : addedColumns.entrySet()) {
            if(stringBuilder.length() != 0){
                stringBuilder.append(",");
            }
            stringBuilder.append(String.format("ADD COLUMN `%s` %s", keyValuePair.getKey(), keyValuePair.getValue().getDatabaseType()));
        }
        return stringBuilder.toString();
    }

    private String getDroppedColumnQueryPart() {
        StringBuilder stringBuilder = new StringBuilder();
        for (String key : droppedColumns) {
            stringBuilder.append(String.format("DROP COLUMN %s,", key));
        }
        return stringBuilder.toString();
    }

    private String getAlteredColumnQueryPart() {
        StringBuilder stringBuilder = new StringBuilder();
        for (Entry<String, DataType> keyValuePair : alteredColumns.entrySet()) {
            stringBuilder.append(String.format("MODIFY COLUMN `%s` %s,", keyValuePair.getKey(), keyValuePair.getValue().getDatabaseType()));
        }
        return stringBuilder.toString();
    }
}
