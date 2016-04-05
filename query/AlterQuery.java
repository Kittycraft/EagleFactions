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

package nl.riebie.mcclans.persistence.query;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import nl.riebie.mcclans.persistence.query.QueryValue.DataType;

public class AlterQuery extends TableQuery {

	private Map<String, DataType> addedColumns = new HashMap<String, DataType>();
	private List<String> droppedColumns = new ArrayList<String>();

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

	//	public AlterQuery addDefaultValue(String key, Object value) { //?
	//		values.put(key, dataType);
	//		return this;
	//	}

	@Override
	public PreparedStatement create() {

		try {

			String query = "ALTER TABLE " + getTableName() + " " + getAddedColumnqueryPart() + " " + getDroppedColumnqueryPart();
			PreparedStatement preparedStatement = getConnection().prepareStatement(query);

			return preparedStatement;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	private String getAddedColumnqueryPart() {
		String stuff = "";
		for (Entry<String, DataType> keyValuePair : addedColumns.entrySet()) {
			stuff += "ADD " + keyValuePair.getValue() + " " + keyValuePair.getKey() + ", ";
		}
		return stuff;
	}

	private String getDroppedColumnqueryPart() {
		String stuff = "";
		for (String key : droppedColumns) {
			stuff += "DROP COLUMN " + key + ", ";
		}
		return stuff;
	}
}
