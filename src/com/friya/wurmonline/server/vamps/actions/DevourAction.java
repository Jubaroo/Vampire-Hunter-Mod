// 
// Decompiled by Procyon v0.5.30
// 

package com.friya.wurmonline.server.vamps.actions;

import com.friya.wurmonline.server.vamps.BloodlessHusk;
import com.friya.wurmonline.server.vamps.Mod;
import com.friya.wurmonline.server.vamps.VampAchievements;
import com.friya.wurmonline.server.vamps.Vampires;
import com.friya.wurmonline.server.vamps.items.SmallRat;
import com.wurmonline.server.Items;
import com.wurmonline.server.WurmCalendar;
import com.wurmonline.server.behaviours.*;
import com.wurmonline.server.bodys.Wound;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.CreatureStatus;
import com.wurmonline.server.creatures.CreaturesProxy;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.players.Achievements;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.skills.*;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zones;
import org.gotti.wurmunlimited.modsupport.actions.ActionPerformer;
import org.gotti.wurmunlimited.modsupport.actions.BehaviourProvider;
import org.gotti.wurmunlimited.modsupport.actions.ModAction;
import org.gotti.wurmunlimited.modsupport.actions.ModActions;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.CRC32;

public class DevourAction implements ModAction
{
    private static Logger logger;
    private static short actionId;
    private final ActionEntry actionEntry;
    private final int castTime = 30;
    
    static {
        DevourAction.logger = Logger.getLogger(DevourAction.class.getName());
    }
    
    public static short getActionId() {
        return DevourAction.actionId;
    }
    
    public DevourAction() {
        DevourAction.logger.log(Level.INFO, "DevourAction()");
        DevourAction.actionId = (short)ModActions.getNextActionId();
        ModActions.registerAction(this.actionEntry = ActionEntry.createEntry(DevourAction.actionId, "Devour", "devouring", new int[] { 6 }));
    }
    
    public BehaviourProvider getBehaviourProvider() {
        return new BehaviourProvider() {
            public List<ActionEntry> getBehavioursFor(final Creature performer, final Item source, final Item object) {
                return this.getBehavioursFor(performer, object);
            }
            
            public List<ActionEntry> getBehavioursFor(final Creature performer, final Item object) {
                if (performer instanceof Player && object != null && (object.getTemplateId() == 272 || object.getTemplateId() == SmallRat.getId()) && Vampires.isHalfOrFullVampire(performer.getWurmId())) {
                    return Arrays.asList(DevourAction.this.actionEntry);
                }
                return null;
            }
        };
    }
    
    public ActionPerformer getActionPerformer() {
        return new ActionPerformer() {
            public short getActionId() {
                return DevourAction.actionId;
            }
            
            public boolean action(final Action act, final Creature performer, final Item target, final short action, final float counter) {
                return DevourAction.this.devour(act, performer, target, counter);
            }
            
            public boolean action(final Action act, final Creature performer, final Item source, final Item target, final short action, final float counter) {
                return this.action(act, performer, target, action, counter);
            }
        };
    }
    
    static boolean isDevourableCorpse(final Item corpse) {
        return !corpse.getName().toLowerCase().contains(" zombie ") && !corpse.getName().contains("bloodless husk") && !corpse.getModelName().contains("towerguard") && !corpse.getModelName().startsWith("model.corpse.guard") && !corpse.getModelName().startsWith("model.corpse.salesman") && !corpse.getModelName().startsWith("model.corpse.human");
    }
    
    private boolean devour(final Action act, final Creature performer, final Item target, final float counter) {
        final boolean isSmallRat = target.getTemplateId() == SmallRat.getId();
        long corpseAge = 0L;
        if (!(performer instanceof Player)) {
            return true;
        }
        if (!Vampires.isHalfOrFullVampire(performer.getWurmId())) {
            return true;
        }
        if (!isDevourableCorpse(target)) {
            performer.getCommunicator().sendNormalServerMessage("Sadly, little nourishment there.");
            return true;
        }
        if (target.getTopParentOrNull() != performer.getInventory() && !Methods.isActionAllowed(performer, (short)120, target)) {
            performer.getCommunicator().sendNormalServerMessage("You are not allowed to do that here.");
            return true;
        }
        if (!MethodsItems.isLootableBy(performer, target)) {
            performer.getCommunicator().sendNormalServerMessage("You are not allowed to do that.");
            return true;
        }
        try {
            if (counter == 1.0f) {
                final int tmpTime = 30;
                performer.getCurrentAction().setTimeLeft(tmpTime);
                performer.sendActionControl("Feeding", true, tmpTime);
                performer.getCommunicator().sendNormalServerMessage("You pull the corpse to your mouth to suck the soul from it...");
                return false;
            }
            if (counter * 10.0f <= act.getTimeLeft()) {
                return false;
            }
        }
        catch (NoSuchActionException e) {
            performer.getCommunicator().sendNormalServerMessage("Hrrm. Did not seem to help at all.");
            return true;
        }
        if (!isSmallRat && BloodlessHusk.isBloodlessHusk(target)) {
            Mod.actionNotify(performer, "You can't get anything nourishing from a bloodless husk. It is ... bloodless.", "%NAME lets out a snarl, frowning with disappointment.", "You hear a snarl, seemingly from out of nowhere.");
            return true;
        }
        if (!isSmallRat && target.isButchered()) {
            Mod.actionNotify(performer, "You take a quick peek at the remains and realize that you will not get much nourishment from this.", "%NAME eagerly looks at the butchered remains, but come to %HIS senses.", "You hear a soft snarl from the direction of the butchered remains.");
            return true;
        }
        if (isSmallRat) {
            corpseAge = 1L;
        }
        else {
            corpseAge = WurmCalendar.getCurrentTime() - target.creationDate;
        }
        final Skills ss = performer.getSkills();
        final double bloodLust = ss.getSkillOrLearn(2147483641).getKnowledge();
        final double anatomy = ss.getSkillOrLearn(2147483638).getKnowledge();
        long ageMod = corpseAge / 60L;
        if (ageMod < 10L) {
            ageMod = 100L;
        }
        else if (ageMod < 20L) {
            ageMod = 90L;
        }
        else if (ageMod < 30L) {
            ageMod = 80L;
        }
        else if (ageMod < 40L) {
            ageMod = 60L;
        }
        else if (ageMod < 50L) {
            ageMod = 40L;
        }
        else if (ageMod < 60L) {
            ageMod = 30L;
        }
        else if (ageMod < 70L) {
            ageMod = 20L;
        }
        else if (ageMod < 80L) {
            ageMod = 10L;
        }
        else if (ageMod < 120L) {
            ageMod = 5L;
        }
        else {
            if (ageMod >= 480L) {
                performer.getCommunicator().sendNormalServerMessage("Upon closer inspection, that corpse is just too old to give you any nourishment.");
                return true;
            }
            ageMod = 1L;
        }
        int feedStrength = (int)(bloodLust + anatomy + ageMod);
        if (bloodLust < 15.0 && feedStrength > 79) {
            feedStrength = 79;
        }
        if (Vampires.isHalfVampire(performer.getWurmId())) {
            feedStrength *= (int)0.4;
        }
        Mod.actionNotify(performer, "It satisfies your vampiric blood lust temporarily. " + ((feedStrength < 100) ? "You gain some little nourishment from the dead, cooling blood." : "You are strengthened by devouring its soul."), "%NAME sucks the life out of a corpse, tearing its throat out in the process!", "A shadowy form sucks the life out of a corpse, tearing its throat out in the process!");
        final CRC32 crc = new CRC32();
        crc.update(target.getName().getBytes());
        final long corpseHash = crc.getValue();
        if (isSmallRat) {
            Items.destroyItem(target.getWurmId());
        }
        else {
            BloodlessHusk.setBloodSucker(target, performer.getWurmId());
            final VolaTile t = Zones.getTileOrNull(target.getTilePos(), target.isOnSurface());
            target.setName(target.getName().replace("corpse", "bloodless husk"));
            if (t != null) {
                t.renameItem(target);
            }
            target.setButchered();
        }
        final boolean success = this.feed(performer, feedStrength);
        if (!success) {
            performer.getCommunicator().sendNormalServerMessage("Uh oh. Something is off with that one, you spit it all out.");
            return true;
        }
        if (bloodLust >= 20.0) {
            addTimedAffinityFromBonus(performer, corpseHash);
        }
        if (bloodLust >= 70.0) {
            addTimedAffinityFromBonus(performer, corpseHash + 10L);
        }
        if (bloodLust >= 90.0) {
            addTimedAffinityFromBonus(performer, corpseHash + 20L);
        }
        if (bloodLust >= 95.0) {
            addTimedAffinityFromBonus(performer, corpseHash + 30L);
        }
        performer.getSkills().getSkillOrLearn(2147483638).skillCheck(1.0, 0.0, false, 1.0f);
        Achievements.triggerAchievement(performer.getWurmId(), VampAchievements.FEED);
        return true;
    }
    
    public static void addTimedAffinityFromBonus(final Creature creature, final long corpseHash) {
        if (!creature.isPlayer()) {
            return;
        }
        int ibonus = 122;
        ibonus += (int)(corpseHash & 0xFFL);
        ibonus += (int)(corpseHash >>> 8 & 0xFFL);
        ibonus += (int)(corpseHash >>> 16 & 0xFFL);
        ibonus += (int)(corpseHash >>> 24 & 0xFFL);
        ibonus += (int)(corpseHash >>> 32 & 0xFFL);
        ibonus += (int)(corpseHash >>> 40 & 0xFFL);
        ibonus += (int)(corpseHash >>> 48 & 0xFFL);
        ibonus += (int)(corpseHash >>> 56 & 0xFFL);
        final SkillTemplate skillTemplate = SkillSystem.getSkillTemplateByIndex(ibonus = (ibonus & 0xFF) % SkillSystem.getNumberOfSkillTemplates());
        if (skillTemplate == null) {
            return;
        }
        final int skillId = skillTemplate.getNumber();
        final int duration = 7200;
        final AffinitiesTimed at = AffinitiesTimed.getTimedAffinitiesByPlayer(creature.getWurmId(), true);
        at.add(skillId, (long)duration);
        creature.getCommunicator().sendNormalServerMessage("You realize that you have more of an insight about " + skillTemplate.getName().toLowerCase() + "!", (byte)2);
        at.sendTimedAffinity(creature, skillTemplate.getNumber());
    }
    
    private boolean feed(final Creature p, final int feedStr) {
        int nutr = 0;
        int food = 0;
        int stam = 0;
        int lust = 0;
        int heal = feedStr / 3;
        final boolean isHalfVamp = Vampires.isHalfVampire(p.getWurmId());
        if (isHalfVamp && heal > 0) {
            heal *= (int)0.4;
        }
        if (feedStr < 40) {
            nutr = 1;
            food = 1;
            stam = 5;
            lust = 5;
        }
        else if (feedStr < 60) {
            nutr = 2;
            food = 2;
            stam = 8;
            lust = 8;
        }
        else if (feedStr < 80) {
            nutr = 4;
            food = 4;
            stam = 12;
            lust = 12;
        }
        else if (feedStr < 100) {
            nutr = 8;
            food = 8;
            stam = 16;
            lust = 16;
        }
        else if (feedStr < 120) {
            nutr = 12;
            food = 12;
            stam = 12;
            lust = 15;
        }
        else if (feedStr < 140) {
            nutr = 12;
            food = 12;
            stam = 12;
            lust = 20;
        }
        else if (feedStr < 160) {
            nutr = 15;
            food = 15;
            stam = 20;
            lust = 17;
        }
        else if (feedStr < 180) {
            nutr = 18;
            food = 18;
            stam = 20;
            lust = 25;
        }
        else if (feedStr < 200) {
            nutr = 20;
            food = 20;
            stam = 20;
            lust = 30;
        }
        else if (feedStr < 220) {
            nutr = 20;
            food = 20;
            stam = 20;
            lust = 35;
        }
        else if (feedStr < 240) {
            nutr = 20;
            food = 20;
            stam = 20;
            lust = 40;
        }
        else if (feedStr < 260) {
            nutr = 20;
            food = 20;
            stam = 20;
            lust = 45;
        }
        else if (feedStr < 280) {
            nutr = 20;
            food = 20;
            stam = 20;
            lust = 60;
        }
        else {
            nutr = 50;
            food = 50;
            stam = 50;
            lust = 100;
        }
        final double healMod = 50000.0;
        final Wound[] w;
        if (heal > 0.0f && healMod * heal / (p.isChampion() ? 1000.0f : 500.0f) > 500.0 && p.getBody() != null && p.getBody().getWounds() != null && (w = p.getBody().getWounds().getWounds()).length > 0) {
            final int reduceSeverity = -(int)(healMod * heal / (p.isChampion() ? 1000.0f : ((p.getCultist() != null && p.getCultist().healsFaster()) ? 250.0f : 500.0f)));
            int healedWounds = 0;
            Wound[] array;
            for (int length = (array = w).length, i = 0; i < length; ++i) {
                final Wound wound = array[i];
                if (healedWounds >= 3) {
                    break;
                }
                wound.modifySeverity(reduceSeverity);
                ++healedWounds;
            }
        }
        final CreatureStatus cs = p.getStatus();
        float modifiedNut = cs.getNutritionlevel() + nutr / 100.0f;
        float ccfp = 0.0f;
        if (modifiedNut > 0.5f && isHalfVamp) {
            modifiedNut = 0.5f;
        }
        if (isHalfVamp) {
            ccfp = 0.1f;
        }
        else {
            ccfp = modifiedNut * 3.0f;
        }
        CreaturesProxy.setHunNutSta(p, Math.max(0, cs.getHunger() - 655 * food), Math.min(0.99f, modifiedNut), Math.min(65535, cs.getStamina() + 655 * stam), Math.min(0.999f, ccfp));
        final Skill bl = p.getSkills().getSkillOrLearn(2147483641);
        bl.setKnowledge(bl.getKnowledge() - lust, false);
        return true;
    }
}
