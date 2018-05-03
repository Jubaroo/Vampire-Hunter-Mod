// 
// Decompiled by Procyon v0.5.30
// 

package com.friya.wurmonline.server.vamps.actions;

import com.friya.wurmonline.server.vamps.Mod;
import com.friya.wurmonline.server.vamps.items.Amulet;
import com.friya.wurmonline.server.vamps.items.Stake;
import com.wurmonline.server.Items;
import com.wurmonline.server.NoSuchItemException;
import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.behaviours.Action;
import com.wurmonline.server.behaviours.ActionEntry;
import com.wurmonline.server.behaviours.NoSuchActionException;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.NoSuchCreatureException;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.zones.NoSuchZoneException;
import com.wurmonline.shared.exceptions.WurmServerException;
import org.gotti.wurmunlimited.modsupport.actions.ActionPerformer;
import org.gotti.wurmunlimited.modsupport.actions.BehaviourProvider;
import org.gotti.wurmunlimited.modsupport.actions.ModAction;
import org.gotti.wurmunlimited.modsupport.actions.ModActions;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MakeSeryllStakeAction implements ModAction
{
    private static Logger logger;
    private final short actionId;
    private final ActionEntry actionEntry;
    private final int castTime = 300;
    
    static {
        MakeSeryllStakeAction.logger = Logger.getLogger(MakeSeryllStakeAction.class.getName());
    }
    
    public MakeSeryllStakeAction() {
        MakeSeryllStakeAction.logger.log(Level.INFO, "MakeSeryllStakeAction()");
        this.actionId = (short)ModActions.getNextActionId();
        ModActions.registerAction(this.actionEntry = ActionEntry.createEntry(this.actionId, "Merge with Amulet", "merging", new int[] { 6 }));
    }
    
    public BehaviourProvider getBehaviourProvider() {
        return new BehaviourProvider() {
            public List<ActionEntry> getBehavioursFor(final Creature performer, final Item amulet, final Item stake) {
                if (amulet != null && stake != null && amulet.getTemplateId() == Amulet.getId() && stake.getTemplateId() == Stake.getId()) {
                    return Arrays.asList(MakeSeryllStakeAction.this.actionEntry);
                }
                return null;
            }
            
            public List<ActionEntry> getBehavioursFor(final Creature performer, final Item object) {
                return null;
            }
        };
    }
    
    public ActionPerformer getActionPerformer() {
        return new ActionPerformer() {
            public short getActionId() {
                return MakeSeryllStakeAction.this.actionId;
            }
            
            public boolean action(final Action act, final Creature performer, final Item target, final short action, final float counter) {
                return true;
            }
            
            public boolean action(final Action act, final Creature performer, final Item source, final Item target, final short action, final float counter) {
                return MakeSeryllStakeAction.this.makeSeryllStake(act, performer, source, target, counter);
            }
        };
    }
    
    private boolean makeSeryllStake(final Action act, final Creature performer, final Item amulet, final Item stake, final float counter) {
        if (!performer.isPlayer() || stake == null || amulet == null || stake.getTemplateId() != Stake.getId() || amulet.getTemplateId() != Amulet.getId()) {
            return true;
        }
        try {
            if (counter == 1.0f) {
                final int tmpTime = 300;
                performer.getCurrentAction().setTimeLeft(tmpTime);
                performer.sendActionControl("merging", true, tmpTime);
                return false;
            }
            if (counter * 10.0f <= act.getTimeLeft()) {
                return false;
            }
        }
        catch (NoSuchActionException e2) {
            return true;
        }
        Mod.actionNotify(performer, "In a puff of smoke you merge the amulet with the stake, making it absolutely amazing.", "%NAME fiddles with an ancient amulet and a stake of vampire banishment.", "A shadow fiddles with an ancient amulet and a stake of vampire banishment.");
        stake.setWeight(amulet.getWeightGrams(), true);
        Items.destroyItem(amulet.getWurmId());
        stake.setMaterial((byte)67);
        stake.updateName();
        try {
            performer.getCommunicator().sendAlertServerMessage("Whoops! In the excitement you accidentally drop it on the ground!", (byte)4);
            stake.putItemInfrontof(performer);
        }
        catch (NoSuchCreatureException | NoSuchItemException | NoSuchPlayerException | NoSuchZoneException ex2) {
            final WurmServerException ex;
            final WurmServerException e = ex;
            e.printStackTrace();
        }
        return true;
    }
}
