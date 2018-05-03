// 
// Decompiled by Procyon v0.5.30
// 

package com.friya.wurmonline.server.vamps.actions;

import com.friya.wurmonline.server.vamps.Cooldowns;
import com.friya.wurmonline.server.vamps.EventDispatcher;
import com.friya.wurmonline.server.vamps.Stakers;
import com.friya.wurmonline.server.vamps.Vampires;
import com.friya.wurmonline.server.vamps.events.EventOnce;
import com.friya.wurmonline.server.vamps.events.RemoveModifierEvent;
import com.wurmonline.server.Server;
import com.wurmonline.server.behaviours.Action;
import com.wurmonline.server.behaviours.ActionEntry;
import com.wurmonline.server.behaviours.NoSuchActionException;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.SpellEffectsEnum;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.modifiers.DoubleValueModifier;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.skills.Skill;
import org.gotti.wurmunlimited.modsupport.actions.ActionPerformer;
import org.gotti.wurmunlimited.modsupport.actions.BehaviourProvider;
import org.gotti.wurmunlimited.modsupport.actions.ModAction;
import org.gotti.wurmunlimited.modsupport.actions.ModActions;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CrippleAction implements ModAction
{
    private static Logger logger;
    private static short actionId;
    private final ActionEntry actionEntry;
    private final int DURATION = 30;
    private final String effectName = "crippleLiving";
    private final int cooldown = 120000;
    private final int castTime = 30;
    
    static {
        CrippleAction.logger = Logger.getLogger(CrippleAction.class.getName());
    }
    
    public static short getActionId() {
        return CrippleAction.actionId;
    }
    
    public CrippleAction() {
        CrippleAction.logger.log(Level.INFO, "CrippleAction()");
        CrippleAction.actionId = (short)ModActions.getNextActionId();
        ModActions.registerAction(this.actionEntry = ActionEntry.createEntry(CrippleAction.actionId, "Cripple Living", "crippling living", new int[] { 6, 23 }));
    }
    
    public BehaviourProvider getBehaviourProvider() {
        return new BehaviourProvider() {
            public List<ActionEntry> getBehavioursFor(final Creature performer, final Creature object) {
                return this.getBehavioursFor(performer, null, object);
            }
            
            public List<ActionEntry> getBehavioursFor(final Creature performer, final Item subject, final Creature target) {
                if (performer.isPlayer() && Vampires.isVampire(performer.getWurmId()) && (Stakers.isHunted(target) || Stakers.isHuntedMount(target))) {
                    return Arrays.asList(CrippleAction.this.actionEntry);
                }
                return null;
            }
        };
    }
    
    public ActionPerformer getActionPerformer() {
        return new ActionPerformer() {
            public short getActionId() {
                return CrippleAction.actionId;
            }
            
            public boolean action(final Action act, final Creature performer, final Creature target, final short action, final float counter) {
                return this.action(act, performer, null, target, action, counter);
            }
            
            public boolean action(final Action act, final Creature performer, final Item source, final Creature target, final short action, final float counter) {
                if (!(performer instanceof Player) || !(target instanceof Creature) || !Vampires.isVampire(performer.getWurmId())) {
                    return true;
                }
                if (!performer.isWithinTileDistanceTo(target.getTileX(), target.getTileY(), 0, 20)) {
                    performer.getCommunicator().sendNormalServerMessage("That is too far away.");
                    return true;
                }
                final String playerEffect = String.valueOf(performer.getName()) + "crippleLiving";
                if (Cooldowns.isOnCooldown(playerEffect, 120000L)) {
                    performer.getCommunicator().sendNormalServerMessage("You are still recovering from your previous cast.");
                    return true;
                }
                boolean targetIsHuntedMount = false;
                if (performer.getPower() < 2) {
                    targetIsHuntedMount = Stakers.isHuntedMount(target);
                    if (!Stakers.isHunted(target) && !targetIsHuntedMount) {
                        performer.getCommunicator().sendNormalServerMessage("You can only cast Cripple Living on hunted slayers or their mounts.");
                        return true;
                    }
                }
                else {
                    targetIsHuntedMount = target.isVehicle();
                }
                try {
                    if (counter == 1.0f) {
                        performer.getCommunicator().sendNormalServerMessage("You start casting Cripple Living...");
                        performer.getCurrentAction().setTimeLeft(30);
                        performer.sendActionControl("Crippling Living", true, 30);
                        return false;
                    }
                    if (counter * 10.0f <= act.getTimeLeft()) {
                        return false;
                    }
                }
                catch (NoSuchActionException e) {
                    return true;
                }
                final double successChance = Math.min(90.0, performer.getSkills().getSkillOrLearn(2147483637).getKnowledge() * 2.0);
                if (Server.rand.nextInt(100) < successChance) {
                    performer.getCommunicator().sendNormalServerMessage("Your Cripple Living failed...");
                    return true;
                }
                final DoubleValueModifier slowMod = new DoubleValueModifier(7, -0.4);
                target.getMovementScheme().addModifier(slowMod);
                EventDispatcher.add(new RemoveModifierEvent(30, EventOnce.Unit.SECONDS, target, slowMod, SpellEffectsEnum.WOUNDMOVE));
                performer.getCommunicator().sendNormalServerMessage("You slow down " + target.getName() + " with Cripple Living.");
                if (target.isPlayer()) {
                    target.getCommunicator().sendAddSpellEffect(SpellEffectsEnum.WOUNDMOVE, 30000, 100.0f);
                    target.getCommunicator().sendNormalServerMessage("You are slowed down by " + (performer.isStealth() ? "" : (String.valueOf(performer.getName()) + "'s ")) + "Cripple Living.");
                }
                if (targetIsHuntedMount) {
                    target.getCommunicator().sendNormalServerMessage("Your mount is affected by " + (performer.isStealth() ? "" : (String.valueOf(performer.getName()) + "'s ")) + "Cripple Living.");
                }
                final Skill sk = performer.getSkills().getSkillOrLearn(2147483637);
                sk.skillCheck(1.0, 0.0, false, 1.0f);
                return true;
            }
        };
    }
}
