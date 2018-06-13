package io.github.aquerr.eaglefactions.commands;

import io.github.aquerr.eaglefactions.EagleFactions;
import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.logic.FactionLogic;
import io.github.aquerr.eaglefactions.logic.PluginMessages;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.text.Text;

public class ReloadCommand implements CommandExecutor {
    @Override
    public CommandResult execute(CommandSource source, CommandContext context) throws CommandException {
        try {
            EagleFactions.getEagleFactions().getConfiguration().load();
            FactionLogic.reload();

            source.sendMessage(Text.of(PluginInfo.PluginPrefix, PluginMessages.CONFIGS_HAS_BEEN_RELOADED));
        } catch (Exception exception) {
            exception.printStackTrace();
        }

        return CommandResult.success();
    }
}
