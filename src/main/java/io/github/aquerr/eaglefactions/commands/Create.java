package io.github.aquerr.eaglefactions.commands;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import io.github.aquerr.eaglefactions.PluginPermissions;
import io.github.aquerr.eaglefactions.caching.FactionsCache;
import io.github.aquerr.eaglefactions.commands.annotations.*;
import io.github.aquerr.eaglefactions.commands.assembly.FactionCommand;
import io.github.aquerr.eaglefactions.commands.enums.ArgType;
import io.github.aquerr.eaglefactions.commands.enums.CommandUser;
import io.github.aquerr.eaglefactions.entities.Faction;
import io.github.aquerr.eaglefactions.entities.FactionPlayer;
import io.github.aquerr.eaglefactions.logic.FactionLogic;
import org.slf4j.Logger;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

import static org.spongepowered.api.text.format.TextColors.*;

@Singleton
@RequiresFaction(false)
@AllowedGroups(groups = CommandUser.PLAYER)
@Arguments(arguments = @Arg(type = ArgType.STRING, key = "name"))
@Subcommand(aliases = {"create", "new"}, desc = "create a new faction", permission = PluginPermissions.CreateCommand)
public class Create extends FactionCommand {

    @Inject
    public Create(FactionsCache cache, FactionLogic factionLogic, @Named("factions") Logger logger) {
        super(cache, factionLogic, logger);
    }

    @Override
    protected boolean executeCommand(CommandSource source, CommandContext context) {
        String factionName = (String) context.getOne("name").get();
        Player player = (Player) source;

        if (cache.getFaction(factionName).isPresent()) {
            source.sendMessage(Text.of(RED, "That name is already in use."));
            return true;
        }

        if (factionName.matches("^[A-Za-z][A-Za-z0-9]*$")) {
            source.sendMessage(Text.of(RED, "That name is already in use."));
            return true;
        }

        Faction faction = new Faction(factionName, new FactionPlayer(player.getUniqueId().toString(), player.getName(), factionName));
        cache.addFaction(faction);
        cache.updatePlayer(player.getUniqueId().toString(), factionName);

        factionLogic.notifyAllPlayers(player, Text.of(YELLOW, " created a new faction "), faction);
        source.sendMessage(Text.of(YELLOW, "You should now: ", AQUA, "/f desc ", DARK_AQUA, "<desc>"));
        return true;
    }
}
