package io.github.aquerr.eaglefactions.commands.permission;

import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.entities.Faction;
import io.github.aquerr.eaglefactions.logic.FactionLogic;
import io.github.aquerr.eaglefactions.entities.Group;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.service.pagination.PaginationService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColor;
import org.spongepowered.api.text.format.TextColors;

import java.util.Optional;

public class PermCommand implements CommandExecutor {

    private static final TextColor constant = TextColors.GOLD;
    private static final TextColor fillIn = TextColors.WHITE;


    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        Optional<PermScope> scope = args.getOne("scope");
        Optional<String> group = args.getOne("group");
        Optional<PermAction> action = args.getOne("action");
        Optional<String> node = args.getOne("node");

        //For debug
        System.out.println("Scope: " + (scope.isPresent() ? scope.get().toString() : "None"));
        System.out.println("Group: " + (group.isPresent() ? group.get() : "None"));
        System.out.println("Action: " + (action.isPresent() ? action.get().toString() : "None"));
        System.out.println("node: " + (node.isPresent() ? node.get() : "None"));


        if (!(src instanceof Player)) {
            src.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, "You must be in game and in a faction to use this command."));
            return CommandResult.success();
        }

        Optional<Faction> optionalFaction = FactionLogic.getFactionByPlayerUUID(((Player) src).getUniqueId());
        Faction faction;
        if (!optionalFaction.isPresent()) {
            src.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, "You must be in a faction to use this command."));
            return CommandResult.success();
        }
        faction = optionalFaction.get();

        if (scope.isPresent()) {
            if (scope.get() == PermScope.help) {
                printHelp(src);
                return CommandResult.success();
            } else if (scope.get() == PermScope.group) {
                if (group.isPresent()) {
                    if (faction.groups.containsKey(group.get())) {
                        if (action.isPresent()) {
                            //TODO
                        } else {
                            Text.Builder text = Text.of("Inherited Commands:\n").toBuilder();
                            faction.groups.get(group.get()).perms.getInheritedNodes(faction).forEach(e -> text.append(Text.of("    \"" + e + "\"\n")));
                            text.append(Text.of("\nPersonal Commands:\n"));
                            faction.groups.get(group.get()).perms.nodes.forEach(e -> text.append(Text.of("    \"" + e + "\"\n")));
                            Sponge.getServiceManager().provide(PaginationService.class).get().builder().title(Text.of(fillIn, "Group \"" + group.get() + "\" (priority=" + faction.groups.get(group.get()).priority + ")"))
                                    .padding(Text.of(constant, "-")).contents(text.build()).sendTo(src);
                        }
                    } else if (action.isPresent() && action.get() == PermAction.create) {
                        faction.groups.put(group.get(), new Group(group.get()));
                        src.sendMessage(Text.of(PluginInfo.PluginPrefix, constant, "Created group \"", fillIn, group.get(), constant, "\" (priority=25)."));
                        FactionLogic.saveFaction(faction);
                        return CommandResult.success();
                    } else {
                        src.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, "Your faction does not contain that group! Use \"/f perm group "
                                + group.get() + "create\" if you want to create a group with that name."));
                        return CommandResult.success();
                    }
                    faction.groups.get(group.get());
                } else {
                    //TODO error, no group given!
                }
            } else {
                //TODO if scope is user
            }
        }else {
            printHelp(src);
        }


        return CommandResult.success();
    }


    //TODO: Write help for permissions
    private static void printHelp(CommandSource source) {
        source.sendMessage(Text.of("This is a placeholder for the help"));
    }
}
