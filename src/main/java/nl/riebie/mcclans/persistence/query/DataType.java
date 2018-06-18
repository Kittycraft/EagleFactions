package nl.riebie.mcclans.persistence.query;

/**
 * Created by riebie on 30/04/2016.
 */
public class DataType<T> {
    public static final DataType<Integer> INTEGER = new DataType<>("INT(11)");
    public static final DataType<String> STRING  = new DataType<>("VARCHAR(255)");
    public static final DataType<Boolean> BOOLEAN = new DataType<>("TINYINT(1)");
    public static final DataType<Long> LONG    = new DataType<>("BIGINT");
    public static final DataType<Double> DOUBLE  = new DataType<>("DOUBLE");
    public static final DataType<Float> FLOAT   = new DataType<>("FLOAT");

    private String databaseType;

    DataType(String databaseType) {
        this.databaseType = databaseType;
    }

    public String getDatabaseType() {
        return databaseType;
    }
}
