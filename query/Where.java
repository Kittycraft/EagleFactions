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

import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class Where {

	private Map<String, QueryValue> values = new HashMap<String, QueryValue>();
	private Query query;

	public Where(String key, String value, Query query) {
		values.put(key, new QueryValue(DataType.STRING, value));
		this.query = query;
	}

	public Where(String key, int value, Query query) {
		values.put(key, new QueryValue(DataType.INTEGER, Integer.toString(value)));
		this.query = query;
	}

	public Where(String key, double value, Query query) {
		values.put(key, new QueryValue(DataType.DOUBLE, Double.toString(value)));
		this.query = query;
	}

	public Where(String key, long value, Query query) {
		values.put(key, new QueryValue(DataType.LONG, Long.toString(value)));
		this.query = query;
	}

	public Where(String key, float value, Query query) {
		values.put(key, new QueryValue(DataType.FLOAT, Float.toString(value)));
		this.query = query;
	}

	public Where(String key, boolean value, Query query) {
		values.put(key, new QueryValue(DataType.BOOLEAN, value ? "1" : "0"));
		this.query = query;
	}

	public Where and(String key, String value) {
		values.put(key, new QueryValue(DataType.STRING, value));
		return this;
	}

	public Where and(String key, int value) {
		values.put(key, new QueryValue(DataType.INTEGER, Integer.toString(value)));
		return this;
	}

	public Where and(String key, double value) {
		values.put(key, new QueryValue(DataType.DOUBLE, Double.toString(value)));
		return this;
	}

	public Where and(String key, long value) {
		values.put(key, new QueryValue(DataType.LONG, Long.toString(value)));
		return this;
	}

	public Where and(String key, boolean value) {
		values.put(key, new QueryValue(DataType.BOOLEAN, value ? "1" : "0"));
		return this;
	}

	public Where and(String key, float value) {
		values.put(key, new QueryValue(DataType.FLOAT, Float.toString(value)));
		return this;
	}

	public WherePart getWherePart() {
		boolean firstRun = true;
		String whereString = "";
		List<QueryValue> queryValues = new ArrayList<>();

		for (Entry<String, QueryValue> keyValuePair : values.entrySet()) {
			if (!firstRun) {
				whereString += " AND ";
			} else {
				whereString += "WHERE ";
				firstRun = false;
			}
			queryValues.add(keyValuePair.getValue());
			whereString += keyValuePair.getKey() + "=?";
		}
		return new WherePart(whereString, queryValues);
	}

    public PreparedStatement create() {
        return query.create();
    }
}
