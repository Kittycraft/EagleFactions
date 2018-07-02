package io.github.aquerr.eaglefactions.commands;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import io.github.aquerr.eaglefactions.PluginPermissions;
import io.github.aquerr.eaglefactions.caching.FactionsCache;
import io.github.aquerr.eaglefactions.commands.annotations.AllowedGroups;
import io.github.aquerr.eaglefactions.commands.annotations.Arg;
import io.github.aquerr.eaglefactions.commands.annotations.Arguments;
import io.github.aquerr.eaglefactions.commands.annotations.Subcommand;
import io.github.aquerr.eaglefactions.commands.assembly.FactionCommand;
import io.github.aquerr.eaglefactions.commands.assembly.SubcommandFactory;
import io.github.aquerr.eaglefactions.commands.enums.ArgType;
import io.github.aquerr.eaglefactions.commands.enums.CommandUser;
import io.github.aquerr.eaglefactions.logic.FactionLogic;
import io.github.aquerr.eaglefactions.logic.FactionsPagination;
import org.slf4j.Logger;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.text.Text;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.spongepowered.api.text.format.TextColors.*;

//TODO: Rework to be usable as sub-sub-command help
@Singleton
@AllowedGroups(groups = {CommandUser.PLAYER, CommandUser.CONSOLE})
@Arguments(arguments = @Arg(type = ArgType.INTEGER, key = "page", optional = true))
@Subcommand(aliases = {"help", "h", "?"}, desc = "", permission = PluginPermissions.HelpCommand, usage = "[page=1]")
public class Help extends FactionCommand {

    private Provider<SubcommandFactory> subcommandFactoryProvider;

    @Inject
    public Help(FactionsCache cache, FactionLogic factionLogic, @Named("factions") Logger logger, Provider<SubcommandFactory> subcommandFactory) {
        super(cache, factionLogic, logger);
        this.subcommandFactoryProvider = subcommandFactory;
    }

    @Override
    protected boolean executeCommand(CommandSource source, CommandContext context) {
        Optional<Integer> page = context.getOne("page");
        FactionsPagination pagination = new FactionsPagination();

        Map<List<String>, CommandSpec> subcommands = (Map<List<String>, CommandSpec>) subcommandFactoryProvider.get().getSubcommands();

        subcommands.forEach((key, value) -> {
            String aliases = "";
            for (String string : key) {
                aliases += "," + string;
            }
            FactionCommand command = (FactionCommand) value.getExecutor();
            Text usage = value.getUsage(source);
            if(command.usage != null){
                usage = Text.of(command.usage);
            }
            pagination.append(Text.of(AQUA, "/f " + aliases.substring(1) + " ", value.testPermission(source) ? DARK_AQUA : RED, usage, " ", YELLOW, value.getShortDescription(source).get()));
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
