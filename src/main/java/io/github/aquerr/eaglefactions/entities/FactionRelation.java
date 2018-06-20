package io.github.aquerr.eaglefactions.entities;

public class FactionRelation implements Cloneable {

    public final String factionA, factionB;
    public final RelationType type;

    public FactionRelation(String factionA, String factionB, RelationType type){
        this.factionA = factionA;
        this.factionB = factionB;
        this.type = type;
    }

    public FactionRelation(String data) throws Exception {
        String[] components = data.split(",");
        if(components.length != 3){
            throw new Exception();
        }
        factionA = components[0];
        factionB = components[1];
        type = RelationType.parseType(components[2]);
    }

    @Override
    public String toString() {
        return factionA + "," + factionB + "," + type.toString();
    }
}
