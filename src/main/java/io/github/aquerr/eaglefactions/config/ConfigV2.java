package io.github.aquerr.eaglefactions.config;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.slf4j.Logger;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

public class ConfigV2 {

    private Path configDir;
    private Logger logger;
    private JsonObject settings;

    @Inject
    ConfigV2(@Named("config dir") Path configDir, @Named("factions") Logger logger) {
        this.configDir = configDir;
        loadFile();
    }

    private void loadFile() {
        try {
            JsonParser parser = new JsonParser();
            settings = parser.parse(new FileReader(configDir.resolve("instance.json").toFile())).getAsJsonObject();
        } catch (FileNotFoundException e) {
            logger.warn("Config file not found!");
            resetConfig();
            loadFile();
        }
    }

    private void resetConfig() {
        logger.warn("Creating new config file!");
        File config = configDir.toFile();
        if (!config.exists()) {
            config.mkdir();
        }

        Path configDestination = configDir.resolve("instance.json");

        try {
            if (Files.exists(configDestination)) {
                logger.warn("Deleting old config...");
                Files.delete(configDestination);
            }
            InputStream inputStream = ConfigV2.class.getClassLoader().getResourceAsStream("instance.json");

            Files.copy(inputStream, configDestination);
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getString(Setting node) {
        try {
            if(settings.has(node.getPath())){
                return settings.get(node.getPath()).getAsString();
            }else{
                logger.warn("Could not find the value \"" + node.getPath() + "\" in config!");
            }
        } catch (ClassCastException e) {
            logger.warn("String value \"" + node.name() + "\" has an illegal type in config!", e);
        } catch (IllegalStateException e) {
            logger.warn("String value \"" + node.name() + "\" can not be an array in config!");
        }
        setValue(node.getPath(), node.getDefaultValue());
        return (String) node.getDefaultValue();
    }

    public boolean getBoolean(Setting node) {
        try {
            if(settings.has(node.getPath())){
                return settings.get(node.getPath()).getAsBoolean();;
            }else{
                logger.warn("Could not find the value \"" + node.getPath() + "\" in config!");
            }
        } catch (ClassCastException e) {
            logger.warn("String value \"" + node.name() + "\" has an illegal type in config!", e);
        } catch (IllegalStateException e) {
            logger.warn("String value \"" + node.name() + "\" can not be an array in config!");
        }
        setValue(node.getPath(), node.getDefaultValue());
        return (boolean) node.getDefaultValue();
    }

    public double getNumber(String node) {
        try {
            String value = settings.get(node.getPath()).getAsString();
            if(value != null){
                return value;
            }else{
                logger.warn("Could not find the value \"" + node.getPath() + "\" in config!");
            }
        } catch (ClassCastException e) {
            logger.warn("String value \"" + node.name() + "\" has an illegal type in config!", e);
        } catch (IllegalStateException e) {
            logger.warn("String value \"" + node.name() + "\" can not be an array in config!");
        }
        setValue(node.getPath(), node.getDefaultValue());
        return (String) node.getDefaultValue();
    }

    public void setValue(String node, Object value) {
        configuration.setValue(value, node);
    }

}
