package io.github.aquerr.eaglefactions.listeners;

import io.github.aquerr.eaglefactions.EagleFactions;
import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.caching.FactionsCache;
import io.github.aquerr.eaglefactions.entities.Faction;
import io.github.aquerr.eaglefactions.entities.RelationType;
import io.github.aquerr.eaglefactions.logic.FactionLogic;
import io.github.aquerr.eaglefactions.logic.MainLogic;
import io.github.aquerr.eaglefactions.logic.PluginMessages;
import io.github.aquerr.eaglefactions.managers.PowerManager;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.cause.entity.damage.source.DamageSource;
import org.spongepowered.api.event.cause.entity.damage.source.EntityDamageSource;
import org.spongepowered.api.event.entity.DamageEntityEvent;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.World;

import java.util.Optional;

//TODO: Review this. May need to be re-written to account for truce.
public class EntityDamageListener {
    @Listener
    public void onEntityDamage(DamageEntityEvent event) {
        if (event.getCause().root() instanceof DamageSource) {
            if (event.getTargetEntity().getType() == EntityTypes.PLAYER) {
                Player attackedPlayer = (Player) event.getTargetEntity();
                World world = attackedPlayer.getWorld();

                if (MainLogic.getSafeZoneWorldNames().contains(world.getName())) {
                    event.setBaseDamage(0);
                    event.setCancelled(true);
                    return;
                }

                //Block all damage an attacked player would get if location is a SafeZone.
                Optional<Faction> chunkFaction = FactionsCache.getInstance().getFactionByChunk(world.getUniqueId(), attackedPlayer.getLocation().getChunkPosition());
                if (chunkFaction.isPresent() && chunkFaction.get().name.equals("SafeZone")) {
                    event.setBaseDamage(0);
                    event.setCancelled(true);
                    return;
                }

                if (event.getCause().root() instanceof EntityDamageSource) {
                    EntityDamageSource entityDamageSource = (EntityDamageSource) event.getCause().root();

                    if (entityDamageSource.getSource() instanceof Player) {
                        Player player = (Player) entityDamageSource.getSource();

                        //Block all damage a player could deal if location is SafeZone.
                        Optional<Faction> playerChunkFaction = FactionsCache.getInstance().getFactionByChunk(world.getUniqueId(), player.getLocation().getChunkPosition());
                        if (playerChunkFaction.isPresent() && playerChunkFaction.get().name.equals("SafeZone")) {
                            event.setBaseDamage(0);
                            event.setCancelled(true);
                            return;
                        } else //If player is is not in a SafeZone.
                        {
                            //Check if player is in a faction.
                            Optional<Faction> optionalPlayerFaction = FactionsCache.getInstance().getFactionByPlayer(player.getUniqueId());
                            if (optionalPlayerFaction.isPresent()) {
                                //Check if attackedPlayer is in a faction.
                                Optional<Faction> optionalAttackedPlayerFaction = FactionsCache.getInstance().getFactionByPlayer(attackedPlayer.getUniqueId());
                                if (optionalAttackedPlayerFaction.isPresent()) {
                                    //Check if players are in the same faction
                                    RelationType relation = FactionLogic.getRelation(optionalPlayerFaction.get().name, optionalAttackedPlayerFaction.get().name);
                                    if (relation == RelationType.SAME) {
                                        //If friendlyfire is off the block the damage.
                                        if (!MainLogic.isFactionFriendlyFire()) {
                                            event.setBaseDamage(0);
                                            event.setCancelled(true);
                                            return;
                                        } else {
                                            //If friendlyfire is on and damage will kill attackedPlayer then penalty the player.
                                            if (event.willCauseDeath()) {
                                                player.sendMessage(Text.of(PluginInfo.PluginPrefix, PluginMessages.YOUR_POWER_HAS_BEEN_DECREASED_BY + " ", TextColors.GOLD, String.valueOf(MainLogic.getPenalty()) + "\n",
                                                        TextColors.GRAY, PluginMessages.CURRENT_POWER + " ", String.valueOf(PowerManager.getPlayerPower(player.getUniqueId())) + "/" + String.valueOf(PowerManager.getPlayerMaxPower(player.getUniqueId()))));
                                                PowerManager.penalty(player.getUniqueId());
                                                return;
                                            }
                                        }
                                        //TODO: Add option to cherry pick friendly fire in permissions.
                                    } else if (relation == RelationType.ALLY || relation == RelationType.TRUCE) {
                                        if (!MainLogic.isAllianceFriendlyFire()) {
                                            event.setBaseDamage(0);
                                            event.setCancelled(true);
                                            return;
                                        } else {
                                            if (EagleFactions.getEagleFactions().getPVPLogger().isActive())
                                                EagleFactions.getEagleFactions().getPVPLogger().addOrUpdatePlayer(attackedPlayer);
                                            if (event.willCauseDeath()) {
                                                player.sendMessage(Text.of(PluginInfo.PluginPrefix, PluginMessages.YOUR_POWER_HAS_BEEN_DECREASED_BY + " ", TextColors.GOLD, String.valueOf(MainLogic.getPenalty()) + "\n",
                                                        TextColors.GRAY, PluginMessages.CURRENT_POWER + " ", String.valueOf(PowerManager.getPlayerPower(player.getUniqueId())) + "/" + String.valueOf(PowerManager.getPlayerMaxPower(player.getUniqueId()))));
                                                PowerManager.penalty(player.getUniqueId());
                                                return;
                                            }
                                        }
                                    } else {
                                        if (EagleFactions.getEagleFactions().getPVPLogger().isActive())
                                            EagleFactions.getEagleFactions().getPVPLogger().addOrUpdatePlayer(attackedPlayer);
                                        if (event.willCauseDeath()) {
                                            player.sendMessage(Text.of(PluginInfo.PluginPrefix, PluginMessages.YOUR_POWER_HAS_BEEN_INCREASED_BY + " ", TextColors.GOLD, String.valueOf(MainLogic.getKillAward()) + "\n",
                                                    TextColors.GRAY, PluginMessages.CURRENT_POWER + " ", String.valueOf(PowerManager.getPlayerPower(player.getUniqueId())) + "/" + String.valueOf(PowerManager.getPlayerMaxPower(player.getUniqueId()))));
                                            PowerManager.addPower(player.getUniqueId(), true);
                                            return;
                                        }
                                    }
                                } else {
                                    if (EagleFactions.getEagleFactions().getPVPLogger().isActive())
                                        EagleFactions.getEagleFactions().getPVPLogger().addOrUpdatePlayer(attackedPlayer);
                                    if (event.willCauseDeath()) {
                                        player.sendMessage(Text.of(PluginInfo.PluginPrefix, PluginMessages.YOUR_POWER_HAS_BEEN_INCREASED_BY + " ", TextColors.GOLD, String.valueOf(MainLogic.getKillAward()) + "\n",
                                                TextColors.GRAY, PluginMessages.CURRENT_POWER + " ", String.valueOf(PowerManager.getPlayerPower(player.getUniqueId())) + "/" + String.valueOf(PowerManager.getPlayerMaxPower(player.getUniqueId()))));
                                        PowerManager.addPower(player.getUniqueId(), true);
                                        return;
                                    }
                                }
                            } else {
                                if (EagleFactions.getEagleFactions().getPVPLogger().isActive())
                                    EagleFactions.getEagleFactions().getPVPLogger().addOrUpdatePlayer(attackedPlayer);
                                if (event.willCauseDeath()) {
                                    player.sendMessage(Text.of(PluginInfo.PluginPrefix, PluginMessages.YOUR_POWER_HAS_BEEN_INCREASED_BY + " ", TextColors.GOLD, String.valueOf(MainLogic.getKillAward()) + "\n",
                                            TextColors.GRAY, PluginMessages.CURRENT_POWER + " ", String.valueOf(PowerManager.getPlayerPower(player.getUniqueId())) + "/" + String.valueOf(PowerManager.getPlayerMaxPower(player.getUniqueId()))));
                                    PowerManager.addPower(player.getUniqueId(), true);
                                    return;
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
