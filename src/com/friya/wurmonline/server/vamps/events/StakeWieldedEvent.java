// 
// Decompiled by Procyon v0.5.30
// 

package com.friya.wurmonline.server.vamps.events;

import com.friya.wurmonline.server.vamps.Mod;
import com.friya.wurmonline.server.vamps.Vampires;
import com.friya.wurmonline.server.vamps.items.Stake;
import com.wurmonline.server.NoSuchItemException;
import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.NoSuchCreatureException;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.zones.NoSuchZoneException;
import com.wurmonline.shared.exceptions.WurmServerException;

import java.util.logging.Level;
import java.util.logging.Logger;

public class StakeWieldedEvent extends EventOnce
{
    private static Logger logger;
    private Item stake;
    private Creature wielder;
    
    static {
        StakeWieldedEvent.logger = Logger.getLogger(StakeWieldedEvent.class.getName());
    }
    
    public StakeWieldedEvent(final int fromNow, final Unit unit, final Creature wielder, final Item stake) {
        super(fromNow, unit);
        this.stake = stake;
        this.wielder = wielder;
        StakeWieldedEvent.logger.log(Level.INFO, "Stake auxdata BEFORE: " + stake.getAuxData());
        stake.setAuxData(Stake.STATUS_WIELDING);
        Mod.actionNotify(wielder, "You wield the stake of Vampire Banishment, you must let it settle for a few seconds before you can use it. You can get rid of the stake by using it on a vampire or tossing it into a trash heap. BEWARE: If a vampire sees you with this wielded, they will likely punish you!", "%NAME started wielding a stake of Vampire Banishment!", "You hear the runes of a stake of Vampire Banishment flare up.");
        StakeWieldedEvent.logger.log(Level.INFO, "StakeWieldedEvent created");
    }
    
    @Override
    public boolean invoke() {
        if (this.stake.getAuxData() == Stake.STATUS_WIELDING) {
            this.stake.setAuxData(Stake.STATUS_READY);
            if (this.wielder != null && Vampires.isHalfOrFullVampire(this.wielder.getWurmId())) {
                Mod.actionNotify(this.wielder, "The magical runes of the stake prevent you from holding on to it longer! You are a vampire!", "%NAME's stake of Vampire Banishment drops to the ground.", "A stake of Vampire Banishment drops to the ground.");
                try {
                    this.stake.putItemInfrontof(this.wielder);
                }
                catch (NoSuchCreatureException | NoSuchItemException | NoSuchPlayerException | NoSuchZoneException ex2) {
                    final WurmServerException ex;
                    final WurmServerException e = ex;
                    StakeWieldedEvent.logger.log(Level.SEVERE, "Failed to move stake out of a vampires hands", e);
                }
            }
            else {
                Mod.actionNotify(this.wielder, "The magical runes of the stake of Vampire Banishment settle. You can now use it!", "%NAME's stake of Vampire Banishment has settled.", "You hear the runes of a stake of Vampire Banishment settle.");
            }
        }
        return true;
    }
}
