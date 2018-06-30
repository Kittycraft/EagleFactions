package io.github.aquerr.eaglefactions.commands.legacy;

import com.flowpowered.math.vector.Vector3i;
import io.github.aquerr.eaglefactions.EagleFactions;
import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.caching.FactionsCache;
import io.github.aquerr.eaglefactions.entities.Faction;
import io.github.aquerr.eaglefactions.entities.FactionClaim;
import io.github.aquerr.eaglefactions.logic.FactionLogic;
import io.github.aquerr.eaglefactions.logic.MainLogic;
import io.github.aquerr.eaglefactions.logic.PluginMessages;
import io.github.aquerr.eaglefactions.managers.PowerManager;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.World;

import java.util.Optional;

public class ClaimCommand implements CommandExecutor {
    @Override
    public CommandResult execute(CommandSource source, CommandContext context) throws CommandException {
        if (source instanceof Player) {
            Player player = (Player) source;
            Optional<Faction> optionalPlayerFaction = FactionsCache.getInstance().getFactionByPlayer(player.getUniqueId());

            if (optionalPlayerFaction.isPresent()) {
                Faction playerFaction = optionalPlayerFaction.get();

                if (!EagleFactions.AdminList.contains(player.getUniqueId())) {
                    World world = player.getWorld();
                    Vector3i chunk = player.getLocation().getChunkPosition();

                    Optional<Faction> optionalChunkFaction = FactionsCache.getInstance().getFactionByChunk(world.getUniqueId(), chunk);

                    if (MainLogic.getClaimableWorldNames().contains(player.getWorld().getName())) {
                        if (!optionalChunkFaction.isPresent()) {
                            if (PowerManager.getFactionPower(playerFaction).doubleValue() > playerFaction.claims.size()) {
                                if (!playerFaction.claims.isEmpty()) {
                                    if (playerFaction.name.equals("SafeZone") || playerFaction.name.equals("WarZone")) {
                                        FactionsCache.getInstance().addOrSetClaim(new FactionClaim(chunk, world.getUniqueId(), playerFaction.name));
                                        player.sendMessage(Text.of(PluginInfo.PluginPrefix, PluginMessages.LAND + " ", TextColors.GOLD, chunk.toString(), TextColors.WHITE, " " + PluginMessages.HAS_BEEN_SUCCESSFULLY + " ", TextColors.GOLD, PluginMessages.CLAIMED, TextColors.WHITE, "!"));

                                        return CommandResult.success();
                                    } else {
                                        if (MainLogic.requireConnectedClaims()) {
                                            if (FactionLogic.isClaimConnected(playerFaction, world.getUniqueId(), chunk)) {
                                                FactionLogic.startClaiming(player, playerFaction, world.getUniqueId(), chunk);
                                                return CommandResult.success();
                                            } else {
                                                source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, PluginMessages.CLAIMS_NEED_TO_BE_CONNECTED));
                                            }
                                        } else {
                                            FactionLogic.startClaiming(player, playerFaction, world.getUniqueId(), chunk);
                                            return CommandResult.success();
                                        }
                                    }
                                } else {
                                    FactionLogic.startClaiming(player, playerFaction, world.getUniqueId(), chunk);
                                    return CommandResult.success();
                                }
                            } else {
                                source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, PluginMessages.YOUR_FACTION_DOES_NOT_HAVE_POWER_TO_CLAIM_MORE_LANDS));
                            }
                        } else {
                            source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, PluginMessages.THIS_PLACE_IS_ALREADY_CLAIMED));
                        }
                    }
                } else {
                    World world = player.getWorld();
                    Vector3i chunk = player.getLocation().getChunkPosition();

                    if (!FactionsCache.getInstance().getClaim(world.getUniqueId(), chunk).isPresent()) {
                        FactionsCache.getInstance().addOrSetClaim(new FactionClaim(chunk, world.getUniqueId(), playerFaction.name));

                        player.sendMessage(Text.of(PluginInfo.PluginPrefix, PluginMessages.LAND + " ", TextColors.GOLD, chunk.toString(), TextColors.WHITE, " " + PluginMessages.HAS_BEEN_SUCCESSFULLY + " ", TextColors.GOLD, PluginMessages.CLAIMED, TextColors.WHITE, "!"));
                        return CommandResult.success();
                    } else {
                        source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, PluginMessages.THIS_PLACE_IS_ALREADY_CLAIMED));
                    }
                }
            } else {
                source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, PluginMessages.YOU_MUST_BE_IN_FACTION_IN_ORDER_TO_USE_THIS_COMMAND));
            }
        } else {
            source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, PluginMessages.ONLY_IN_GAME_PLAYERS_CAN_USE_THIS_COMMAND));
        }

        return CommandResult.success();
    }
}
