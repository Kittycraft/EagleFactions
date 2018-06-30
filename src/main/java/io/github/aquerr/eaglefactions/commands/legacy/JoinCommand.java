package io.github.aquerr.eaglefactions.commands.legacy;

import io.github.aquerr.eaglefactions.EagleFactions;
import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.caching.FactionsCache;
import io.github.aquerr.eaglefactions.entities.Faction;
import io.github.aquerr.eaglefactions.entities.FactionPlayer;
import io.github.aquerr.eaglefactions.entities.Invite;
import io.github.aquerr.eaglefactions.logic.FactionLogic;
import io.github.aquerr.eaglefactions.config.Settings;
import io.github.aquerr.eaglefactions.managers.PlayerManager;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.Optional;

public class JoinCommand implements CommandExecutor {
    @Override
    public CommandResult execute(CommandSource source, CommandContext context) throws CommandException {
        Optional<String> optionalFactionName = context.getOne("faction uuid");

        if (optionalFactionName.isPresent()) {
            if (source instanceof Player) {
                Player player = (Player) source;
                String rawFactionName = optionalFactionName.get();

                if (!FactionsCache.getInstance().getFactionByPlayer(player.getUniqueId()).isPresent()) {
                    Optional<Faction> faction = FactionsCache.getInstance().getFaction(rawFactionName);
                    if (!faction.isPresent()) {
                        player.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, "There is no faction called ", TextColors.GOLD, rawFactionName + "!"));
                    } else {
                        //If player has admin mode then force join.
                        if (EagleFactions.AdminList.contains(player.getUniqueId())) {
                            FactionPlayer factionPlayer = new FactionPlayer(player.getUniqueId().toString(), PlayerManager.getPlayerName(player.getUniqueId()).get(), faction.get().name);
                            factionPlayer.addGroup("recruit");
                            faction.get().members.add(factionPlayer);
                            FactionsCache.getInstance().updatePlayer(player.getUniqueId().toString(), faction.get().name);
                            FactionLogic.informFaction(faction.get(), Text.of(PluginInfo.PluginPrefix, TextColors.WHITE, player.getDisplayNameData().displayName().get(), TextColors.GREEN, " joined the faction!"));
                        } else {

                            for (Invite invite : EagleFactions.InviteList) {
                                if (invite.getPlayerUUID().equals(player.getUniqueId()) && invite.getFactionName().equals(faction.get().name)) {
                                    try {
                                        if (Settings.isPlayerLimit()) {
                                            if (faction.get().members.size() >= Settings.getPlayerLimit()) {
                                                player.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, "You can not join this faction because it has reached the player limit!"));
                                                return CommandResult.success();
                                            }
                                        }
                                        FactionPlayer factionPlayer = new FactionPlayer(player.getUniqueId().toString(), PlayerManager.getPlayerName(player.getUniqueId()).get(), faction.get().name);
                                        factionPlayer.addGroup("recruit");
                                        faction.get().members.add(factionPlayer);
                                        FactionsCache.getInstance().updatePlayer(player.getUniqueId().toString(), faction.get().name);
                                        FactionLogic.informFaction(faction.get(), Text.of(PluginInfo.PluginPrefix, TextColors.WHITE, player.getDisplayNameData().displayName().get(), TextColors.GREEN, " joined the faction!"));

                                        EagleFactions.InviteList.remove(new Invite(faction.get().name, player.getUniqueId()));
                                        return CommandResult.success();
                                    } catch (Exception exception) {
                                        exception.printStackTrace();
                                    }
                                }
                            }
                            source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, "You have not been invited to this faction!"));
                        }
                    }
                } else {
                    source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, "You are already in a faction!"));
                }
            } else {
                source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, "Only in game players can use this command!"));
            }
        } else {
            source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, "Wrong command arguments."));
            source.sendMessage(Text.of(TextColors.RED, "Usage: /f join <faction uuid>"));
        }

        return CommandResult.success();
    }
}
