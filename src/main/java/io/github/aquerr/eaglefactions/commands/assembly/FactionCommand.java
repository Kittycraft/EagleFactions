package io.github.aquerr.eaglefactions.commands.assembly;

import com.google.inject.Inject;
import com.sun.istack.internal.NotNull;
import io.github.aquerr.eaglefactions.caching.FactionsCache;
import io.github.aquerr.eaglefactions.commands.annotations.*;
import io.github.aquerr.eaglefactions.commands.arguments.FactionArgument;
import io.github.aquerr.eaglefactions.commands.arguments.IntegerArgument;
import io.github.aquerr.eaglefactions.commands.enums.CommandUser;
import io.github.aquerr.eaglefactions.entities.Faction;
import io.github.aquerr.eaglefactions.logic.FactionLogic;
import org.slf4j.Logger;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.spongepowered.api.text.format.TextColors.AQUA;
import static org.spongepowered.api.text.format.TextColors.DARK_AQUA;

/**
 * A collection of anything a command could need. Anything else can be injected by the specific class.
 */
public abstract class FactionCommand implements CommandExecutor {
    protected FactionsCache cache;
    protected FactionLogic factionLogic;
    protected Logger logger;
    public final String usage;

    @Inject
    public FactionCommand(FactionsCache cache, FactionLogic factionLogic, Logger logger) {
        this.cache = cache;
        this.factionLogic = factionLogic;
        this.logger = logger;
        Subcommand subcommand = getAnnotation(Subcommand.class);
        if (!subcommand.usage().equals("")) {
            usage = subcommand.usage();
        } else {
            usage = null;
        }
    }

    @Override
    public CommandResult execute(CommandSource source, CommandContext context) throws CommandException {
        //TODO: Maybe get this from player
        final Map<String, ?> textTemplateMap = new HashMap<>();
        if (verifyConstraints(source)) {
            if (!executeCommand(source, context)) {
                throw new CommandException(Text.of(TextColors.RED, "Something went wrong in ", getClass().getCanonicalName()));
            }
        }
        return CommandResult.success();
    }


    protected boolean verifyConstraints(CommandSource source) {
        AllowedGroups allowedGroups = getAnnotation(AllowedGroups.class);
        RequiresFaction requiresFaction = getAnnotation(RequiresFaction.class);
        if (allowedGroups != null) {
            if (!Arrays.asList(allowedGroups.groups()).contains(CommandUser.getUserType(source))) {
                return false;
            }
        }
        if (this instanceof Player) {
            Optional<Faction> faction = cache.getFactionByPlayer(((Player) source).getUniqueId());
            if (requiresFaction != null) {
                if (requiresFaction.value() != faction.isPresent()) {
                    if (faction.isPresent()) {
                        source.sendMessage(Text.of(TextColors.RED, "You must leave your current faction first."));
                    } else {
                        source.sendMessage(Text.of(TextColors.RED, "You must leave your current faction first."));
                    }
                    return false;
                }
            }
        }
        return true;
    }

    protected abstract boolean executeCommand(CommandSource source, CommandContext context);

    public CommandElement getArguments() {
        Arguments arguments = getAnnotation(Arguments.class);
        if (arguments != null) {
            Arg[] args = arguments.arguments();
            CommandElement[] elements = new CommandElement[args.length];
            for (int i = 0; i < args.length; i++) {
                elements[i] = getElement(args[i]);
            }
            return GenericArguments.seq(elements);
        }
        return GenericArguments.none();
    }

    private CommandElement getElement(@NotNull Arg arg) {
        CommandElement inner;
        switch (arg.type()) {
            case PLAYER:
                inner = GenericArguments.player(Text.of(arg.key()));
                break;
            case FACTION:
                inner = new FactionArgument(Text.of(arg.key()));
                break;
            case INTEGER:
                inner = new IntegerArgument(Text.of(arg.key()), getUsage(), arg.optional());
                break;
            default:
                inner = GenericArguments.string(Text.of(arg.key()));
        }
        return arg.optional() ? GenericArguments.optional(inner) : inner;
    }

    private Text getUsage() {
        String aliases = "";
        for (String string : getAnnotation(Subcommand.class).aliases()) {
            aliases += "," + string;
        }
        if (usage == null) {
            System.err.println("Usage is required for subcommand: " + this.getClass().getCanonicalName());
            System.exit(0);
        }
        return Text.of(AQUA, "/f " + aliases.substring(1) + " ", DARK_AQUA, usage);
    }

    private <T extends Annotation> T getAnnotation(Class<T> tClass) {
        for (Annotation annotation : this.getClass().getAnnotations()) {
            if (tClass.isInstance(annotation)) {
                return (T) annotation;
            }
        }
        return null;
    }
}
