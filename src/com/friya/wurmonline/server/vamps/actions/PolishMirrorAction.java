// 
// Decompiled by Procyon v0.5.30
// 

package com.friya.wurmonline.server.vamps.actions;

import com.friya.wurmonline.server.vamps.Mod;
import com.friya.wurmonline.server.vamps.items.Mirror;
import com.wurmonline.server.Items;
import com.wurmonline.server.Server;
import com.wurmonline.server.behaviours.Action;
import com.wurmonline.server.behaviours.ActionEntry;
import com.wurmonline.server.behaviours.NoSuchActionException;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import org.gotti.wurmunlimited.modsupport.actions.ActionPerformer;
import org.gotti.wurmunlimited.modsupport.actions.BehaviourProvider;
import org.gotti.wurmunlimited.modsupport.actions.ModAction;
import org.gotti.wurmunlimited.modsupport.actions.ModActions;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PolishMirrorAction implements ModAction
{
    private static Logger logger;
    private final short actionId;
    private final ActionEntry actionEntry;
    private final int castTime = 100;
    
    static {
        PolishMirrorAction.logger = Logger.getLogger(PolishMirrorAction.class.getName());
    }
    
    public PolishMirrorAction() {
        PolishMirrorAction.logger.log(Level.INFO, "PolishMirrorAction()");
        this.actionId = (short)ModActions.getNextActionId();
        ModActions.registerAction(this.actionEntry = ActionEntry.createEntry(this.actionId, "Polish mirror", "polishing", new int[] { 6 }));
    }
    
    public BehaviourProvider getBehaviourProvider() {
        return new BehaviourProvider() {
            public List<ActionEntry> getBehavioursFor(final Creature performer, final Item pelt, final Item mirror) {
                if (pelt != null && mirror != null && pelt.getTemplateId() == 313 && mirror.getTemplateId() == Mirror.getId()) {
                    return Arrays.asList(PolishMirrorAction.this.actionEntry);
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
                return PolishMirrorAction.this.actionId;
            }
            
            public boolean action(final Action act, final Creature performer, final Item target, final short action, final float counter) {
                return true;
            }
            
            public boolean action(final Action act, final Creature performer, final Item source, final Item target, final short action, final float counter) {
                return PolishMirrorAction.this.polishMirror(act, performer, source, target, counter);
            }
        };
    }
    
    private boolean polishMirror(final Action act, final Creature performer, final Item pelt, final Item mirror, final float counter) {
        if (!performer.isPlayer() || mirror == null || pelt == null || mirror.getTemplateId() != Mirror.getId() || pelt.getTemplateId() != 313) {
            return true;
        }
        if (mirror.getAuxData() == 0) {
            performer.getCommunicator().sendNormalServerMessage("The mirror is already as clean as it can be.");
            return true;
        }
        if (pelt.getCurrentQualityLevel() < 75.0f) {
            performer.getCommunicator().sendNormalServerMessage("That pelt is simply too low quality to clean this mess.");
            return true;
        }
        try {
            if (counter == 1.0f) {
                final int tmpTime = 100;
                performer.getCurrentAction().setTimeLeft(tmpTime);
                performer.sendActionControl("polishing", true, tmpTime);
                return false;
            }
            if (counter * 10.0f <= act.getTimeLeft()) {
                return false;
            }
        }
        catch (NoSuchActionException e) {
            return true;
        }
        Mod.actionNotify(performer, "You polish the mirror to perfection. It can now be used again.", "%NAME looks proudly into a shiny silver mirror.", "In the corner of your eye you see a shiny reflection of something.");
        if (Server.rand.nextInt(100) < 80) {
            performer.getCommunicator().sendNormalServerMessage("The pelt disappears in a puff of smoke.");
            Items.destroyItem(pelt.getWurmId());
        }
        mirror.setAuxData((byte)0);
        return true;
    }
}
