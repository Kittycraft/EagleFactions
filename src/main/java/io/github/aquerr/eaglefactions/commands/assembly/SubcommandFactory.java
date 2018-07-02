package io.github.aquerr.eaglefactions.commands.assembly;

import com.google.inject.*;
import com.google.inject.name.Named;
import io.github.aquerr.eaglefactions.commands.annotations.Subcommand;
import org.reflections.Reflections;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.text.Text;

import java.util.*;

@Singleton
public class SubcommandFactory extends AbstractModule {
    private Map<List<String>, CommandSpec> subcommands = new HashMap<>();

    @Inject
    SubcommandFactory(Injector injector) {
        Reflections reflections = new Reflections("io.github.aquerr.eaglefactions");
        Set<Class<?>> subTypes = reflections.getTypesAnnotatedWith(Subcommand.class);
        for (Class<?> listener : subTypes) {
            Subcommand instance = (Subcommand) injector.getInstance(listener);
            CommandSpec.Builder builder = CommandSpec.builder()
                    .description(Text.of(instance.desc()))
                    .permission(instance.permission())
                    .executor((CommandExecutor) instance)
                    .arguments(((FactionCommand) instance).getArguments());
            subcommands.put(Arrays.asList(instance.aliases()), builder.build());
        }
    }

    @Override
    protected void configure() {
    }

    @Provides
    @Named("subcommands")
    Map getSubcommands() {
        return subcommands;
    }
}
