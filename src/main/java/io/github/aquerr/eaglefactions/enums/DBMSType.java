package io.github.aquerr.eaglefactions.enums;

import io.github.aquerr.eaglefactions.EagleFactions;

public enum DBMSType {
    MYSQL, H2;

    public static DBMSType getType(String type) {
        try {
            return DBMSType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            EagleFactions.getLogger().warn("Could not get database type. Reverting to default (MySQL)");
            return MYSQL;
        }
    }
}
