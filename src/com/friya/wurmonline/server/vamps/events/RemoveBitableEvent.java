// 
// Decompiled by Procyon v0.5.30
// 

package com.friya.wurmonline.server.vamps.events;

import com.friya.wurmonline.server.vamps.Stakers;
import com.friya.wurmonline.server.vamps.Vampires;
import com.wurmonline.server.Players;
import com.wurmonline.server.players.Player;

import java.util.logging.Level;
import java.util.logging.Logger;

public class RemoveBitableEvent extends EventOnce
{
    private static Logger logger;
    private long wurmId;
    private boolean giveMessage;
    
    static {
        RemoveBitableEvent.logger = Logger.getLogger(RemoveBitableEvent.class.getName());
    }
    
    public RemoveBitableEvent(final int fromNow, final Unit unit, final long wurmId) {
        super(fromNow, unit);
        this.giveMessage = false;
        this.wurmId = wurmId;
        RemoveBitableEvent.logger.log(Level.INFO, "RemoveBitableEvent created");
    }
    
    public RemoveBitableEvent(final int fromNow, final Unit unit, final long wurmId, final boolean giveMessage) {
        this(fromNow, unit, wurmId);
        this.giveMessage = giveMessage;
    }
    
    @Override
    public boolean invoke() {
        Stakers.removeBitable(this.wurmId);
        if (this.giveMessage && !Stakers.isHunted(this.wurmId)) {
            final Player p = Players.getInstance().getPlayerOrNull(this.wurmId);
            if (p != null) {
                Vampires.broadcast(String.valueOf(p.getName()) + " is no longer marked as a vampire slayer. The time of the hunt will now cease!", true, true, false);
            }
        }
        return true;
    }
}
