package io.github.aquerr.eaglefactions.parsers;

import io.github.aquerr.eaglefactions.EagleFactions;
import io.github.aquerr.eaglefactions.logic.FactionLogic;
import io.github.aquerr.eaglefactions.services.PlayerService;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.ArgumentParseException;
import org.spongepowered.api.command.args.CommandArgs;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.service.user.UserStorageService;
import org.spongepowered.api.text.Text;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EaglePlayerElement extends CommandElement
{
    CommandArgs errorargs;

    public EaglePlayerElement(@Nullable Text key)
    {
        super(key);
    }

    @Nullable
    @Override
    protected Optional<Player> parseValue(CommandSource source, CommandArgs args) throws ArgumentParseException
    {
        errorargs = args;

        try
        {
            UserStorageService userStorageService = Sponge.getServiceManager().provide(UserStorageService.class).get();

            String playerName = args.next();
            Optional<User> optionalUser = userStorageService.get(playerName);

            if (optionalUser.isPresent())
            {
                EagleFactions.getEagleFactions().getLogger().info("Found user = " + optionalUser.get().toString());
                EagleFactions.getEagleFactions().getLogger().info("Player = " + optionalUser.get().getPlayer().toString());
                return optionalUser.get().getPlayer();
            }
            else
            {
                return Optional.empty();
            }
        }
        catch (Exception e){
            throw errorargs.createError(Text.of("'" + args + "' is not a vaild player!"));
        }
    }

    @Override
    public List<String> complete(CommandSource src, CommandArgs args, CommandContext context)
    {
        List<String> playerNames = new ArrayList<>();

        FactionLogic.getAllServerPlayersNicknames().forEach(x-> playerNames.add(x));

        return playerNames;
    }
}
