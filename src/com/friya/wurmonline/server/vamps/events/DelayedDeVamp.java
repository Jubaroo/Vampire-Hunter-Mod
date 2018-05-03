// 
// Decompiled by Procyon v0.5.30
// 

package com.friya.wurmonline.server.vamps.events;

import com.friya.wurmonline.server.vamps.Vampires;
import com.wurmonline.server.creatures.Creature;

import java.util.logging.Level;
import java.util.logging.Logger;

public class DelayedDeVamp extends EventOnce
{
    private static Logger logger;
    private Creature creature;
    private String message;
    
    static {
        DelayedDeVamp.logger = Logger.getLogger(DelayedDeVamp.class.getName());
    }
    
    public DelayedDeVamp(final int fromNow, final Unit unit, final Creature c, final String msg) {
        super(fromNow, unit);
        this.creature = c;
        this.message = msg;
        DelayedDeVamp.logger.log(Level.INFO, "DelayedDeVamp created");
    }
    
    @Override
    public boolean invoke() {
        if (this.creature == null || this.creature.isOffline()) {
            return true;
        }
        this.creature.getCommunicator().sendAlertServerMessage(this.message, (byte)4);
        Vampires.deVamp(this.creature);
        if (this.creature.getPower() < 1) {
            this.creature.die(false);
        }
        else {
            this.creature.getCommunicator().sendAlertServerMessage("You are an admin, sparing you from death...", (byte)4);
        }
        return true;
    }
}
