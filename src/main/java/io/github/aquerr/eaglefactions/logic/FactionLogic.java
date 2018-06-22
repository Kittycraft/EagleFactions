package io.github.aquerr.eaglefactions.logic;

import com.flowpowered.math.vector.Vector3i;
import io.github.aquerr.eaglefactions.EagleFactions;
import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.caching.FactionsCache;
import io.github.aquerr.eaglefactions.entities.*;
import io.github.aquerr.eaglefactions.managers.PlayerManager;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.entity.PlayerInventory;
import org.spongepowered.api.item.inventory.query.QueryOperationTypes;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColor;
import org.spongepowered.api.text.format.TextColors;

import javax.annotation.Nullable;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static io.github.aquerr.eaglefactions.entities.RelationType.*;

/**
 * Created by Aquerr on 2017-07-12.
 */
public class FactionLogic {

    @Deprecated
    public static void setup(Path configDir) {
        //factionsStorage = new HOCONFactionStorage(configDir);
    }

    @Deprecated
    public static void reload() {
//        factionsStorage.load();
    }

    public static List<Player> getOnlinePlayers(Faction faction) {
        List<Player> factionPlayers = new ArrayList<>();

        for (FactionPlayer uuid : faction.members) {
            if (!uuid.uuid.equals("") && PlayerManager.isPlayerOnline(UUID.fromString(uuid.uuid))) {
                factionPlayers.add(PlayerManager.getPlayer(UUID.fromString(uuid.uuid)).get());
            }
        }

        return factionPlayers;
    }

    public static RelationType getOneWayRelation(String factionA, String factionB) {
        for (FactionRelation relation : FactionsCache.getInstance().getRelations()) {
            if (relation.factionA.equals(factionA) && relation.factionB.equals(factionB)) {
                return relation.type;
            }
        }
        return NEUTRAL;
    }

    public static List<Faction> getRelationGroup(String faction, RelationType type) {
        List<Faction> all = FactionsCache.getInstance().getFactions(), end = new ArrayList<>();
        for (Faction f : all) {
            if (getRelation(faction, f.name) == type) {
                end.add(f);
            }
        }
        return end;
    }

    public static TextColor getRelationColor(String factionA, String factionB) {
        switch (getRelation(factionA, factionB)) {
            case ALLY:
                return TextColors.GREEN;
            case SAME:
                return TextColors.GREEN;
            case ENEMY:
                return TextColors.RED;
            case TRUCE:
                return TextColors.GREEN;
            default:
                return TextColors.GRAY;
        }
    }

    public static RelationType getRelation(String factionA, String factionB) {
        if (factionA.equals(factionB)) {
            return SAME;
        }
        List<FactionRelation> relations = FactionsCache.getInstance().getRelations();
        RelationType a = NEUTRAL, b = NEUTRAL;
        for (FactionRelation relation : relations) {
            if (relation.factionA.equals(factionA) && relation.factionB.equals(factionB)) {
                a = relation.type;
            } else if (relation.factionB.equals(factionA) && relation.factionA.equals(factionB)) {
                b = relation.type;
            }
        }
        if (a == ENEMY || b == ENEMY) {
            return ENEMY;
        } else if (a != NEUTRAL && b != NEUTRAL) {
            if (a == ALLY && b == ALLY) {
                return ALLY;
            }
            return TRUCE;
        }
        return NEUTRAL;
    }

    public static void informFaction(Faction faction, Text text) {
        getOnlinePlayers(faction).forEach(p -> p.sendMessage(text));
    }

    public static void createFaction(String factionName, String factionTag, UUID playerUUID) {
        Faction faction = new Faction(factionName, factionTag, new FactionPlayer(playerUUID.toString(), PlayerManager.getPlayerName(playerUUID).get(), factionName));
        FactionsCache.getInstance().addFaction(faction);
        FactionsCache.getInstance().updatePlayer(playerUUID.toString(), factionName);
    }

    public static void leaveFaction(UUID playerUUID, String factionName) {
        Optional<Faction> faction = FactionsCache.getInstance().getFaction(factionName);
        if (faction.isPresent()) {
            faction.get().members.remove(faction.get().getMember(playerUUID.toString()));
            FactionsCache.getInstance().removePlayer(playerUUID);
        }
    }

    public static boolean isClaimConnected(Faction faction, UUID worldUUID, Vector3i chunk) {
        List<FactionClaim> claimsList = faction.claims;

        for (FactionClaim object : claimsList) {
            if (object.world.equals(worldUUID)) {
                if ((object.chunk.getX() == chunk.getX()) && Math.abs(object.chunk.getZ() - chunk.getZ()) == 1) {
                    return true;
                } else if ((object.chunk.getZ() == chunk.getZ()) && Math.abs(object.chunk.getX() - chunk.getX()) == 1) {
                    return true;
                }
            }
        }
        return false;
    }

    public static void setHome(@Nullable UUID worldUUID, Faction faction, @Nullable Vector3i home) {
        if (home != null && worldUUID != null) {
            faction.Home = new FactionHome(worldUUID, home);
        } else {
            faction.Home = null;
        }

    }

    @Deprecated
    public static List<String> getFactionsTags() {
        List<Faction> factionsList = FactionsCache.getInstance().getFactions();
        List<String> factionsTags = new ArrayList<>();

        for (Faction faction : factionsList) {
            factionsTags.add(faction.Tag.toPlain());
        }

        return factionsTags;
    }

    public static boolean hasOnlinePlayers(Faction faction) {
        for (FactionPlayer player : faction.members) {
            if (PlayerManager.isPlayerOnline(UUID.fromString(player.uuid))) {
                return true;
            }
        }
        return false;
    }

    public static void removeClaims(Faction faction) {
        int num = faction.claims.size();
        while (faction.claims.size() > 0) {
            FactionsCache.getInstance().removeClaim(faction.claims.get(0).world, faction.claims.get(0).chunk);
            num--;
            if (num < 10) {
                EagleFactions.getLogger().warn("Unable to remove all claims from faction \"" + faction.name + "\"");
                break;
            }
        }

    }

    private static Consumer<Task> addClaimWithDelay(Player player, Faction faction, UUID worldUUID, Vector3i chunk) {
        return new Consumer<Task>() {
            int seconds = 1;

            @Override
            public void accept(Task task) {
                if (chunk.toString().equals(player.getLocation().getChunkPosition().toString())) {
                    if (seconds >= MainLogic.getClaimingDelay()) {
                        if (MainLogic.shouldClaimByItems()) {
                            if (addClaimByItems(player, faction, worldUUID, chunk))
                                player.sendMessage(Text.of(PluginInfo.PluginPrefix, PluginMessages.LAND + " ", TextColors.GOLD, chunk.toString(), TextColors.WHITE, " " + PluginMessages.HAS_BEEN_SUCCESSFULLY + " ", TextColors.GOLD, PluginMessages.CLAIMED, TextColors.WHITE, "!"));
                            else
                                player.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, PluginMessages.YOU_DONT_HAVE_ENOUGH_RESOURCES_TO_CLAIM_A_TERRITORY));
                        } else {
                            FactionsCache.getInstance().addOrSetClaim(new FactionClaim(chunk, worldUUID, faction.name));

                            player.sendMessage(Text.of(PluginInfo.PluginPrefix, PluginMessages.LAND + " ", TextColors.GOLD, chunk.toString(), TextColors.WHITE, " " + PluginMessages.HAS_BEEN_SUCCESSFULLY + " ", TextColors.GOLD, PluginMessages.CLAIMED, TextColors.WHITE, "!"));
                        }
                    } else {
                        player.sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.RESET, seconds));
                        seconds++;
                    }
                } else {
                    player.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, PluginMessages.YOU_MOVED_FROM_THE_CHUNK));
                    task.cancel();
                }
            }
        };
    }

    public static void startClaiming(Player player, Faction faction, UUID worldUUID, Vector3i chunk) {
        if (MainLogic.isDelayedClaimingToggled()) {
            player.sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.GREEN, PluginMessages.CLAIMING_HAS_BEEN_STARTED + " " + PluginMessages.STAY_IN_THE_CHUNK_FOR + " ", TextColors.GOLD, MainLogic.getClaimingDelay() + " " + PluginMessages.SECONDS, TextColors.GREEN, " " + PluginMessages.TO_CLAIM_IT));

            Task.Builder taskBuilder = Sponge.getScheduler().createTaskBuilder();

            taskBuilder.delay(1, TimeUnit.SECONDS).interval(1, TimeUnit.SECONDS).execute(addClaimWithDelay(player, faction, worldUUID, chunk)).submit(EagleFactions.getEagleFactions());
        } else {
            if (MainLogic.shouldClaimByItems()) {
                if (addClaimByItems(player, faction, worldUUID, chunk))
                    player.sendMessage(Text.of(PluginInfo.PluginPrefix, PluginMessages.LAND + " ", TextColors.GOLD, chunk.toString(), TextColors.WHITE, " " + PluginMessages.HAS_BEEN_SUCCESSFULLY + " ", TextColors.GOLD, PluginMessages.CLAIMED, TextColors.WHITE, "!"));
                else
                    player.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, PluginMessages.YOU_DONT_HAVE_ENOUGH_RESOURCES_TO_CLAIM_A_TERRITORY));
            } else {
                player.sendMessage(Text.of(PluginInfo.PluginPrefix, PluginMessages.LAND + " ", TextColors.GOLD, chunk.toString(), TextColors.WHITE, " " + PluginMessages.HAS_BEEN_SUCCESSFULLY + " ", TextColors.GOLD, PluginMessages.CLAIMED, TextColors.WHITE, "!"));
                FactionsCache.getInstance().addOrSetClaim(new FactionClaim(chunk, worldUUID, faction.name));
            }
        }
    }

    private static boolean addClaimByItems(Player player, Faction faction, UUID worldUUID, Vector3i chunk) {
        HashMap<String, Integer> requiredItems = MainLogic.getRequiredItemsToClaim();
        PlayerInventory inventory = player.getInventory().query(QueryOperationTypes.INVENTORY_TYPE.of(PlayerInventory.class));
        int allRequiredItems = requiredItems.size();
        int foundItems = 0;

        for (String requiredItem : requiredItems.keySet()) {
            String[] idAndVariant = requiredItem.split(":");

            String itemId = idAndVariant[0] + ":" + idAndVariant[1];
            Optional<ItemType> itemType = Sponge.getRegistry().getType(ItemType.class, itemId);

            if (itemType.isPresent()) {
                ItemStack itemStack = ItemStack.builder()
                        .itemType(itemType.get()).build();
                itemStack.setQuantity(requiredItems.get(requiredItem));

                if (idAndVariant.length == 3) {
                    if (itemType.get().getBlock().isPresent()) {
                        int variant = Integer.parseInt(idAndVariant[2]);
                        BlockState blockState = (BlockState) itemType.get().getBlock().get().getAllBlockStates().toArray()[variant];
                        itemStack = ItemStack.builder().fromBlockState(blockState).build();
                    }
                }

                if (inventory.contains(itemStack)) {
                    foundItems += 1;
                } else {
                    return false;
                }
            }
        }

        if (allRequiredItems == foundItems) {
            for (String requiredItem : requiredItems.keySet()) {
                String[] idAndVariant = requiredItem.split(":");
                String itemId = idAndVariant[0] + ":" + idAndVariant[1];

                Optional<ItemType> itemType = Sponge.getRegistry().getType(ItemType.class, itemId);

                if (itemType.isPresent()) {
                    ItemStack itemStack = ItemStack.builder()
                            .itemType(itemType.get()).build();
                    itemStack.setQuantity(requiredItems.get(requiredItem));

                    if (idAndVariant.length == 3) {
                        if (itemType.get().getBlock().isPresent()) {
                            int variant = Integer.parseInt(idAndVariant[2]);
                            BlockState blockState = (BlockState) itemType.get().getBlock().get().getAllBlockStates().toArray()[variant];
                            itemStack = ItemStack.builder().fromBlockState(blockState).build();
                        }
                    }

                    inventory.query(QueryOperationTypes.ITEM_TYPE.of(itemType.get())).poll(itemStack.getQuantity());
                }
            }
            FactionsCache.getInstance().addOrSetClaim(new FactionClaim(chunk, worldUUID, faction.name));
            return true;
        } else return false;
    }

    public static Optional<Faction> getFactionByIdentifier(Optional<String> identifier) {
        if (identifier.isPresent()) {
            Optional<Faction> faction = FactionsCache.getInstance().getFaction(identifier.get());
            if (faction.isPresent()) {
                return faction;
            }

            Optional<Player> player = Sponge.getGame().getServer().getPlayer(identifier.get());
            if (player.isPresent()) {
                return FactionsCache.getInstance().getFactionByPlayer(player.get().getUniqueId());
            }
        }
        return Optional.empty();

    }

    public static void setMember(String playerUUID, String factionName) {
        Optional<Faction> faction = FactionsCache.getInstance().getFaction(factionName);
        if (faction.isPresent()) {
            FactionPlayer player = faction.get().getMember(playerUUID);
            player.clearGroups();
            player.addGroup("member");
        }
    }

    public static void setOfficer(String playerUUID, String factionName) {
        Optional<Faction> faction = FactionsCache.getInstance().getFaction(factionName);
        if (faction.isPresent()) {
            FactionPlayer player = faction.get().getMember(playerUUID);
            player.clearGroups();
            player.addGroup("officer");
        }
    }

}
