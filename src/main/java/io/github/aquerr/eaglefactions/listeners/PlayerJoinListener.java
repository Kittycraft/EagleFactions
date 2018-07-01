package io.github.aquerr.eaglefactions.listeners;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.github.aquerr.eaglefactions.EagleFactions;
import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.PluginPermissions;
import io.github.aquerr.eaglefactions.caching.FactionsCache;
import io.github.aquerr.eaglefactions.config.Settings;
import io.github.aquerr.eaglefactions.logic.PluginMessages;
import io.github.aquerr.eaglefactions.managers.PowerManager;
import io.github.aquerr.eaglefactions.version.VersionChecker;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

@Singleton
public class PlayerJoinListener extends GenericListener
{

    private PowerManager powerManager;
    @Inject
    PlayerJoinListener(FactionsCache cache, Settings settings, EagleFactions eagleFactions, PowerManager powerManager)
    {
        super(cache, settings, eagleFactions);
        this.powerManager = powerManager;
    }

    @Listener
    public void onPlayerJoin(ClientConnectionEvent.Join event)
    {

        if (event.getCause().root() instanceof Player)
        {
            Player player = (Player) event.getCause().root();

            if (player.hasPermission(PluginPermissions.VersionNotify) && !VersionChecker.isLatest(PluginInfo.Version))
            {
                player.sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.GREEN, PluginMessages.A_NEW_VERSION_OF + " ", TextColors.AQUA, "Eagle Factions", TextColors.GREEN, " " + PluginMessages.IS_AVAILABLE));
            }

            if (powerManager.checkIfPlayerExists(player.getUniqueId()))
            {
                powerManager.startIncreasingPower(player.getUniqueId());
            } else
            {
                //Create player file and set power.
                powerManager.addPlayer(player.getUniqueId());
            }

            //Check if the world that player is connecting to is already in the config file
            if (!settings.getDetectedWorldNames().contains(player.getWorld().getName()))
            {
                settings.addWorld(player.getWorld().getName());
            }
        }
    }
}
