package io.github.aquerr.eaglefactions.listeners;

import com.google.inject.Inject;
import com.google.inject.Injector;
import io.github.aquerr.eaglefactions.EagleFactions;
import io.github.aquerr.eaglefactions.caching.FactionsCache;
import io.github.aquerr.eaglefactions.config.ConfigV2;
import io.github.aquerr.eaglefactions.config.Settings;
import org.reflections.Reflections;
import org.spongepowered.api.Sponge;

import java.util.Set;

public abstract class GenericListener {

    protected FactionsCache cache;
    protected ConfigV2 settings;

    @Inject
    GenericListener(FactionsCache cache, ConfigV2 settings, EagleFactions eagleFactions) {
        this.cache = cache;
        this.settings = settings;
        Sponge.getEventManager().registerListeners(eagleFactions, this);
    }

    @Inject
    protected GenericListener(FactionsCache cache, ConfigV2 settings){
        this.cache = cache;
        this.settings = settings;
    }

    public static void initListeners(Injector injector) {
        Reflections reflections = new Reflections("io.github.aquerr.eaglefactions");
        Set<Class<? extends GenericListener>> subTypes = reflections.getSubTypesOf(GenericListener.class);
        for (Class<?> listener : subTypes) {
            injector.getInstance(listener);
        }
    }
}
