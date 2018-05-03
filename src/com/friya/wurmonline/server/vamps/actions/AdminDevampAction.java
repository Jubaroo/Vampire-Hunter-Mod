// 
// Decompiled by Procyon v0.5.30
// 

package com.friya.wurmonline.server.vamps.actions;

import com.friya.wurmonline.server.vamps.Vampires;
import com.wurmonline.server.behaviours.Action;
import com.wurmonline.server.behaviours.ActionEntry;
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

public class AdminDevampAction implements ModAction
{
    private static Logger logger;
    private final short actionId;
    private final ActionEntry actionEntry;
    
    static {
        AdminDevampAction.logger = Logger.getLogger(AdminDevampAction.class.getName());
    }
    
    public AdminDevampAction() {
        AdminDevampAction.logger.log(Level.INFO, "AdminDevampAction()");
        this.actionId = (short)ModActions.getNextActionId();
        ModActions.registerAction(this.actionEntry = ActionEntry.createEntry(this.actionId, "Admin: Remove Vampire Status", "fiddling", new int[0]));
    }
    
    public BehaviourProvider getBehaviourProvider() {
        return new BehaviourProvider() {
            public List<ActionEntry> getBehavioursFor(final Creature performer, final Item subject, final Creature target) {
                if (performer.getPower() > 1 && Vampires.isHalfOrFullVampire(target.getWurmId()) && target.isPlayer() && subject != null && subject.getTemplateId() == 176) {
                    return Arrays.asList(AdminDevampAction.this.actionEntry);
                }
                return null;
            }
        };
    }
    
    public ActionPerformer getActionPerformer() {
        return new ActionPerformer() {
            public short getActionId() {
                return AdminDevampAction.this.actionId;
            }
            
            public boolean action(final Action act, final Creature performer, final Creature object, final short action, final float counter) {
                return true;
            }
            
            public boolean action(final Action act, final Creature performer, final Item source, final Creature target, final short action, final float counter) {
                if (!target.isPlayer() || performer.getPower() < 2 || source.getTemplateId() != 176) {
                    return true;
                }
                if (!Vampires.isHalfOrFullVampire(target.getWurmId())) {
                    performer.getCommunicator().sendNormalServerMessage("That is not a half vampire or vampire.");
                    return true;
                }
                performer.getCommunicator().sendNormalServerMessage("Removing vampire status .");
                final boolean success = Vampires.deVampWithoutLoss(target);
                if (!success) {
                    performer.getCommunicator().sendNormalServerMessage("FAILED! They are probably fully or partially still vampire.");
                }
                else {
                    target.getCommunicator().sendAlertServerMessage("You are no longer a vampire.", (byte)4);
                    performer.getCommunicator().sendNormalServerMessage("Success! " + target.getName() + " is no longer a vampire.");
                }
                return true;
            }
        };
    }
}
