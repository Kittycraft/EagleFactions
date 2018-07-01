package io.github.aquerr.eaglefactions.listeners;

import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.math.vector.Vector3i;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.github.aquerr.eaglefactions.EagleFactions;
import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.PluginPermissions;
import io.github.aquerr.eaglefactions.caching.FactionsCache;
import io.github.aquerr.eaglefactions.config.Settings;
import io.github.aquerr.eaglefactions.entities.Faction;
import io.github.aquerr.eaglefactions.logic.PluginMessages;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.entity.living.humanoid.HandInteractEvent;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.Optional;

@Singleton
public class PlayerInteractListener extends GenericListener {

    @Inject
    PlayerInteractListener(FactionsCache cache, Settings settings, EagleFactions eagleFactions) {
        super(cache, settings, eagleFactions);
    }

    @Listener
    public void onPlayerInteract(HandInteractEvent event, @Root Player player) {
        if (!EagleFactions.AdminList.contains(player.getUniqueId())) {
            if (event.getInteractionPoint().isPresent()) {
                World world = player.getWorld();

                if (settings.getSafeZoneWorldNames().contains(world.getName()) && player.hasPermission(PluginPermissions.SAFE_ZONE_INTERACT)) {
                    return;
                }
                if (settings.getWarZoneWorldNames().contains(world.getName()) && player.hasPermission(PluginPermissions.WAR_ZONE_INTERACT)) {
                    return;
                }

                Vector3d vector3d = event.getInteractionPoint().get();
                Location location = new Location(world, vector3d);
                Vector3i claim = location.getChunkPosition();

                Optional<Faction> optionalPlayerFaction = FactionsCache.getInstance().getFactionByPlayer(player.getUniqueId());
                Optional<Faction> optionalChunkFaction = FactionsCache.getInstance().getFactionByChunk(world.getUniqueId(), claim);

                if (optionalChunkFaction.isPresent()) {
                    if (optionalChunkFaction.get().name.equals("SafeZone") && player.hasPermission(PluginPermissions.SAFE_ZONE_INTERACT)) {
                        return;
                    } else if (optionalChunkFaction.get().name.equals("WarZone") && player.hasPermission(PluginPermissions.WAR_ZONE_INTERACT)) {
                        return;
                    } else if (optionalPlayerFaction.isPresent()) {
                        //TODO: Find out if allies give permission for interacting (Reference: optionalChunkFaction.get())
                        if (!optionalPlayerFaction.get().isAllowed(player.getUniqueId().toString(), "interact")) {
                            player.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, PluginMessages.YOU_DONT_HAVE_PRIVILEGES_TO_INTERACT_HERE));
                            event.setCancelled(true);
                        }
                    } else {
                        event.setCancelled(true);
                        player.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, PluginMessages.YOU_DONT_HAVE_ACCESS_TO_DO_THIS));
                        return;
                    }
                }
            }
        }
    }
}
