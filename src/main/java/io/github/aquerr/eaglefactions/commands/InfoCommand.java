package io.github.aquerr.eaglefactions.commands;

import io.github.aquerr.eaglefactions.entities.Faction;
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
 *
 * Finished for now.
 *
 * TODO:
 * Show relationships only if there is at least one in the specific category.
 */
public class InfoCommand implements CommandExecutor {

    private static final String dash = "-----------------------------------------------";

    @Override
    public CommandResult execute(CommandSource source, CommandContext context) throws CommandException {
        Optional<String> optionalFactionName = context.getOne("faction name");

        Faction faction;
        if (optionalFactionName.isPresent()) {
            faction = FactionLogic.getFactionByName(optionalFactionName.get());
        } else {
            if (source instanceof Player && FactionLogic.getFactionByPlayerUUID(((Player) source).getUniqueId()).isPresent()) {
                faction = FactionLogic.getFactionByPlayerUUID(((Player) source).getUniqueId()).get();
            } else {
                faction = FactionLogic.getFactionByName("wilderness");
            }
        }
        showFactionInfo(source, faction);
        return CommandResult.success();
    }

    private void showFactionInfo(CommandSource source, Faction faction) {
        Text.Builder text = Text.of("").toBuilder();
        text.append(Text.of(TextColors.GOLD, dash.substring(0, 24 - faction.Name.length() / 2),
                ".[ ", TextColors.GREEN, faction.Name, TextColors.GOLD, " ].", dash.substring(0, 24 - faction.Name.length() / 2), "\nDescription: ", TextColors.YELLOW, "Not supported \u2764\n"));

        String alliancesList = "";
        if (!faction.Alliances.isEmpty()) {
            for (String alliance : faction.Alliances) {
                alliancesList += alliance + ", ";
            }
            alliancesList = alliancesList.substring(0, alliancesList.length() - 2);
        }

        String enemiesList = "";
        if (!faction.Enemies.isEmpty()) {
            for (String enemy : faction.Enemies) {
                enemiesList += enemy + ", ";
            }
            enemiesList = enemiesList.substring(0, enemiesList.length() - 2);
        }

        text.append(Text.of(TextColors.GOLD, "Land / Power / Maxpower: ", TextColors.YELLOW, faction.Claims.size() + " / " + PowerManager.getFactionPower(faction) + " / " + PowerManager.getFactionMaxPower(faction) + "\n"))
                .append(Text.of(TextColors.GOLD, "Allies: ", TextColors.GREEN, alliancesList + "\n"))
                .append(Text.of(TextColors.GOLD, "Enemies: ", TextColors.RED, enemiesList + "\n"));

        List<Player> onlinePlayers = FactionLogic.getOnlinePlayers(faction);
        text.append(Text.of(TextColors.GOLD, "Online Players (" + onlinePlayers.size() + "): ", TextColors.YELLOW));
        Text.Builder offline = Text.of(TextColors.GOLD, "Offline Players (" + (faction.Members.size() - onlinePlayers.size()) + "): ", TextColors.YELLOW).toBuilder();
        mainLoop: for(int i = 0, j = 0; i < faction.Members.size(); i++){
            for(Player p : onlinePlayers) {
                if(p.getUniqueId().toString().equals(faction.Members.get(i).name)) {
                    text.append(p.getDisplayNameData().displayName().get());
                    if ((j += 1) != onlinePlayers.size()) {
                        text.append(Text.of(", "));
                    }
                    continue mainLoop;
                }
            }
            offline.append(Text.of(PlayerManager.getPlayerName(UUID.fromString(faction.Members.get(i).name)).get()));
            if (i - j != faction.Members.size() - onlinePlayers.size() - 1) {
                offline.append(Text.of(", "));
            }
        }
        text.append(Text.of("\n", offline.build()));
        source.sendMessage(text.build());
    }
}
