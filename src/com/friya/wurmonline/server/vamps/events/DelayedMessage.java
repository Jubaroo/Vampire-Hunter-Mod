// 
// Decompiled by Procyon v0.5.30
// 

package com.friya.wurmonline.server.vamps.events;

import com.wurmonline.server.creatures.Creature;

import java.util.logging.Level;
import java.util.logging.Logger;

public class DelayedMessage extends EventOnce
{
    private static Logger logger;
    private Creature creature;
    private String message;
    
    static {
        DelayedMessage.logger = Logger.getLogger(DelayedMessage.class.getName());
    }
    
    public DelayedMessage(final int fromNow, final Unit unit, final Creature c, final String msg) {
        super(fromNow, unit);
        this.creature = c;
        this.message = msg;
        DelayedMessage.logger.log(Level.INFO, "DelayedMessage created");
    }
    
    @Override
    public boolean invoke() {
        if (this.creature == null || this.creature.isOffline()) {
            return true;
        }
        this.creature.getCommunicator().sendAlertServerMessage(this.message, (byte)4);
        return true;
    }
}
