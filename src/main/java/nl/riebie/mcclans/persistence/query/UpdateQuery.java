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

public class UpdateQuery extends Query {

	public UpdateQuery(String tableName, Connection connection) {
		super(tableName, connection);
	}

	@Override
	public PreparedStatement create() {
		List<QueryValue> parameters = new ArrayList<QueryValue>();

		String values = "";
		boolean firstRun = true;
		for (Entry<String, QueryValue> keyValuePair : getValues().entrySet()) {
			if (!firstRun) {
				values += ",";
			} else {
				firstRun = false;
			}
			parameters.add(keyValuePair.getValue());
			values += keyValuePair.getKey() + "=" + "?";
		}
		WherePart wherePart = getWherePart();
		String query = "UPDATE " + getTableName() + " SET " + values;
		if (wherePart != null) {
			query += " " + wherePart.getWhereString();
			parameters.addAll(wherePart.getQueryValues());
		}
		try {
			PreparedStatement preparedStatement = getConnection().prepareStatement(query);
			for (int i = 0; i < parameters.size(); i++) {
				parameters.get(i).appendValue(preparedStatement, i);
			}

			return preparedStatement;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	public void execute() {

	}

}
