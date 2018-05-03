// 
// Decompiled by Procyon v0.5.30
// 

package com.friya.wurmonline.server.vamps.actions;

import com.friya.wurmonline.server.vamps.Mod;
import com.friya.wurmonline.server.vamps.Vampire;
import com.friya.wurmonline.server.vamps.Vampires;
import com.wurmonline.server.behaviours.Action;
import com.wurmonline.server.behaviours.ActionEntry;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.players.Player;
import org.gotti.wurmunlimited.modsupport.actions.ActionPerformer;
import org.gotti.wurmunlimited.modsupport.actions.BehaviourProvider;
import org.gotti.wurmunlimited.modsupport.actions.ModAction;
import org.gotti.wurmunlimited.modsupport.actions.ModActions;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AdminVampAction implements ModAction
{
    private static Logger logger;
    private final short actionId;
    private final ActionEntry actionEntry;
    
    static {
        AdminVampAction.logger = Logger.getLogger(AdminVampAction.class.getName());
    }
    
    public AdminVampAction() {
        AdminVampAction.logger.log(Level.INFO, "AdminVampAction()");
        this.actionId = (short)ModActions.getNextActionId();
        ModActions.registerAction(this.actionEntry = ActionEntry.createEntry(this.actionId, "Admin: Make a Vampire", "fiddling", new int[0]));
    }
    
    public BehaviourProvider getBehaviourProvider() {
        return new BehaviourProvider() {
            public List<ActionEntry> getBehavioursFor(final Creature performer, final Item subject, final Creature target) {
                if (performer.getPower() > 1 && !Vampires.isHalfOrFullVampire(target.getWurmId()) && target.isPlayer() && subject != null && subject.getTemplateId() == 176) {
                    return Arrays.asList(AdminVampAction.this.actionEntry);
                }
                return null;
            }
        };
    }
    
    public ActionPerformer getActionPerformer() {
        return new ActionPerformer() {
            public short getActionId() {
                return AdminVampAction.this.actionId;
            }
            
            public boolean action(final Action act, final Creature performer, final Creature object, final short action, final float counter) {
                return true;
            }
            
            public boolean action(final Action act, final Creature performer, final Item source, final Creature target, final short action, final float counter) {
                if (!target.isPlayer() || performer.getPower() < 2 || source.getTemplateId() != 176) {
                    return true;
                }
                if (Vampires.isHalfOrFullVampire(target.getWurmId())) {
                    performer.getCommunicator().sendNormalServerMessage("They are a half vampire or vampire already.");
                    return true;
                }
                performer.getCommunicator().sendNormalServerMessage("Adding vampire status.");
                final Vampire vampire = Vampires.createVampire((Player)target, false);
                if (vampire == null) {
                    performer.getCommunicator().sendNormalServerMessage("FAILED! They might now be partially vampire, but not quite. They should probably log out and log back in at least.");
                }
                else {
                    Mod.loginVampire((Player)target);
                    target.getCommunicator().sendAlertServerMessage("You are now a vampire - the boring way.", (byte)4);
                    performer.getCommunicator().sendNormalServerMessage("Success! " + target.getName() + " is now a vampire. The boring way.");
                }
                return true;
            }
        };
    }
}
