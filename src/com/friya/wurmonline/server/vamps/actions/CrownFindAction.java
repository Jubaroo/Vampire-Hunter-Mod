// 
// Decompiled by Procyon v0.5.30
// 

package com.friya.wurmonline.server.vamps.actions;

import com.friya.wurmonline.server.vamps.items.Crown;
import com.wurmonline.server.behaviours.Action;
import com.wurmonline.server.behaviours.ActionEntry;
import com.wurmonline.server.behaviours.NoSuchActionException;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemsProxy;
import com.wurmonline.server.questions.PinpointHumanoidQuestion;
import org.gotti.wurmunlimited.modsupport.actions.ActionPerformer;
import org.gotti.wurmunlimited.modsupport.actions.BehaviourProvider;
import org.gotti.wurmunlimited.modsupport.actions.ModAction;
import org.gotti.wurmunlimited.modsupport.actions.ModActions;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CrownFindAction implements ModAction
{
    private static Logger logger;
    public static short actionId;
    private final ActionEntry actionEntry;
    private int castTime;
    
    static {
        CrownFindAction.logger = Logger.getLogger(CrownFindAction.class.getName());
        CrownFindAction.actionId = -10;
    }
    
    public static short getActionId() {
        return CrownFindAction.actionId;
    }
    
    public CrownFindAction() {
        this.castTime = 50;
        CrownFindAction.logger.log(Level.INFO, "CrownFindAction()");
        CrownFindAction.actionId = (short)ModActions.getNextActionId();
        ModActions.registerAction(this.actionEntry = ActionEntry.createEntry(CrownFindAction.actionId, "Find...", "finding", new int[] { 6 }));
    }
    
    public BehaviourProvider getBehaviourProvider() {
        return new BehaviourProvider() {
            public List<ActionEntry> getBehavioursFor(final Creature performer, final Item target) {
                return this.getBehavioursFor(performer, null, target);
            }
            
            public List<ActionEntry> getBehavioursFor(final Creature performer, final Item source, final Item target) {
                if (performer.isPlayer() && source != null && source.getTemplateId() == Crown.getId() && ItemsProxy.isWornAsArmour(source)) {
                    return Arrays.asList(CrownFindAction.this.actionEntry);
                }
                return null;
            }
        };
    }
    
    public ActionPerformer getActionPerformer() {
        return new ActionPerformer() {
            public short getActionId() {
                return CrownFindAction.actionId;
            }
            
            public boolean action(final Action act, final Creature performer, final Item target, final short action, final float counter) {
                return this.action(act, performer, null, target, action, counter);
            }
            
            public boolean action(final Action act, final Creature performer, final Item source, final Item target, final short action, final float counter) {
                if (!performer.isPlayer() || target.getTemplateId() != Crown.getId()) {
                    return true;
                }
                if (source.getQualityLevel() < 99.0f) {
                    performer.getCommunicator().sendNormalServerMessage("Finding with the crown will only work if it is of absolute top quality. It's still nice, though!");
                    return true;
                }
                if (!ItemsProxy.isWornAsArmour(source)) {
                    performer.getCommunicator().sendNormalServerMessage("You must be wearing the crown.");
                    return true;
                }
                try {
                    if (counter == 1.0f) {
                        final int tmpTime = CrownFindAction.this.castTime;
                        performer.getCurrentAction().setTimeLeft(tmpTime);
                        performer.sendActionControl("Find human", true, tmpTime);
                        return false;
                    }
                    if (counter * 10.0f <= act.getTimeLeft()) {
                        return false;
                    }
                }
                catch (NoSuchActionException e) {
                    return true;
                }
                final PinpointHumanoidQuestion aq = new PinpointHumanoidQuestion(performer, "Find...", "Who are you looking for?", -1, source.getWurmId());
                aq.extraQuestionNote = "Warning: Be careful, the one you are finding will get YOUR name and YOUR location when you use it!";
                aq.ignoreNoLo = true;
                aq.reverseFind = true;
                aq.sendQuestion();
                return true;
            }
        };
    }
}
