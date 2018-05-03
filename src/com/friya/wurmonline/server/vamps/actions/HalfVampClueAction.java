// 
// Decompiled by Procyon v0.5.30
// 

package com.friya.wurmonline.server.vamps.actions;

import com.friya.wurmonline.server.vamps.items.HalfVampireClue;
import com.wurmonline.server.behaviours.Action;
import com.wurmonline.server.behaviours.ActionEntry;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.questions.HalfVampClueQuestion;
import org.gotti.wurmunlimited.modsupport.actions.ActionPerformer;
import org.gotti.wurmunlimited.modsupport.actions.BehaviourProvider;
import org.gotti.wurmunlimited.modsupport.actions.ModAction;
import org.gotti.wurmunlimited.modsupport.actions.ModActions;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HalfVampClueAction implements ModAction
{
    private static Logger logger;
    public static short actionId;
    private final ActionEntry actionEntry;
    
    static {
        HalfVampClueAction.logger = Logger.getLogger(HalfVampClueAction.class.getName());
    }
    
    public HalfVampClueAction() {
        HalfVampClueAction.logger.log(Level.INFO, "HalfVampClueAction()");
        HalfVampClueAction.actionId = (short)ModActions.getNextActionId();
        ModActions.registerAction(this.actionEntry = ActionEntry.createEntry(HalfVampClueAction.actionId, "Take a closer look...", "studying", new int[] { 6 }));
    }
    
    public BehaviourProvider getBehaviourProvider() {
        return new BehaviourProvider() {
            public List<ActionEntry> getBehavioursFor(final Creature performer, final Item source, final Item object) {
                return this.getBehavioursFor(performer, object);
            }
            
            public List<ActionEntry> getBehavioursFor(final Creature performer, final Item object) {
                if (performer instanceof Player && object != null && object.getTemplateId() == HalfVampireClue.getId()) {
                    return Arrays.asList(HalfVampClueAction.this.actionEntry);
                }
                return null;
            }
        };
    }
    
    public ActionPerformer getActionPerformer() {
        return new ActionPerformer() {
            public short getActionId() {
                return HalfVampClueAction.actionId;
            }
            
            public boolean action(final Action act, final Creature performer, final Item target, final short action, final float counter) {
                return HalfVampClueAction.this.study(act, performer, target, counter);
            }
            
            public boolean action(final Action act, final Creature performer, final Item source, final Item target, final short action, final float counter) {
                return this.action(act, performer, target, action, counter);
            }
        };
    }
    
    private boolean study(final Action act, final Creature performer, final Item target, final float counter) {
        if (!performer.isPlayer() || target == null || target.getTemplateId() != HalfVampireClue.getId()) {
            return true;
        }
        final HalfVampClueQuestion aq = new HalfVampClueQuestion(performer, "This is the best clue I have so far, " + performer.getName() + "...", "", performer.getWurmId());
        aq.sendQuestion();
        return true;
    }
}
