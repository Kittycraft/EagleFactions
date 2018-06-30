package io.github.aquerr.eaglefactions.entities;

import java.util.ArrayList;
import java.util.List;

public class Group {

    public final String name;
    public PermissionsObject perms;
    public int priority = 100;

    public Group(String name){
       this(name, 15, new String[]{});
    }

    public Group(String name, int priority, String... basicPerms){
        this.name = name;
        perms = new PermissionsObject();
        this.priority = priority;
        for(String s : basicPerms){
            perms.addNode(s);
        }
    }

    public Group(String name, int priority, List<String> inherit, List<String> nodes){
        this.name = name;
        this.priority = priority;
        this.perms = new PermissionsObject(inherit, nodes);
    }

    public Group clone(){
        return new Group(name, priority, (List<String>) ((ArrayList) perms.parents).clone(), (List<String>) ((ArrayList) perms.nodes).clone());
    }

}
