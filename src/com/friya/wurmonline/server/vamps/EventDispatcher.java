// 
// Decompiled by Procyon v0.5.30
// 

package com.friya.wurmonline.server.vamps;

import com.friya.wurmonline.server.vamps.events.EventOnce;

import java.util.ArrayList;

public class EventDispatcher
{
    private static ArrayList<EventOnce> events;
    
    static {
        EventDispatcher.events = new ArrayList<EventOnce>();
    }
    
    public static void add(final EventOnce event) {
        EventDispatcher.events.add(event);
    }
    
    public static void poll() {
        if (EventDispatcher.events.size() == 0) {
            return;
        }
        final ArrayList<EventOnce> found = new ArrayList<EventOnce>();
        final long ts = System.currentTimeMillis();
        for (final EventOnce event : EventDispatcher.events) {
            if (event.getInvokeAt() < ts && event.invoke()) {
                found.add(event);
            }
        }
        if (found.size() > 0) {
            EventDispatcher.events.removeAll(found);
        }
    }
}
