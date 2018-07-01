package io.github.aquerr.eaglefactions.listeners;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.github.aquerr.eaglefactions.EagleFactions;
import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.caching.FactionsCache;
import io.github.aquerr.eaglefactions.config.Settings;
import io.github.aquerr.eaglefactions.entities.Faction;
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

@Singleton
public class PlayerDeathListener extends GenericListener
{

    private PowerManager powerManager;

    @Inject
    PlayerDeathListener(FactionsCache cache, Settings settings, EagleFactions eagleFactions, PowerManager powerManager)
    {
        super(cache, settings, eagleFactions);
        this.powerManager = powerManager;
    }

    @Listener
    public void onPlayerDeath(DestructEntityEvent.Death event)
    {
        if (event.getTargetEntity() instanceof Player) {
            Player player = (Player) event.getTargetEntity();

            powerManager.decreasePower(player.getUniqueId());

            player.sendMessage(Text.of(PluginInfo.PluginPrefix, PluginMessages.YOUR_POWER_HAS_BEEN_DECREASED_BY + " ", TextColors.GOLD, String.valueOf(settings.getPowerDecrement()) + "\n",
                    TextColors.GRAY, PluginMessages.CURRENT_POWER + " ", String.valueOf(powerManager.getPlayerPower(player.getUniqueId())) + "/" + String.valueOf(powerManager.getPlayerMaxPower(player.getUniqueId()))));

            Optional<Faction> optionalChunkFaction = FactionsCache.getInstance().getFactionByChunk(player.getWorld().getUniqueId(), player.getLocation().getChunkPosition());

            if (settings.getWarZoneWorldNames().contains(player.getWorld().getName()) || (optionalChunkFaction.isPresent() && optionalChunkFaction.get().name.equals("WarZone"))) {
                PlayerManager.setDeathInWarZone(player.getUniqueId(), true);
            }

            if (settings.shouldBlockHomeAfterDeathInOwnFaction()) {
                Optional<Faction> optionalPlayerFaction = FactionsCache.getInstance().getFactionByPlayer(player.getUniqueId());

                if (optionalChunkFaction.isPresent() && optionalPlayerFaction.isPresent() && optionalChunkFaction.get().name.equals(optionalPlayerFaction.get().name)) {
                    if (EagleFactions.BlockedHome.containsKey(player.getUniqueId())) {
                        EagleFactions.BlockedHome.replace(player.getUniqueId(), settings.getHomeBlockTimeAfterDeath());
                    } else {
                        EagleFactions.BlockedHome.put(player.getUniqueId(), settings.getHomeBlockTimeAfterDeath());
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
