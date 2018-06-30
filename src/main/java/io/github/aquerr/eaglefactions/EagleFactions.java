/**
 * This file now contains portions of code which are under copyright and an MIT licence.
 * Reference: "Copyright (c) 2016 riebie, Kippers <https://bitbucket.org/Kippers/mcclans-core-sponge>"
 */
package io.github.aquerr.eaglefactions;

import com.google.inject.Inject;
import io.github.aquerr.eaglefactions.commands.legacy.HelpCommand;
import io.github.aquerr.eaglefactions.commands.legacy.SubcommandFactory;
import io.github.aquerr.eaglefactions.config.Config;
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
import nl.riebie.mcclans.persistence.DatabaseConnectionOwner;
import nl.riebie.mcclans.persistence.DatabaseHandler;
import nl.riebie.mcclans.persistence.FileUtils;
import nl.riebie.mcclans.persistence.TaskExecutor;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GameStoppingServerEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.io.File;
import java.util.*;

@Plugin(id = PluginInfo.Id, name = PluginInfo.Name, version = PluginInfo.Version, description = PluginInfo.Description, authors = PluginInfo.Author)
public class EagleFactions {
    public static List<Invite> InviteList = new ArrayList<>();
    public static List<UUID> AutoClaimList = new ArrayList<>();
    public static List<UUID> AutoMapList = new ArrayList<>();
    public static List<UUID> AdminList = new ArrayList<>();
    public static Map<UUID, Integer> BlockedHome = new HashMap<>();
    public static Map<UUID, ChatEnum> ChatList = new HashMap<>();
    public static Map<UUID, Integer> HomeCooldownPlayers = new HashMap<>();
    private static EagleFactions eagleFactions;
    private Configuration configuration;
    private PVPLogger pvpLogger;
    private boolean loadError = false;
    @Inject
    private Logger logger;
    @Inject
    @ConfigDir(sharedRoot = false)
    private File configDir;

    //TODO: Phase out this function in favor of get plugin
    @Deprecated
    public static EagleFactions getEagleFactions() {
        return eagleFactions;
    }

    public static EagleFactions getPlugin(){
        return eagleFactions;
    }

    public static Logger getLogger() {
        return EagleFactions.getPlugin().logger;
    }

    @Listener
    public void onServerInitialization(GameInitializationEvent event) {
        eagleFactions = this;

        Sponge.getServer().getConsole().sendMessage(Text.of(TextColors.AQUA, "Preparing wings..."));

        SetupConfigs();

        Sponge.getServer().getConsole().sendMessage(Text.of(TextColors.AQUA, "Configs loaded..."));

        //PVPLogger
        pvpLogger = new PVPLogger();

        //Build all commands
        CommandSpec commandEagleFactions = CommandSpec.builder()
                .description(Text.of("Help Command"))
                .executor(new HelpCommand())
                .children(SubcommandFactory.getSubcommands())
                .build();

        //Register commands
        Sponge.getCommandManager().register(this, commandEagleFactions, "factions", "faction", "f");
        Sponge.getServer().getConsole().sendMessage(Text.of(TextColors.AQUA, "Commands loaded..."));


        //From Mcclans main
        // Init database/xml
        if (Config.getBoolean(Config.USE_DATABASE) && !DatabaseHandler.getInstance().setupConnection()) {
            Config.setValue(Config.USE_DATABASE, false);
            getLogger().warn("Failed to setup connection with database " + Config.getString(Config.DATABASE_NAME) + ". Deactivating database usage for EagleFactions", true);
        }
        if (Config.getBoolean(Config.USE_DATABASE)) {
            DatabaseHandler.getInstance().setupDatabase();
            getLogger().info("Starting load from database...", true);
            long databaseLoadStartTime = System.currentTimeMillis();
            try {
                DatabaseHandler.getInstance().load();
            } catch (Exception e) {
                getLogger().error("EagleFactions: Fatal error during data load: " + e.getMessage(), true);
                Sponge.getServer().shutdown(Text.of("EagleFactions: Fatal error during data load!"));
                loadError = true;
                throw e;
            }
            getLogger().info("Finished loading in: " + (System.currentTimeMillis() - databaseLoadStartTime) + "ms", true);
            getLogger().info("Database updater starting...", false);
            if (TaskExecutor.getInstance().initialize()) {
                getLogger().info("Database updater successfully started", false);
            }
            registerDatabasePollingTask();
        } else {
            getLogger().info("Starting load from flat file...", true);
            long databaseLoadStartTime = System.currentTimeMillis();
            try {
                if (DatabaseHandler.getInstance().load()) {
                    getLogger().info("Finished loading in: " + (System.currentTimeMillis() - databaseLoadStartTime) + "ms", true);
                } else {
                    getLogger().info("No data loaded from flat file", true);
                }
            } catch (Exception e) {
                getLogger().error("EagleFactions: Fatal error during data load: " + e.getMessage(), true);
                Sponge.getServer().shutdown(Text.of("EagleFactions: Fatal error during data load!"));
                loadError = true;
                throw e;
            }
        }

        if (Config.getInteger(Config.CREATE_BACKUP_AFTER_HOURS) != 0) {
            registerBackupTask();
        }


        //Register listeners
        Arrays.asList(new EntityDamageListener(), new PlayerJoinListener(), new PlayerDeathListener(),
                new PlayerBlockPlaceListener(), new BlockBreakListener(), new PlayerInteractListener(),
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

    public File getDataFolder(){
        return new File(configDir, "data");
    }

    private void registerBackupTask() {
        File backupFolder = new File(new File(configDir, "data"), "backup");
        File lastBackup = FileUtils.getLastModifiedFileInFolder(backupFolder);
        long nextBackupInTicks;
        if (lastBackup != null) {
            long milisSinceLastBackup = System.currentTimeMillis() - lastBackup.lastModified();
            long milisTillNextBackup = (Config.getInteger(Config.CREATE_BACKUP_AFTER_HOURS) * 3600000) - milisSinceLastBackup;
            if (milisTillNextBackup < 50) {
                milisTillNextBackup = 50;
            }
            nextBackupInTicks = milisTillNextBackup / 50;
        } else {
            nextBackupInTicks = 1;
        }

        long delayBetweenBackupsInTicks = Config.getInteger(Config.CREATE_BACKUP_AFTER_HOURS) * 72000; // hours * 3600 * 20 = ticks

        org.spongepowered.api.scheduler.Task.Builder taskBuilder = Sponge.getScheduler().createTaskBuilder();
        taskBuilder.execute(() -> DatabaseHandler.getInstance().backup());
        taskBuilder.delayTicks(nextBackupInTicks).intervalTicks(delayBetweenBackupsInTicks).submit(this);

        getLogger().info("Registered backup task to run every " + delayBetweenBackupsInTicks / 20 + "s (" + delayBetweenBackupsInTicks / 72000 + "h), starting in " + nextBackupInTicks / 20 + "s", false);

    }

    private void registerDatabasePollingTask() {
        int delayInSeconds = 3600;
        int delayInTicks = delayInSeconds * 20;

        Task.Builder taskBuilder = Sponge.getScheduler().createTaskBuilder();
        taskBuilder.execute(() -> {
            if (!DatabaseConnectionOwner.getInstance().isValid()) {
                DatabaseConnectionOwner.getInstance().setupConnection();
            }
        });
        taskBuilder.delayTicks(delayInTicks).intervalTicks(delayInTicks).submit(this);

        getLogger().info("Registered database polling task to run every " + delayInSeconds + "s (" + delayInSeconds / 3600 + "h)", false);
    }

    @Listener
    public void onServerStop(GameStoppingServerEvent event) {
        if (loadError) {
            if (Config.getBoolean(Config.USE_DATABASE)) {
                TaskExecutor.getInstance().terminate();
                DatabaseConnectionOwner.getInstance().close();
            }
            return;
        }

        if (Config.getBoolean(Config.USE_DATABASE)) {
            getLogger().info("Database updater shutting down...", false);
            TaskExecutor.getInstance().terminate();

            getLogger().info("Database updater successfully shut down", false);
            getLogger().info("Starting save to database...", true);
            long databaseSaveStartTime = System.currentTimeMillis();
            if (DatabaseHandler.getInstance().save()) {
                getLogger().info("Successfully saved to database in: " + (System.currentTimeMillis() - databaseSaveStartTime) + "ms", true);
            } else {
                getLogger().error("Error saving database!", true);
            }
            DatabaseConnectionOwner.getInstance().close();
        } else {
            getLogger().info("Starting save to flat file..", true);
            long databaseSaveStartTime = System.currentTimeMillis();
            if (DatabaseHandler.getInstance().save()) {
                getLogger().info("Successfully saved to flat file in: " + (System.currentTimeMillis() - databaseSaveStartTime) + "ms", true);
            } else {
                getLogger().error("Error saving flat file!", true);
            }
        }
    }


    // Create configs
    private void SetupConfigs() {
        configuration = new Configuration(configDir);

        FactionLogic.setup(configDir.toPath());
        PlayerManager.setup(configDir.toPath());
        PowerManager.setup(configDir.toPath());

        MessageLoader messageLoader = new MessageLoader(configDir.toPath());
    }


    public Configuration getConfiguration() {
        return this.configuration;
    }

    public PVPLogger getPVPLogger() {
        return this.pvpLogger;
    }
}
