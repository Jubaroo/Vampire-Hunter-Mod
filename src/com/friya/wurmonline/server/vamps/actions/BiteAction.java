// 
// Decompiled by Procyon v0.5.30
// 

package com.friya.wurmonline.server.vamps.actions;

import com.friya.wurmonline.server.vamps.*;
import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.Server;
import com.wurmonline.server.behaviours.Action;
import com.wurmonline.server.behaviours.ActionEntry;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.skills.Affinities;
import com.wurmonline.server.skills.Affinity;
import com.wurmonline.server.skills.Skill;
import org.gotti.wurmunlimited.modsupport.actions.ActionPerformer;
import org.gotti.wurmunlimited.modsupport.actions.BehaviourProvider;
import org.gotti.wurmunlimited.modsupport.actions.ModAction;
import org.gotti.wurmunlimited.modsupport.actions.ModActions;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BiteAction implements ModAction
{
    private static Logger logger;
    private static short actionId;
    private final ActionEntry actionEntry;
    private final String effectName = "bite";
    private final int cooldown = 7000;
    
    static {
        BiteAction.logger = Logger.getLogger(BiteAction.class.getName());
    }
    
    public static short getActionId() {
        return BiteAction.actionId;
    }
    
    public BiteAction() {
        BiteAction.logger.log(Level.INFO, "BiteAction()");
        BiteAction.actionId = (short)ModActions.getNextActionId();
        ModActions.registerAction(this.actionEntry = ActionEntry.createEntry(BiteAction.actionId, "BITE (beware)", "biting", new int[] { 6, 23 }));
    }
    
    public BehaviourProvider getBehaviourProvider() {
        return new BehaviourProvider() {
            public List<ActionEntry> getBehavioursFor(final Creature performer, final Creature object) {
                return this.getBehavioursFor(performer, null, object);
            }
            
            public List<ActionEntry> getBehavioursFor(final Creature performer, final Item subject, final Creature target) {
                if (Vampires.isVampire(performer.getWurmId()) && target.isPlayer()) {
                    return Arrays.asList(BiteAction.this.actionEntry);
                }
                return null;
            }
        };
    }
    
    public ActionPerformer getActionPerformer() {
        return new ActionPerformer() {
            public short getActionId() {
                return BiteAction.actionId;
            }
            
            public boolean action(final Action act, final Creature performer, final Creature object, final short action, final float counter) {
                return this.action(act, performer, null, object, action, counter);
            }
            
            public boolean action(final Action act, final Creature performer, final Item source, final Creature target, final short action, final float counter) {
                if (!Vampires.isVampire(performer.getWurmId())) {
                    return true;
                }
                if (!target.isPlayer() && performer.getPower() <= 1) {
                    return true;
                }
                if (!performer.isWithinTileDistanceTo(target.getTileX(), target.getTileY(), 0, 1)) {
                    performer.getCommunicator().sendNormalServerMessage("That is too far away.");
                    return true;
                }
                if (target.getSkills().getSkillOrLearn(1023).getKnowledge() < 35.0) {
                    performer.getCommunicator().sendNormalServerMessage(String.valueOf(target.getName()) + " is just too inexperienced to even pose a threat.");
                    return true;
                }
                final String playerEffect = String.valueOf(performer.getName()) + "bite";
                if (Cooldowns.isOnCooldown(playerEffect, 7000L)) {
                    performer.getCommunicator().sendNormalServerMessage("You are temporarily satiated and can draw no more blood for a little while.");
                    return true;
                }
                if (!Stakers.isHunted(target) && !Stakers.mayPunish(target.getWurmId())) {
                    performer.getCommunicator().sendAlertServerMessage("OUCH!", (byte)4);
                    Mod.actionNotify(performer, "You reel in pain! You cannot go around and feed on ordinary citizens! You have been warned!", "%NAME reels in pain, screaming loudly!", "The shadowy form of a vampire reels in pain, screaming loudly!");
                    final ActionSkillGain actionSkillGain = ActionSkillGains.getRandomSkillToPunish(target);
                    if (actionSkillGain != null) {
                        final Skill vampireSkill = performer.getSkills().getSkillOrLearn(actionSkillGain.getId());
                        final int actionCount = actionSkillGain.getModifiedLostActionCount(vampireSkill.getKnowledge(), 40, 0.05f);
                        final double skillLoss = actionSkillGain.getRawSkillLossForActionCount(vampireSkill.getKnowledge(), actionCount);
                        vampireSkill.setKnowledge(vampireSkill.getKnowledge() - skillLoss, false, true);
                    }
                    if (performer.getPower() < 3) {
                        return true;
                    }
                    BiteAction.logger.log(Level.INFO, "Admin bit something illegal, continuing past illegal check (might throw errors after this)...");
                }
                if (performer.getWurmId() == target.getWurmId()) {
                    performer.getCommunicator().sendNormalServerMessage("Yourself? Really?");
                    return true;
                }
                Staker staker = null;
                long staking = -1L;
                if (Stakers.isStaker(target.getWurmId())) {
                    try {
                        staker = Stakers.getStaker(target.getWurmId());
                        staking = staker.getId();
                    }
                    catch (NoSuchPlayerException e) {
                        e.printStackTrace();
                    }
                }
                else {
                    BiteAction.logger.info("Bitten target " + target.getName() + " is not (yet) a marked staker");
                }
                if (staker != null && !staker.mayBite()) {
                    performer.getCommunicator().sendNormalServerMessage("This poor slayer's been sucked dry.");
                    return true;
                }
                Cooldowns.setUsed(playerEffect);
                performer.getStatus().modifyStamina((float)(int)(performer.getStatus().getStamina() * 0.8f));
                final double dodgeChance = Math.min(20.0, target.getSkills().getSkillOrLearn(2147483640).getKnowledge() + target.getSkills().getSkillOrLearn(2147483639).getKnowledge());
                if (Server.rand.nextInt(100) < dodgeChance) {
                    Mod.actionNotify(performer, "You attempt to pierce " + target.getName() + "'s neck with your fangs, but " + (target.isNotFemale() ? "he" : "she") + " dodges.", "%NAME attacks " + target.getName() + "'s neck with %HIS lethal fangs bared, but " + (target.isNotFemale() ? "he" : "she") + " dodges!", "The shadowy form of a vampire makes " + target.getName() + " quickly move out of its way!", new Creature[] { target });
                    if (performer.isStealth()) {
                        target.getCommunicator().sendAlertServerMessage(Mod.fixActionString(performer, "The shadowy form of a vampire lunges at your neck with %HIS lethal fangs bared, you dodge!"), (byte)4);
                    }
                    else {
                        target.getCommunicator().sendAlertServerMessage(Mod.fixActionString(performer, "%NAME lunges at your neck with %HIS lethal fangs bared, you dodge!"), (byte)4);
                    }
                    return true;
                }
                double slayerSkillLevelBefore = 1.0;
                double vampireSkillLevelBefore = 1.0;
                int exchangedStatNum = 0;
                String exchangedStatName = "";
                int actionCount2 = 0;
                double skillLoss2 = 0.0;
                double skillGain = 0.0;
                final float transactionModifier = 0.8f;
                ActionSkillGain actionSkillGain2;
                if (staker != null) {
                    actionSkillGain2 = ActionSkillGains.getRandomHighSkillToPunish(target, staker.getAffectedSkill());
                }
                else {
                    actionSkillGain2 = ActionSkillGains.getRandomHighSkillToPunish(target);
                }
                if (actionSkillGain2 != null) {
                    final Skill stakerSkill = target.getSkills().getSkillOrLearn(actionSkillGain2.getId());
                    final Skill vampireSkill2 = performer.getSkills().getSkillOrLearn(actionSkillGain2.getId());
                    slayerSkillLevelBefore = stakerSkill.getKnowledge();
                    vampireSkillLevelBefore = vampireSkill2.getKnowledge();
                    exchangedStatNum = stakerSkill.getNumber();
                    exchangedStatName = stakerSkill.getName();
                    BiteAction.logger.log(Level.INFO, "Skill affected by bite: " + vampireSkill2.getName());
                    final Skill bl = performer.getSkills().getSkillOrLearn(2147483641);
                    int bloodlustBonus = 0;
                    if (bl.getKnowledge() > 85.0) {
                        bloodlustBonus = Vampires.BITE_ACTION_COUNT_REWARD / 10;
                    }
                    actionCount2 = actionSkillGain2.getModifiedLostActionCount(stakerSkill.getKnowledge(), bloodlustBonus + Vampires.BITE_ACTION_COUNT_REWARD, 0.05f);
                    if (Stakers.isHunted(target.getWurmId()) && actionCount2 > 0) {
                        actionCount2 += actionCount2 / 3;
                    }
                    skillLoss2 = actionSkillGain2.getRawSkillLossForActionCount(stakerSkill.getKnowledge(), actionCount2);
                    skillGain = actionSkillGain2.getRawSkillGainForActionCount(vampireSkill2.getKnowledge(), actionCount2);
                    BiteAction.logger.log(Level.INFO, "Skill loss for Staker: " + skillLoss2);
                    BiteAction.logger.log(Level.INFO, "Skill gain for Vampire: " + skillGain + " moved down to 80%: " + skillGain * transactionModifier);
                    skillGain *= transactionModifier;
                    final DecimalFormat df = new DecimalFormat("#.####");
                    stakerSkill.setKnowledge(stakerSkill.getKnowledge() - skillLoss2, false, true);
                    target.getCommunicator().sendNormalServerMessage("You have lost " + df.format(skillLoss2) + " points in " + exchangedStatName + " to " + performer.getName() + ".");
                    vampireSkill2.setKnowledge(vampireSkill2.getKnowledge() + skillGain, false, false);
                    performer.getCommunicator().sendAlertServerMessage(Mod.fixActionString(performer, "You have taken " + df.format(skillGain) + " points in " + exchangedStatName + " from " + target.getName() + "."), (byte)4);
                    if (Server.rand.nextInt(100) < 9) {
                        final Affinity[] affs = Affinities.getAffinities(target.getWurmId());
                        if (affs.length > 0) {
                            final Affinity aff = affs[Server.rand.nextInt(affs.length)];
                            final String affName = target.getSkills().getSkillOrLearn(aff.getSkillNumber()).getName();
                            target.decreaseAffinity(aff.skillNumber, 1);
                            target.getCommunicator().sendNormalServerMessage("You lost your affinity in " + affName + ".");
                            performer.increaseAffinity(aff.skillNumber, 1);
                            performer.getCommunicator().sendNormalServerMessage("You have gained an affinity in " + affName + ".");
                            BiteAction.logger.log(Level.INFO, "Vampire " + performer.getName() + " took affinity in " + affName + " from slayer (" + target.getName() + ")");
                        }
                    }
                    final Skill anatomy = performer.getSkills().getSkillOrLearn(2147483638);
                    anatomy.skillCheck(1.0, 0.0, false, 1.0f);
                }
                else {
                    performer.getCommunicator().sendNormalServerMessage("This poor slayer had no skills to speak of...");
                    BiteAction.logger.log(Level.SEVERE, "No suitable skill found for: " + target.getName());
                }
                Mod.actionNotify(performer, "You pierce " + target.getName() + "'s neck with your fangs, feeding on the vital life force.", "%NAME pierces " + target.getName() + "'s neck with %HIS lethal fangs!", "The shadowy form of a vampire pierces " + target.getName() + "'s neck with %HIS  lethal fangs!", new Creature[] { target });
                target.playAnimation("wounded", false, performer.getWurmId());
                if (performer.isStealth()) {
                    target.getCommunicator().sendAlertServerMessage(Mod.fixActionString(performer, "The shadowy form of a vampire pierces your neck with %HIS lethal fangs, feeding on your vital lifeforce!"), (byte)4);
                }
                else {
                    target.getCommunicator().sendAlertServerMessage(Mod.fixActionString(performer, "%NAME pierces your neck with %HIS lethal fangs, feeding on your vital lifeforce!"), (byte)4);
                }
                if (staker != null) {
                    staker.addBitten();
                }
                Vampires.createBite(performer, target, exchangedStatNum, exchangedStatName, vampireSkillLevelBefore, skillLoss2, actionCount2, slayerSkillLevelBefore, skillGain, staking);
                return true;
            }
        };
    }
}
