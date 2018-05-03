// 
// Decompiled by Procyon v0.5.30
// 

package com.friya.wurmonline.server.vamps.events;

import com.wurmonline.server.Players;

import java.util.logging.Level;
import java.util.logging.Logger;

public class RemoveEffectEvent extends EventOnce
{
    private static Logger logger;
    private int effectId;
    
    static {
        RemoveEffectEvent.logger = Logger.getLogger(RemoveEffectEvent.class.getName());
    }
    
    public RemoveEffectEvent(final int fromNow, final Unit unit, final int effectId) {
        super(fromNow, unit);
        this.effectId = effectId;
        RemoveEffectEvent.logger.log(Level.INFO, "RemoveEffectEvent created");
    }
    
    @Override
    public boolean invoke() {
        Players.getInstance().removeGlobalEffect((long)this.effectId);
        return true;
    }
}
