package nl.riebie.mcclans.persistence.query.table;

import nl.riebie.mcclans.persistence.query.DataType;

import java.sql.PreparedStatement;

/**
 * Created by riebie on 30/04/2016.
 */
public class CreateQueryColumn <T>{

    private final CreateQuery createQuery;
    private final String key;
    private final DataType dataType;
    private boolean notNull;
    private Object defaultValue;

    public <T> CreateQueryColumn(CreateQuery createQuery, String key, DataType<T> dataType) {
        this.createQuery = createQuery;
        this.key = key;
        this.dataType = dataType;
    }

    public CreateQueryColumn<T> setNotNull() {
        notNull = true;
        return this;
    }

    public CreateQueryColumn<T> addToPrimaryKey() {
        createQuery.addPrimaryKey(key);
        return this;
    }

    public CreateQueryColumn<T> setDefaultValue(T defaultValue) {
        this.defaultValue = defaultValue;
        return this;
    }

    public <T> CreateQueryColumn<T> column(String key, DataType<T> dataType) {
        return createQuery.column(key, dataType);
    }

    protected DataType getDataType() {
        return dataType;
    }

    protected boolean isNotNull() {
        return notNull;
    }

    protected Object getDefaultValue() {
        return defaultValue;
    }

    public String getKey(){
        return key;
    }
}
