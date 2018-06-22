package io.github.aquerr.eaglefactions.listeners;

import io.github.aquerr.eaglefactions.EagleFactions;
import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.caching.FactionsCache;
import io.github.aquerr.eaglefactions.entities.Faction;
import io.github.aquerr.eaglefactions.logic.FactionLogic;
import io.github.aquerr.eaglefactions.logic.MainLogic;
import io.github.aquerr.eaglefactions.logic.PluginMessages;
import io.github.aquerr.eaglefactions.managers.PlayerManager;
import io.github.aquerr.eaglefactions.managers.PowerManager;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.entity.DestructEntityEvent;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class PlayerDeathListener {
    @Listener
    public void onPlayerDeath(DestructEntityEvent.Death event) {
        if (event.getTargetEntity() instanceof Player) {
            Player player = (Player) event.getTargetEntity();

            PowerManager.decreasePower(player.getUniqueId());

            player.sendMessage(Text.of(PluginInfo.PluginPrefix, PluginMessages.YOUR_POWER_HAS_BEEN_DECREASED_BY + " ", TextColors.GOLD, String.valueOf(MainLogic.getPowerDecrement()) + "\n",
                    TextColors.GRAY, PluginMessages.CURRENT_POWER + " ", String.valueOf(PowerManager.getPlayerPower(player.getUniqueId())) + "/" + String.valueOf(PowerManager.getPlayerMaxPower(player.getUniqueId()))));

            Optional<Faction> optionalChunkFaction = FactionsCache.getInstance().getFactionByChunk(player.getWorld().getUniqueId(), player.getLocation().getChunkPosition());

            if (MainLogic.getWarZoneWorldNames().contains(player.getWorld().getName()) || (optionalChunkFaction.isPresent() && optionalChunkFaction.get().name.equals("WarZone"))) {
                PlayerManager.setDeathInWarZone(player.getUniqueId(), true);
            }

            if (MainLogic.shouldBlockHomeAfterDeathInOwnFaction()) {
                Optional<Faction> optionalPlayerFaction = FactionsCache.getInstance().getFactionByPlayer(player.getUniqueId());

                if (optionalChunkFaction.isPresent() && optionalPlayerFaction.isPresent() && optionalChunkFaction.get().name.equals(optionalPlayerFaction.get().name)) {
                    if (EagleFactions.BlockedHome.containsKey(player.getUniqueId())) {
                        EagleFactions.BlockedHome.replace(player.getUniqueId(), MainLogic.getHomeBlockTimeAfterDeath());
                    } else {
                        EagleFactions.BlockedHome.put(player.getUniqueId(), MainLogic.getHomeBlockTimeAfterDeath());
                        Task.Builder taskBuilder = Sponge.getScheduler().createTaskBuilder();
                        taskBuilder.interval(1, TimeUnit.SECONDS).execute(task -> {
                            if (EagleFactions.BlockedHome.containsKey(player.getUniqueId())) {
                                int seconds = EagleFactions.BlockedHome.get(player.getUniqueId());

                                if (seconds <= 0) {
                                    EagleFactions.BlockedHome.remove(player.getUniqueId());
                                    task.cancel();
                                } else {
                                    EagleFactions.BlockedHome.replace(player.getUniqueId(), seconds, seconds - 1);
                                }
                            }
                        }).submit(EagleFactions.getPlugin());
                    }
                }
            }
        }
    }
}
