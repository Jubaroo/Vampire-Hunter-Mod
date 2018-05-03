// 
// Decompiled by Procyon v0.5.30
// 

package com.friya.wurmonline.server.vamps.actions;

import com.friya.wurmonline.server.vamps.*;
import com.friya.wurmonline.server.vamps.events.EventOnce;
import com.friya.wurmonline.server.vamps.events.RemoveBitableEvent;
import com.wurmonline.server.NoSuchItemException;
import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.Server;
import com.wurmonline.server.behaviours.Action;
import com.wurmonline.server.behaviours.ActionEntry;
import com.wurmonline.server.behaviours.NoSuchActionException;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.NoSuchCreatureException;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.sounds.SoundPlayer;
import com.wurmonline.server.zones.NoSuchZoneException;
import com.wurmonline.shared.exceptions.WurmServerException;
import org.gotti.wurmunlimited.modsupport.actions.ActionPerformer;
import org.gotti.wurmunlimited.modsupport.actions.BehaviourProvider;
import org.gotti.wurmunlimited.modsupport.actions.ModAction;
import org.gotti.wurmunlimited.modsupport.actions.ModActions;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DisarmAction implements ModAction
{
    private static Logger logger;
    private static short actionId;
    private final ActionEntry actionEntry;
    private final String effectName = "disarmHunter";
    private final int cooldown = 10000;
    private final int castTime = 20;
    
    static {
        DisarmAction.logger = Logger.getLogger(DisarmAction.class.getName());
    }
    
    public static short getActionId() {
        return DisarmAction.actionId;
    }
    
    public DisarmAction() {
        DisarmAction.logger.log(Level.INFO, "CrippleAction()");
        DisarmAction.actionId = (short)ModActions.getNextActionId();
        ModActions.registerAction(this.actionEntry = ActionEntry.createEntry(DisarmAction.actionId, "Disarm Hunter", "disarming", new int[] { 6, 23 }));
    }
    
    public BehaviourProvider getBehaviourProvider() {
        return new BehaviourProvider() {
            public List<ActionEntry> getBehavioursFor(final Creature performer, final Creature object) {
                return this.getBehavioursFor(performer, null, object);
            }
            
            public List<ActionEntry> getBehavioursFor(final Creature performer, final Item subject, final Creature target) {
                if (performer.isPlayer() && target.isPlayer() && Vampires.isVampire(performer.getWurmId())) {
                    return Arrays.asList(DisarmAction.this.actionEntry);
                }
                return null;
            }
        };
    }
    
    public ActionPerformer getActionPerformer() {
        return new ActionPerformer() {
            public short getActionId() {
                return DisarmAction.actionId;
            }
            
            public boolean action(final Action act, final Creature performer, final Creature target, final short action, final float counter) {
                return this.action(act, performer, null, target, action, counter);
            }
            
            public boolean action(final Action act, final Creature performer, final Item source, final Creature target, final short action, final float counter) {
                if (!(performer instanceof Player) || !(target instanceof Creature) || !Vampires.isVampire(performer.getWurmId())) {
                    return true;
                }
                if (!performer.isWithinTileDistanceTo(target.getTileX(), target.getTileY(), 0, 1)) {
                    performer.getCommunicator().sendNormalServerMessage("That is too far away.");
                    return true;
                }
                final String playerEffect = String.valueOf(performer.getName()) + "disarmHunter";
                if (Cooldowns.isOnCooldown(playerEffect, 10000L)) {
                    performer.getCommunicator().sendNormalServerMessage("You are still recovering from your previous disarm.");
                    return true;
                }
                try {
                    if (counter == 1.0f) {
                        performer.getCurrentAction().setTimeLeft(20);
                        performer.sendActionControl("Disarming Hunter", true, 20);
                        return false;
                    }
                    if (counter * 10.0f <= act.getTimeLeft()) {
                        return false;
                    }
                }
                catch (NoSuchActionException e2) {
                    return true;
                }
                performer.getStatus().modifyStamina((float)(int)(performer.getStatus().getStamina() * 0.5f));
                final double rand = Server.rand.nextInt(100);
                final double stakerDex = target.getSkills().getSkillOrLearn(2147483640).getKnowledge();
                double vampireDex = performer.getSkills().getSkillOrLearn(2147483640).getKnowledge();
                final double vampireDisarming = performer.getSkills().getSkillOrLearn(2147483636).getKnowledge();
                vampireDex += Math.min(100.0, vampireDisarming / 10.0 + vampireDex);
                final double dexCheck = Math.min(vampireDex / stakerDex * 90.0, 90.0);
                if (dexCheck < rand) {
                    Mod.actionNotify(performer, String.valueOf(target.getName()) + "'s sixth sense enables them to dodge your disarming attempt.", "%NAME makes a weak attempt to disarm " + target.getName() + " and fails.", null, new Creature[] { performer, target });
                    target.getCommunicator().sendNormalServerMessage(String.valueOf(performer.getName()) + " makes an attempt to disarm you! But you dodge.");
                    target.playAnimation("wounded", false, performer.getWurmId());
                    SoundPlayer.playSound("sound.combat.miss.heavy", target, 1.6f);
                    return true;
                }
                try {
                    final Item stake = Stakers.getWieldedStake(target);
                    if (stake == null) {
                        performer.getCommunicator().sendNormalServerMessage("They are not wielding a stake, someone must have beaten you to it.");
                        return true;
                    }
                    stake.putItemInfrontof(target);
                    target.getCommunicator().sendNormalServerMessage("You were disarmed" + (performer.isStealth() ? "." : (" by " + performer.getName() + ".")) + " The stake lands on the ground.");
                    performer.getCommunicator().sendNormalServerMessage("You disarm " + target.getName() + ", the stake lands on the ground.");
                    Stakers.addBitable(target.getWurmId());
                    EventDispatcher.add(new RemoveBitableEvent(25, EventOnce.Unit.SECONDS, target.getWurmId()));
                    final Skill sk = performer.getSkills().getSkillOrLearn(2147483636);
                    sk.skillCheck(1.0, 0.0, false, 1.0f);
                }
                catch (NoSuchCreatureException | NoSuchItemException | NoSuchPlayerException | NoSuchZoneException ex2) {
                    final WurmServerException ex;
                    final WurmServerException e = ex;
                    performer.getCommunicator().sendNormalServerMessage("Error in the fabric of space when disarming, please let an admin know.");
                    e.printStackTrace();
                }
                return true;
            }
        };
    }
}
