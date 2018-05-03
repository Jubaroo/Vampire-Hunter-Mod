// 
// Decompiled by Procyon v0.5.30
// 

package com.friya.wurmonline.server.vamps.actions;

import com.friya.wurmonline.server.vamps.Vampires;
import com.wurmonline.server.behaviours.Action;
import com.wurmonline.server.behaviours.ActionEntry;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.questions.ToplistVampsQuestion;
import org.gotti.wurmunlimited.modsupport.actions.ActionPerformer;
import org.gotti.wurmunlimited.modsupport.actions.BehaviourProvider;
import org.gotti.wurmunlimited.modsupport.actions.ModAction;
import org.gotti.wurmunlimited.modsupport.actions.ModActions;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ToplistVampsAction implements ModAction
{
    private static Logger logger;
    private static short actionId;
    private final ActionEntry actionEntry;
    
    static {
        ToplistVampsAction.logger = Logger.getLogger(ToplistVampsAction.class.getName());
    }
    
    public static short getActionId() {
        return ToplistVampsAction.actionId;
    }
    
    public ToplistVampsAction() {
        ToplistVampsAction.logger.log(Level.INFO, "ToplistVampsAction()");
        ToplistVampsAction.actionId = (short)ModActions.getNextActionId();
        ModActions.registerAction(this.actionEntry = ActionEntry.createEntry(ToplistVampsAction.actionId, "Top Vampires", "checking", new int[] { 6 }));
    }
    
    public BehaviourProvider getBehaviourProvider() {
        return new BehaviourProvider() {
            public List<ActionEntry> getBehavioursFor(final Creature performer, final Item source, final Item object) {
                return this.getBehavioursFor(performer, object);
            }
            
            public List<ActionEntry> getBehavioursFor(final Creature performer, final Item object) {
                if (performer.isPlayer() && object != null && object.getTemplateId() == 16 && Vampires.isVampire(performer.getWurmId())) {
                    return Arrays.asList(ToplistVampsAction.this.actionEntry);
                }
                return null;
            }
        };
    }
    
    public ActionPerformer getActionPerformer() {
        return new ActionPerformer() {
            public short getActionId() {
                return ToplistVampsAction.actionId;
            }
            
            public boolean action(final Action act, final Creature performer, final Item target, final short action, final float counter) {
                if (!Vampires.isVampire(performer.getWurmId())) {
                    return true;
                }
                final ToplistVampsQuestion aq = new ToplistVampsQuestion(performer, "Highest rated vampires", "", performer.getWurmId());
                aq.sendQuestion();
                return true;
            }
            
            public boolean action(final Action act, final Creature performer, final Item source, final Item target, final short action, final float counter) {
                return this.action(act, performer, target, action, counter);
            }
        };
    }
}
