package io.github.aquerr.eaglefactions.entities;

import com.flowpowered.math.vector.Vector3i;

import java.util.UUID;


//Basically a Pojo already, so until any changes are made it will take both functions.
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
