package io.github.aquerr.eaglefactions.commands.legacy;

import io.github.aquerr.eaglefactions.PluginPermissions;
import io.github.aquerr.eaglefactions.commands.permission.PermAction;
import io.github.aquerr.eaglefactions.commands.permission.PermCommand;
import io.github.aquerr.eaglefactions.commands.permission.PermScope;
import io.github.aquerr.eaglefactions.commands.relation.AllyCommand;
import io.github.aquerr.eaglefactions.commands.relation.EnemyCommand;
import io.github.aquerr.eaglefactions.commands.relation.NeutralCommand;
import io.github.aquerr.eaglefactions.commands.relation.TruceCommand;
import io.github.aquerr.eaglefactions.entities.ChatEnum;
import io.github.aquerr.eaglefactions.parsers.FactionNameArgument;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.text.Text;

import java.util.*;

public class SubcommandFactory {

    private static Map<List<String>, CommandSpec> Subcommands;

    public static Map<List<String>, CommandSpec> getSubcommands() {
       if(Subcommands == null){
           createSubcommands();
       }
       return Subcommands;
    }

    private static void createSubcommands(){
            //Help command should display all possible commands in plugin.
        Subcommands = new HashMap<>();
            Subcommands.put(Collections.singletonList("help"), CommandSpec.builder()
                    .description(Text.of("Help"))
                    .permission(PluginPermissions.HelpCommand)
                    .executor(new HelpCommand())
                    .build());

            //Create faction command.
            Subcommands.put(Arrays.asList("create"), CommandSpec.builder()
                    .description(Text.of("Create Faction Command"))
                    .permission(PluginPermissions.CreateCommand)
                    .arguments(GenericArguments.optional(GenericArguments.string(Text.of("tag"))),
                            GenericArguments.optional(GenericArguments.string(Text.of("faction uuid"))))
                    .executor(new CreateCommand())
                    .build());

            //Disband faction command.
            Subcommands.put(Collections.singletonList("disband"), CommandSpec.builder()
                    .description(Text.of("Disband Faction Command"))
                    .permission(PluginPermissions.DisbandCommand)
                    .executor(new DisbandCommand())
                    .build());

            //List all factions.
            Subcommands.put(Collections.singletonList("list"), CommandSpec.builder()
                    .description(Text.of("List all factions"))
                    .permission(PluginPermissions.ListCommand)
                    .executor(new ListCommand())
                    .build());

            //Invite a player to the faction.
            Subcommands.put(Collections.singletonList("invite"), CommandSpec.builder()
                    .description(Text.of("Invites a player to the faction"))
                    .permission(PluginPermissions.InviteCommand)
                    .arguments(GenericArguments.optional(GenericArguments.player(Text.of("player"))))
                    .executor(new InviteCommand())
                    .build());

            //Kick a player from the faction.
            Subcommands.put(Collections.singletonList("kick"), CommandSpec.builder()
                    .description(Text.of("Kicks a player from the faction"))
                    .permission(PluginPermissions.KickCommand)
                    .arguments(GenericArguments.optional(GenericArguments.player(Text.of("player"))))
                    .executor(new KickCommand())
                    .build());

            //Join faction command
            Subcommands.put(Arrays.asList("j", "join"), CommandSpec.builder()
                    .description(Text.of("Join a specific faction"))
                    .permission(PluginPermissions.JoinCommand)
                    .arguments(new FactionNameArgument(Text.of("faction uuid")))
                    .executor(new JoinCommand())
                    .build());

            //Leave faction command
            Subcommands.put(Collections.singletonList("leave"), CommandSpec.builder()
                    .description(Text.of("Leave a faction"))
                    .permission(PluginPermissions.LeaveCommand)
                    .executor(new LeaveCommand())
                    .build());

            //Version command
            Subcommands.put(Arrays.asList("v", "version"), CommandSpec.builder()
                    .description(Text.of("Shows plugin version"))
                    .permission(PluginPermissions.VersionCommand)
                    .executor(new VersionCommand())
                    .build());

            //Info command. Shows info about a faction.
            Subcommands.put(Arrays.asList("i", "info", "f", "show", "faction"), CommandSpec.builder()
                    .description(Text.of("Show info about a faction"))
                    .arguments(new FactionNameArgument(Text.of("faction uuid")))
                    .permission(PluginPermissions.InfoCommand)
                    .executor(new InfoCommand())
                    .build());

            //TODO: Reformat how /f p looks
            //FactionPlayer command. Shows info about a player. (their faction etc.)
            Subcommands.put(Arrays.asList("p", "player"), CommandSpec.builder()
                    .description(Text.of("Show info about a player"))
                    .permission(PluginPermissions.PlayerCommand)
                    .arguments(GenericArguments.optional(GenericArguments.player(Text.of("player"))))
                    .executor(new PlayerCommand())
                    .build());


            //New ally command
            Subcommands.put(Arrays.asList("a", "ally"), CommandSpec.builder()
                    .description(Text.of("Send or accept an alliance request"))
                    .permission(PluginPermissions.AllyCommands)
                    .arguments(new FactionNameArgument(Text.of("faction uuid")))
                    .executor(new AllyCommand())
                    .build());

            //New enemy command
            Subcommands.put(Arrays.asList("e", "enemy"), CommandSpec.builder()
                    .description(Text.of("Enemy another faction"))
                    .permission(PluginPermissions.EnemyCommands)
                    .arguments(new FactionNameArgument(Text.of("faction uuid")))
                    .executor(new EnemyCommand())
                    .build());

            Subcommands.put(Arrays.asList("neutral"), CommandSpec.builder()
                    .description(Text.of("Request to be neutral with another faction"))
                    .permission(PluginPermissions.EnemyCommands)
                    .arguments(new FactionNameArgument(Text.of("faction uuid")))
                    .executor(new NeutralCommand())
                    .build());

            Subcommands.put(Arrays.asList("t", "truce"), CommandSpec.builder()
                    .description(Text.of("Send or accept a truce request"))
                    .permission(PluginPermissions.EnemyCommands)
                    .arguments(new FactionNameArgument(Text.of("faction uuid")))
                    .executor(new TruceCommand())
                    .build());

            //Officer command. Add or remove officers.
            Subcommands.put(Collections.singletonList("officer"), CommandSpec.builder()
                    .description(Text.of("Add or Remove officer"))
                    .arguments(GenericArguments.optional(GenericArguments.player(Text.of("player"))))
                    .permission(PluginPermissions.OfficerCommand)
                    .executor(new OfficerCommand())
                    .build());

            //Member command.
            Subcommands.put(Collections.singletonList("member"), CommandSpec.builder()
                    .description(Text.of("Add or remove member"))
                    .arguments(GenericArguments.optional(GenericArguments.player(Text.of("player"))))
                    .permission(PluginPermissions.MemberCommand)
                    .executor(new MemberCommand())
                    .build());

            //Claim command.
            Subcommands.put(Collections.singletonList("claim"), CommandSpec.builder()
                    .description(Text.of("Claim a land for your faction"))
                    .permission(PluginPermissions.ClaimCommand)
                    .executor(new ClaimCommand())
                    .build());

            //Unclaim command.
            Subcommands.put(Collections.singletonList("unclaim"), CommandSpec.builder()
                    .description(Text.of("Unclaim a land captured by your faction."))
                    .permission(PluginPermissions.UnclaimCommand)
                    .executor(new UnclaimCommand())
                    .build());

            //Add Unclaimall Command
            Subcommands.put(Collections.singletonList("unclaimall"), CommandSpec.builder()
                    .description(Text.of("Remove all claims"))
                    .permission(PluginPermissions.UnclaimAllCommand)
                    .executor(new UnclaimallCommand())
                    .build());

            //Map command
            Subcommands.put(Collections.singletonList("map"), CommandSpec.builder()
                    .description(Text.of("Turn on/off factions map"))
                    .permission(PluginPermissions.MapCommand)
                    .executor(new MapCommand())
                    .build());

            //Sethome command
            Subcommands.put(Collections.singletonList("sethome"), CommandSpec.builder()
                    .description(Text.of("Set faction's home"))
                    .permission(PluginPermissions.SetHomeCommand)
                    .executor(new SetHomeCommand())
                    .build());

            //Home command
            Subcommands.put(Collections.singletonList("home"), CommandSpec.builder()
                    .description(Text.of("Teleport to faction's home"))
                    .permission(PluginPermissions.HomeCommand)
                    .executor(new HomeCommand())
                    .build());

            //Add autoclaim command.
            Subcommands.put(Collections.singletonList("autoclaim"), CommandSpec.builder()
                    .description(Text.of("Autoclaim Command"))
                    .permission(PluginPermissions.AutoClaimCommand)
                    .executor(new AutoClaimCommand())
                    .build());

            //Add automap command
            Subcommands.put(Collections.singletonList("automap"), CommandSpec.builder()
                    .description(Text.of("Automap command"))
                    .permission(PluginPermissions.AutoMapCommand)
                    .executor(new AutoMapCommand())
                    .build());

            //Add admin command
            Subcommands.put(Collections.singletonList("admin"), CommandSpec.builder()
                    .description(Text.of("Toggle admin mode"))
                    .permission(PluginPermissions.AdminCommand)
                    .executor(new AdminCommand())
                    .build());

            //Add SetPower Command
            Subcommands.put(Collections.singletonList("setpower"), CommandSpec.builder()
                    .description(Text.of("Set player's power"))
                    .permission(PluginPermissions.SetPowerCommand)
                    .arguments(GenericArguments.optional(GenericArguments.player(Text.of("player"))),
                            GenericArguments.optional(GenericArguments.string(Text.of("power"))))
                    .executor(new SetPowerCommand())
                    .build());

            //Reload Command
            Subcommands.put(Collections.singletonList("reload"), CommandSpec.builder()
                    .description(Text.of("Reload config file"))
                    .permission(PluginPermissions.ReloadCommand)
                    .executor(new ReloadCommand())
                    .build());

            //Chat Command
            Subcommands.put(Arrays.asList("chat", "c"), CommandSpec.builder()
                    .description(Text.of("Chat command"))
                    .permission(PluginPermissions.ChatCommand)
                    .arguments(GenericArguments.optional(GenericArguments.enumValue(Text.of("chat"), ChatEnum.class)))
                    .executor(new ChatCommand())
                    .build());

            //Top Command
            Subcommands.put(Collections.singletonList("top"), CommandSpec.builder()
                    .description(Text.of("Top Command"))
                    .permission(PluginPermissions.TopCommand)
                    .executor(new TopCommand())
                    .build());

            //Setleader Command
            Subcommands.put(Arrays.asList("setleader", "leader"), CommandSpec.builder()
                    .description(Text.of("Set someone as leader (removes you as a leader if you are one)"))
                    .permission(PluginPermissions.SetLeaderCommand)
                    .arguments(GenericArguments.optional(GenericArguments.player(Text.of("player"))))
                    .executor(new SetLeaderCommand())
                    .build());

            Subcommands.put(Arrays.asList("p", "perm", "permission", "permissions"),CommandSpec.builder()
                    .description(Text.of("Shows and edits your faction's permission"))
                    .arguments(GenericArguments.optional(GenericArguments.seq(GenericArguments.choices(Text.of("scope"), PermScope.choices), GenericArguments.optional(GenericArguments.seq(GenericArguments.string(Text.of("group")),
                            GenericArguments.optional(GenericArguments.seq(GenericArguments.choices(Text.of("action"), PermAction.choices), GenericArguments.optional(GenericArguments.remainingJoinedStrings(Text.of("node"))))))))))
                    .permission(PluginPermissions.PermissionCommand)
                    .executor(new PermCommand())
                    .build());

            //TODO: Tag color depends on relation!
    }
}
