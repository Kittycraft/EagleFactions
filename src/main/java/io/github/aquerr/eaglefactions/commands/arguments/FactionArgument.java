package io.github.aquerr.eaglefactions.commands.arguments;

import io.github.aquerr.eaglefactions.caching.FactionsCache;
import io.github.aquerr.eaglefactions.entities.Faction;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.ArgumentParseException;
import org.spongepowered.api.command.args.CommandArgs;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class FactionArgument extends CommandElement {

    private FactionsCache cache = FactionsCache.getInstance();

    public FactionArgument(@Nullable Text key) {
        super(key);
    }

    @Nullable
    @Override
    protected Faction parseValue(CommandSource source, CommandArgs args) throws ArgumentParseException {
        Optional<Faction> faction = Optional.empty();
        if(args.hasNext()) {
            faction = cache.getFaction(args.next());
        }
        if(!faction.isPresent() && source instanceof Player){
            faction = cache.getFactionByPlayer(((Player) source).getUniqueId());
        }
        return faction.isPresent() ? faction.get() : null;
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

    @Override
    public Text getUsage(CommandSource src) {
        return Text.of("[faction=your]");
    }
}
