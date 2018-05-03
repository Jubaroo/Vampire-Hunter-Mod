// 
// Decompiled by Procyon v0.5.30
// 

package com.friya.wurmonline.server.vamps.actions;

import com.friya.wurmonline.server.vamps.Vampires;
import com.friya.wurmonline.server.vamps.items.SmallRat;
import com.wurmonline.server.behaviours.Action;
import com.wurmonline.server.behaviours.ActionEntry;
import com.wurmonline.server.behaviours.NoSuchActionException;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.questions.AidQuestion;
import org.gotti.wurmunlimited.modsupport.actions.ActionPerformer;
import org.gotti.wurmunlimited.modsupport.actions.BehaviourProvider;
import org.gotti.wurmunlimited.modsupport.actions.ModAction;
import org.gotti.wurmunlimited.modsupport.actions.ModActions;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AidAction implements ModAction
{
    private static Logger logger;
    public static short actionId;
    private final ActionEntry actionEntry;
    private int castTime;
    
    static {
        AidAction.logger = Logger.getLogger(AidAction.class.getName());
        AidAction.actionId = -10;
    }
    
    public static short getActionId() {
        return AidAction.actionId;
    }
    
    public AidAction() {
        this.castTime = 1000;
        AidAction.logger.log(Level.INFO, "AidAction()");
        AidAction.actionId = (short)ModActions.getNextActionId();
        ModActions.registerAction(this.actionEntry = ActionEntry.createEntry(AidAction.actionId, "Aid a vampire", "aiding", new int[] { 6 }));
    }
    
    public BehaviourProvider getBehaviourProvider() {
        return new BehaviourProvider() {
            public List<ActionEntry> getBehavioursFor(final Creature performer, final Item target) {
                return this.getBehavioursFor(performer, null, target);
            }
            
            public List<ActionEntry> getBehavioursFor(final Creature performer, final Item source, final Item target) {
                if (performer.isPlayer() && Vampires.isVampire(performer.getWurmId()) && target.getTemplateId() == SmallRat.getId()) {
                    return Arrays.asList(AidAction.this.actionEntry);
                }
                return null;
            }
        };
    }
    
    public ActionPerformer getActionPerformer() {
        return new ActionPerformer() {
            public short getActionId() {
                return AidAction.actionId;
            }
            
            public boolean action(final Action act, final Creature performer, final Item target, final short action, final float counter) {
                return this.action(act, performer, null, target, action, counter);
            }
            
            public boolean action(final Action act, final Creature performer, final Item source, final Item target, final short action, final float counter) {
                if (!performer.isPlayer() || !Vampires.isVampire(performer.getWurmId()) || target.getTemplateId() != SmallRat.getId()) {
                    return true;
                }
                try {
                    if (counter == 1.0f) {
                        final int tmpTime = AidAction.this.castTime - (int)performer.getSkills().getSkillOrLearn(2147483635).getKnowledge();
                        performer.getCurrentAction().setTimeLeft(tmpTime);
                        performer.sendActionControl("Aiding vampire", true, tmpTime);
                        return false;
                    }
                    if (counter * 10.0f <= act.getTimeLeft()) {
                        return false;
                    }
                }
                catch (NoSuchActionException e) {
                    return true;
                }
                final AidQuestion aq = new AidQuestion(performer, "Aid...", "To whom would you like to send this rat to?", performer.getWurmId(), 100.0, target);
                aq.sendQuestion();
                return true;
            }
        };
    }
}
