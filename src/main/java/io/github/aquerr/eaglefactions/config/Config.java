package io.github.aquerr.eaglefactions.config;

import io.github.aquerr.eaglefactions.EagleFactions;

//Link between mcclans database logic and config
public class Config {
    private static Configuration configuration = EagleFactions.getEagleFactions().getConfiguration();

    //General
    public static final String DEBUGGING = "debugging";

    //Database
    public static final String DBMS_TYPE = "dbms-type";
    public static final String USE_DATABASE = "use-database";
    public static final String DATABASE_NAME = "database-name";
    public static final String CREATE_BACKUP_AFTER_HOURS = "create-backup-after-hours";
    public static final String DATABASE_SERVER = "database-server";
    public static final String DATABASE_SERVER_PORT = "database-server-port";
    public static final String DATABASE_SERVER_USER = "database-server-user";
    public static final String DATABASE_SERVER_PASSWORD = "database-server-password";


    public static String getString(String node) {
        return configuration.getString("", node);
    }

    public static boolean getBoolean(String node) {
        return configuration.getBoolean(false, node);
    }

    public static int getInteger(String node) {
        return configuration.getInt(0, node);
    }

    public static double getDouble(String node) {
        return configuration.getDouble(0, node);
    }

    //Unsafe! But convenient :D
    public static void setValue(String node, Object value){
        configuration.setValue(value, node);
    }
}
