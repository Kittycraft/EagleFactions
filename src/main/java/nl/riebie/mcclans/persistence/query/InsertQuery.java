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
import java.util.List;
import java.util.Map.Entry;

public class InsertQuery extends Query {

	public InsertQuery(String tableName, Connection connection) {
		super(tableName, connection);
	}

	@Override
	public PreparedStatement create() {
		String fields = "";
		String values = "";
		boolean firstRun = true;
		List<QueryValue> queryValues = new ArrayList<QueryValue>();

		for (Entry<String, QueryValue> keyValuePair : getValues().entrySet()) {
			if (!firstRun) {
				fields += ",";
				values += ",";
			} else {
				firstRun = false;
			}
			fields += keyValuePair.getKey();
			values += "?";
			queryValues.add(keyValuePair.getValue());
		}

		String query = "INSERT INTO " + getTableName() + " (" + fields + ") VALUES (" + values + ")";
		try {
			PreparedStatement preparedStatement = getConnection().prepareStatement(query);
			for (int i = 0; i < queryValues.size(); i++) {
				queryValues.get(i).appendValue(preparedStatement, i);
			}

			return preparedStatement;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

}
