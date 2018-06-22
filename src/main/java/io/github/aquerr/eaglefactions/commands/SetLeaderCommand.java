package io.github.aquerr.eaglefactions.commands;

import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.caching.FactionsCache;
import io.github.aquerr.eaglefactions.entities.Faction;
import io.github.aquerr.eaglefactions.entities.FactionPlayer;
import io.github.aquerr.eaglefactions.logic.FactionLogic;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.Optional;

public class SetLeaderCommand implements CommandExecutor {
    @Override
    public CommandResult execute(CommandSource source, CommandContext context) throws CommandException {
        Optional<Player> optionalNewLeaderPlayer = context.getOne("player");

        if (optionalNewLeaderPlayer.isPresent()) {
            if (source instanceof Player) {
                Player player = (Player) source;
                Player newLeaderPlayer = optionalNewLeaderPlayer.get();
                Optional<Faction> optionalPlayerFaction = FactionsCache.getInstance().getFactionByPlayer(player.getUniqueId());
                Optional<Faction> optionalNewLeaderPlayerFaction = FactionsCache.getInstance().getFactionByPlayer(newLeaderPlayer.getUniqueId());

                if (optionalPlayerFaction.isPresent()) {
                    Faction playerFaction = optionalPlayerFaction.get();

                    if (optionalNewLeaderPlayerFaction.isPresent() && optionalNewLeaderPlayerFaction.get().name.equals(playerFaction.name)) {
                        if (!playerFaction.Leader.equals(newLeaderPlayer.getUniqueId().toString())) {
                            FactionPlayer newLeader = playerFaction.getMember(newLeaderPlayer.getUniqueId().toString());
                            newLeader.clearGroups();
                            newLeader.addGroup("leader");
                            source.sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.WHITE, "You set ", TextColors.GOLD, newLeaderPlayer.getName(), TextColors.WHITE, " as your new ", TextColors.BLUE, "Leader", TextColors.WHITE, "!"));
                        } else {
                            source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, "You are already the leader of this faction!"));
                        }
                    } else {
                        source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, "That player is not in the faction!"));
                    }
                } else {
                    source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, "You must be in a faction to use this command!"));
                }
            } else {
                source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, "Only in game players can use this command!"));
            }
        } else {
            source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, "Wrong command arguments!"));
            source.sendMessage(Text.of(TextColors.RED, "Usage: /f setleader <player>"));
        }

        return CommandResult.success();
    }
}
