// 
// Decompiled by Procyon v0.5.30
// 

package com.friya.wurmonline.server.vamps.actions;

import com.friya.wurmonline.server.vamps.Cooldowns;
import com.friya.wurmonline.server.vamps.Vampires;
import com.wurmonline.server.behaviours.Action;
import com.wurmonline.server.behaviours.ActionEntry;
import com.wurmonline.server.behaviours.NoSuchActionException;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.zones.VolaTile;
import org.gotti.wurmunlimited.modsupport.actions.ActionPerformer;
import org.gotti.wurmunlimited.modsupport.actions.BehaviourProvider;
import org.gotti.wurmunlimited.modsupport.actions.ModAction;
import org.gotti.wurmunlimited.modsupport.actions.ModActions;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AssistSlainAction implements ModAction
{
    private static Logger logger;
    private static short actionId;
    private final ActionEntry actionEntry;
    private final int castTime = 600;
    private final int cooldown = 43200000;
    
    static {
        AssistSlainAction.logger = Logger.getLogger(AssistSlainAction.class.getName());
    }
    
    public static short getActionId() {
        return AssistSlainAction.actionId;
    }
    
    public AssistSlainAction() {
        AssistSlainAction.logger.log(Level.INFO, "AssistSlainAction()");
        AssistSlainAction.actionId = (short)ModActions.getNextActionId();
        ModActions.registerAction(this.actionEntry = ActionEntry.createEntry(AssistSlainAction.actionId, "Assist Slain...", "assisting", new int[] { 6 }));
    }
    
    public BehaviourProvider getBehaviourProvider() {
        return new BehaviourProvider() {
            public List<ActionEntry> getBehavioursFor(final Creature performer, final Item source, final Item object) {
                return this.getBehavioursFor(performer, object);
            }
            
            public List<ActionEntry> getBehavioursFor(final Creature performer, final Item object) {
                if (performer.isPlayer() && object != null && object.getTemplateId() == 16 && Vampires.isVampire(performer.getWurmId()) && Vampires.getStakedTeleportPosition() != null) {
                    return Arrays.asList(AssistSlainAction.this.actionEntry);
                }
                return null;
            }
        };
    }
    
    public ActionPerformer getActionPerformer() {
        return new ActionPerformer() {
            public short getActionId() {
                return AssistSlainAction.actionId;
            }
            
            public boolean action(final Action act, final Creature performer, final Item target, final short action, final float counter) {
                if (!Vampires.isVampire(performer.getWurmId())) {
                    return true;
                }
                final String playerEffect = String.valueOf(performer.getName()) + "assistSlain";
                if (Cooldowns.isOnCooldown(playerEffect, 43200000L)) {
                    performer.getCommunicator().sendNormalServerMessage("You can only use this every few hours. Don't let the hunter go unpunished, though.");
                    return true;
                }
                try {
                    if (counter == 1.0f) {
                        final int tmpTime = 600 - (int)performer.getSkills().getSkillOrLearn(2147483635).getKnowledge() / 2;
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
                if (Vampires.getStakedTeleportPosition() == null) {
                    performer.getCommunicator().sendNormalServerMessage("The red pillar of the slain faded.");
                    return true;
                }
                final VolaTile t = Vampires.getStakedTeleportPosition();
                performer.setTeleportPoints((short)t.getTileX(), (short)t.getTileY(), t.isOnSurface() ? 0 : -1, 0);
                if (!performer.startTeleporting()) {
                    performer.getCommunicator().sendNormalServerMessage("Fzzzt!");
                    return true;
                }
                performer.getCommunicator().sendTeleport(false);
                performer.setBridgeId(-10L);
                performer.teleport(true);
                Cooldowns.setUsed(playerEffect);
                final Skill s = performer.getSkills().getSkillOrLearn(2147483635);
                s.skillCheck(1.0, 5.0, false, 1.0f);
                return true;
            }
            
            public boolean action(final Action act, final Creature performer, final Item source, final Item target, final short action, final float counter) {
                return this.action(act, performer, target, action, counter);
            }
        };
    }
}
