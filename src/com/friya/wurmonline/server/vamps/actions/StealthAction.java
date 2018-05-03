// 
// Decompiled by Procyon v0.5.30
// 

package com.friya.wurmonline.server.vamps.actions;

import com.friya.wurmonline.server.vamps.Cooldowns;
import com.friya.wurmonline.server.vamps.Mod;
import com.friya.wurmonline.server.vamps.Vampires;
import com.wurmonline.server.Players;
import com.wurmonline.server.behaviours.Action;
import com.wurmonline.server.behaviours.ActionEntry;
import com.wurmonline.server.behaviours.NoSuchActionException;
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

public class StealthAction implements ModAction
{
    private static Logger logger;
    private static short actionId;
    private final ActionEntry actionEntry;
    private final int castTime = 5;
    private final String effectName = "instantstealth";
    private final int cooldown = 900000;
    
    static {
        StealthAction.logger = Logger.getLogger(StealthAction.class.getName());
    }
    
    public static short getActionId() {
        return StealthAction.actionId;
    }
    
    public StealthAction() {
        StealthAction.logger.log(Level.INFO, "StealthAction()");
        StealthAction.actionId = (short)ModActions.getNextActionId();
        ModActions.registerAction(this.actionEntry = ActionEntry.createEntry(StealthAction.actionId, "Stealth", "stealthing", new int[] { 6 }));
    }
    
    public BehaviourProvider getBehaviourProvider() {
        return new BehaviourProvider() {
            public List<ActionEntry> getBehavioursFor(final Creature performer, final Item source, final Item object) {
                return this.getBehavioursFor(performer, object);
            }
            
            public List<ActionEntry> getBehavioursFor(final Creature performer, final Item object) {
                if (performer.isPlayer() && object != null && object.getTemplateId() == 16 && Vampires.isVampire(performer.getWurmId())) {
                    return Arrays.asList(StealthAction.this.actionEntry);
                }
                return null;
            }
        };
    }
    
    public ActionPerformer getActionPerformer() {
        return new ActionPerformer() {
            public short getActionId() {
                return StealthAction.actionId;
            }
            
            public boolean action(final Action act, final Creature performer, final Item target, final short action, final float counter) {
                if (!Vampires.isVampire(performer.getWurmId())) {
                    return true;
                }
                try {
                    if (counter == 1.0f) {
                        performer.getCurrentAction().setTimeLeft(5);
                        performer.sendActionControl("Stealthing", true, 5);
                        return false;
                    }
                    if (counter * 10.0f <= act.getTimeLeft()) {
                        return false;
                    }
                }
                catch (NoSuchActionException e) {
                    return true;
                }
                if (this.isWithinDistanceToOthers(performer)) {
                    performer.getCommunicator().sendNormalServerMessage("You are too close to someone to be able to stealth.");
                    return true;
                }
                final String playerEffect = String.valueOf(performer.getName()) + "instantstealth";
                if (Cooldowns.isOnCooldown(playerEffect, 900000L)) {
                    performer.getCommunicator().sendNormalServerMessage("You need to wait a little while before you can do this again. You can still use the normal stealth.");
                    if (!Mod.isTestEnv()) {
                        return true;
                    }
                    performer.getCommunicator().sendNormalServerMessage("... but you are on testenv, so allowing it!");
                }
                performer.setStealth(true);
                Cooldowns.setUsed(playerEffect);
                return true;
            }
            
            private boolean isWithinDistanceToOthers(final Creature stealther) {
                final Player[] players = Players.getInstance().getPlayers();
                Player[] array;
                for (int length = (array = players).length, i = 0; i < length; ++i) {
                    final Player p = array[i];
                    if (!p.isStealth() && p.getPower() <= 0) {
                        if (p.getWurmId() != stealther.getWurmId()) {
                            if (p.isWithinDistanceTo(stealther, 30.0f)) {
                                StealthAction.logger.info(String.valueOf(p.getName()) + " is too close to " + stealther.getName() + " to be able to insta-stealth");
                                return true;
                            }
                        }
                    }
                }
                return false;
            }
            
            public boolean action(final Action act, final Creature performer, final Item source, final Item target, final short action, final float counter) {
                return this.action(act, performer, target, action, counter);
            }
        };
    }
}
