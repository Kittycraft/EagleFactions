package io.github.aquerr.eaglefactions.managers;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.service.user.UserStorageService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.UUID;

/**
 * Created by Aquerr on 2017-08-04.
 */
//TODO: Review the use of knowing if a player died in the warzone.
@Singleton
public class PlayerManager {
    private Path playersPath;

    @Inject
    PlayerManager(@Named("config dir") Path configDir) {
        try {
            playersPath = configDir.resolve("players");
            if (!Files.exists(playersPath)) Files.createDirectory(playersPath);
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    public Optional<String> getPlayerName(UUID playerUUID) {
        Optional<User> oUser = getUser(playerUUID);

        return Optional.of(oUser.get().getName());
    }

    public Optional<Player> getPlayer(UUID playerUUID) {
        Optional<User> oUser = getUser(playerUUID);

        return oUser.get().getPlayer();
    }

    private Optional<User> getUser(UUID playerUUID) {
        UserStorageService userStorageService = Sponge.getServiceManager().provideUnchecked(UserStorageService.class);
        Optional<User> oUser = userStorageService.get(playerUUID);

        if (oUser.isPresent()) {
            return oUser;
        } else return Optional.empty();
    }

    public boolean isPlayerOnline(UUID playerUUID) {
        Optional<User> oUser = getUser(playerUUID);

        if (oUser.isPresent()) {
            return oUser.get().isOnline();
        } else return false;
    }

    public void setDeathInWarZone(UUID playerUUID, boolean didDieInWarZone) {
        Path playerFile = Paths.get(playersPath + "/" + playerUUID.toString() + ".conf");

        try {
            ConfigurationLoader<CommentedConfigurationNode> configLoader = HoconConfigurationLoader.builder().setPath(playerFile).build();

            CommentedConfigurationNode playerNode = configLoader.load();

            playerNode.getNode("death-in-warzone").setValue(didDieInWarZone);

            configLoader.save(playerNode);
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    public boolean lastDeathAtWarZone(UUID playerUUID) {
        Path playerFile = Paths.get(playersPath + "/" + playerUUID.toString() + ".conf");

        try {
            ConfigurationLoader<CommentedConfigurationNode> configLoader = HoconConfigurationLoader.builder().setPath(playerFile).build();

            CommentedConfigurationNode playerNode = configLoader.load();

            Object value = playerNode.getNode("death-in-warzone").getValue();

            if (value != null) {
                return (boolean) value;
            } else {
                playerNode.getNode("death-in-warzone").setValue(false);
                configLoader.save(playerNode);
                return false;
            }
        } catch (IOException exception) {
            exception.printStackTrace();
        }

        return false;
    }
}
