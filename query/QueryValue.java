/*
 * Copyright (c) 2016 riebie, Kippers <https) {//bitbucket.org/Kippers/mcclans-core-sponge>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions) {
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
import java.sql.SQLException;

public class QueryValue {

    private final DataType dataType;

    private final String dataValue;

    public QueryValue(DataType dataType, String dataValue) {
        this.dataType = dataType;
        this.dataValue = dataValue;
    }

    public void appendValue(PreparedStatement statement, int index) throws SQLException {
        index = index + 1;
        if (dataType == DataType.BOOLEAN) {
            statement.setBoolean(index, dataValue.equals("1"));
        } else if (dataType == DataType.INTEGER) {
            statement.setInt(index, Integer.parseInt(dataValue));
        } else if (dataType == DataType.STRING) {
            statement.setString(index, dataValue);
        } else if (dataType == DataType.DOUBLE) {
            statement.setDouble(index, Double.parseDouble(dataValue));
        } else if (dataType == DataType.FLOAT) {
            statement.setFloat(index, Float.parseFloat(dataValue));
        } else if (dataType == DataType.LONG) {
            statement.setLong(index, Long.parseLong(dataValue));
        }
    }
}
