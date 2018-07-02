package io.github.aquerr.eaglefactions.commands;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.github.aquerr.eaglefactions.PluginPermissions;
import io.github.aquerr.eaglefactions.caching.FactionsCache;
import io.github.aquerr.eaglefactions.commands.annotations.Arg;
import io.github.aquerr.eaglefactions.commands.annotations.Arguments;
import io.github.aquerr.eaglefactions.commands.annotations.RequiresFaction;
import io.github.aquerr.eaglefactions.commands.annotations.Subcommand;
import io.github.aquerr.eaglefactions.commands.assembly.FactionCommand;
import io.github.aquerr.eaglefactions.commands.enums.ArgType;
import io.github.aquerr.eaglefactions.logic.FactionLogic;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;

import java.util.logging.Logger;

@Singleton
@RequiresFaction(false)
@Arguments(arguments = @Arg(type = ArgType.STRING, key = "faction"))
@Subcommand(aliases = {"create", "new"}, desc = "create a new faction", permission = PluginPermissions.CreateCommand)
public class Create extends FactionCommand {

    @Inject
    public Create(FactionsCache cache, FactionLogic factionLogic, Logger logger) {
        super(cache, factionLogic, logger);
    }

    @Override
    protected boolean executeCommand(CommandSource source, CommandContext context) {
        return false;
    }
}
