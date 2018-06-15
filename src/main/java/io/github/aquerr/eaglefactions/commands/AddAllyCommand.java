package io.github.aquerr.eaglefactions.commands;

import io.github.aquerr.eaglefactions.EagleFactions;
import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.entities.AllyInvite;
import io.github.aquerr.eaglefactions.entities.Faction;
import io.github.aquerr.eaglefactions.logic.FactionLogic;
import io.github.aquerr.eaglefactions.managers.PlayerManager;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Created by Aquerr on 2017-08-04.
 */
public class AddAllyCommand implements CommandExecutor {
    @Override
    public CommandResult execute(CommandSource source, CommandContext context) throws CommandException {
        Optional<String> optionalFactionName = context.<String>getOne(Text.of("faction name"));

        if (optionalFactionName.isPresent()) {
            if (source instanceof Player) {
                Player player = (Player) source;
                String rawFactionName = optionalFactionName.get();
                Optional<Faction> optionalPlayerFaction = FactionLogic.getFactionByPlayerUUID(player.getUniqueId());
                String invitedFactionName = FactionLogic.getRealFactionName(rawFactionName);

                if (invitedFactionName == null) {
                    player.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, "There is no faction called ", TextColors.GOLD, rawFactionName, TextColors.RED, "!"));
                    return CommandResult.success();
                }

                if (optionalPlayerFaction.isPresent()) {
                    Faction playerFaction = optionalPlayerFaction.get();

                    if (EagleFactions.AdminList.contains(player.getUniqueId())) {
                        if (!playerFaction.Enemies.contains(invitedFactionName)) {
                            if (!playerFaction.Alliances.contains(invitedFactionName)) {
                                FactionLogic.addAlly(playerFaction.Name, invitedFactionName);
                                player.sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.GREEN, "Faction has been added to the alliance!"));
                            } else {
                                player.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, "You are already in an alliance with this faction!"));
                            }
                        } else {
                            player.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, "You are currently at war with that faction! Send them a peace request first before asking them to be allies."));
                        }
                        return CommandResult.success();
                    }
                    if (!playerFaction.Enemies.contains(invitedFactionName)) {
                        if (!playerFaction.Alliances.contains(invitedFactionName)) {
                            AllyInvite checkInvite = new AllyInvite(invitedFactionName, playerFaction.Name);

                            //TODO: Check if player is online
                            Player invitedFactionLeader = PlayerManager.getPlayer(UUID.fromString(playerFaction.Leader.name)).get();

                            if (EagleFactions.AllayInviteList.contains(checkInvite)) {
                                FactionLogic.addAlly(playerFaction.Name, invitedFactionName);

                                player.sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.GREEN, "You have accepted an invitation from ", TextColors.GOLD, invitedFactionName + "!"));

                                invitedFactionLeader.sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.WHITE, "Faction ", TextColors.GOLD, playerFaction.Name, TextColors.WHITE, " accepted your alliance request!"));

                                EagleFactions.AllayInviteList.remove(checkInvite);
                            } else if (!EagleFactions.AllayInviteList.contains(checkInvite)) {
                                AllyInvite invite = new AllyInvite(playerFaction.Name, invitedFactionName);
                                EagleFactions.AllayInviteList.add(invite);

                                invitedFactionLeader.sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.WHITE, "Faction ", TextColors.GOLD, playerFaction.Name, TextColors.WHITE,
                                        " has sent you an invite to their alliance! You have ", TextColors.BLUE, "2", TextColors.WHITE, " minutes to accept it. (", TextColors.GOLD,
                                        "/f ally add " + playerFaction.Name, TextColors.WHITE, ")"));

                                //TODO: Send message about invitation to officers.

                                player.sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.WHITE, "Your faction has invited ", TextColors.GOLD, invitedFactionName, TextColors.WHITE, " to the alliance!"));

                                Task.Builder taskBuilder = Sponge.getScheduler().createTaskBuilder();

                                taskBuilder.execute(() -> {
                                    if (EagleFactions.AllayInviteList.contains(invite) && EagleFactions.AllayInviteList != null) {
                                        EagleFactions.AllayInviteList.remove(invite);
                                    }
                                }).delay(2, TimeUnit.MINUTES).name("EagleFaction - Remove Invite").submit(Sponge.getPluginManager().getPlugin(PluginInfo.Id).get().getInstance().get());
                            }
                        } else {
                            source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, "You are already allied to that faction!"));
                        }
                    } else {
                        source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, "You are currently at war with that faction! Send them a peace request first before asking them to be allies."));
                    }
                } else {
                    source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, "You must be in a faction to use that command!"));
                }
            } else {
                source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, "You must be in game to use that command!"));
            }
        } else {
            source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, "Wrong command arguments."));
            source.sendMessage(Text.of(TextColors.RED, "Usage: /f ally add <faction name>"));
            return CommandResult.success();
        }

        return CommandResult.success();
    }
}
