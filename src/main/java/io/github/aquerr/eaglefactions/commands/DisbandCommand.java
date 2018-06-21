package io.github.aquerr.eaglefactions.commands;

import io.github.aquerr.eaglefactions.EagleFactions;
import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.caching.FactionsCache;
import io.github.aquerr.eaglefactions.entities.Faction;
import io.github.aquerr.eaglefactions.logic.FactionLogic;
import io.github.aquerr.eaglefactions.logic.PluginMessages;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.Optional;

//TODO: Allow players to specify a faction that they want to disband.
public class DisbandCommand implements CommandExecutor {
    @Override
    public CommandResult execute(CommandSource source, CommandContext context) throws CommandException {
        if (source instanceof Player) {
            Player player = (Player) source;
            Optional<Faction> optionalPlayerFaction = FactionsCache.getInstance().getFactionByPlayer(player.getUniqueId());
            if (optionalPlayerFaction.isPresent()) {
                FactionsCache.getInstance().removeFaction(optionalPlayerFaction.get().name);
                player.sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.GREEN, "Faction has been disbanded."));

                if (EagleFactions.AutoClaimList.contains(player.getUniqueId())) {
                    EagleFactions.AutoClaimList.remove(player.getUniqueId());
                }
            } else {
                player.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, "You must be in a faction in order to use this command!"));
            }
        } else {
            source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, "Only in game players can use this command!"));
        }

        return CommandResult.success();
    }
}
