package io.github.aquerr.eaglefactions.permissions;

import io.github.aquerr.eaglefactions.entities.Faction;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class PermObject {

    public transient static final Group empty = new Group("", Integer.MAX_VALUE);

    public List<String> nodes = new ArrayList<>();
    public List<String> inherit = new LinkedList<>();

    public PermObject(){
        nodes.add("-r ^(?!-)(?!(f($|( .*)|(action($|( .*)|(s($|( .*)))))))|(build)|(interact)).*");
    }

    PermObject(List<String> inherit, List<String> nodes){
        this.inherit = inherit;
        this.nodes = nodes;
    }

    public boolean hasNode(String node, Faction parent) {
        if(containsPersonalNode(node) && !containsPersonalNode("-" + node)){
            return true;
        }
        for(String groupName : inherit) {
            PermObject group = parent.groups.getOrDefault(groupName, empty).perms;
            if (group.containsPersonalNode(node) && !group.containsPersonalNode("-" + node)) {
                return true;
            }
        }
        return false;
    }

    private boolean containsPersonalNode(String node) {
        for (String s : nodes) {
            if(s.startsWith("-r ")){
                if(node.matches(s.substring(3, s.length()))){
                    return true;
                }
            }else if (node.matches("^" + s.replaceAll("\\.", "\\.").replaceAll("\\*", ".*") + ".*$")) {
                return true;
            }
        }
        return false;
    }

    public void addGroup(String groupName){
        if(!inherit.contains(groupName)) {
            inherit.add(groupName);
        }
    }

    public void removeGroup(String groupName){
        if(inherit.contains(groupName)) {
            inherit.remove(groupName);
        }
    }

    public void clearGroups(){
        inherit.clear();
    }

    public void addNode(String node){
        nodes.add(node.replaceFirst("^((f )|(faction )|(factions ))", "((f )|(faction )|(factions ))").toLowerCase());
    }

    public void removeNode(String node){
        if(inherit.contains(node)) {
            inherit.remove(node);
        }
    }

}
