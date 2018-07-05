package io.github.aquerr.eaglefactions.commands;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import io.github.aquerr.eaglefactions.PluginPermissions;
import io.github.aquerr.eaglefactions.caching.FactionsCache;
import io.github.aquerr.eaglefactions.commands.annotations.AllowedGroups;
import io.github.aquerr.eaglefactions.commands.annotations.Arg;
import io.github.aquerr.eaglefactions.commands.annotations.Arguments;
import io.github.aquerr.eaglefactions.commands.annotations.Subcommand;
import io.github.aquerr.eaglefactions.commands.assembly.FactionCommand;
import io.github.aquerr.eaglefactions.commands.enums.ArgType;
import io.github.aquerr.eaglefactions.commands.enums.CommandUser;
import io.github.aquerr.eaglefactions.entities.Faction;
import io.github.aquerr.eaglefactions.logic.FactionLogic;
import io.github.aquerr.eaglefactions.logic.FactionsPagination;
import io.github.aquerr.eaglefactions.wrapper.Wilderness;
import org.slf4j.Logger;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

import java.util.Comparator;
import java.util.Optional;
import java.util.UUID;

import static org.spongepowered.api.text.format.TextColors.DARK_GREEN;

@Singleton
@AllowedGroups(groups = {CommandUser.PLAYER, CommandUser.CONSOLE})
@Arguments(arguments = @Arg(type = ArgType.INTEGER, key = "page", optional = true))
@Subcommand(aliases = {"list", "ls"}, desc = "", permission = PluginPermissions.ListCommand, usage = "[page=1]")
public class List extends FactionCommand {

    @Inject
    public List(FactionsCache cache, FactionLogic factionLogic, @Named("factions") Logger logger) {
        super(cache, factionLogic, logger);
    }

    @Override
    protected boolean executeCommand(CommandSource source, CommandContext context) {
        Optional<Integer> page = context.getOne("page");
        FactionsPagination pagination = new FactionsPagination();
        cache.getFactions().sort(Comparator.comparingInt(a -> factionLogic.getOnlinePlayers(a).size()));
        UUID userFaction;
        if (source instanceof Player) {
            Optional<Faction> faction = cache.getFactionByPlayer(((Player) source).getUniqueId());
            userFaction = faction.isPresent() ? faction.get().fid : Wilderness.get().fid;
        }else{
            userFaction = Wilderness.get().fid;
        }

        cache.getFactions().forEach((faction) -> {
            pagination.append(Text.of(FactionLogic.getRelationColor(faction.fid, userFaction), " " + faction.name + " ",
                    factionLogic.getOnlinePlayers(faction) + "/" + faction.members.size() + " online " + faction.getClaimRatio()));
        });
        pagination.title(Text.of(DARK_GREEN, "Help for command \"f\""));
        pagination.baseCommand("/f ?");
        if (page.isPresent()) {
            pagination.page(page.get());
        }
        pagination.sendTo(source);
        return true;
    }

}
