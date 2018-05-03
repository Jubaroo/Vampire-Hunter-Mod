// 
// Decompiled by Procyon v0.5.30
// 

package com.friya.wurmonline.server.vamps;

import com.friya.wurmonline.server.vamps.items.AltarOfSouls;
import com.wurmonline.math.TilePos;
import com.wurmonline.server.Server;
import com.wurmonline.server.WurmCalendar;
import com.wurmonline.server.behaviours.Terraforming;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.players.Achievements;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zones;

import java.util.logging.Level;
import java.util.logging.Logger;

public class BloodLust
{
    private static Logger logger;
    
    static {
        BloodLust.logger = Logger.getLogger(BloodLust.class.getName());
    }
    
    public static void onItemTemplatesCreated() {
        BloodLust.logger.log(Level.INFO, "preInit completed");
    }
    
    public static void setBloodlust(final Player p, final double amount) {
        p.getSkills().getSkillOrLearn(2147483641).setKnowledge(amount, false);
        BloodLust.logger.log(Level.INFO, "setBloodlust() to " + amount + " for " + p.getName());
    }
    
    public static void poll(final Player p) {
        final Skill bl = p.getSkills().getSkillOrLearn(2147483641);
        final float currentBloodlust = (float)bl.getKnowledge();
        if (p.isDead()) {
            if (currentBloodlust > 60.0f) {
                setBloodlust(p, 60.0);
            }
            return;
        }
        float lMod = 5.0f;
        if (bl.affinity > 0) {
            lMod -= bl.affinity * 0.1f;
        }
        if (p.hasSleepBonus()) {
            lMod *= 0.5f;
        }
        if (currentBloodlust <= 5.0f && Server.getSecondsUptime() % 40 == 0) {
            p.getCommunicator().sendNormalServerMessage("Your vampiric bloodlust is beginning to grow.");
        }
        else if (currentBloodlust >= 40.0f && currentBloodlust <= 60.0f && Server.getSecondsUptime() % 600 == 0) {
            p.getCommunicator().sendNormalServerMessage("You feel the need to feed once again.");
        }
        else if (currentBloodlust > 60.0f && currentBloodlust <= 70.0f && Server.getSecondsUptime() % 500 == 0) {
            p.getCommunicator().sendNormalServerMessage("The bloodlust is neglected. You must feed soon.");
        }
        else if (currentBloodlust > 70.0f && currentBloodlust <= 80.0f && Server.getSecondsUptime() % 500 == 0) {
            p.getCommunicator().sendNormalServerMessage("Vampiric urges gnaw at your gut.");
        }
        else if (currentBloodlust > 80.0f && currentBloodlust <= 95.0f && Server.getSecondsUptime() % 500 == 0) {
            p.getCommunicator().sendNormalServerMessage("Your bloodlust tears at your soul with a vengeance. It must be sated!");
        }
        else if (currentBloodlust > 95.0f && currentBloodlust <= 98.0f && Server.getSecondsUptime() % 180 == 0) {
            final Item altar = getAltarProtection(p);
            if (altar != null) {
                p.getCommunicator().sendNormalServerMessage("Your vampiric bloodlust begins feeding on souls in the altar next to you!");
                Achievements.triggerAchievement(p.getWurmId(), VampAchievements.ALTAR_SOULFEED);
                if (Server.rand.nextInt(5) == 0) {
                    AltarOfSouls.setCharge(altar, (byte)(AltarOfSouls.getCharge(altar) - 1));
                }
            }
            else {
                p.getCommunicator().sendNormalServerMessage("The vampiric bloodlust begins feeding slowly on your soul!");
                Achievements.triggerAchievement(p.getWurmId(), VampAchievements.SOULFEED);
                damagePlayer(p, 3500.0f);
            }
        }
        else if (currentBloodlust > 98.0f && currentBloodlust <= 99.0f && Server.getSecondsUptime() % 30 == 0) {
            final Item altar = getAltarProtection(p);
            if (altar != null) {
                p.getCommunicator().sendNormalServerMessage("Your vampiric bloodlust is feeding slowly on souls in the altar next to you!");
                if (Server.rand.nextInt(5) == 0) {
                    AltarOfSouls.setCharge(altar, (byte)(AltarOfSouls.getCharge(altar) - 1));
                }
            }
            else {
                p.getCommunicator().sendNormalServerMessage("The vampiric bloodlust is feeding slowly on YOUR soul! You must feed soon or waste away into oblivion.");
                damagePlayer(p, 3500.0f);
            }
        }
        else if (currentBloodlust > 99.0f && Server.getSecondsUptime() % 30 == 0) {
            final Item altar = getAltarProtection(p);
            if (altar != null) {
                p.getCommunicator().sendNormalServerMessage("Your vampiric bloodlust is feeding on souls in the altar next to you!");
                if (Server.rand.nextInt(5) == 0) {
                    AltarOfSouls.setCharge(altar, (byte)(AltarOfSouls.getCharge(altar) - 1));
                }
            }
            else {
                p.getCommunicator().sendNormalServerMessage("The vampiric bloodlust is feeding on YOUR soul! You MUST feed now.");
                damagePlayer(p, 7000.0f);
            }
        }
        if (Server.getSecondsUptime() % 27 == 0) {
            float blIncrease = 0.0f;
            float maxTick = 2.0f;
            if (Vampires.isHalfVampire(p.getWurmId())) {
                blIncrease = lMod / 2.0f * (100.0f / (Math.max(1.0f, currentBloodlust) * 30.0f));
                maxTick = 1.5f;
            }
            else {
                blIncrease = lMod * (100.0f / (Math.max(1.0f, currentBloodlust) * 30.0f));
            }
            p.setSkill(2147483641, currentBloodlust + Math.min(blIncrease, maxTick));
        }
    }
    
    private static void damagePlayer(final Player p, float amount) {
        if (WurmCalendar.isNight()) {
            amount *= 0.9;
        }
        p.addWoundOfType(null, (byte)6, 1, true, 0.0f, true, (double)amount, 0.0f, 0.0);
    }
    
    private static Item getAltarProtection(final Player p) {
        if (p.isOnSurface() || !Terraforming.isFlat(p.getTileX(), p.getTileY(), p.isOnSurface(), 0)) {
            return null;
        }
        final Item foundAltar = getNearbyAltarOfSouls(p);
        if (foundAltar == null) {
            return null;
        }
        if (!AltarOfSouls.isCleanArea(foundAltar)) {
            p.getCommunicator().sendNormalServerMessage("The area around Altar of Souls is too cluttered with items.");
            return null;
        }
        if (!AltarOfSouls.isCharged(foundAltar)) {
            p.getCommunicator().sendNormalServerMessage("The altar demand new souls.");
            return null;
        }
        return foundAltar;
    }
    
    private static Item getNearbyAltarOfSouls(final Player p) {
        final TilePos t = p.getTilePos();
        Item[] tmpItems = null;
        VolaTile tmpTile = null;
        for (int x = -1; x <= 1; ++x) {
            for (int y = -1; y <= 1; ++y) {
                tmpTile = Zones.getTileOrNull(t.x + x, t.y + y, false);
                if (tmpTile != null) {
                    tmpItems = tmpTile.getItems();
                    Item[] array;
                    for (int length = (array = tmpItems).length, i = 0; i < length; ++i) {
                        final Item item = array[i];
                        if (item.getTemplateId() == AltarOfSouls.getId()) {
                            return item;
                        }
                    }
                }
            }
        }
        return null;
    }
}
