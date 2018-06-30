package io.github.aquerr.eaglefactions.commands.legacy;

import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.caching.FactionsCache;
import io.github.aquerr.eaglefactions.entities.Faction;
import io.github.aquerr.eaglefactions.entities.RelationType;
import io.github.aquerr.eaglefactions.logic.FactionLogic;
import io.github.aquerr.eaglefactions.managers.PlayerManager;
import io.github.aquerr.eaglefactions.managers.PowerManager;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Created by Aquerr on 2017-08-03.
 * <p>
 * Finished for now.
 * <p>
 * TODO:
 * Show relationships only if there is at least one in the specific category.
 */
public class InfoCommand implements CommandExecutor {

    private static final String dash = "-----------------------------------------------";

    @Override
    public CommandResult execute(CommandSource source, CommandContext context) throws CommandException {
        Optional<String> optionalFactionName = context.getOne("faction uuid");

        Faction faction;
        if (optionalFactionName.isPresent()) {
            faction = FactionLogic.getFactionByIdentifier(optionalFactionName).get();
        } else {
            if (source instanceof Player && FactionsCache.getInstance().getFactionByPlayer(((Player) source).getUniqueId()).isPresent()) {
                faction = FactionsCache.getInstance().getFactionByPlayer(((Player) source).getUniqueId()).get();
            } else {
                faction = FactionsCache.getInstance().getFaction("wilderness").get();
            }
        }
        if(faction == null){
            source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, "Could not find faction or user."));
        }else {
            showFactionInfo(source, faction);
        }
        return CommandResult.success();
    }

    private String compileRelationList(String faction, RelationType type){
        StringBuilder sb = new StringBuilder();
        for (Faction alliance : FactionLogic.getRelationGroup(faction, type)) {
             sb.append(alliance.name + ", ");
        }
        return sb.length() > 2 ? sb.substring(0, sb.length() - 2) : "";
    }

    private void showFactionInfo(CommandSource source, Faction faction) {
        Text.Builder text = Text.of("").toBuilder();
        text.append(Text.of(TextColors.GOLD, dash.substring(0, 24 - faction.name.length() / 2),
                ".[ ", TextColors.GREEN, faction.name, TextColors.GOLD, " ].", dash.substring(0, 24 - faction.name.length() / 2), "\nDescription: ", TextColors.YELLOW, "Not supported \u2764\n"));

        text.append(Text.of(TextColors.GOLD, "Land / Power / Maxpower: ", TextColors.YELLOW, faction.claims.size() + " / " + PowerManager.getFactionPower(faction) + " / " + PowerManager.getFactionMaxPower(faction) + "\n"))
                .append(Text.of(TextColors.GOLD, "Allies: ", TextColors.GREEN, compileRelationList(faction.name, RelationType.ALLY) + "\n"))
                .append(Text.of(TextColors.GOLD, "Truces: ", TextColors.GREEN, compileRelationList(faction.name, RelationType.TRUCE) + "\n"))
                .append(Text.of(TextColors.GOLD, "Enemies: ", TextColors.GREEN, compileRelationList(faction.name, RelationType.ENEMY) + "\n"));

        List<Player> onlinePlayers = FactionLogic.getOnlinePlayers(faction);
        text.append(Text.of(TextColors.GOLD, "Online Players (" + onlinePlayers.size() + "): ", TextColors.YELLOW));
        Text.Builder offline = Text.of(TextColors.GOLD, "Offline Players (" + (faction.members.size() - onlinePlayers.size()) + "): ", TextColors.YELLOW).toBuilder();
        mainLoop:
        for (int i = 0, j = 0; i < faction.members.size(); i++) {
            for (Player p : onlinePlayers) {
                if (p.getUniqueId().toString().equals(faction.members.get(i).uuid)) {
                    text.append(p.getDisplayNameData().displayName().get());
                    if ((j += 1) != onlinePlayers.size()) {
                        text.append(Text.of(", "));
                    }
                    continue mainLoop;
                }
            }
            offline.append(Text.of(PlayerManager.getPlayerName(UUID.fromString(faction.members.get(i).uuid)).get()));
            if (i - j != faction.members.size() - onlinePlayers.size() - 1) {
                offline.append(Text.of(", "));
            }
        }
        text.append(Text.of("\n", offline.build()));
        source.sendMessage(text.build());
    }
}
