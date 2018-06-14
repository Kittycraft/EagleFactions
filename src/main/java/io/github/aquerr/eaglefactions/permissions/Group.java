package io.github.aquerr.eaglefactions.permissions;

import java.util.List;

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

    public Group(String name, int priority, String... basicPerms){
        this(name);
        this.priority = priority;
        for(String s : basicPerms){
            perms.addNode(s);
        }
    }

    public Group(String name, String prefix, int priority, List<String> inherit, List<String> nodes){
        this.name = name;
        this.prefix = prefix;
        this.priority = priority;
        this.perms = new PermObject(inherit, nodes);
    }

}
