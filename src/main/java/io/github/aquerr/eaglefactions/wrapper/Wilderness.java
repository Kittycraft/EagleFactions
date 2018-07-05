package io.github.aquerr.eaglefactions.wrapper;

import io.github.aquerr.eaglefactions.caching.FactionsCache;
import io.github.aquerr.eaglefactions.entities.Faction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class Wilderness extends Faction {

    private static Faction self;
    private static final byte[] uuidBase = new byte[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

    private Wilderness() {
        super("Wilderness", "", new ArrayList<>(), new ArrayList<>(), null, new HashMap<>(), 0, UUID.nameUUIDFromBytes(uuidBase));
        self = this;
        FactionsCache.getInstance().addFaction(this);
    }

    public static Faction get(){
        return self == null ? new Wilderness() : self;
    }
}
