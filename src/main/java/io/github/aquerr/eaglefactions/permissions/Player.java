package io.github.aquerr.eaglefactions.permissions;

public class Player extends PermObject {

    public final String name;

    public Player(String name){
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
