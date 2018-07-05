package io.github.aquerr.eaglefactions.wrapper;

import io.github.aquerr.eaglefactions.caching.FactionsCache;
import io.github.aquerr.eaglefactions.entities.*;

import java.util.*;

public class SafeZone extends Faction {

    private static Faction self;
    private static final byte[] uuidBase = new byte[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1};

    private SafeZone() {
        super("SafeZone", "", new ArrayList<>(), new ArrayList<>(), null, new HashMap<>(), 0, UUID.nameUUIDFromBytes(uuidBase));
        self = this;
        FactionsCache.getInstance().addFaction(this);
    }

    public static Faction get(){
        return self == null ? new SafeZone() : self;
    }
}
