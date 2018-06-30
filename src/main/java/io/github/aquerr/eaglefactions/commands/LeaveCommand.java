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

public class LeaveCommand implements CommandExecutor {
    @Override
    public CommandResult execute(CommandSource source, CommandContext context) throws CommandException {
        if (source instanceof Player) {
            Player player = (Player) source;

            Optional<Faction> optionalPlayerFaction = FactionsCache.getInstance().getFactionByPlayer(player.getUniqueId());

            if (optionalPlayerFaction.isPresent()) {
                if (!optionalPlayerFaction.get().owner.equals(player.getUniqueId().toString())) {
                    FactionLogic.informFaction(optionalPlayerFaction.get(), Text.of(PluginInfo.PluginPrefix, TextColors.WHITE,
                            player.getDisplayNameData().displayName().get(), TextColors.GREEN, " left the faction!"));
                    FactionLogic.leaveFaction(player.getUniqueId(), optionalPlayerFaction.get().name);

                    EagleFactions.AutoClaimList.remove(player.getUniqueId());
                } else {
                    source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, "You cant leave the faction because you are it's owner! Disband your faction or set someone else as the owner."));
                }
            } else {
                source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, "You need to be in a faction to use this command."));
            }
        } else {
            source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, "Only in game players can use this command."));
        }

        return CommandResult.success();
    }
}
