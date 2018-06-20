package io.github.aquerr.eaglefactions.entities;

import java.util.ArrayList;
import java.util.List;

public class Group {

    public final String name;
    public String prefix;
    public PermObject perms;
    public int priority = 100;

    public Group(String name){
       this(name, 15, new String[]{});
    }

    public Group(String name, int priority, String... basicPerms){
        this.name = name;
        prefix = name;
        perms = new PermObject();
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

    public Group clone(){
        return new Group(name, prefix, priority, (List<String>) ((ArrayList) perms.inherit).clone(), (List<String>) ((ArrayList) perms.nodes).clone());
    }

}