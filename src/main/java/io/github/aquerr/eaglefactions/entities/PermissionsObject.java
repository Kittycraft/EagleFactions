package io.github.aquerr.eaglefactions.entities;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

public class PermissionsObject {

    public transient static final Group empty = new Group("", Integer.MAX_VALUE);

    public List<String> nodes = new ArrayList<>();
    public List<String> parents = new ArrayList<>();

    public PermissionsObject() {
        nodes.add("-r ^(?!-)(?!(f($|( .*)|(action($|( .*)|(s($|( .*)))))))|(build)|(interact)).*");
    }

    public PermissionsObject(List<String> parents, List<String> nodes) {
        this.parents = parents;
        this.nodes = nodes;
    }

    public boolean hasNode(String node, Faction parent) {
        if (containsPersonalNode(node) && !containsPersonalNode("-" + node)) {
            return true;
        }
        for (String groupName : parents) {
            PermissionsObject group = parent.groups.getOrDefault(groupName, empty).perms;
            if (group.containsPersonalNode(node) && !group.containsPersonalNode("-" + node)) {
                return true;
            }
        }
        return false;
    }

    private boolean containsPersonalNode(String node) {
        for (String s : nodes) {
            if (s.startsWith("-r ")) {
                if (node.matches(s.substring(3, s.length()))) {
                    return true;
                }
            } else if (node.matches("^" + s.replaceAll("\\.", "\\.").replaceAll("\\*", ".*") + ".*$")) {
                return true;
            }
        }
        return false;
    }

    public void addGroup(String groupName) {
        if (!parents.contains(groupName)) {
            parents.add(groupName);
        }
    }

    public void removeGroup(String groupName) {
        if (parents.contains(groupName)) {
            parents.remove(groupName);
        }
    }

    public void clearGroups() {
        parents.clear();
    }

    public void addNode(String node) {
        nodes.add(node.replaceFirst("^((f )|(faction )|(factions ))", "((f )|(faction )|(factions ))").toLowerCase());
    }

    public void removeNode(String node) {
        if (parents.contains(node)) {
            parents.remove(node);
        }
    }

    public Collection<String> getInheritedNodes(Faction context) {
        Collection<String> set = new HashSet<>();
        parents.forEach(e -> context.groups.get(e).perms.addAllNodesToSet(set, context));
        return set;
    }

    private void addAllNodesToSet(Collection<String> set, Faction context) {
        set.addAll(nodes);
        parents.forEach(e -> context.groups.get(e).perms.addAllNodesToSet(set, context));
    }

}
