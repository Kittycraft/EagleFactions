package io.github.aquerr.eaglefactions.permissions;

import io.github.aquerr.eaglefactions.entities.Faction;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class PermObject {

    private transient static final PermObject empty = new PermObject();

    private final List<String> nodes = new ArrayList<>();
    private final List<String> inherit = new LinkedList<>();

    public boolean hasNode(String node, Faction parent) {
        if(containsPersonalNode(node) && !containsPersonalNode("-" + node)){
            return true;
        }
        for(String groupName : inherit) {
            PermObject group = parent.groups.getOrDefault(groupName, empty);
            if (group.containsPersonalNode(node) && !group.containsPersonalNode("-" + node)) {
                return true;
            }
        }
        return false;
    }

    private boolean containsPersonalNode(String node) {
        for (String s : nodes) {
            if (node.matches("^" + s.replaceAll("\\.", "\\.").replaceAll("\\*", "[A-Za-z0-9\\.]*") + "$")) {
                return true;
            }
        }
        return false;
    }


}
