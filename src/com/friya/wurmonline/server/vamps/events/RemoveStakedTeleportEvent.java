// 
// Decompiled by Procyon v0.5.30
// 

package com.friya.wurmonline.server.vamps.events;

import com.friya.wurmonline.server.vamps.Vampires;

public class RemoveStakedTeleportEvent extends EventOnce
{
    public RemoveStakedTeleportEvent(final int fromNow, final Unit unit) {
        super(fromNow, unit);
    }
    
    @Override
    public boolean invoke() {
        Vampires.clearStakedTeleportPosition();
        return true;
    }
}
