package io.github.aquerr.eaglefactions.permissions;

public class Group {

    public final String name;
    public String prefix;
    public PermObject perms;
    public int priority = 100;

    public Group(String name){
        this.name = name;
        prefix = name;
        perms = new PermObject();
    }

    public Group(String name, int priority){
        this(name);
        this.priority = priority;
    }

}
