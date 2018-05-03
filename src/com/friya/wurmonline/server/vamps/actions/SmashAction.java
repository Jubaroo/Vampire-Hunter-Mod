// 
// Decompiled by Procyon v0.5.30
// 

package com.friya.wurmonline.server.vamps.actions;

import com.friya.wurmonline.server.vamps.Cooldowns;
import com.friya.wurmonline.server.vamps.Mod;
import com.friya.wurmonline.server.vamps.Vampires;
import com.wurmonline.server.Server;
import com.wurmonline.server.behaviours.*;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.skills.Skill;
import org.gotti.wurmunlimited.modsupport.actions.ActionPerformer;
import org.gotti.wurmunlimited.modsupport.actions.BehaviourProvider;
import org.gotti.wurmunlimited.modsupport.actions.ModAction;
import org.gotti.wurmunlimited.modsupport.actions.ModActions;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SmashAction implements ModAction, ActionTypesProxy
{
    private static Logger logger;
    private static short actionId;
    private final ActionEntry actionEntry;
    int castTime;
    private final String effectName = "smash";
    private final int cooldown = 5000;
    private final int staminaCost = 15000;
    private boolean canMiss;
    
    static {
        SmashAction.logger = Logger.getLogger(SmashAction.class.getName());
    }
    
    public static short getActionId() {
        return SmashAction.actionId;
    }
    
    public SmashAction() {
        this.castTime = 30;
        this.canMiss = false;
        SmashAction.logger.log(Level.INFO, "SmashAction()");
        SmashAction.actionId = (short)ModActions.getNextActionId();
        ModActions.registerAction(this.actionEntry = ActionEntry.createEntry(SmashAction.actionId, "Smash", "smashing", new int[] { 6, 3, 48 }));
    }
    
    public BehaviourProvider getBehaviourProvider() {
        return new BehaviourProvider() {
            public List<ActionEntry> getBehavioursFor(final Creature performer, final Creature object) {
                return this.getBehavioursFor(performer, null, object);
            }
            
            public List<ActionEntry> getBehavioursFor(final Creature performer, final Item subject, final Creature target) {
                if (Vampires.isVampire(performer.getWurmId()) && performer.isPlayer() && !target.isPlayer()) {
                    return Arrays.asList(SmashAction.this.actionEntry);
                }
                return null;
            }
        };
    }
    
    public ActionPerformer getActionPerformer() {
        return new ActionPerformer() {
            public short getActionId() {
                return SmashAction.actionId;
            }
            
            public boolean action(final Action act, final Creature performer, final Creature target, final short action, final float counter) {
                return this.action(act, performer, null, target, action, counter);
            }
            
            public boolean action(final Action act, final Creature performer, final Item source, final Creature target, final short action, final float counter) {
                final String playerEffect = String.valueOf(performer.getName()) + "smash";
                if (!Vampires.isVampire(performer.getWurmId()) || !performer.isPlayer() || target.isPlayer()) {
                    return true;
                }
                if (target.getWurmId() == performer.getWurmId() || target.getKingdomId() == performer.getKingdomId() || target.getPower() >= 2) {
                    performer.getCommunicator().sendNormalServerMessage("That would be frowned upon.");
                    return true;
                }
                if (Cooldowns.isOnCooldown(playerEffect, 5000L)) {
                    performer.getCommunicator().sendNormalServerMessage("You need to wait a little while. ");
                    return true;
                }
                if (performer.isFighting()) {
                    performer.getCommunicator().sendNormalServerMessage("You can't use smash while fighting.");
                    return true;
                }
                try {
                    if (counter == 1.0f) {
                        performer.getCurrentAction().setTimeLeft(SmashAction.this.castTime);
                        performer.sendActionControl("Smashing", true, SmashAction.this.castTime);
                        return false;
                    }
                    if (counter * 10.0f <= act.getTimeLeft()) {
                        return false;
                    }
                }
                catch (NoSuchActionException e2) {
                    return true;
                }
                Cooldowns.setUsed(playerEffect);
                if (!performer.mayAttack(target)) {
                    performer.getCommunicator().sendNormalServerMessage("You may not attack that.");
                    return true;
                }
                final double dex = performer.getSkills().getSkillOrLearn(2147483640).getKnowledge();
                if (dex < 30.0) {
                    performer.getCommunicator().sendNormalServerMessage("You still lack the dexterity to do that efficiently.");
                    return true;
                }
                if (performer.getStatus().getStamina() > 15000) {
                    performer.getStatus().modifyStamina((float)(performer.getStatus().getStamina() - 15000));
                    if (SmashAction.this.canMiss) {
                        final double dexCheck = Math.min(95.0, 30.0 + dex * 0.7);
                        final int rndCheck = Server.rand.nextInt(100);
                        SmashAction.logger.log(Level.INFO, "smash(): rnd " + rndCheck + " > dex " + dexCheck + " == " + ((rndCheck > dexCheck) ? "" : "not ") + "miss!");
                        if (rndCheck > dexCheck) {
                            Mod.actionNotify(performer, "You swing viciously, but miss.", "%NAME swings viciously, but miss.", "A shadowy form swings viciously, but miss");
                            return true;
                        }
                    }
                    try {
                        final double bloodLust = performer.getSkills().getSkill(2147483641).getKnowledge();
                        final double power = 23.0 + bloodLust * 0.7699999809265137;
                        double damage = 20000.0 + 13000.0 * (power / 100.0);
                        damage *= (performer.isStealth() ? 1.1f : 1.0f);
                        final byte pos = target.getBody().getRandomWoundPos();
                        target.addWoundOfType(performer, (byte)0, (int)pos, false, 1.0f, true, damage);
                        Mod.actionNotify(performer, "You smash the mortal viciously with preternatural strength!", "%NAME smashes " + target.getName() + " viciously!", "A shadowy form smashes " + target.getName() + " viciously!");
                        target.setStealth(false);
                        final boolean done = performer.getCombatHandler().attack(target, Server.getCombatCounter(), false, counter, act);
                        CreatureBehaviour.setOpponent(performer, target, done, act);
                        final Skill s = target.getSkills().getSkillOrLearn(2147483640);
                        s.skillCheck(1.0, 0.0, false, 1.0f);
                    }
                    catch (Exception e) {
                        SmashAction.logger.log(Level.SEVERE, "failed to smash", e);
                        return true;
                    }
                    return true;
                }
                performer.getCommunicator().sendNormalServerMessage("You are too tired.");
                return true;
            }
        };
    }
}
