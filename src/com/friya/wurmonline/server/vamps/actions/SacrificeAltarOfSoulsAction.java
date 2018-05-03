// 
// Decompiled by Procyon v0.5.30
// 

package com.friya.wurmonline.server.vamps.actions;

import com.friya.wurmonline.server.vamps.Vampires;
import com.friya.wurmonline.server.vamps.items.AltarOfSouls;
import com.wurmonline.server.Items;
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

public class SacrificeAltarOfSoulsAction implements ModAction
{
    private static Logger logger;
    private final short actionId;
    private final ActionEntry actionEntry;
    private final int castTime = 40;
    
    static {
        SacrificeAltarOfSoulsAction.logger = Logger.getLogger(SacrificeAltarOfSoulsAction.class.getName());
    }
    
    public SacrificeAltarOfSoulsAction() {
        SacrificeAltarOfSoulsAction.logger.log(Level.INFO, "AltarOfSoulsSacrifice()");
        this.actionId = (short)ModActions.getNextActionId();
        ModActions.registerAction(this.actionEntry = ActionEntry.createEntry(this.actionId, "Sacrifice", "sacrificing", new int[] { 6, 23 }));
    }
    
    public BehaviourProvider getBehaviourProvider() {
        return new BehaviourProvider() {
            public List<ActionEntry> getBehavioursFor(final Creature performer, final Item target) {
                return this.getBehavioursFor(performer, null, target);
            }
            
            public List<ActionEntry> getBehavioursFor(final Creature performer, final Item subject, final Item target) {
                if (target.getTemplateId() == AltarOfSouls.getId() && Vampires.isVampire(performer.getWurmId())) {
                    return Arrays.asList(SacrificeAltarOfSoulsAction.this.actionEntry);
                }
                return null;
            }
        };
    }
    
    public ActionPerformer getActionPerformer() {
        return new ActionPerformer() {
            public short getActionId() {
                return SacrificeAltarOfSoulsAction.this.actionId;
            }
            
            public boolean action(final Action act, final Creature performer, final Creature object, final short action, final float counter) {
                return this.action(act, performer, null, object, action, counter);
            }
            
            public boolean action(final Action act, final Creature performer, final Item item, final Item target, final short action, final float counter) {
                if (target.getTemplateId() != AltarOfSouls.getId()) {
                    return true;
                }
                if (item == null || item.getTemplateId() != 272) {
                    performer.getCommunicator().sendNormalServerMessage("You can only sacrifice corpses.");
                    return true;
                }
                if (!Vampires.isVampire(performer.getWurmId())) {
                    return true;
                }
                if (!DevourAction.isDevourableCorpse(item)) {
                    performer.getCommunicator().sendNormalServerMessage("Not enough nourishment in that.");
                    return true;
                }
                if (!AltarOfSouls.isCleanArea(target)) {
                    performer.getCommunicator().sendNormalServerMessage("The area around the altar is too cluttered.");
                    return true;
                }
                try {
                    if (counter == 1.0f) {
                        performer.getCurrentAction().setTimeLeft(40);
                        performer.sendActionControl("Sacrificing", true, 40);
                        return false;
                    }
                    if (counter * 10.0f <= act.getTimeLeft()) {
                        return false;
                    }
                }
                catch (NoSuchActionException e) {
                    return true;
                }
                performer.getCommunicator().sendNormalServerMessage("You sacrifice " + item.getName() + " at the Altar of Souls.");
                Items.destroyItem(item.getWurmId());
                AltarOfSouls.setCharge(target, (byte)(AltarOfSouls.getCharge(target) + 21));
                return true;
            }
        };
    }
}
