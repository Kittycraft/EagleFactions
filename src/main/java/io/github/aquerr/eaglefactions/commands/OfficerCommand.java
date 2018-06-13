package io.github.aquerr.eaglefactions.commands;

import io.github.aquerr.eaglefactions.EagleFactions;
import io.github.aquerr.eaglefactions.PluginInfo;
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

public class OfficerCommand implements CommandExecutor {
    @Override
    public CommandResult execute(CommandSource source, CommandContext context) throws CommandException {
        Optional<Player> optionalNewOfficerPlayer = context.<Player>getOne("player");

        if (optionalNewOfficerPlayer.isPresent()) {
            if (source instanceof Player) {
                Player player = (Player) source;
                Player newOfficerPlayer = optionalNewOfficerPlayer.get();
                Optional<Faction> optionalPlayerFaction = FactionLogic.getFactionByPlayerUUID(player.getUniqueId());
                Optional<Faction> optionalNewOfficerFaction = FactionLogic.getFactionByPlayerUUID(newOfficerPlayer.getUniqueId());

                if (optionalPlayerFaction.isPresent()) {
                    Faction playerFaction = optionalPlayerFaction.get();
                    if (EagleFactions.AdminList.contains(player.getUniqueId())) {
                        if (optionalNewOfficerFaction.isPresent() && optionalNewOfficerFaction.get().Name.equals(playerFaction.Name)) {
                            if (!playerFaction.Leader.equals(newOfficerPlayer.getUniqueId().toString())) {
                                if (playerFaction.Members.contains(newOfficerPlayer.getUniqueId().toString())) {
                                    FactionLogic.addOfficerAndRemoveMember(newOfficerPlayer.getUniqueId().toString(), playerFaction.Name);
                                    source.sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.WHITE, PluginMessages.YOU_ADDED + " ", TextColors.GOLD, newOfficerPlayer.getName(), TextColors.WHITE, " " + PluginMessages.AS_YOUR_NEW + " ", TextColors.BLUE, PluginMessages.OFFICER, TextColors.WHITE, "!"));
                                } else if (playerFaction.Officers.contains(newOfficerPlayer.getUniqueId().toString())) {
                                    FactionLogic.removeOfficerAndSetAsMember(newOfficerPlayer.getUniqueId().toString(), playerFaction.Name);
                                    source.sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.WHITE, PluginMessages.YOU_REMOVED + " ", TextColors.GOLD, newOfficerPlayer.getName(), TextColors.WHITE, " " + PluginMessages.FROM_YOUR + " ", TextColors.BLUE, PluginMessages.OFFICERS, TextColors.WHITE, "!"));
                                }
                                //TODO: Add promotion from recruit rank.
                            } else {
                                source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, PluginMessages.YOU_CANT_SET_FACTIONS_LEADER_AS_OFFICER));
                            }
                        } else {
                            source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, PluginMessages.THIS_PLAYER_IS_NOT_IN_YOUR_FACTION));
                        }

                        return CommandResult.success();
                    }

                    if (playerFaction.Leader.equals(player.getUniqueId().toString())) {
                        if (optionalNewOfficerFaction.isPresent() && optionalNewOfficerFaction.get().Name.equals(playerFaction.Name)) {
                            if (!playerFaction.Leader.equals(newOfficerPlayer.getUniqueId().toString())) {
                                if (!playerFaction.Officers.contains(newOfficerPlayer.getUniqueId().toString())) {
                                    FactionLogic.addOfficerAndRemoveMember(newOfficerPlayer.getUniqueId().toString(), playerFaction.Name);
                                    source.sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.WHITE, PluginMessages.YOU_ADDED + " ", TextColors.GOLD, newOfficerPlayer.getName(), TextColors.WHITE, " " + PluginMessages.AS_YOUR_NEW + " ", TextColors.BLUE, PluginMessages.OFFICER, TextColors.WHITE, "!"));
                                } else {
                                    FactionLogic.removeOfficerAndSetAsMember(newOfficerPlayer.getUniqueId().toString(), playerFaction.Name);
                                    source.sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.WHITE, PluginMessages.YOU_REMOVED + " ", TextColors.GOLD, newOfficerPlayer.getName(), TextColors.WHITE, " " + PluginMessages.FROM_YOUR + " ", TextColors.BLUE, PluginMessages.OFFICERS, TextColors.WHITE, "!"));
                                }
                            } else {
                                source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, PluginMessages.YOU_CANT_SET_FACTIONS_LEADER_AS_OFFICER));
                            }
                        } else {
                            source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, PluginMessages.THIS_PLAYER_IS_NOT_IN_YOUR_FACTION));
                        }

                    } else {
                        source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, PluginMessages.YOU_MUST_BE_THE_FACTIONS_LEADER_TO_DO_THIS));
                    }
                } else {
                    source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, PluginMessages.YOU_MUST_BE_IN_FACTION_IN_ORDER_TO_USE_THIS_COMMAND));
                }
            } else {
                source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, PluginMessages.ONLY_IN_GAME_PLAYERS_CAN_USE_THIS_COMMAND));
            }
        } else {
            source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, PluginMessages.WRONG_COMMAND_ARGUMENTS));
            source.sendMessage(Text.of(TextColors.RED, PluginMessages.USAGE + " /f officer <player>"));
        }

        return CommandResult.success();
    }
}
