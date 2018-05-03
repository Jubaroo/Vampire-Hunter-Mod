// 
// Decompiled by Procyon v0.5.30
// 

package com.friya.wurmonline.server.vamps.events;

import com.friya.wurmonline.server.vamps.Mod;
import com.friya.wurmonline.server.vamps.Vampire;
import com.friya.wurmonline.server.vamps.Vampires;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.players.Player;

public class DelayedVamp extends EventOnce
{
    private Creature creature;
    private String message;
    
    public DelayedVamp(final int fromNow, final Unit unit, final Creature c, final String msg) {
        super(fromNow, unit);
        this.creature = c;
        this.message = msg;
    }
    
    @Override
    public boolean invoke() {
        if (this.creature == null || this.creature.isOffline()) {
            return true;
        }
        this.creature.getCommunicator().sendAlertServerMessage(this.message, (byte)4);
        final Vampire v = Vampires.getVampire(this.creature.getWurmId());
        v.convertHalfToFull();
        Mod.loginVampire((Player)this.creature);
        return true;
    }
}
