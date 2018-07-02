package io.github.aquerr.eaglefactions.commands.arguments;

import io.github.aquerr.eaglefactions.entities.Faction;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.ArgumentParseException;
import org.spongepowered.api.command.args.CommandArgs;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

import static org.spongepowered.api.text.format.TextColors.*;


import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public class IntegerArgument extends CommandElement {

    private Text usage;
    private final boolean optional;

    public IntegerArgument(@Nullable Text key, Text usage, boolean optional) {
        super(key);
        this.usage = usage;
        this.optional = optional;
    }

    @Nullable
    @Override
    protected Integer parseValue(CommandSource source, CommandArgs args) throws ArgumentParseException {
        if(args.hasNext()) {
            String arg = args.next();
            try{
                return Integer.parseInt(arg);
            }catch (NumberFormatException e){
                throw new GeneralArgumentParseException(Text.of(RED, "\"", LIGHT_PURPLE, arg, RED, "\" is not a number."));
            }
        }
        if(optional){
            return null;
        }
        throw new GeneralArgumentParseException(Text.of(new Object[] {RED, "Not enough command input. ", YELLOW, "You should use it like this:", '\n', usage}));
    }

    @Override
    public List<String> complete(CommandSource src, CommandArgs args, CommandContext context) {
        return Collections.EMPTY_LIST;
    }
}
