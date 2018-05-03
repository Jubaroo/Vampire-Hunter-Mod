// 
// Decompiled by Procyon v0.5.30
// 

package com.friya.wurmonline.server.vamps.actions;

import com.friya.wurmonline.server.vamps.Cooldowns;
import com.friya.wurmonline.server.vamps.Mod;
import com.friya.wurmonline.server.vamps.Vampires;
import com.friya.wurmonline.server.vamps.items.Mirror;
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

public class MirrorAction implements ModAction
{
    private static Logger logger;
    private final short actionId;
    private final ActionEntry actionEntry;
    private final String effectName = "mirror";
    private final int cooldown = 20000;
    private final int castTime = 30;
    
    static {
        MirrorAction.logger = Logger.getLogger(MirrorAction.class.getName());
    }
    
    public MirrorAction() {
        MirrorAction.logger.log(Level.INFO, "MirrorAction()");
        this.actionId = (short)ModActions.getNextActionId();
        ModActions.registerAction(this.actionEntry = ActionEntry.createEntry(this.actionId, "Check reflection", "mirroring", new int[] { 6, 23 }));
    }
    
    public BehaviourProvider getBehaviourProvider() {
        return new BehaviourProvider() {
            public List<ActionEntry> getBehavioursFor(final Creature performer, final Creature object) {
                return this.getBehavioursFor(performer, null, object);
            }
            
            public List<ActionEntry> getBehavioursFor(final Creature performer, final Item subject, final Creature target) {
                if (subject != null && performer.isPlayer() && target.isPlayer() && subject.getTemplateId() == Mirror.getId()) {
                    return Arrays.asList(MirrorAction.this.actionEntry);
                }
                return null;
            }
        };
    }
    
    public ActionPerformer getActionPerformer() {
        return new ActionPerformer() {
            public short getActionId() {
                return MirrorAction.this.actionId;
            }
            
            public boolean action(final Action act, final Creature performer, final Creature object, final short action, final float counter) {
                return this.action(act, performer, null, object, action, counter);
            }
            
            public boolean action(final Action act, final Creature performer, final Item mirror, final Creature target, final short action, final float counter) {
                if (mirror.getTemplateId() != Mirror.getId()) {
                    return true;
                }
                if (mirror.getAuxData() == 1) {
                    Mod.actionNotify(performer, "The mirror is all smudged from before, you will need a top notch pelt to clean it. NOTE: This might consume the pelt.", "%NAME looks curiously at a silver mirror then frowns.", "In the corner of your eye you see a shiny reflection of something.");
                    return true;
                }
                if (!target.isPlayer() && performer.getPower() <= 1) {
                    return true;
                }
                if (!performer.isWithinTileDistanceTo(target.getTileX(), target.getTileY(), 0, 1)) {
                    performer.getCommunicator().sendNormalServerMessage("That is too far away.");
                    return true;
                }
                if (performer.getWurmId() == target.getWurmId()) {
                    performer.getCommunicator().sendNormalServerMessage("You look fantastic.");
                    return true;
                }
                final String playerEffect = String.valueOf(performer.getName()) + "mirror";
                if (Cooldowns.isOnCooldown(playerEffect, 20000L)) {
                    performer.getCommunicator().sendNormalServerMessage("The silver mirror needs to rest a little while.");
                    return true;
                }
                try {
                    if (counter == 1.0f) {
                        final int tmpTime = 30;
                        performer.getCurrentAction().setTimeLeft(tmpTime);
                        performer.sendActionControl("Checking reflection", true, tmpTime);
                        performer.getCommunicator().sendNormalServerMessage("You angle the mirror to look at " + target.getName() + ".");
                        return false;
                    }
                    if (counter * 10.0f <= act.getTimeLeft()) {
                        return false;
                    }
                }
                catch (NoSuchActionException e) {
                    return true;
                }
                Cooldowns.setUsed(playerEffect);
                performer.getStatus().modifyStamina((float)(int)(performer.getStatus().getStamina() * 0.5f));
                if (Vampires.isHalfVampire(target.getWurmId())) {
                    final String msg = Mod.fixActionString(target, "You see %NAME as a faded image in the mirror. %NAME is a half vampire, but there is still hope for %HIM in this world.");
                    performer.getCommunicator().sendNormalServerMessage(msg);
                    target.getCommunicator().sendNormalServerMessage(Mod.fixActionString(performer, "%NAME tilts %HIS silver mirror, looking at you through it."));
                }
                else if (Vampires.isVampire(target.getWurmId())) {
                    final String msg = Mod.fixActionString(target, "%NAME has absolutely no image in the mirror at all! %NAME is a vampire in all respects, a demon of the night.");
                    performer.getCommunicator().sendNormalServerMessage(msg);
                    target.getCommunicator().sendAlertServerMessage(String.valueOf(performer.getName()) + " checked your (lack of) reflection in a shiny silver mirror.", (byte)0);
                }
                else {
                    final String msg = Mod.fixActionString(target, "You see a common reflection of %NAME in the mirror. You feel kind of silly now, in the embarrassment you smudge the mirror. You'll need to polish it.");
                    performer.getCommunicator().sendNormalServerMessage(msg);
                    target.getCommunicator().sendNormalServerMessage(Mod.fixActionString(performer, "%NAME tilts %HIS silver mirror, looking at you through it."));
                    mirror.setAuxData((byte)1);
                }
                return true;
            }
        };
    }
}
