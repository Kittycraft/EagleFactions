package io.github.aquerr.eaglefactions.entities;

import com.flowpowered.math.vector.Vector3i;

import java.util.UUID;

public class FactionClaim {

    public final Vector3i chunk;
    public final UUID world;
    public final String faction;

    public FactionClaim(Vector3i chunk, UUID world, String faction) {
        this.chunk = chunk;
        this.world = world;
        this.faction = faction;
    }

}
