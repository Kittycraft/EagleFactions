package io.github.aquerr.eaglefactions.managers;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import io.github.aquerr.eaglefactions.EagleFactions;
import io.github.aquerr.eaglefactions.entities.Faction;
import io.github.aquerr.eaglefactions.entities.FactionPlayer;
import io.github.aquerr.eaglefactions.config.Settings;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.scheduler.Task;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@Singleton
public class PowerManager {
    private static CommentedConfigurationNode _factionsNode;
    private static Path playersPath;
    private Settings settings;

    @Inject
    PowerManager(@Named("config dir") Path configDir, Settings settings) {
        this.settings = settings;
        try {
            _factionsNode = HoconConfigurationLoader.builder().setPath(Paths.get(configDir.resolve("data") + "/factions.conf")).build().load();
            playersPath = configDir.resolve("players");
            if (!Files.exists(playersPath)) Files.createDirectory(playersPath);
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    public boolean checkIfPlayerExists(UUID playerUUID) {
        Path playerFile = Paths.get(playersPath + "/" + playerUUID.toString() + ".conf");
        if (Files.exists(playerFile)) {
            return true;
        } else {
            return false;
        }
    }

    public void addPlayer(UUID playerUUID) {
        Path playerFile = Paths.get(playersPath + "/" + playerUUID.toString() + ".conf");

        try {
            Files.createFile(playerFile);

            ConfigurationLoader<CommentedConfigurationNode> configLoader = HoconConfigurationLoader.builder().setPath(playerFile).build();

            CommentedConfigurationNode playerNode = configLoader.load();

            playerNode.getNode("power").setValue(settings.getStartingPower());
            playerNode.getNode("maxpower").setValue(settings.getGlobalMaxPower());
            configLoader.save(playerNode);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public BigDecimal getPlayerPower(UUID playerUUID) {
        Path playerFile = Paths.get(playersPath + "/" + playerUUID.toString() + ".conf");

        if (checkIfPlayerExists(playerUUID)) {
            try {
                ConfigurationLoader<CommentedConfigurationNode> configLoader = HoconConfigurationLoader.builder().setPath(playerFile).build();

                CommentedConfigurationNode playerNode = configLoader.load();

                if (playerNode.getNode("power").getValue() != null) {
                    BigDecimal playerPower = new BigDecimal(playerNode.getNode("power").getString());
                    return playerPower;
                }
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        } else {
            addPlayer(playerUUID);
            return getPlayerPower(playerUUID);
        }
        return BigDecimal.ZERO;
    }

    public BigDecimal getFactionPower(Faction faction) {
        if (faction.name.equals("SafeZone") || faction.name.equals("WarZone")) {
            ConfigurationNode powerNode = _factionsNode.getNode("factions", faction.name, "power");

            BigDecimal factionPowerInFile = new BigDecimal(powerNode.getDouble());

            return factionPowerInFile;
        }

        BigDecimal factionPower = BigDecimal.ZERO;
        if (faction.members != null && !faction.members.isEmpty()) {
            for (FactionPlayer member : faction.members) {
                BigDecimal memberPower = getPlayerPower(UUID.fromString(member.uuid));
                factionPower = factionPower.add(memberPower);
            }
        }
        return factionPower;
    }

    public BigDecimal getFactionMaxPower(Faction faction) {
        if (faction.name.equals("SafeZone") || faction.name.equals("WarZone")) {
            ConfigurationNode powerNode = _factionsNode.getNode("factions", faction.name, "power");

            BigDecimal factionPowerInFile = new BigDecimal(powerNode.getDouble());

            return factionPowerInFile;
        }

        BigDecimal factionMaxPower = BigDecimal.ZERO;

        if (faction.members != null && !faction.members.isEmpty()) {
            for (FactionPlayer member : faction.members) {
                factionMaxPower = factionMaxPower.add(getPlayerMaxPower(UUID.fromString(member.uuid)));
            }
        }

        return factionMaxPower;
    }

    public BigDecimal getPlayerMaxPower(UUID playerUUID) {
        Path playerFile = Paths.get(playersPath + "/" + playerUUID.toString() + ".conf");

        try {
            ConfigurationLoader<CommentedConfigurationNode> configLoader = HoconConfigurationLoader.builder().setPath(playerFile).build();

            CommentedConfigurationNode playerNode = configLoader.load();

            Object value = playerNode.getNode("maxpower").getValue();

            if (value != null) {
                BigDecimal playerMaxPower = new BigDecimal(value.toString());

                return playerMaxPower;
            } else {
                playerNode.getNode("maxpower").setValue(settings.getGlobalMaxPower());

                return settings.getGlobalMaxPower();
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }

        return BigDecimal.ZERO;
    }

    public void addPower(UUID playerUUID, boolean isKillAward) {
        Path playerFile = Paths.get(playersPath + "/" + playerUUID.toString() + ".conf");

        try {
            ConfigurationLoader<CommentedConfigurationNode> configLoader = HoconConfigurationLoader.builder().setPath(playerFile).build();

            CommentedConfigurationNode playerNode = configLoader.load();

            BigDecimal playerPower = new BigDecimal(playerNode.getNode("power").getString());

            if (getPlayerPower(playerUUID).add(settings.getPowerIncrement()).doubleValue() < getPlayerMaxPower(playerUUID).doubleValue()) {
                if (isKillAward) {
                    BigDecimal killAward = settings.getKillAward();
                    playerNode.getNode("power").setValue(playerPower.add(killAward));
                } else {
                    playerNode.getNode("power").setValue(playerPower.add(settings.getPowerIncrement()));
                }

                configLoader.save(playerNode);
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }

    }

    public void setPower(UUID playerUUID, BigDecimal power) {
        Path playerFile = Paths.get(playersPath + "/" + playerUUID.toString() + ".conf");

        try {
            ConfigurationLoader<CommentedConfigurationNode> configLoader = HoconConfigurationLoader.builder().setPath(playerFile).build();

            CommentedConfigurationNode playerNode = configLoader.load();

            playerNode.getNode("power").setValue(power);
            configLoader.save(playerNode);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public void startIncreasingPower(UUID playerUUID) {
        Task.Builder taskBuilder = Sponge.getScheduler().createTaskBuilder();

        taskBuilder.interval(1, TimeUnit.MINUTES).execute(task -> {
            if (!PlayerManager.isPlayerOnline(playerUUID)) task.cancel();

            if (getPlayerPower(playerUUID).add(settings.getPowerIncrement()).doubleValue() < getPlayerMaxPower(playerUUID).doubleValue()) {
                addPower(playerUUID, false);
            } else {
                setPower(playerUUID, getPlayerMaxPower(playerUUID));
            }
        }).submit(EagleFactions.getPlugin());
    }

    public void decreasePower(UUID playerUUID) {
        if (getPlayerPower(playerUUID).subtract(settings.getPowerDecrement()).doubleValue() > BigDecimal.ZERO.doubleValue()) {
            Path playerFile = Paths.get(playersPath + "/" + playerUUID.toString() + ".conf");

            try {
                ConfigurationLoader<CommentedConfigurationNode> configLoader = HoconConfigurationLoader.builder().setPath(playerFile).build();

                CommentedConfigurationNode playerNode = configLoader.load();

                BigDecimal playerPower = new BigDecimal(playerNode.getNode("power").getString());

                playerNode.getNode("power").setValue(playerPower.subtract(settings.getPowerDecrement()));
                configLoader.save(playerNode);
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        } else {
            setPower(playerUUID, BigDecimal.ZERO);
        }
    }

    public void penalty(UUID playerUUID) {
        Path playerFile = Paths.get(playersPath + "/" + playerUUID.toString() + ".conf");

        try {
            ConfigurationLoader<CommentedConfigurationNode> configLoader = HoconConfigurationLoader.builder().setPath(playerFile).build();

            CommentedConfigurationNode playerNode = configLoader.load();

            BigDecimal playerPower = new BigDecimal(playerNode.getNode("power").getString());

            BigDecimal penalty = settings.getPenalty();

            if (playerPower.doubleValue() - penalty.doubleValue() > 0) {
                playerNode.getNode("power").setValue(playerPower.subtract(penalty));
//                updateFactionPower(playerUUID, penalty, false);
            } else {
                playerNode.getNode("power").setValue(0.0);
//                updateFactionPower(playerUUID, penalty, false);
            }

            configLoader.save(playerNode);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public void setMaxPower(UUID playerUUID, BigDecimal power) {
        Path playerFile = Paths.get(playersPath + "/" + playerUUID.toString() + ".conf");

        try {
            ConfigurationLoader<CommentedConfigurationNode> configLoader = HoconConfigurationLoader.builder().setPath(playerFile).build();

            CommentedConfigurationNode playerNode = configLoader.load();

            playerNode.getNode("maxpower").setValue(power);
            configLoader.save(playerNode);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

}
