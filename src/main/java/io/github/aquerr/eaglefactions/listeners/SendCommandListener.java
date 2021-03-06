package io.github.aquerr.eaglefactions.listeners;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.github.aquerr.eaglefactions.EagleFactions;
import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.caching.FactionsCache;
import io.github.aquerr.eaglefactions.config.Settings;
import io.github.aquerr.eaglefactions.entities.Faction;
import io.github.aquerr.eaglefactions.logic.PVPLogger;
import io.github.aquerr.eaglefactions.logic.PluginMessages;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.command.SendCommandEvent;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.Optional;

@Singleton
public class SendCommandListener extends GenericListener {

    private PVPLogger pvpLogger;

    @Inject
    SendCommandListener(FactionsCache cache, Settings settings, EagleFactions eagleFactions, PVPLogger pvpLogger) {
        super(cache, settings, eagleFactions);
        this.pvpLogger = pvpLogger;
    }

    @Listener(order = Order.EARLY)
    public void onCommandSend(SendCommandEvent event, @Root Player player) {
        if (pvpLogger.isActive() && pvpLogger.shouldBlockCommand(player, event.getCommand() + " " + event.getArguments())) {
            player.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, PluginMessages.YOU_CANT_USE_COMMAND_WHILE_BEING_IN_A_FIGHT));
            player.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, PluginMessages.TIME_LEFT + " ", TextColors.YELLOW, pvpLogger.getPlayerBlockTime(player) + " " + PluginMessages.SECONDS));
            event.setCancelled(true);
            return;
        }

        //Analyze command with perms
        //Always leave a way out
        if (event.getCommand().matches("^(f)|(factions)|(faction)$") && (event.getArguments().equals("leave") || event.getArguments().equals("disband"))) {
            return;
        }

        Optional<Faction> faction = FactionsCache.getInstance().getFactionByPlayer(player.getUniqueId());
        if (faction.isPresent()) {
            String node = event.getCommand() + " " + event.getArguments();
            if (!EagleFactions.AdminList.contains(player.getUniqueId()) && !faction.get().getMember(player.getUniqueId().toString()).hasNode(node.toLowerCase(), faction.get())) {
                event.setCancelled(true);
                player.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, "Your faction does not allow you to do this! (", TextColors.DARK_RED, "/f leave", TextColors.RED, " is always available)"));
            }
        }

    }
}
