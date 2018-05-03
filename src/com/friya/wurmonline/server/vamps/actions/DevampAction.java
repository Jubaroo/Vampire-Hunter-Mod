// 
// Decompiled by Procyon v0.5.30
// 

package com.friya.wurmonline.server.vamps.actions;

import com.friya.wurmonline.server.vamps.Vampires;
import com.friya.wurmonline.server.vamps.items.HalfVampireClue;
import com.wurmonline.server.Items;
import com.wurmonline.server.behaviours.Action;
import com.wurmonline.server.behaviours.ActionEntry;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.questions.DeVampQuestion;
import org.gotti.wurmunlimited.modsupport.actions.ActionPerformer;
import org.gotti.wurmunlimited.modsupport.actions.BehaviourProvider;
import org.gotti.wurmunlimited.modsupport.actions.ModAction;
import org.gotti.wurmunlimited.modsupport.actions.ModActions;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DevampAction implements ModAction
{
    private static Logger logger;
    public static short actionId;
    private final ActionEntry actionEntry;
    
    static {
        DevampAction.logger = Logger.getLogger(DevampAction.class.getName());
    }
    
    public DevampAction() {
        DevampAction.logger.log(Level.INFO, "DevampAction()");
        DevampAction.actionId = (short)ModActions.getNextActionId();
        ModActions.registerAction(this.actionEntry = ActionEntry.createEntry(DevampAction.actionId, "Talk about vampires", "talking", new int[] { 6 }));
    }
    
    public BehaviourProvider getBehaviourProvider() {
        return new BehaviourProvider() {
            public List<ActionEntry> getBehavioursFor(final Creature performer, final Creature object) {
                return this.getBehavioursFor(performer, null, object);
            }
            
            public List<ActionEntry> getBehavioursFor(final Creature performer, final Item subject, final Creature target) {
                if (performer.isPlayer() && !target.isPlayer() && target.getName().equals("van Helsing")) {
                    return Arrays.asList(DevampAction.this.actionEntry);
                }
                return null;
            }
        };
    }
    
    public ActionPerformer getActionPerformer() {
        return new ActionPerformer() {
            public short getActionId() {
                return DevampAction.actionId;
            }
            
            public boolean action(final Action act, final Creature performer, final Creature target, final short action, final float counter) {
                return this.action(act, performer, null, target, action, counter);
            }
            
            public boolean action(final Action act, final Creature performer, final Item activeItem, final Creature target, final short action, final float counter) {
                if (!performer.isPlayer() || target == null || !target.getName().equals("van Helsing")) {
                    return true;
                }
                if (!Vampires.isHalfOrFullVampire(performer.getWurmId())) {
                    performer.getCommunicator().sendNormalServerMessage("They are around, you just don't see them. You may come back ... later.");
                    return true;
                }
                final Item clue = performer.getInventory().findItem(HalfVampireClue.getId(), true);
                if (clue != null) {
                    Items.destroyItem(clue.getWurmId());
                }
                final DeVampQuestion aq = new DeVampQuestion(performer, "Talk about vampires...", "Are you sure you want to get rid of your bloodlust?", performer.getWurmId());
                aq.sendQuestion();
                return true;
            }
        };
    }
}
