package io.github.aquerr.eaglefactions.commands.relation;

import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.caching.FactionsCache;
import io.github.aquerr.eaglefactions.entities.Faction;
import io.github.aquerr.eaglefactions.entities.FactionRelation;
import io.github.aquerr.eaglefactions.entities.RelationType;
import io.github.aquerr.eaglefactions.logic.FactionLogic;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColor;
import org.spongepowered.api.text.format.TextColors;

import java.util.List;
import java.util.Optional;

public class AllyCommand implements CommandExecutor {

    @Override
    public CommandResult execute(CommandSource source, CommandContext context) throws CommandException {
        Optional<String> optionalFactionName = context.getOne(Text.of("faction uuid"));
        if (!(source instanceof Player)) {
            source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, "You must be in game in order to use this command!"));
            return CommandResult.success();
        }

        Player player = (Player) source;

        Optional<Faction> factionA = FactionsCache.getInstance().getFactionByPlayer(player.getUniqueId());
        Optional<Faction> factionB = FactionLogic.getFactionByIdentifier(optionalFactionName);

        if (!factionA.isPresent()) {
            player.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, "You must be in a faction!"));
        } else if (!factionB.isPresent()) {
            player.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, "You must specify a faction or player!"));
        } else if (factionA.get().equals(factionB.get())) {
            player.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, "You can not ally your own faction!"));
        } else {
            List<FactionRelation> relations = FactionsCache.getInstance().getRelations();
            for (int i = 0; i < relations.size(); i++) {
                if (relations.get(i).factionA.equals(factionA.get().fid) && relations.get(i).factionB.equals(factionB.get().fid)) {
                    if (relations.get(i).type == RelationType.ALLY) {
                        player.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, "You are already allies with that faction!"));
                        return CommandResult.success();
                    }
                    relations.remove(i);
                    break;
                }
            }
            relations.add(new FactionRelation(factionA.get().fid, factionB.get().fid, RelationType.ALLY));
            if (FactionLogic.getOneWayRelation(factionB.get().name, factionA.get().name) == RelationType.ALLY) {
                FactionLogic.informFaction(factionA.get(), Text.of(PluginInfo.PluginPrefix, TextColors.WHITE, "You are now allied to ", TextColors.GREEN, factionB.get().name, TextColors.WHITE, "!"));
                FactionLogic.informFaction(factionB.get(), Text.of(PluginInfo.PluginPrefix, TextColors.WHITE, "The faction ", TextColors.GREEN, factionA.get().name, TextColors.WHITE, " has accepted your alliance request!"));
            } else {
                TextColor color = FactionLogic.getRelationColor(factionA.get().name, factionB.get().name);
                FactionLogic.informFaction(factionA.get(), Text.of(PluginInfo.PluginPrefix, TextColors.WHITE, "Your faction has sent an alliance request to ", color, factionB.get().name, TextColors.WHITE, "!"));
                FactionLogic.informFaction(factionB.get(), Text.of(PluginInfo.PluginPrefix, TextColors.WHITE, "The faction ", color, factionA.get().name, TextColors.WHITE, " has requested an alliance!"));
            }
        }
        return CommandResult.success();
    }
}
