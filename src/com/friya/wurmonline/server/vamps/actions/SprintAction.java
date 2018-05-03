// 
// Decompiled by Procyon v0.5.30
// 

package com.friya.wurmonline.server.vamps.actions;

import com.friya.wurmonline.server.vamps.Cooldowns;
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

public class SprintAction implements ModAction
{
    private static Logger logger;
    private static short actionId;
    private final ActionEntry actionEntry;
    private static String effectName;
    private static long cooldown;
    
    static {
        SprintAction.logger = Logger.getLogger(SprintAction.class.getName());
        SprintAction.effectName = "sprint";
        SprintAction.cooldown = 3600000L;
    }
    
    public static short getActionId() {
        return SprintAction.actionId;
    }
    
    public SprintAction() {
        SprintAction.logger.log(Level.INFO, "SprintAction()");
        SprintAction.actionId = (short)ModActions.getNextActionId();
        ModActions.registerAction(this.actionEntry = ActionEntry.createEntry(SprintAction.actionId, "Sprint", "sprinting", new int[] { 6 }));
    }
    
    public BehaviourProvider getBehaviourProvider() {
        return new BehaviourProvider() {
            public List<ActionEntry> getBehavioursFor(final Creature performer, final int tilex, final int tiley, final boolean onSurface, final int tile, final int dir) {
                return Arrays.asList(SprintAction.this.actionEntry);
            }
            
            public List<ActionEntry> getBehavioursFor(final Creature performer, final Item subject, final int tilex, final int tiley, final boolean onSurface, final int tile) {
                return Arrays.asList(SprintAction.this.actionEntry);
            }
            
            public List<ActionEntry> getBehavioursFor(final Creature performer, final int tilex, final int tiley, final boolean onSurface, final int tile) {
                return Arrays.asList(SprintAction.this.actionEntry);
            }
        };
    }
    
    public ActionPerformer getActionPerformer() {
        return new ActionPerformer() {
            public short getActionId() {
                return SprintAction.actionId;
            }
            
            public boolean action(final Action act, final Creature performer, final Item source, final int tilex, final int tiley, final boolean onSurface, final int heightOffset, final int tile, final short action, final float counter) {
                this.action(act, performer, tilex, tiley, onSurface, tile, action, counter);
                return true;
            }
            
            public boolean action(final Action act, final Creature performer, final int tilex, final int tiley, final boolean onSurface, final int tile, final short action, final float counter) {
                if (!performer.isPlayer() || performer.getVehicle() != -10L) {
                    return true;
                }
                final String playerEffect = String.valueOf(performer.getName()) + SprintAction.effectName;
                if (Cooldowns.isOnCooldown(playerEffect, Vampires.isVampire(performer.getWurmId()) ? (SprintAction.cooldown / 2L) : SprintAction.cooldown)) {
                    performer.getCommunicator().sendNormalServerMessage("You're still exhausted.");
                    return true;
                }
                Cooldowns.setUsed(playerEffect);
                ((Player)performer).setFarwalkerSeconds((byte)20);
                performer.getMovementScheme().setFarwalkerMoveMod(true);
                performer.getStatus().sendStateString();
                performer.getCommunicator().sendNormalServerMessage("Your legs tingle and you feel fantastic!");
                return true;
            }
        };
    }
}
