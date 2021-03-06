package io.github.aquerr.eaglefactions.commands.assembly;

import io.github.aquerr.eaglefactions.caching.FactionsCache;
import io.github.aquerr.eaglefactions.commands.annotations.AllowedGroups;
import io.github.aquerr.eaglefactions.commands.annotations.RequiresFaction;
import io.github.aquerr.eaglefactions.commands.enums.CommandUser;
import io.github.aquerr.eaglefactions.entities.Faction;
import io.github.aquerr.eaglefactions.logic.FactionLogic;
import org.slf4j.Logger;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

/**
 * Gets rid of all of the annoying repetitive checks so a command can contain what it really needs.
 */
@RequiresFaction
@AllowedGroups(groups = CommandUser.PLAYER)
public abstract class FactionPlayerCommand extends FactionCommand {

    public FactionPlayerCommand(FactionsCache cache, FactionLogic factionLogic, Logger logger) {
        super(cache, factionLogic, logger);
    }

    @Override
    public CommandResult execute(CommandSource source, CommandContext context) throws CommandException {
        if (verifyConstraints(source)) {
            if (!executeCommand((Player) source, cache.getFactionByPlayer(((Player) source).getUniqueId()).get(), context)) {
                throw new CommandException(Text.of(TextColors.RED, "Something went wrong in ", getClass().getCanonicalName()));
            }
        }
        return CommandResult.success();
    }

    @Override
    protected boolean executeCommand(CommandSource source, CommandContext context) {
        return true;
    }

    protected abstract boolean executeCommand(Player player, Faction faction, CommandContext context);
}
