package io.github.aquerr.eaglefactions.entities;

import java.util.UUID;

public class FactionRelation implements Cloneable {

    public final UUID factionA, factionB;
    public final RelationType type;

    public FactionRelation(UUID factionA, UUID factionB, RelationType type) {
        this.factionA = factionA;
        this.factionB = factionB;
        this.type = type;
    }

    public FactionRelation(String data) throws Exception {
        String[] components = data.split(",");
        if (components.length != 3) {
            throw new Exception();
        }
        factionA = UUID.fromString(components[0]);
        factionB = UUID.fromString(components[1]);
        type = RelationType.parseType(components[2]);
    }

    @Override
    public String toString() {
        return factionA + "," + factionB + "," + type.toString();
    }
}
