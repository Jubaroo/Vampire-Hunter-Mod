// 
// Decompiled by Procyon v0.5.30
// 

package com.friya.wurmonline.server.vamps.actions;

import com.friya.wurmonline.server.vamps.Vampires;
import com.wurmonline.server.behaviours.Action;
import com.wurmonline.server.behaviours.ActionEntry;
import com.wurmonline.server.behaviours.NoSuchActionException;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.CreaturesProxy;
import com.wurmonline.server.creatures.Offspring;
import com.wurmonline.server.creatures.Traits;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.skills.Skill;
import org.gotti.wurmunlimited.modsupport.actions.ActionPerformer;
import org.gotti.wurmunlimited.modsupport.actions.BehaviourProvider;
import org.gotti.wurmunlimited.modsupport.actions.ModAction;
import org.gotti.wurmunlimited.modsupport.actions.ModActions;

import java.util.Arrays;
import java.util.BitSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SenseAction implements ModAction
{
    private static Logger logger;
    private final short actionId;
    private final ActionEntry actionEntry;
    
    static {
        SenseAction.logger = Logger.getLogger(SenseAction.class.getName());
    }
    
    public SenseAction() {
        SenseAction.logger.log(Level.INFO, "SenseAction()");
        this.actionId = (short)ModActions.getNextActionId();
        ModActions.registerAction(this.actionEntry = ActionEntry.createEntry(this.actionId, "Sense offspring", "senses offspring", new int[] { 6 }));
    }
    
    public BehaviourProvider getBehaviourProvider() {
        return new BehaviourProvider() {
            public List<ActionEntry> getBehavioursFor(final Creature performer, final Creature object) {
                return this.getBehavioursFor(performer, null, object);
            }
            
            public List<ActionEntry> getBehavioursFor(final Creature performer, final Item subject, final Creature target) {
                if (performer instanceof Player && target instanceof Creature && Vampires.isVampire(performer.getWurmId()) && target.isAnimal()) {
                    return Arrays.asList(SenseAction.this.actionEntry);
                }
                return null;
            }
        };
    }
    
    public ActionPerformer getActionPerformer() {
        return new ActionPerformer() {
            public short getActionId() {
                return SenseAction.this.actionId;
            }
            
            public boolean action(final Action act, final Creature performer, final Creature target, final short action, final float counter) {
                return this.action(act, performer, null, target, action, counter);
            }
            
            public boolean action(final Action act, final Creature performer, final Item source, final Creature target, final short action, final float counter) {
                final int castTime = 50;
                if (!(performer instanceof Player) || !Vampires.isVampire(performer.getWurmId()) || !(target instanceof Creature) || !target.isAnimal()) {
                    return true;
                }
                if (!Vampires.isVampire(performer.getWurmId()) && performer.getPower() < 1) {
                    return true;
                }
                try {
                    if (counter == 1.0f) {
                        performer.getCommunicator().sendNormalServerMessage("You close your eyes and focus your senses on " + target.getName() + "...");
                        performer.getCurrentAction().setTimeLeft(castTime);
                        performer.sendActionControl("Sensing offspring", true, castTime);
                        return false;
                    }
                    if (counter * 10.0f <= act.getTimeLeft()) {
                        return false;
                    }
                }
                catch (NoSuchActionException e) {
                    return true;
                }
                final Offspring offspring = target.getOffspring();
                if (offspring == null) {
                    performer.getCommunicator().sendNormalServerMessage(String.valueOf(target.getName()) + " is not pregnant.");
                    return true;
                }
                final BitSet traits = SenseAction.this.toBitSet(CreaturesProxy.getTraits(offspring));
                final StringBuffer ret = new StringBuffer();
                String color = null;
                for (int i = 0; i < 27; ++i) {
                    if (traits.get(i)) {
                        ret.append(Traits.getTraitString(i));
                        ret.append(" ");
                    }
                }
                if (traits.get(15)) {
                    color = "brown";
                }
                else if (traits.get(16)) {
                    color = "gold";
                }
                else if (traits.get(17)) {
                    color = "black";
                }
                else if (traits.get(18)) {
                    color = "white";
                }
                else if (traits.get(23)) {
                    color = "ebony black";
                }
                else if (traits.get(24)) {
                    color = "piebald pinto";
                }
                else if (traits.get(25)) {
                    color = "blood bay";
                }
                else {
                    color = "gray";
                }
                ret.append("It will be ");
                ret.append(color);
                ret.append(".");
                performer.getCommunicator().sendNormalServerMessage("You sense the coming offspring of " + target.getName() + ": " + ret.toString());
                final Skill anatomy = target.getSkills().getSkillOrLearn(2147483638);
                anatomy.skillCheck(1.0, 0.0, false, 1.0f);
                return true;
            }
        };
    }
    
    private BitSet toBitSet(final long bits) {
        final BitSet traitbits = new BitSet(64);
        for (int x = 0; x < 64; ++x) {
            if (x == 0) {
                if ((bits & 0x1L) == 0x1L) {
                    traitbits.set(x, true);
                }
                else {
                    traitbits.set(x, false);
                }
            }
            else if ((bits >> x & 0x1L) == 0x1L) {
                traitbits.set(x, true);
            }
            else {
                traitbits.set(x, false);
            }
        }
        return traitbits;
    }
}
