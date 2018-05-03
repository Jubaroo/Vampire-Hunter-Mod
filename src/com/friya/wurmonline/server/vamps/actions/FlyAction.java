// 
// Decompiled by Procyon v0.5.30
// 

package com.friya.wurmonline.server.vamps.actions;

import com.friya.wurmonline.server.vamps.Mod;
import com.friya.wurmonline.server.vamps.VampZones;
import com.friya.wurmonline.server.vamps.Vampires;
import com.wurmonline.server.Point;
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

public class FlyAction implements ModAction
{
    private static Logger logger;
    private static short actionId;
    private final ActionEntry actionEntry;
    private final int castTime = 3000;
    
    static {
        FlyAction.logger = Logger.getLogger(FlyAction.class.getName());
    }
    
    public static short getActionId() {
        return FlyAction.actionId;
    }
    
    public FlyAction() {
        FlyAction.logger.log(Level.INFO, "FlyAction()");
        FlyAction.actionId = (short)ModActions.getNextActionId();
        ModActions.registerAction(this.actionEntry = ActionEntry.createEntry(FlyAction.actionId, "Fly to the Coven", "teleporting", new int[] { 6 }));
    }
    
    public BehaviourProvider getBehaviourProvider() {
        return new BehaviourProvider() {
            public List<ActionEntry> getBehavioursFor(final Creature performer, final Item source, final Item object) {
                return this.getBehavioursFor(performer, object);
            }
            
            public List<ActionEntry> getBehavioursFor(final Creature performer, final Item object) {
                if (performer.isPlayer() && object != null && object.getTemplateId() == 16 && Vampires.isVampire(performer.getWurmId())) {
                    return Arrays.asList(FlyAction.this.actionEntry);
                }
                return null;
            }
        };
    }
    
    public ActionPerformer getActionPerformer() {
        return new ActionPerformer() {
            public short getActionId() {
                return FlyAction.actionId;
            }
            
            public boolean action(final Action act, final Creature performer, final Item target, final short action, final float counter) {
                if (!Vampires.isVampire(performer.getWurmId())) {
                    return true;
                }
                try {
                    if (counter == 1.0f) {
                        int tmpTime = Mod.isTestEnv() ? 50 : 3000;
                        if (performer.getPower() > 2) {
                            tmpTime = 20;
                        }
                        performer.getCurrentAction().setTimeLeft(tmpTime);
                        performer.sendActionControl("Teleporting", true, tmpTime);
                        return false;
                    }
                    if (counter * 10.0f <= act.getTimeLeft()) {
                        return false;
                    }
                }
                catch (NoSuchActionException e) {
                    return true;
                }
                final Point loc = VampZones.getCovenRespawnPoint();
                performer.setTeleportPoints((short)loc.getX(), (short)loc.getY(), VampZones.getCovenLayer(), 0);
                if (!performer.startTeleporting()) {
                    performer.getCommunicator().sendNormalServerMessage("Fzzzt!");
                    return true;
                }
                performer.getCommunicator().sendTeleport(false);
                performer.setBridgeId(-10L);
                performer.teleport(true);
                return true;
            }
            
            public boolean action(final Action act, final Creature performer, final Item source, final Item target, final short action, final float counter) {
                return this.action(act, performer, target, action, counter);
            }
        };
    }
}
