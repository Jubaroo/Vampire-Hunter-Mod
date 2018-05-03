// 
// Decompiled by Procyon v0.5.30
// 

package com.friya.wurmonline.server.vamps.events;

import com.friya.wurmonline.server.vamps.Mod;
import com.friya.wurmonline.server.vamps.items.Stake;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;

import java.util.logging.Level;
import java.util.logging.Logger;

public class StakeRecoverEvent extends EventOnce
{
    private static Logger logger;
    private Item stake;
    private Creature wielder;
    
    static {
        StakeRecoverEvent.logger = Logger.getLogger(StakeRecoverEvent.class.getName());
    }
    
    public StakeRecoverEvent(final int fromNow, final Unit unit, final Creature wielder, final Item stake) {
        super(fromNow, unit);
        this.stake = stake;
        this.wielder = wielder;
        stake.setAuxData(Stake.STATUS_RECOVERING);
        StakeRecoverEvent.logger.log(Level.FINE, "StakeRecoverEvent created");
    }
    
    @Override
    public boolean invoke() {
        if (this.stake.getAuxData() == Stake.STATUS_RECOVERING) {
            this.stake.setAuxData(Stake.STATUS_READY);
            Mod.actionNotify(this.wielder, "You have fully recovered from your recent staking attempt.", null, null);
        }
        return true;
    }
}
