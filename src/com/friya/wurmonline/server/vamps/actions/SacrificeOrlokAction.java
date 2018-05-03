// 
// Decompiled by Procyon v0.5.30
// 

package com.friya.wurmonline.server.vamps.actions;

import com.friya.wurmonline.server.vamps.EventDispatcher;
import com.friya.wurmonline.server.vamps.Stakers;
import com.friya.wurmonline.server.vamps.Vampires;
import com.friya.wurmonline.server.vamps.events.DelayedMessage;
import com.friya.wurmonline.server.vamps.events.DelayedVamp;
import com.friya.wurmonline.server.vamps.events.EventOnce;
import com.friya.wurmonline.server.vamps.items.HalfVampireClue;
import com.wurmonline.server.Items;
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

public class SacrificeOrlokAction implements ModAction
{
    private static Logger logger;
    private final short actionId;
    private final ActionEntry actionEntry;
    int castTime;
    
    static {
        SacrificeOrlokAction.logger = Logger.getLogger(SacrificeOrlokAction.class.getName());
    }
    
    public SacrificeOrlokAction() {
        this.castTime = 600;
        SacrificeOrlokAction.logger.log(Level.INFO, "SacrificeAction()");
        this.actionId = (short)ModActions.getNextActionId();
        ModActions.registerAction(this.actionEntry = ActionEntry.createEntry(this.actionId, "Sacrifice Corpse", "sacrificing", new int[] { 6 }));
    }
    
    public BehaviourProvider getBehaviourProvider() {
        return new BehaviourProvider() {
            public List<ActionEntry> getBehavioursFor(final Creature performer, final Creature object) {
                return this.getBehavioursFor(performer, null, object);
            }
            
            public List<ActionEntry> getBehavioursFor(final Creature performer, final Item subject, final Creature target) {
                if (performer.isPlayer() && !target.isPlayer() && target.getName().equals("Orlok")) {
                    return Arrays.asList(SacrificeOrlokAction.this.actionEntry);
                }
                return null;
            }
        };
    }
    
    public ActionPerformer getActionPerformer() {
        return new ActionPerformer() {
            public short getActionId() {
                return SacrificeOrlokAction.this.actionId;
            }
            
            public boolean action(final Action act, final Creature performer, final Creature target, final short action, final float counter) {
                if (performer instanceof Player && target instanceof Creature && target.getName().equals("Orlok")) {
                    performer.getCommunicator().sendNormalServerMessage("You will need a fresh corpse of something worthy.");
                }
                return true;
            }
            
            public boolean action(final Action act, final Creature performer, final Item activeItem, final Creature target, final short action, final float counter) {
                if (!performer.isPlayer() || target == null || activeItem == null || !target.getName().equals("Orlok")) {
                    return true;
                }
                if (Stakers.isHunted(performer.getWurmId())) {
                    performer.getCommunicator().sendNormalServerMessage("Grinning, Orlok says: You are an enemy of my bretheren, slayer. You will not be accepted among them until you petition them for their permission.");
                    return true;
                }
                if (Vampires.isVampire(performer.getWurmId())) {
                    performer.getCommunicator().sendNormalServerMessage("You have already proven yourself worthy.");
                    return true;
                }
                if (!Vampires.isHalfVampire(performer.getWurmId())) {
                    performer.getCommunicator().sendNormalServerMessage("Who do you think you are? You are a mere human. Go away.");
                    return true;
                }
                if (activeItem.getTemplateId() != 272) {
                    performer.getCommunicator().sendNormalServerMessage("You will need a fresh corpse of something worthy.");
                    return true;
                }
                if (activeItem.getName().contains("bloodless husk")) {
                    performer.getCommunicator().sendNormalServerMessage("Some leftovers that you fed on? Something fresh!");
                    return true;
                }
                try {
                    if (counter == 1.0f) {
                        final int tmpTime = (performer.getPower() > 0) ? 20 : SacrificeOrlokAction.this.castTime;
                        performer.getCurrentAction().setTimeLeft(tmpTime);
                        performer.sendActionControl("Sacrificing Corpse", true, tmpTime);
                        return false;
                    }
                    if (counter * 10.0f <= act.getTimeLeft()) {
                        return false;
                    }
                }
                catch (NoSuchActionException e) {
                    return true;
                }
                if (!activeItem.getName().contains(" champion ")) {
                    performer.getCommunicator().sendNormalServerMessage("That is simply not good enough...");
                    return true;
                }
                final Item clue = performer.getInventory().findItem(HalfVampireClue.getId(), true);
                if (clue != null) {
                    Items.destroyItem(clue.getWurmId());
                }
                Items.destroyItem(activeItem.getWurmId());
                performer.getStatus().setStunned(21.0f);
                EventDispatcher.add(new DelayedMessage(1, EventOnce.Unit.SECONDS, performer, "Uh oh..."));
                EventDispatcher.add(new DelayedMessage(3, EventOnce.Unit.SECONDS, performer, "Orlok smiles, bestowing his unholy blessing upon you."));
                EventDispatcher.add(new DelayedMessage(8, EventOnce.Unit.SECONDS, performer, "Slashing his wrist, he feeds you from his own blood."));
                EventDispatcher.add(new DelayedMessage(12, EventOnce.Unit.SECONDS, performer, "You become a complete vampire in all respects."));
                EventDispatcher.add(new DelayedMessage(16, EventOnce.Unit.SECONDS, performer, "Powers that you did not know even existed course through your veins."));
                EventDispatcher.add(new DelayedVamp(20, EventOnce.Unit.SECONDS, performer, "You have awakened to the life of a true vampire."));
                return true;
            }
        };
    }
}
