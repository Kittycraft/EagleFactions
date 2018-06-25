package io.github.aquerr.eaglefactions.listeners;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.github.aquerr.eaglefactions.EagleFactions;
import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.caching.FactionsCache;
import io.github.aquerr.eaglefactions.config.Settings;
import io.github.aquerr.eaglefactions.entities.Faction;
import io.github.aquerr.eaglefactions.logic.FactionLogic;
import io.github.aquerr.eaglefactions.logic.PluginMessages;
import io.github.aquerr.eaglefactions.managers.PowerManager;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.EventManager;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.cause.entity.damage.source.DamageSource;
import org.spongepowered.api.event.cause.entity.damage.source.EntityDamageSource;
import org.spongepowered.api.event.entity.DamageEntityEvent;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.World;

import java.util.Optional;

@Singleton
public class EntityDamageListener extends GenericListener
{

    @Inject
    public EntityDamageListener(FactionsCache cache, Settings settings, EagleFactions eagleFactions, EventManager eventManager)
    {
        super(cache, settings, eagleFactions, eventManager);
    }

    @Listener
    public void onEntityDamage(DamageEntityEvent event)
    {
        if (event.getCause().root() instanceof DamageSource)
        {
            if (event.getTargetEntity().getType() == EntityTypes.PLAYER)
            {
                Player attackedPlayer = (Player) event.getTargetEntity();
                World world = attackedPlayer.getWorld();

                if (settings.getSafeZoneWorldNames().contains(world.getName()))
                {
                    event.setBaseDamage(0);
                    event.setCancelled(true);
                    return;
                }

                //Block all damage an attacked player would get if location is a SafeZone.
                Optional<Faction> chunkFaction = FactionLogic.getFactionByChunk(world.getUniqueId(), attackedPlayer.getLocation().getChunkPosition());
                if (chunkFaction.isPresent() && chunkFaction.get().Name.equals("SafeZone"))
                {
                    event.setBaseDamage(0);
                    event.setCancelled(true);
                    return;
                }

                if (event.getCause().root() instanceof EntityDamageSource)
                {
                    EntityDamageSource entityDamageSource = (EntityDamageSource) event.getCause().root();

                    if (entityDamageSource.getSource() instanceof Player)
                    {
                        Player player = (Player) entityDamageSource.getSource();

                        //Block all damage a player could deal if location is SafeZone.
                        Optional<Faction> playerChunkFaction = FactionLogic.getFactionByChunk(world.getUniqueId(), player.getLocation().getChunkPosition());
                        if (playerChunkFaction.isPresent() && playerChunkFaction.get().Name.equals("SafeZone"))
                        {
                            event.setBaseDamage(0);
                            event.setCancelled(true);
                            return;
                        } else //If player is is not in a SafeZone.
                        {
                            //Check if player is in a faction.
                            Optional<Faction> optionalPlayerFaction = FactionLogic.getFactionByPlayerUUID(player.getUniqueId());
                            if (optionalPlayerFaction.isPresent())
                            {
                                //Check if attackedPlayer is in a faction.
                                Optional<Faction> optionalAttackedPlayerFaction = FactionLogic.getFactionByPlayerUUID(attackedPlayer.getUniqueId());
                                if (optionalAttackedPlayerFaction.isPresent())
                                {
                                    //Check if players are in the same faction
                                    if (optionalPlayerFaction.get().Name.equals(optionalAttackedPlayerFaction.get().Name))
                                    {
                                        //If friendlyfire is off the block the damage.
                                        if (!settings.isFactionFriendlyFire())
                                        {
                                            event.setBaseDamage(0);
                                            event.setCancelled(true);
                                            return;
                                        } else
                                        {
                                            //If friendlyfire is on and damage will kill attackedPlayer then penalty the player.
                                            if (event.willCauseDeath())
                                            {
                                                player.sendMessage(Text.of(PluginInfo.PluginPrefix, PluginMessages.YOUR_POWER_HAS_BEEN_DECREASED_BY + " ", TextColors.GOLD, String.valueOf(settings.getPenalty()) + "\n",
                                                        TextColors.GRAY, PluginMessages.CURRENT_POWER + " ", String.valueOf(PowerManager.getPlayerPower(player.getUniqueId())) + "/" + String.valueOf(PowerManager.getPlayerMaxPower(player.getUniqueId()))));
                                                PowerManager.penalty(player.getUniqueId());
                                                return;
                                            }
                                        }
                                    }//Check if players are in the alliance.
                                    else if (optionalPlayerFaction.get().Alliances.contains(optionalAttackedPlayerFaction.get().Name) && !settings.isAllianceFriendlyFire())
                                    {
                                        event.setBaseDamage(0);
                                        event.setCancelled(true);
                                        return;
                                    } else if (optionalPlayerFaction.get().Alliances.contains(optionalAttackedPlayerFaction.get().Name) && settings.isAllianceFriendlyFire())
                                    {
                                        if (EagleFactions.getPlugin().getPVPLogger().isActive())
                                            EagleFactions.getPlugin().getPVPLogger().addOrUpdatePlayer(attackedPlayer);
                                        if (event.willCauseDeath())
                                        {
                                            player.sendMessage(Text.of(PluginInfo.PluginPrefix, PluginMessages.YOUR_POWER_HAS_BEEN_DECREASED_BY + " ", TextColors.GOLD, String.valueOf(settings.getPenalty()) + "\n",
                                                    TextColors.GRAY, PluginMessages.CURRENT_POWER + " ", String.valueOf(PowerManager.getPlayerPower(player.getUniqueId())) + "/" + String.valueOf(PowerManager.getPlayerMaxPower(player.getUniqueId()))));
                                            PowerManager.penalty(player.getUniqueId());
                                            return;
                                        }
                                    } else
                                    {
                                        if (EagleFactions.getPlugin().getPVPLogger().isActive())
                                            EagleFactions.getPlugin().getPVPLogger().addOrUpdatePlayer(attackedPlayer);
                                        if (event.willCauseDeath())
                                        {
                                            player.sendMessage(Text.of(PluginInfo.PluginPrefix, PluginMessages.YOUR_POWER_HAS_BEEN_INCREASED_BY + " ", TextColors.GOLD, String.valueOf(settings.getKillAward()) + "\n",
                                                    TextColors.GRAY, PluginMessages.CURRENT_POWER + " ", String.valueOf(PowerManager.getPlayerPower(player.getUniqueId())) + "/" + String.valueOf(PowerManager.getPlayerMaxPower(player.getUniqueId()))));
                                            PowerManager.addPower(player.getUniqueId(), true);
                                            return;
                                        }
                                    }
                                } else
                                {
                                    if (EagleFactions.getPlugin().getPVPLogger().isActive())
                                        EagleFactions.getPlugin().getPVPLogger().addOrUpdatePlayer(attackedPlayer);
                                    if (event.willCauseDeath())
                                    {
                                        player.sendMessage(Text.of(PluginInfo.PluginPrefix, PluginMessages.YOUR_POWER_HAS_BEEN_INCREASED_BY + " ", TextColors.GOLD, String.valueOf(settings.getKillAward()) + "\n",
                                                TextColors.GRAY, PluginMessages.CURRENT_POWER + " ", String.valueOf(PowerManager.getPlayerPower(player.getUniqueId())) + "/" + String.valueOf(PowerManager.getPlayerMaxPower(player.getUniqueId()))));
                                        PowerManager.addPower(player.getUniqueId(), true);
                                        return;
                                    }
                                }
                            } else
                            {
                                if (EagleFactions.getPlugin().getPVPLogger().isActive())
                                    EagleFactions.getPlugin().getPVPLogger().addOrUpdatePlayer(attackedPlayer);
                                if (event.willCauseDeath())
                                {
                                    player.sendMessage(Text.of(PluginInfo.PluginPrefix, PluginMessages.YOUR_POWER_HAS_BEEN_INCREASED_BY + " ", TextColors.GOLD, String.valueOf(settings.getKillAward()) + "\n",
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
