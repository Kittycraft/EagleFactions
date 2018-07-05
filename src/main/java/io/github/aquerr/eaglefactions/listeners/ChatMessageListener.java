package io.github.aquerr.eaglefactions.listeners;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.github.aquerr.eaglefactions.EagleFactions;
import io.github.aquerr.eaglefactions.caching.FactionsCache;
import io.github.aquerr.eaglefactions.config.Settings;
import io.github.aquerr.eaglefactions.entities.ChatEnum;
import io.github.aquerr.eaglefactions.entities.Faction;
import io.github.aquerr.eaglefactions.entities.RelationType;
import io.github.aquerr.eaglefactions.logic.FactionLogic;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.message.MessageChannelEvent;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.text.channel.MessageReceiver;
import org.spongepowered.api.text.format.TextColors;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Singleton
public class ChatMessageListener extends GenericListener {

    private FactionLogic factionLogic;

    @Inject
    public ChatMessageListener(FactionsCache cache, Settings settings, EagleFactions eagleFactions, FactionLogic factionLogic) {
        super(cache, settings, eagleFactions);
        this.factionLogic = factionLogic;
    }

    @Listener
    public void onChatMessage(MessageChannelEvent.Chat event, @Root Player player) {

    }
}
