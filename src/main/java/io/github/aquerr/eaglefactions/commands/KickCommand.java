package io.github.aquerr.eaglefactions.commands;

import io.github.aquerr.eaglefactions.EagleFactions;
import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.PluginPermissions;
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

public class KickCommand implements CommandExecutor {
    @Override
    public CommandResult execute(CommandSource source, CommandContext context) throws CommandException {
        Optional<Player> optionalSelectedPlayer = context.getOne(Text.of("player"));

        if (optionalSelectedPlayer.isPresent()) {
            if (source instanceof Player) {
                Player player = (Player) source;
                Optional<Faction> optionalPlayerFaction = FactionLogic.getFactionByPlayerUUID(player.getUniqueId());

                if (optionalPlayerFaction.isPresent()) {
                    Faction playerFaction = optionalPlayerFaction.get();

                    if (playerFaction.isAllowed(player.getUniqueId().toString(), PluginPermissions.KickCommand)) {
                        //if (playerFaction.Leader.equals(player.getUniqueId().toString()) || playerFaction.Officers.contains(player.getUniqueId().toString())) {
                        Player selectedPlayer = optionalSelectedPlayer.get();
                        Optional<Faction> optionalSelectedPlayerFaction = FactionLogic.getFactionByPlayerUUID(selectedPlayer.getUniqueId());

                        if (optionalSelectedPlayerFaction.isPresent() && optionalSelectedPlayerFaction.get().Name.equals(playerFaction.Name)) {
                            if (!playerFaction.Leader.equals(selectedPlayer.getUniqueId().toString())) {

                                //if (!playerFaction.Officers.contains(selectedPlayer.getUniqueId().toString()) || playerFaction.Leader.equals(player.getUniqueId().toString())) {
                                if (playerFaction.getMember(player.getUniqueId().toString()).getPriority(playerFaction) > playerFaction.getMember(selectedPlayer.getUniqueId().toString()).getPriority(playerFaction)) {
                                    FactionLogic.kickPlayer(selectedPlayer.getUniqueId(), playerFaction.Name);

                                    //TODO: Add listener that will inform players in a faction that someone has left their faction.

                                    source.sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.GREEN, PluginMessages.YOU_KICKED + " ", TextColors.GOLD, selectedPlayer.getName(), TextColors.GREEN, " " + PluginMessages.FROM_THE_FACTION));
                                    selectedPlayer.sendMessage(Text.of(PluginInfo.PluginPrefix, PluginMessages.YOU_WERE_KICKED_FROM_THE_FACTION));

                                    if (EagleFactions.AutoClaimList.contains(selectedPlayer.getUniqueId()))
                                        EagleFactions.AutoClaimList.remove(selectedPlayer.getUniqueId());

                                    return CommandResult.success();
                                } else {
                                    source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, PluginMessages.YOU_CANT_KICK_THIS_PLAYER));
                                }
                            } else {
                                source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, PluginMessages.YOU_CANT_KICK_THIS_PLAYER));
                            }
                        } else {
                            source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, PluginMessages.THIS_PLAYER_IS_NOT_IN_YOUR_FACTION));
                        }
                    } else {
                        source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, PluginMessages.YOU_MUST_BE_THE_FACTIONS_LEADER_OR_OFFICER_TO_DO_THIS));
                    }
                } else {
                    source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, PluginMessages.YOU_MUST_BE_IN_FACTION_IN_ORDER_TO_USE_THIS_COMMAND));
                }
            } else {
                source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, PluginMessages.ONLY_IN_GAME_PLAYERS_CAN_USE_THIS_COMMAND));
            }
        } else {
            source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, PluginMessages.WRONG_COMMAND_ARGUMENTS));
            source.sendMessage(Text.of(TextColors.RED, PluginMessages.USAGE + " /f kick <player>"));
        }

        return CommandResult.success();
    }
}
