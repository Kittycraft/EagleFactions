package io.github.aquerr.eaglefactions.commands.legacy;

import io.github.aquerr.eaglefactions.EagleFactions;
import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.caching.FactionsCache;
import io.github.aquerr.eaglefactions.entities.Faction;
import io.github.aquerr.eaglefactions.entities.Invite;
import io.github.aquerr.eaglefactions.config.Settings;
import io.github.aquerr.eaglefactions.logic.PluginMessages;
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
import java.util.concurrent.TimeUnit;

public class InviteCommand implements CommandExecutor {

    @Override
    public CommandResult execute(CommandSource source, CommandContext context) throws CommandException {
        Optional<Player> optionalInvitedPlayer = context.getOne("player");

        if (optionalInvitedPlayer.isPresent()) {
            if (source instanceof Player) {
                Player senderPlayer = (Player) source;
                Player invitedPlayer = optionalInvitedPlayer.get();
                Optional<Faction> optionalSenderFaction = FactionsCache.getInstance().getFactionByPlayer(senderPlayer.getUniqueId());

                if (optionalSenderFaction.isPresent()) {
                    Faction senderFaction = optionalSenderFaction.get();

                        if (Settings.isPlayerLimit()) {
                            if (senderFaction.members.size() >= Settings.getPlayerLimit()) {
                                senderPlayer.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, PluginMessages.YOU_CANT_INVITE_MORE_PLAYERS_TO_YOUR_FACTION + " " + PluginMessages.FACTIONS_PLAYER_LIMIT_HAS_BEEN_REACHED));
                                return CommandResult.success();
                            }
                        }

                        if (!FactionsCache.getInstance().getFactionByPlayer(invitedPlayer.getUniqueId()).isPresent()) {
                            try {
                                Invite invite = new Invite(senderFaction.name, invitedPlayer.getUniqueId());
                                EagleFactions.InviteList.add(invite);

                                invitedPlayer.sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.GREEN, PluginMessages.FACTION + " ", TextColors.GOLD, senderFaction.name, TextColors.GREEN, " " + PluginMessages.HAS_SENT_YOU_AN_INVITE + " " + PluginMessages.YOU_HAVE_TWO_MINUTES_TO_ACCEPT_IT +
                                        " " + PluginMessages.TYPE + " ", TextColors.GOLD, "/f join " + senderFaction.name, TextColors.WHITE, " " + PluginMessages.TO_JOIN));

                                senderPlayer.sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.GREEN, PluginMessages.YOU_INVITED + " ", TextColors.GOLD, invitedPlayer.getName(), TextColors.GREEN, " " + PluginMessages.TO_YOUR_FACTION));

                                //TODO: Create a separate listener for removing invitations.

                                Task.Builder taskBuilder = Sponge.getScheduler().createTaskBuilder();

                                taskBuilder.execute(() -> {
                                    if (EagleFactions.InviteList.contains(invite) && EagleFactions.InviteList != null) {
                                        EagleFactions.InviteList.remove(invite);
                                    }
                                }).delay(2, TimeUnit.MINUTES).name("EagleFaction - Remove Invite").submit(EagleFactions.getEagleFactions());

                                return CommandResult.success();
                            } catch (Exception exception) {
                                exception.printStackTrace();
                            }

                        } else {
                            source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, PluginMessages.PLAYER_IS_ALREADY_IN_A_FACTION));
                        }
                } else {
                    source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, PluginMessages.YOU_MUST_BE_IN_FACTION_IN_ORDER_TO_USE_THIS_COMMAND));
                }
            } else {
                source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, PluginMessages.ONLY_IN_GAME_PLAYERS_CAN_USE_THIS_COMMAND));
            }
        } else {
            source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, PluginMessages.WRONG_COMMAND_ARGUMENTS));
            source.sendMessage(Text.of(TextColors.RED, PluginMessages.USAGE + " /f invite <player>"));
        }

        return CommandResult.success();


    }
}
