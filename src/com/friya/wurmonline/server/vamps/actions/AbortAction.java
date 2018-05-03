// 
// Decompiled by Procyon v0.5.30
// 

package com.friya.wurmonline.server.vamps.actions;

import com.friya.wurmonline.server.vamps.Cooldowns;
import com.friya.wurmonline.server.vamps.Mod;
import com.friya.wurmonline.server.vamps.Vampires;
import com.wurmonline.server.behaviours.Action;
import com.wurmonline.server.behaviours.ActionEntry;
import com.wurmonline.server.behaviours.NoSuchActionException;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.CreaturesProxy;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.skills.Skill;
import org.gotti.wurmunlimited.modsupport.actions.ActionPerformer;
import org.gotti.wurmunlimited.modsupport.actions.BehaviourProvider;
import org.gotti.wurmunlimited.modsupport.actions.ModAction;
import org.gotti.wurmunlimited.modsupport.actions.ModActions;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AbortAction implements ModAction
{
    private static Logger logger;
    private final short actionId;
    private final ActionEntry actionEntry;
    private final int cooldown = 82800000;
    
    static {
        AbortAction.logger = Logger.getLogger(AbortAction.class.getName());
    }
    
    public AbortAction() {
        AbortAction.logger.log(Level.INFO, "AbortAction()");
        this.actionId = (short)ModActions.getNextActionId();
        ModActions.registerAction(this.actionEntry = ActionEntry.createEntry(this.actionId, "Abort offspring", "aborts offspring", new int[] { 6 }));
    }
    
    public BehaviourProvider getBehaviourProvider() {
        return new BehaviourProvider() {
            public List<ActionEntry> getBehavioursFor(final Creature performer, final Creature object) {
                return this.getBehavioursFor(performer, null, object);
            }
            
            public List<ActionEntry> getBehavioursFor(final Creature performer, final Item subject, final Creature target) {
                if (performer instanceof Player && target instanceof Creature && Vampires.isVampire(performer.getWurmId()) && target.isAnimal() && target.isPregnant()) {
                    return Arrays.asList(AbortAction.this.actionEntry);
                }
                return null;
            }
        };
    }
    
    public ActionPerformer getActionPerformer() {
        return new ActionPerformer() {
            public short getActionId() {
                return AbortAction.this.actionId;
            }
            
            public boolean action(final Action act, final Creature performer, final Creature target, final short action, final float counter) {
                return this.action(act, performer, null, target, action, counter);
            }
            
            public boolean action(final Action act, final Creature performer, final Item source, final Creature target, final short action, final float counter) {
                final int castTime = 100;
                final String playerEffect = String.valueOf(performer.getName()) + "abortoffspring";
                if (!(performer instanceof Player) || !Vampires.isVampire(performer.getWurmId()) || !(target instanceof Creature) || !target.isAnimal() || !target.isPregnant()) {
                    return true;
                }
                if (!Vampires.isVampire(performer.getWurmId()) && performer.getPower() < 1) {
                    return true;
                }
                final Skill anatomy = performer.getSkills().getSkillOrLearn(2147483638);
                if (anatomy.getKnowledge() < 30.0) {
                    performer.getCommunicator().sendNormalServerMessage("You need a bit more skill in anatomy for that...");
                    return true;
                }
                if (Cooldowns.isOnCooldown(playerEffect, 82800000L)) {
                    performer.getCommunicator().sendNormalServerMessage("It's mentally exhausting, you will need to wait before you can do that again.");
                    return true;
                }
                try {
                    if (counter == 1.0f) {
                        performer.getCommunicator().sendNormalServerMessage("You close your eyes and focus your senses the offspring of " + target.getName() + "...");
                        performer.getCurrentAction().setTimeLeft(castTime);
                        performer.sendActionControl("Aborting offspring", true, castTime);
                        return false;
                    }
                    if (counter * 10.0f <= act.getTimeLeft()) {
                        return false;
                    }
                }
                catch (NoSuchActionException e) {
                    return true;
                }
                if (performer.getVillageId() <= 0 || !target.isBranded() || !target.isBrandedBy(performer.getVillageId())) {
                    performer.getCommunicator().sendNormalServerMessage(String.valueOf(target.getName()) + " is not branded by your settlement.");
                    return true;
                }
                CreaturesProxy.deleteOffspringSettings(target.getWurmId());
                Mod.actionNotify(performer, "You magically and swiftly make the offspring of " + target.getName() + " vanish in a puff of smoke.", "%NAME mumbles some incoherent phrases and " + target.getName() + " seems healthier.", "A shadowy form mumbles some incoherent phrases and " + target.getName() + " seems healthier.");
                Cooldowns.setUsed(playerEffect);
                return true;
            }
        };
    }
}
