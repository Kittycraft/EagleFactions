package io.github.aquerr.eaglefactions.commands.relation;

import io.github.aquerr.eaglefactions.PluginInfo;
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

public class NeutralCommand implements CommandExecutor {

    @Override
    public CommandResult execute(CommandSource source, CommandContext context) throws CommandException {
        Optional<String> optionalFactionName = context.getOne(Text.of("faction name"));
        if (!(source instanceof Player)) {
            source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, "You must be in game in order to use this command!"));
            return CommandResult.success();
        }

        Player player = (Player) source;

        Optional<Faction> factionA = FactionLogic.getFactionByPlayerUUID(player.getUniqueId());
        Optional<Faction> factionB = FactionLogic.getFactionByIdentifier(optionalFactionName);

        if (!factionA.isPresent()) {
            player.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, "You must be in a faction!"));
        } else if (!factionB.isPresent()) {
            player.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, "You must specify a faction or player!"));
        } else if (factionA.get().Name.equals(factionB.get().Name)) {
            player.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, "You can not set a relation with your own faction!"));
        } else {
            List<FactionRelation> relations = FactionLogic.getRelations();
            RelationType pre = FactionLogic.getRelation(factionA.get().Name, factionB.get().Name);
            boolean change = false;
            for (int i = 0; i < relations.size(); i++) {
                if (relations.get(i).factionA.equals(factionA.get().Name) && relations.get(i).factionB.equals(factionB.get().Name)) {
                    change = true;
                    relations.remove(i);
                    break;
                }
            }
            if (change) {
                player.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, "You are already neutral with that faction!"));
                return CommandResult.success();
            }
            if(pre == RelationType.NEUTRAL){
                FactionLogic.informFaction(factionA.get(), Text.of(PluginInfo.PluginPrefix, TextColors.WHITE, "Your faction has dropped their ally/truce request with ", TextColors.GRAY, factionB.get().Name, TextColors.WHITE, "!"));
                FactionLogic.informFaction(factionB.get(), Text.of(PluginInfo.PluginPrefix, TextColors.WHITE, "The faction ", TextColors.GRAY, factionA.get().Name, TextColors.WHITE, " has removed their ally/truce request!"));
            } else if(pre == RelationType.ALLY || pre == RelationType.TRUCE) {
                FactionLogic.informFaction(factionA.get(), Text.of(PluginInfo.PluginPrefix, TextColors.WHITE, "Your faction is no longer allied/truced with ", TextColors.GRAY, factionB.get().Name, TextColors.WHITE, "!"));
                FactionLogic.informFaction(factionB.get(), Text.of(PluginInfo.PluginPrefix, TextColors.WHITE, "The faction ", TextColors.GRAY, factionA.get().Name, TextColors.WHITE, " no longer wishes to be allied/truced with your faction!"));
            } else {
                TextColor color = FactionLogic.getRelationColor(factionA.get().Name, factionB.get().Name);
                FactionLogic.informFaction(factionA.get(), Text.of(PluginInfo.PluginPrefix, TextColors.WHITE, "Your faction no longer wants to be enemies with ", color, factionB.get().Name, TextColors.WHITE, "!"));
                FactionLogic.informFaction(factionB.get(), Text.of(PluginInfo.PluginPrefix, TextColors.WHITE, "The faction ", color, factionA.get().Name, TextColors.WHITE, " no longer wants to be enemies with your faction!"));
            }
            FactionLogic.saveRelations();
        }
        return CommandResult.success();
    }
}
