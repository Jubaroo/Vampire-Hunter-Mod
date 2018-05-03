// 
// Decompiled by Procyon v0.5.30
// 

package com.friya.wurmonline.server.vamps.actions;

import com.friya.wurmonline.server.vamps.*;
import com.friya.wurmonline.server.vamps.events.EventOnce;
import com.friya.wurmonline.server.vamps.events.RemoveBitableEvent;
import com.friya.wurmonline.server.vamps.events.RemoveEffectEvent;
import com.friya.wurmonline.server.vamps.events.StakeRecoverEvent;
import com.friya.wurmonline.server.vamps.items.Amulet;
import com.friya.wurmonline.server.vamps.items.Stake;
import com.friya.wurmonline.server.vamps.items.VampireFang;
import com.wurmonline.mesh.Tiles;
import com.wurmonline.server.*;
import com.wurmonline.server.behaviours.Action;
import com.wurmonline.server.behaviours.ActionEntry;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.NoSuchCreatureException;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemFactory;
import com.wurmonline.server.items.NoSpaceException;
import com.wurmonline.server.items.NoSuchTemplateException;
import com.wurmonline.server.players.Achievement;
import com.wurmonline.server.players.Achievements;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.skills.Affinities;
import com.wurmonline.server.skills.Affinity;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.sounds.SoundPlayer;
import com.wurmonline.server.zones.NoSuchZoneException;
import com.wurmonline.server.zones.Zones;
import com.wurmonline.shared.exceptions.WurmServerException;
import org.gotti.wurmunlimited.modsupport.actions.ActionPerformer;
import org.gotti.wurmunlimited.modsupport.actions.BehaviourProvider;
import org.gotti.wurmunlimited.modsupport.actions.ModAction;
import org.gotti.wurmunlimited.modsupport.actions.ModActions;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class StakeAction implements ModAction
{
    private static Logger logger;
    private final short actionId;
    private final ActionEntry actionEntry;
    private static final int STAMINA_COST = 4000;
    private static final int STAKE_ACTION_COUNT_REWARD = 2000;
    private static final float STAKE_ACTION_COUNT_CAP_MULTIPLIER = 0.25f;
    private static final float transactionModifier = 0.8f;
    
    static {
        StakeAction.logger = Logger.getLogger(StakeAction.class.getName());
    }
    
    public StakeAction() {
        StakeAction.logger.log(Level.INFO, "StakeAction()");
        this.actionId = (short)ModActions.getNextActionId();
        ModActions.registerAction(this.actionEntry = ActionEntry.createEntry(this.actionId, "STAKE (beware)", "staking", new int[] { 6, 23 }));
    }
    
    public BehaviourProvider getBehaviourProvider() {
        return new BehaviourProvider() {
            public List<ActionEntry> getBehavioursFor(final Creature performer, final Creature object) {
                return this.getBehavioursFor(performer, null, object);
            }
            
            public List<ActionEntry> getBehavioursFor(final Creature performer, final Item subject, final Creature target) {
                if (performer instanceof Player && (target instanceof Player || performer.getPower() > 2) && ((performer.getRighthandItem() != null && performer.getRighthandItem().getTemplateId() == Stake.getId()) || (performer.getLefthandItem() != null && performer.getLefthandItem().getTemplateId() == Stake.getId()))) {
                    return Arrays.asList(StakeAction.this.actionEntry);
                }
                return null;
            }
        };
    }
    
    public ActionPerformer getActionPerformer() {
        return new ActionPerformer() {
            public short getActionId() {
                return StakeAction.this.actionId;
            }
            
            public boolean action(final Action act, final Creature performer, final Creature object, final short action, final float counter) {
                if ((performer.getRighthandItem() != null && performer.getRighthandItem().getTemplateId() == Stake.getId()) || (performer.getLefthandItem() != null && performer.getLefthandItem().getTemplateId() == Stake.getId())) {
                    return true;
                }
                Mod.actionNotify(performer, "You fumble a bit with the stake. You should probably activate a mallet. Embarrassing...", "%NAME fingers a pointed wooden stake.", "In the corner of your eye you see a shadowy figure moving.");
                return true;
            }
            
            public boolean action(final Action act, final Creature performer, final Item source, final Creature target, final short action, final float counter) {
                boolean undodgableStake = false;
                final int redPillarSeconds = 600;
                Item stake;
                if (performer.getRighthandItem() != null && performer.getRighthandItem().getTemplateId() == Stake.getId()) {
                    stake = performer.getRighthandItem();
                }
                else {
                    if (performer.getLefthandItem() == null || performer.getLefthandItem().getTemplateId() != Stake.getId()) {
                        return true;
                    }
                    stake = performer.getLefthandItem();
                }
                if (source.getTemplateId() != 63) {
                    return this.action(act, performer, target, action, counter);
                }
                if (target.isInvulnerable()) {
                    performer.getCommunicator().sendNormalServerMessage(String.valueOf(target.getName()) + " is still invulnerable.");
                    return true;
                }
                boolean isVampire;
                if (performer.getPower() > 2) {
                    StakeAction.logger.log(Level.SEVERE, "WARNING: admins AND players with the name 'Staker' can stake anything due to testing!");
                    isVampire = true;
                }
                else {
                    isVampire = (target instanceof Player && Vampires.isVampire((Player)target));
                }
                if (VampZones.getCovenZone().covers(target.getTileX(), target.getTileY())) {
                    performer.getCommunicator().sendNormalServerMessage("Fzzzt. The stake does not seem to work here.");
                    return true;
                }
                if (performer.getSkills().getSkillOrLearn(1023).getKnowledge() < 35.0) {
                    performer.getCommunicator().sendNormalServerMessage("You cannot trifle with such power yet. You should at least go get a bit of experience in fighting.");
                    return true;
                }
                if (stake.getAuxData() == Stake.STATUS_WIELDING) {
                    Mod.actionNotify(performer, "The magical runes of the stake need a few seconds to settle before you can use it.", "%NAME swings a pointed stake around.", null);
                    return true;
                }
                if (stake.getAuxData() == Stake.STATUS_RECOVERING) {
                    Mod.actionNotify(performer, "You are still recovering from your previous attempt.", null, null);
                    return true;
                }
                if (performer.getWurmId() == target.getWurmId()) {
                    Mod.actionNotify(performer, "Stake yourself? Suicide is never the answer.", null, null);
                    return true;
                }
                if (Vampires.isHalfOrFullVampire(performer.getWurmId()) && performer.getPower() <= 0) {
                    Mod.actionNotify(performer, "You are a Vampire, unable to use this weapon in such a way.", null, null);
                    return true;
                }
                if (Stakers.isHunted(performer)) {
                    Mod.actionNotify(performer, "The blood on your hands prevent you from using its magical properties.", "%NAME handles a pointed wooden stake.", "A shadowy figure temporarily reveals %HIMSELF, but is gone before you could identify them.");
                    return true;
                }
                if (performer.getVehicle() != -10L) {
                    Mod.actionNotify(performer, "You have to be on solid ground.", null, null);
                    return true;
                }
                if (performer.getPower() <= 0 && target.getPower() > 0) {
                    Mod.actionNotify(performer, "You would have to be a FOOL to trifle with such power.", "%NAME tries to injure " + target.getName() + ", but fails.", "%NAME tries to injure " + target.getName() + ", but fails.");
                    return true;
                }
                if (!performer.isWithinTileDistanceTo(target.getTileX(), target.getTileY(), 0, 1)) {
                    performer.getCommunicator().sendNormalServerMessage("That is too far away.");
                    return true;
                }
                performer.getStatus().modifyStamina(-4000.0f);
                performer.playAnimation("fight_strike", false);
                if (Vampires.isHalfVampire(target.getWurmId())) {
                    performer.addWoundOfType(null, (byte)6, 1, true, 0.0f, true, 100000.0, 0.0f, 0.0);
                    performer.getCommunicator().sendAlertServerMessage("&*^#$*&^#!", (byte)4);
                    Mod.actionNotify(performer, String.valueOf(target.getName()) + " is not a true vampire! " + target.getName() + " is only a half vampire for whom there is still hope in this world! The magic of the stake turns on you, punishing you for using it against its purpose!", "%NAME is burnt as %HIS stake flares up!", "%NAME is burnt as %HIS stake flares up!");
                    return true;
                }
                if (!isVampire) {
                    performer.getCommunicator().sendAlertServerMessage("&*^#$*&^#!", (byte)4);
                    Mod.actionNotify(performer, "The magical runes of the stake light up, burning you!  That is not a Vampire!", "%NAME's stake lights up, burning %HIM!", "%NAME's stake lights up, burning %HIM!");
                    performer.die(false);
                    Items.destroyItem(stake.getWurmId());
                    return true;
                }
                if (stake.getMaterial() == 67) {
                    undodgableStake = true;
                }
                try {
                    if (target.getEquippedItem((byte)36).getTemplateId() == Amulet.getId()) {
                        performer.getCommunicator().sendAlertServerMessage("BOOM!", (byte)4);
                        Mod.actionNotify(performer, "Your stake strikes a solid ancient amulet lying over the vampire's heart. Both the stake and the amulet are consumed in a flash of fire!", "%NAME's stake is consumed in a flash of fire as it strikes a magical amulet over " + target.getName() + "'s heart!", "A stake is consumed in a flash of fire as it strikes a magical amulet over " + target.getName() + "'s heart!");
                        target.getCommunicator().sendAlertServerMessage("BOOM! Your amulet protects you!", (byte)4);
                        target.getCommunicator().sendNormalServerMessage("Your ancient amulet is consumed in a burst of fire as it is hit with a magical stake of vampire banishment wielded by " + performer.getName());
                        target.playAnimation("wounded", false);
                        Zones.flash(target.getTileX(), target.getTileY(), false);
                        Items.destroyItem(target.getEquippedItem((byte)36).getWurmId());
                        Items.destroyItem(stake.getWurmId());
                        Vampires.broadcast(Mod.fixActionString(performer, "%NAME has revealed %HIMSELF as a hunter by slamming a stake at an ancient amulet!"), true, false, true);
                        Vampires.broadcast(Mod.fixActionString(performer, "They can be hunted, but this is not a full-length hunt and they can still stake you!"), true, true, false);
                        performer.getCommunicator().sendAlertServerMessage("YOU HIT AN ANCIENT AMULET AND ARE HUNTED!", (byte)4);
                        performer.getCommunicator().sendAlertServerMessage("You have blood on your hands for a *short* while. *All* Vampires can seek their revenge. Run!", (byte)4);
                        performer.playPersonalSound("sound.spawn.item.central");
                        Stakers.addBitable(performer.getWurmId());
                        EventDispatcher.add(new RemoveBitableEvent(300, EventOnce.Unit.SECONDS, performer.getWurmId(), true));
                        return true;
                    }
                }
                catch (NoSuchItemException ex2) {}
                catch (NoSpaceException ex3) {}
                if (!undodgableStake) {
                    final double stakerDex = performer.getSkills().getSkillOrLearn(2147483640).getKnowledge();
                    final double vampireDex = target.getSkills().getSkillOrLearn(2147483640).getKnowledge();
                    final double stakerPerception = performer.getSkills().getSkillOrLearn(2147483639).getKnowledge();
                    final double vampirePerception = target.getSkills().getSkillOrLearn(2147483639).getKnowledge();
                    final double stakerStaminaPerCent = (performer.getStatus().getStamina() + 4000) / 65535.0 * 100.0;
                    final double vampireStaminaPerCent = target.getStatus().getStamina() / 65535.0 * 100.0;
                    final double stakerPower = stakerDex * 1.5 + stakerStaminaPerCent * 0.5 + stakerPerception * 0.75;
                    final double vampirePower = vampireDex * 1.5 + vampireStaminaPerCent * 0.5 + vampirePerception * 0.75 + (WurmCalendar.isNight() ? 27.5f : 0.0f);
                    final double skillCheck = stakerPower - vampirePower;
                    final double rand = Server.rand.nextInt(100);
                    final boolean dodged = (skillCheck < -50.0 || rand < 60.0) && rand > 4.0;
                    final DecimalFormat df = new DecimalFormat("#.##");
                    StakeAction.logger.log(Level.INFO, "StakeAction: " + (dodged ? "DODGED" : "STAKED") + ", rand " + rand + ", DEX " + df.format(stakerDex) + " vs Vamp's " + df.format(vampireDex) + ", PER " + df.format(stakerPerception) + " vs Vamp's " + df.format(vampirePerception) + ", STA " + df.format(stakerStaminaPerCent) + " vs Vamp's " + df.format(vampireStaminaPerCent) + ", Staker power: " + df.format(stakerPower) + "; Vamp power: " + df.format(vampirePower) + "; skillCheck: " + df.format(skillCheck));
                    if (dodged) {
                        target.getCommunicator().sendAlertServerMessage("*dodge*", (byte)4);
                        performer.getCommunicator().sendAlertServerMessage("*dodge*", (byte)4);
                        Mod.actionNotify(performer, String.valueOf(target.getName()) + "'s sixth sense enables them to dodge your feeble attack.", "%NAME makes a weak attempt to stake " + target.getName() + " and misses.", null);
                        target.getCommunicator().sendNormalServerMessage(String.valueOf(performer.getName()) + " makes an attempt to stake you! But your sixth sense allow you to dodge the blow.");
                        target.playAnimation("wounded", false, performer.getWurmId());
                        SoundPlayer.playSound("sound.combat.miss.heavy", target, 1.6f);
                        EventDispatcher.add(new StakeRecoverEvent(3, EventOnce.Unit.SECONDS, performer, stake));
                        return true;
                    }
                }
                else {
                    performer.getCommunicator().sendNormalServerMessage("The beast tried to dodge, but your seryll stake does the job.");
                }
                Mod.actionNotify(performer, "You stake the vampire through the heart! The stake banishes it back to the realm of darkness and rewards you for your successful hunt!", "%NAME stakes " + target.getName() + " through the heart! " + target.getName() + " is revealed as a vampire and banished to the realm of darkness!", "%NAME stakes " + target.getName() + " through the heart! " + target.getName() + " is revealed as a vampire and banished to the realm of darkness!");
                Mod.actionNotify(target, String.valueOf(performer.getName()) + " stakes you through the heart! You are punished for being caught by the mortal. You are banished from your material form.", null, null);
                performer.getCommunicator().sendNormalServerMessage("A bloody fang drops to the ground.");
                Server.getInstance().broadCastAction("A bloody fang drops to the ground.", performer, 6);
                Mod.actionNotify(target, "You turn to mist and flow back to the Coven.", String.valueOf(target.getName()) + " turns to mist and flows away.", null);
                final int tmpEffectId = Server.rand.nextInt(12345678) + 12345678;
                final short effectNum = 25;
                Players.getInstance().sendGlobalNonPersistantEffect((long)tmpEffectId, effectNum, target.getTileX(), target.getTileY(), Tiles.decodeHeightAsFloat(Server.surfaceMesh.getTile(target.getTileX(), target.getTileY())));
                EventDispatcher.add(new RemoveEffectEvent(redPillarSeconds, EventOnce.Unit.SECONDS, tmpEffectId));
                double vampireSkillLevelBefore = 1.0;
                double stakerSkillLevelBefore = 1.0;
                int exchangedStatNum = 0;
                String exchangedStatName = "";
                int actionCount = 0;
                double skillLoss = 0.0;
                double skillGain = 0.0;
                final ActionSkillGain actionSkillGain = ActionSkillGains.getRandomHighSkillToPunish(target);
                if (actionSkillGain != null) {
                    final Skill vampireSkill = target.getSkills().getSkillOrLearn(actionSkillGain.getId());
                    final Skill stakerSkill = performer.getSkills().getSkillOrLearn(actionSkillGain.getId());
                    vampireSkillLevelBefore = vampireSkill.getKnowledge();
                    stakerSkillLevelBefore = stakerSkill.getKnowledge();
                    exchangedStatNum = vampireSkill.getNumber();
                    exchangedStatName = vampireSkill.getName();
                    StakeAction.logger.log(Level.INFO, "Skill affected by stake: " + vampireSkill.getName());
                    actionCount = actionSkillGain.getModifiedLostActionCount(vampireSkill.getKnowledge(), 2000, 0.25f);
                    skillLoss = actionSkillGain.getRawSkillLossForActionCount(vampireSkill.getKnowledge(), actionCount);
                    skillGain = actionSkillGain.getRawSkillGainForActionCount(stakerSkill.getKnowledge(), actionCount);
                    StakeAction.logger.log(Level.INFO, "Skill loss for Vampire: " + skillLoss);
                    StakeAction.logger.log(Level.INFO, "Skill gain for Staker: " + skillGain + " moved down to create a loss to system: " + skillGain * 0.800000011920929);
                    skillGain *= 0.800000011920929;
                    final DecimalFormat df2 = new DecimalFormat("#.####");
                    vampireSkill.setKnowledge(vampireSkill.getKnowledge() - skillLoss, false, true);
                    target.getCommunicator().sendNormalServerMessage("You have lost " + df2.format(skillLoss) + " points in " + vampireSkill.getName() + " to " + performer.getName() + ".");
                    stakerSkill.setKnowledge(stakerSkill.getKnowledge() + skillGain, false, false);
                    performer.getCommunicator().sendNormalServerMessage("You have successfully taken " + df2.format(skillGain) + " points in " + stakerSkill.getName() + ".");
                    if (Server.rand.nextInt(100) < 25) {
                        final Affinity[] affs = Affinities.getAffinities(target.getWurmId());
                        if (affs.length > 0) {
                            final Affinity aff = affs[Server.rand.nextInt(affs.length)];
                            final String affName = target.getSkills().getSkillOrLearn(aff.getSkillNumber()).getName();
                            target.decreaseAffinity(aff.skillNumber, 1);
                            target.getCommunicator().sendNormalServerMessage("You lost your affinity in " + affName + ".");
                            performer.increaseAffinity(aff.skillNumber, 1);
                            performer.getCommunicator().sendNormalServerMessage("You have gained an affinity in " + affName + ".");
                            StakeAction.logger.log(Level.INFO, "Staker " + performer.getName() + " took affinity in " + affName + " from vampire (" + target.getName() + ")");
                        }
                    }
                    final Skill anatomy = target.getSkills().getSkillOrLearn(2147483638);
                    anatomy.skillCheck(1.0, 0.0, false, 1.0f);
                }
                else {
                    performer.getCommunicator().sendNormalServerMessage("This poor bloodsucker had no skills to speak of...");
                    StakeAction.logger.log(Level.SEVERE, "No suitable skill found for: " + target.getName());
                }
                try {
                    final Item item = ItemFactory.createItem(VampireFang.getId(), (float)vampireSkillLevelBefore, (byte)0, null);
                    item.setMaterial((byte)55);
                    item.putItemInfrontof(performer);
                }
                catch (FailedException | NoSuchTemplateException | NoSuchCreatureException | NoSuchItemException | NoSuchPlayerException | NoSuchZoneException ex4) {
                    final WurmServerException ex;
                    final WurmServerException e = ex;
                    StakeAction.logger.log(Level.SEVERE, "Could not drop Vampire fang", e);
                }
                target.getStatus().setStunned(2.0f);
                target.playAnimation("die", false);
                Vampires.broadcast(Mod.fixActionString(performer, "%NAME has revealed %HIMSELF as a vampire slayer!"), true, false, true);
                Vampires.broadcast(Mod.fixActionString(performer, "LET THE HUNT BEGIN!"), true, true, false);
                performer.getCommunicator().sendAlertServerMessage("YOU HAVE SLAIN A VAMPIRE!", (byte)4);
                performer.getCommunicator().sendAlertServerMessage("You have blood on your hands. *All* Vampires can seek their revenge. Run!", (byte)4);
                performer.playPersonalSound("sound.spawn.item.central");
                target.playPersonalSound("sound.spawn.item.central");
                Achievements.triggerAchievement(performer.getWurmId(), VampAchievements.STAKINGS);
                Achievements.triggerAchievement(target.getWurmId(), VampAchievements.STAKED);
                if (!VampTitles.hasTitle(performer, VampTitles.VAMPIRE_SLAYER)) {
                    performer.addTitle(VampTitles.getTitle(VampTitles.VAMPIRE_SLAYER));
                }
                final Achievement stakings = Achievements.getAchievementObject(performer.getWurmId()).getAchievement(VampAchievements.STAKINGS);
                if (stakings.getCounter() == 25 && !VampTitles.hasTitle(performer, VampTitles.VAN_HELSING)) {
                    performer.addTitle(VampTitles.getTitle(VampTitles.VAN_HELSING));
                }
                Items.destroyItem(stake.getWurmId());
                Vampires.setStakedTeleportPosition(target.currentTile, redPillarSeconds);
                final Skill bl = target.getSkills().getSkillOrLearn(2147483641);
                bl.setKnowledge(1.0, false);
                if (target instanceof Player) {
                    final Point loc = VampZones.getCovenRespawnPoint();
                    target.setTeleportPoints((short)loc.getX(), (short)loc.getY(), VampZones.getCovenLayer(), 0);
                    target.startTeleporting();
                    target.getCommunicator().sendTeleport(false);
                    target.setBridgeId(-10L);
                }
                else {
                    performer.getCommunicator().sendNormalServerMessage("Target is an NPC, not moving it to The Coven...");
                }
                Stakers.createStaker(performer, target, exchangedStatNum, exchangedStatName, vampireSkillLevelBefore, skillLoss, actionCount, stakerSkillLevelBefore, skillGain);
                return true;
            }
        };
    }
}
