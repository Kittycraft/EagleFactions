package io.github.aquerr.eaglefactions.listeners;

import org.spongepowered.api.event.Order;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class EventPriorityParser {

    private static final Map<String, Order> orderMap;

    static {
        orderMap = new HashMap<>();
        orderMap.put("LOWEST", Order.LAST);
        orderMap.put("LOW", Order.LATE);
        orderMap.put("NORMAL", Order.DEFAULT);
        orderMap.put("HIGH", Order.EARLY);
        orderMap.put("HIGHEST", Order.FIRST);
        orderMap.put("MONITOR", Order.DEFAULT);
        orderMap.put("DEFAULT", Order.DEFAULT);
        orderMap.put("FIRST", Order.FIRST);
        orderMap.put("EARLY", Order.EARLY);
        orderMap.put("LATE", Order.LATE);
        orderMap.put("AFTER_PRE", Order.AFTER_PRE);
        orderMap.put("BEFORE_POST", Order.BEFORE_POST);
        orderMap.put("LAST", Order.LAST);
        orderMap.put("POST", Order.POST);
        orderMap.put("PRE", Order.PRE);
    }

    public static Order getOrder(String in){
        return orderMap.getOrDefault(in.toUpperCase(), Order.DEFAULT);
    }
}
