// 
// Decompiled by Procyon v0.5.30
// 

package com.friya.wurmonline.server.vamps.actions;

import com.friya.wurmonline.server.vamps.Stakers;
import com.friya.wurmonline.server.vamps.Vampires;
import com.wurmonline.server.behaviours.Action;
import com.wurmonline.server.behaviours.ActionEntry;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.questions.HalfVampQuestion;
import org.gotti.wurmunlimited.modsupport.actions.ActionPerformer;
import org.gotti.wurmunlimited.modsupport.actions.BehaviourProvider;
import org.gotti.wurmunlimited.modsupport.actions.ModAction;
import org.gotti.wurmunlimited.modsupport.actions.ModActions;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HalfVampAction implements ModAction
{
    private static Logger logger;
    public static short actionId;
    private final ActionEntry actionEntry;
    int castTime;
    
    static {
        HalfVampAction.logger = Logger.getLogger(HalfVampAction.class.getName());
    }
    
    public HalfVampAction() {
        this.castTime = 30;
        HalfVampAction.logger.log(Level.INFO, "HalfVampAction()");
        HalfVampAction.actionId = (short)ModActions.getNextActionId();
        ModActions.registerAction(this.actionEntry = ActionEntry.createEntry(HalfVampAction.actionId, "Offer Instructions", "offering", new int[] { 6 }));
    }
    
    public BehaviourProvider getBehaviourProvider() {
        return new BehaviourProvider() {
            public List<ActionEntry> getBehavioursFor(final Creature performer, final Creature object) {
                return this.getBehavioursFor(performer, null, object);
            }
            
            public List<ActionEntry> getBehavioursFor(final Creature performer, final Item subject, final Creature target) {
                if (performer.isPlayer() && !target.isPlayer() && target.getName().equals("Dhampira the Ponderer")) {
                    return Arrays.asList(HalfVampAction.this.actionEntry);
                }
                return null;
            }
        };
    }
    
    public ActionPerformer getActionPerformer() {
        return new ActionPerformer() {
            public short getActionId() {
                return HalfVampAction.actionId;
            }
            
            public boolean action(final Action act, final Creature performer, final Creature target, final short action, final float counter) {
                if (performer.isPlayer() && !target.isPlayer() && target.getName().equals("Dhampira the Ponderer")) {
                    performer.getCommunicator().sendNormalServerMessage("Dhampira the Ponderer is looking for a papyrus sheet with some information about vampires...");
                }
                return true;
            }
            
            public boolean action(final Action act, final Creature performer, final Item activeItem, final Creature target, final short action, final float counter) {
                if (!performer.isPlayer() || target.isPlayer() || target == null || activeItem == null || !target.getName().equals("Dhampira the Ponderer")) {
                    return true;
                }
                if (performer.getPower() < 2) {
                    if (activeItem.getTemplateId() != 748) {
                        performer.getCommunicator().sendNormalServerMessage("I need information about the vampires. I've heard of a papyrus sheet spreading through the world with information about them. Please bring that to me.");
                        return true;
                    }
                    if (activeItem.getAuxData() != -104) {
                        performer.getCommunicator().sendNormalServerMessage("That's an interesting papyrus sheet, alas, not what I am looking for. I need information about the vampires.");
                        return true;
                    }
                    if (Vampires.isVampire(performer.getWurmId())) {
                        performer.getCommunicator().sendNormalServerMessage("You are one of them, I can sense it. Do you know where to find ... him?");
                        return true;
                    }
                    if (Stakers.isHunted(performer.getWurmId()) || Stakers.isWieldingStake(performer.getWurmId())) {
                        performer.getCommunicator().sendNormalServerMessage("Go away. I don't trust you.");
                        return true;
                    }
                    if (performer.getSkills().getSkillOrLearn(1023).getKnowledge() < 35.0) {
                        performer.getCommunicator().sendNormalServerMessage("You are clearly not experienced enough to be of any use to me. Come back when you have at least improved in fighting...");
                        return true;
                    }
                }
                else {
                    performer.getCommunicator().sendNormalServerMessage("You are admin, skipping all checks.");
                }
                final HalfVampQuestion aq = new HalfVampQuestion(performer, "There are no such things as Vampires?", "I urge you to read carefully, " + performer.getName() + "...", performer.getWurmId());
                aq.sendQuestion();
                return true;
            }
        };
    }
}
