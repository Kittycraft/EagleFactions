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
import java.util.HashMap;
import java.util.Map;

public abstract class Query {

	private final String tableName;
	private final Connection connection;
	private HashMap<String, QueryValue> values = new HashMap<>();
	private Where where;

	public Query(String tableName, Connection connection) {
		this.tableName = tableName;
		this.connection = connection;
	}

	public Query value(String key, String value) {
		values.put(key, new QueryValue(DataType.STRING, value));
		return this;
	}

	public Query value(String key, int value) {
		values.put(key, new QueryValue(DataType.INTEGER, Integer.toString(value)));
		return this;
	}

	public Query value(String key, long value) {
		values.put(key, new QueryValue(DataType.LONG, Long.toString(value)));
		return this;
	}

	public Query value(String key, double value) {
		values.put(key, new QueryValue(DataType.DOUBLE, Double.toString(value)));
		return this;
	}

	public Query value(String key, float value) {
		values.put(key, new QueryValue(DataType.FLOAT, Float.toString(value)));
		return this;
	}

	public Query value(String key, boolean value) {
		values.put(key, new QueryValue(DataType.BOOLEAN, value ? "1" : "0"));
		return this;
	}

	public Where where(String key, String value) {
		this.where = new Where(key, value, this);
		return this.where;
	}

	public Where where(String key, int value) {
		this.where = new Where(key, value, this);
		return this.where;
	}

	public Where where(String key, double value) {
		this.where = new Where(key, value, this);
		return this.where;
	}

	public Where where(String key, long value) {
		this.where = new Where(key, value, this);
		return this.where;
	}

	public Where where(String key, boolean value) {
		this.where = new Where(key, value, this);
		return this.where;
	}

	public Where where(String key, float value) {
		this.where = new Where(key, value, this);
		return this.where;
	}

	protected Map<String, QueryValue> getValues() {
		return values;
	}

	protected String getTableName() {
		return tableName;
	}

	protected WherePart getWherePart() {
		return where == null ? null : where.getWherePart();
	}

	public Connection getConnection() {
		return connection;
	}

	public abstract PreparedStatement create();
}
