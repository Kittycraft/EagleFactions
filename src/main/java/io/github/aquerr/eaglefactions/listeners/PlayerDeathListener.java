package io.github.aquerr.eaglefactions.listeners;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.github.aquerr.eaglefactions.EagleFactions;
import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.caching.FactionsCache;
import io.github.aquerr.eaglefactions.config.ConfigV2;
import io.github.aquerr.eaglefactions.config.Setting;
import io.github.aquerr.eaglefactions.config.Settings;
import io.github.aquerr.eaglefactions.entities.Faction;
import io.github.aquerr.eaglefactions.logic.PluginMessages;
import io.github.aquerr.eaglefactions.managers.PlayerManager;
import io.github.aquerr.eaglefactions.managers.PowerManager;
import javafx.scene.layout.Priority;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.EventListener;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.entity.DestructEntityEvent;
import org.spongepowered.api.event.entity.living.humanoid.player.RespawnPlayerEvent;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Singleton
public class PlayerDeathListener extends GenericListener {

    private PlayerManager playerManager;

    @Inject
    PlayerDeathListener(FactionsCache cache, ConfigV2 settings, EagleFactions eagleFactions, PlayerManager playerManager) {
        super(cache, settings);
        this.playerManager = playerManager;
        Sponge.getEventManager().registerListener(eagleFactions, DestructEntityEvent.Death.class, new deathListener());
        if(settings.getBoolean(Setting.HOME_ON_DEATH)) {
            Sponge.getEventManager().registerListener(eagleFactions, RespawnPlayerEvent.class, EventPriorityParser.getOrder(settings.getString(Setting.HOME_ON_DEATH_PRIORITY)), new respawnListener());
        }
    }

    /**
     * Objective:
     *  1) If needed, teleport home on death.
     *  2) Tell the player if they lost power
     */

    private class deathListener implements EventListener<DestructEntityEvent.Death> {
        @Override
        public void handle(DestructEntityEvent.Death event) throws Exception {
            if (event.getSource() instanceof Player) {
                Player player = (Player) event.getSource();
                //TODO: Notify
                player.sendMessage(Text.of(TextColors.WHITE, "Woah, you just died! Umm... so what now?"));
                //<white><i>You didn't lose any power since the territory you died in works that way.
                //<white><i>You didn't lose any power due to the world you died in.
                //<white><i>Your power is now <h>%.2f / %.2f
            }
        }
    }

    private class respawnListener implements EventListener<RespawnPlayerEvent> {
        @Override
        public void handle(RespawnPlayerEvent event) throws Exception {
            //Check if it is player because of npcs and sponge black magic witch craft.
            if (event.getSource() instanceof Player && event.isDeath() && !event.isBedSpawn()) {
                Player player = (Player) event.getSource();
                if (settings.getBoolean(Setting.HOME_ON_DEATH)) {

                }
            }
        }
    }
}
