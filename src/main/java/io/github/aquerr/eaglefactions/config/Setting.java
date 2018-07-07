package io.github.aquerr.eaglefactions.config;

public enum Setting {
    MAX_POWER("powerMax", 10),
    MIN_POWER("powerMin", 0),
    POWER_PER_HOUR("powerPerHour", 2),
    POWER_PER_DEATH("powerPerDeath", -2),
    HOME_ON_DEATH("homesTeleportToOnDeathActive", false),
    HOME_ON_DEATH_PRIORITY("homesTeleportToOnDeathPriority", "NORMAL");

    private final String path;
    private final Object defaultValue;

    Setting(String path, Object defaultValue){
        this.path = path;
        this.defaultValue = defaultValue;
    }

    public String getPath(){
        return path;
    }

    public Object getDefaultValue() {
        return defaultValue;
    }
}
