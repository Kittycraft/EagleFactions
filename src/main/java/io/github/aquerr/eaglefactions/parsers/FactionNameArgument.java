package io.github.aquerr.eaglefactions.parsers;

import io.github.aquerr.eaglefactions.caching.FactionsCache;
import io.github.aquerr.eaglefactions.logic.FactionLogic;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.ArgumentParseException;
import org.spongepowered.api.command.args.CommandArgs;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.text.Text;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

public class FactionNameArgument extends CommandElement {
    public FactionNameArgument(@Nullable Text key) {
        super(key);
    }

    @Nullable
    @Override
    protected String parseValue(CommandSource source, CommandArgs args) throws ArgumentParseException {
        if (args.hasNext()) {
            return args.next();
        } else {
            return null;
        }
    }

    @Override
    public List<String> complete(CommandSource src, CommandArgs args, CommandContext context) {
        Set<String> factionNames = FactionsCache.getInstance().getFactionNames();

        if (args.hasNext()) {
            String charSequence = args.nextIfPresent().get();
            return factionNames.stream().filter(x -> x.contains(charSequence)).collect(Collectors.toList());
        }

        return new ArrayList(factionNames);
    }
}
