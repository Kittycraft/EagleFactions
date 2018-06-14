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

public class MemberCommand implements CommandExecutor {
    @Override
    public CommandResult execute(CommandSource source, CommandContext context) throws CommandException {
        Optional<Player> optionalNewMemberPlayer = context.getOne("player");

        if (optionalNewMemberPlayer.isPresent()) {
            if (source instanceof Player) {
                Player player = (Player) source;
                Player newMemberPlayer = optionalNewMemberPlayer.get();
                Optional<Faction> optionalPlayerFaction = FactionLogic.getFactionByPlayerUUID(player.getUniqueId());
                Optional<Faction> optionalNewMemberFaction = FactionLogic.getFactionByPlayerUUID(newMemberPlayer.getUniqueId());

                if (optionalPlayerFaction.isPresent()) {
                    Faction playerFaction = optionalPlayerFaction.get();
                    if (EagleFactions.AdminList.contains(player.getUniqueId())) {
                        if (optionalNewMemberFaction.isPresent() && optionalNewMemberFaction.get().Name.equals(playerFaction.Name)) {
                            if (!playerFaction.Leader.name.equals(newMemberPlayer.getUniqueId().toString())) {
                                FactionLogic.setMember(newMemberPlayer.getUniqueId().toString(), playerFaction.Name);
                                source.sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.WHITE, "You set ", TextColors.GOLD, newMemberPlayer.getName(), TextColors.WHITE, "'s rank the the faction to ", TextColors.BLUE, PluginMessages.MEMBERS, TextColors.WHITE, "!"));
                            } else {
                                source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, PluginMessages.YOU_CANT_SET_FACTIONS_LEADER_AS_MEMBER));
                            }
                        } else {
                            source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, PluginMessages.THIS_PLAYER_IS_NOT_IN_YOUR_FACTION));
                        }

                        return CommandResult.success();
                    }

//                    if (playerFaction.isAllowed(player.getUniqueId().toString(), PluginPermissions.RemoveEnemyCommand)) {
                        if (optionalNewMemberFaction.isPresent() && optionalNewMemberFaction.get().Name.equals(playerFaction.Name)) {
                            if (!playerFaction.Leader.name.equals(newMemberPlayer.getUniqueId().toString())) {
                                FactionLogic.setMember(newMemberPlayer.getUniqueId().toString(), playerFaction.Name);
                                source.sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.WHITE, "You set ", TextColors.GOLD, newMemberPlayer.getName(), TextColors.WHITE, "'s rank the the faction to ", TextColors.BLUE, PluginMessages.MEMBERS, TextColors.WHITE, "!"));
                            } else {
                                source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, PluginMessages.YOU_CANT_SET_FACTIONS_LEADER_AS_MEMBER));
                            }
                        } else {
                            source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, PluginMessages.THIS_PLAYER_IS_NOT_IN_YOUR_FACTION));
                        }

//                    } else {
//                        source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, "Your faction does not allow you to do this! (", TextColors.DARK_RED, "/f leave", TextColors.RED," is always available)"));
//                    }
                } else {
                    source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, PluginMessages.YOU_MUST_BE_IN_FACTION_IN_ORDER_TO_USE_THIS_COMMAND));
                }
            } else {
                source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, PluginMessages.ONLY_IN_GAME_PLAYERS_CAN_USE_THIS_COMMAND));
            }
        } else {
            source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, PluginMessages.WRONG_COMMAND_ARGUMENTS));
            source.sendMessage(Text.of(TextColors.RED, PluginMessages.USAGE + " /f member <player>"));
        }

        return CommandResult.success();
    }
}
