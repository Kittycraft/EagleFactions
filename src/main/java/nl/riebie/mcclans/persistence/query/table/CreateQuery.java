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
import java.util.List;

import nl.riebie.mcclans.persistence.query.DataType;

public class CreateQuery extends TableQuery {

    //	private Map<String, DataType> addedColumns = new HashMap<>();
    private List<CreateQueryColumn> addedColumns = new ArrayList<>();
    private List<String> primaryKeys = new ArrayList<>();

    public CreateQuery(String tableName, Connection connection) {
        super(tableName, connection);
    }

    public <T> CreateQueryColumn<T> column(String key, DataType<T> dataType) {
        CreateQueryColumn createQueryColumn = new CreateQueryColumn(this, key, dataType);
        addedColumns.add(createQueryColumn);
        return createQueryColumn;
    }

    protected void addPrimaryKey(String key){
        primaryKeys.add(key);
    }

    @Override
    public PreparedStatement create() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(String.format("CREATE TABLE IF NOT EXISTS `%s` (%s", getTableName(), getNewColumns()));
        if(!primaryKeys.isEmpty()){
            stringBuilder.append(String.format("PRIMARY KEY (`%s`)", getPrimaryKeys()));
        }
           stringBuilder.append(") ENGINE=InnoDB;") ;
        try {
            return  getConnection().prepareStatement(stringBuilder.toString());
        } catch (SQLException e) {
            e.printStackTrace();
        }
       return null;
    }

    private String getNewColumns(){
        StringBuilder stringBuilder = new StringBuilder();
        for(CreateQueryColumn<?> column : addedColumns){
            stringBuilder.append(String.format("`%s` %s", column.getKey(), column.getDataType().getDatabaseType()));
            if(column.isNotNull()){
                stringBuilder.append(" NOT NULL");
            } else if(column.getDefaultValue() != null){
                stringBuilder.append(String.format(" DEFAULT %s", column.getDefaultValue()));
            }
            stringBuilder.append(",");
        }
        return stringBuilder.toString();
    }

    private String getPrimaryKeys(){
        StringBuilder stringBuilder = new StringBuilder();
        for(String key : primaryKeys){
            if(stringBuilder.length() > 0){
                stringBuilder.append(", ");
            }
            stringBuilder.append(key);
        }
        return stringBuilder.toString();
    }

}
