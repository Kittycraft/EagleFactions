package io.github.aquerr.eaglefactions.listeners;

import com.flowpowered.math.vector.Vector3i;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.github.aquerr.eaglefactions.EagleFactions;
import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.caching.FactionsCache;
import io.github.aquerr.eaglefactions.config.Settings;
import io.github.aquerr.eaglefactions.entities.Faction;
import io.github.aquerr.eaglefactions.logic.PluginMessages;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.World;

import java.util.Optional;

@Singleton
public class BlockBreakListener extends GenericListener {

    @Inject
    public BlockBreakListener(FactionsCache cache, Settings settings, EagleFactions eagleFactions) {
        super(cache, settings, eagleFactions);
    }

    @Listener
    public void onBlockBreak(ChangeBlockEvent.Break event) {
        if (event.getCause().root() instanceof Player) {
            Player player = (Player) event.getCause().root();

            if (!EagleFactions.AdminList.contains(player.getUniqueId())) {
                for (Transaction<BlockSnapshot> transaction : event.getTransactions()) {
                    World world = player.getWorld();

                    if (settings.getSafeZoneWorldNames().contains(world.getName())) {
                        event.setCancelled(true);
                        return;
                    } else if (settings.getWarZoneWorldNames().contains(world.getName()) && settings.isBlockDestroyingInWarZoneDisabled()) {
                        event.setCancelled(true);
                        return;
                    }

                    Vector3i claim = transaction.getFinal().getLocation().get().getChunkPosition();

                    Optional<Faction> optionalPlayerFaction = FactionsCache.getInstance().getFactionByPlayer(player.getUniqueId());

                    Optional<Faction> optionalChunkFaction = FactionsCache.getInstance().getFactionByChunk(world.getUniqueId(), claim);

                    if (optionalChunkFaction.isPresent()) {
                        if (optionalChunkFaction.get().name.equals("SafeZone") && player.hasPermission("eaglefactions.safezone.build")) {
                            return;
                        } else if (optionalChunkFaction.get().name.equals("WarZone") && player.hasPermission("eaglefactions.warzone.build")) {
                            return;
                        } else if (optionalPlayerFaction.isPresent()) {
                            //TODO: Find out if allies give permission for building (Reference: optionalChunkFaction.get())
                            if (!optionalPlayerFaction.get().isAllowed(player.getUniqueId().toString(), "build")) {
                                player.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, PluginMessages.YOU_DONT_HAVE_PRIVILEGES_TO_DESTROY_BLOCKS_HERE));
                                event.setCancelled(true);
                                return;
                            }
                        } else {
                            event.setCancelled(true);
                            player.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, PluginMessages.THIS_LAND_BELONGS_TO_SOMEONE_ELSE));
                            return;
                        }
                    }
                }
            }
        } else {
            for (Transaction<BlockSnapshot> transaction : event.getTransactions()) {
                World world = transaction.getFinal().getLocation().get().getExtent();

                if (settings.getSafeZoneWorldNames().contains(world.getName())) {
                    event.setCancelled(true);
                    return;
                } else if (settings.getWarZoneWorldNames().contains(world.getName()) && settings.isBlockDestroyingInWarZoneDisabled()) {
                    event.setCancelled(true);
                    return;
                }

                Vector3i claim = transaction.getFinal().getLocation().get().getChunkPosition();
                Optional<Faction> optionalChunkFaction = FactionsCache.getInstance().getFactionByChunk(world.getUniqueId(), claim);

                if (optionalChunkFaction.isPresent()) {
                    if (!optionalChunkFaction.get().name.equals("SafeZone") && !optionalChunkFaction.get().name.equals("WarZone") && settings.isBlockDestroyingDisabled()) {
                        event.setCancelled(true);
                        return;
                    } else if (optionalChunkFaction.get().name.equals("SafeZone")) {
                        event.setCancelled(true);
                        return;
                    } else if (optionalChunkFaction.get().name.equals("WarZone") && settings.isBlockDestroyingInWarZoneDisabled()) {
                        event.setCancelled(true);
                        return;
                    }
                }
            }
        }
    }
}
