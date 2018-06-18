package io.github.aquerr.eaglefactions;

import com.google.inject.Inject;
import io.github.aquerr.eaglefactions.commands.HelpCommand;
import io.github.aquerr.eaglefactions.commands.SubcommandFactory;
import io.github.aquerr.eaglefactions.config.Configuration;
import io.github.aquerr.eaglefactions.entities.ChatEnum;
import io.github.aquerr.eaglefactions.entities.Invite;
import io.github.aquerr.eaglefactions.listeners.*;
import io.github.aquerr.eaglefactions.logic.FactionLogic;
import io.github.aquerr.eaglefactions.logic.MessageLoader;
import io.github.aquerr.eaglefactions.logic.PVPLogger;
import io.github.aquerr.eaglefactions.managers.PlayerManager;
import io.github.aquerr.eaglefactions.managers.PowerManager;
import io.github.aquerr.eaglefactions.version.VersionChecker;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.nio.file.Path;
import java.util.*;

@Plugin(id = PluginInfo.Id, name = PluginInfo.Name, version = PluginInfo.Version, description = PluginInfo.Description, authors = PluginInfo.Author)
public class EagleFactions {
    public static List<Invite> InviteList = new ArrayList<>();
    public static List<UUID> AutoClaimList = new ArrayList<>();
    public static List<UUID> AutoMapList = new ArrayList<>();
    public static List<UUID> AdminList = new ArrayList<>();
    public static Map<String, Integer> AttackedFactions = new HashMap<>();
    public static Map<UUID, Integer> BlockedHome = new HashMap<>();
    public static Map<UUID, ChatEnum> ChatList = new HashMap<>();
    public static Map<UUID, Integer> HomeCooldownPlayers = new HashMap<>();
    private static EagleFactions eagleFactions;
    private Configuration configuration;
    private PVPLogger _pvpLogger;
    @Inject
    private Logger _logger;
    @Inject
    @ConfigDir(sharedRoot = false)
    private Path _configDir;

    public static EagleFactions getEagleFactions() {
        return eagleFactions;
    }

    public static Logger getLogger(){
        return EagleFactions.getEagleFactions()._logger;
    }

    @Listener
    public void onServerInitialization(GameInitializationEvent event) {
        eagleFactions = this;

        Sponge.getServer().getConsole().sendMessage(Text.of(TextColors.AQUA, "Preparing wings..."));

        SetupConfigs();

        Sponge.getServer().getConsole().sendMessage(Text.of(TextColors.AQUA, "Configs loaded..."));

        //Build all commands
        CommandSpec commandEagleFactions = CommandSpec.builder()
                .description(Text.of("Help Command"))
                .executor(new HelpCommand())
                .children(SubcommandFactory.getSubcommands())
                .build();

        //Register commands
        Sponge.getCommandManager().register(this, commandEagleFactions, "factions", "faction", "f");

        Sponge.getServer().getConsole().sendMessage(Text.of(TextColors.AQUA, "Commands loaded..."));

        //Register listeners
        Arrays.asList(new EntityDamageListener(),new PlayerJoinListener(), new PlayerDeathListener(),
                new PlayerBlockPlaceListener(), new BlockBreakListener(),new PlayerInteractListener(),
                new PlayerMoveListener(), new ChatMessageListener(), new EntitySpawnListener(),
                new FireBlockPlaceListener(), new PlayerDisconnectListener(), new MobTargetListener(),
                new SendCommandListener())
                .forEach(e -> Sponge.getEventManager().registerListeners(this, e));

        //Display some info text in the console.
        Sponge.getServer().getConsole().sendMessage(Text.of(TextColors.GREEN, "=========================================="));
        Sponge.getServer().getConsole().sendMessage(Text.of(TextColors.AQUA, "Eagle Factions", TextColors.WHITE, " is ready to use!"));
        Sponge.getServer().getConsole().sendMessage(Text.of(TextColors.WHITE, "Thank you for choosing this plugin!"));
        Sponge.getServer().getConsole().sendMessage(Text.of(TextColors.WHITE, "Current version: " + PluginInfo.Version));
        Sponge.getServer().getConsole().sendMessage(Text.of(TextColors.WHITE, "Have a great time with Eagle Factions! :D"));
        Sponge.getServer().getConsole().sendMessage(Text.of(TextColors.GREEN, "=========================================="));

        if (!VersionChecker.isLatest(PluginInfo.Version)) {
            Sponge.getServer().getConsole().sendMessage(Text.of(TextColors.GOLD, "Hey! A new version of ", TextColors.AQUA, PluginInfo.Name, TextColors.GOLD, " is available online!"));
            Sponge.getServer().getConsole().sendMessage(Text.of(TextColors.GREEN, "=========================================="));
        }
    }

    private void SetupConfigs() {
        // Create configs
        configuration = new Configuration(_configDir);

        FactionLogic.setup(_configDir);
        PlayerManager.setup(_configDir);
        PowerManager.setup(_configDir);

        MessageLoader messageLoader = new MessageLoader(_configDir);

        //PVPLogger
        _pvpLogger = new PVPLogger();
    }


    public Configuration getConfiguration() {
        return this.configuration;
    }

    public PVPLogger getPVPLogger() {
        return this._pvpLogger;
    }
}
