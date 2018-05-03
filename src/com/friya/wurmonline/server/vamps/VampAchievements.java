// 
// Decompiled by Procyon v0.5.30
// 

package com.friya.wurmonline.server.vamps;

import com.wurmonline.server.players.Achievement;
import com.wurmonline.server.players.AchievementTemplate;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

public class VampAchievements
{
    private static Logger logger;
    public static int STAKINGS;
    public static int STAKE_A_VAMPIRE;
    public static int STAKE_5_VAMPIRES;
    public static int POUCHES;
    public static int BUY_A_POUCH;
    public static int STAKED;
    public static int GET_STAKED;
    public static int FEED;
    public static int HALFVAMP;
    public static int FULLVAMP;
    public static int SOULFEED;
    public static int ALTAR_SOULFEED;
    
    static {
        VampAchievements.logger = Logger.getLogger(VampAchievements.class.getName());
        VampAchievements.STAKINGS = 818801;
        VampAchievements.STAKE_A_VAMPIRE = 818802;
        VampAchievements.STAKE_5_VAMPIRES = 818803;
        VampAchievements.POUCHES = 818821;
        VampAchievements.BUY_A_POUCH = 818822;
        VampAchievements.STAKED = 818831;
        VampAchievements.GET_STAKED = 818832;
        VampAchievements.FEED = 818841;
        VampAchievements.HALFVAMP = 818851;
        VampAchievements.FULLVAMP = 818852;
        VampAchievements.SOULFEED = 818853;
        VampAchievements.ALTAR_SOULFEED = 818854;
    }
    
    public static void onServerStarted() {
        VampAchievements.logger.log(Level.INFO, "Creating achievements...");
        addAchievement(new AchievementTemplate(VampAchievements.STAKINGS, "Invisible:Stakings", true, 1, (byte)3, false, false, ""), null, new int[] { VampAchievements.STAKE_A_VAMPIRE, VampAchievements.STAKE_5_VAMPIRES }, new int[0]);
        addAchievement(new AchievementTemplate(VampAchievements.STAKE_A_VAMPIRE, "Stake a Vampire", false, 1, (byte)1, true, true, "Find and stake a vampire"), "This is just one, there are more of them...", new int[0], new int[0]);
        addAchievement(new AchievementTemplate(VampAchievements.STAKE_5_VAMPIRES, "Stake five vampires", false, 5, (byte)2, true, true, "Just a handful..."), "On my way to a necklace...", new int[0], new int[0]);
        addAchievement(new AchievementTemplate(VampAchievements.POUCHES, "Invisible:Pouches", true, 1, (byte)1, false, false, ""), null, new int[] { VampAchievements.BUY_A_POUCH }, new int[0]);
        addAchievement(new AchievementTemplate(VampAchievements.BUY_A_POUCH, "Wonder what's in it...", false, 1, (byte)3, true, true, "Buy a pouch from Vampire Hunter D"), "Should probably have read the instructions...", new int[0], new int[0]);
        addAchievement(new AchievementTemplate(VampAchievements.STAKED, "Invisible:Staked", true, 1, (byte)3, false, false, ""), null, new int[] { VampAchievements.GET_STAKED }, new int[0]);
        addAchievement(new AchievementTemplate(VampAchievements.GET_STAKED, "Ouch! Through the heart!", false, 1, (byte)1, true, true, "Don't."), "Luckily beaing near immortal helps...", new int[0], new int[0]);
        addAchievement(new AchievementTemplate(VampAchievements.FEED, "Ahhh!", false, 1, (byte)1, true, true, "Feed on a corpse."), "Rat or not; tasty.", new int[0], new int[0]);
        addAchievement(new AchievementTemplate(VampAchievements.HALFVAMP, "Becoming a dark one!", false, 1, (byte)1, true, true, "Become a half vampire."), "Must feed now.", new int[0], new int[0]);
        addAchievement(new AchievementTemplate(VampAchievements.FULLVAMP, "Becoming a dweller of darkness!", false, 1, (byte)1, true, true, "Become a vampire."), "Mmmm blood.", new int[0], new int[0]);
        addAchievement(new AchievementTemplate(VampAchievements.SOULFEED, "Soulfeed", false, 1, (byte)1, true, true, "Wait..."), "It fed on YOUR soul.", new int[0], new int[0]);
        addAchievement(new AchievementTemplate(VampAchievements.ALTAR_SOULFEED, "Protected Soul", false, 1, (byte)1, true, true, "Saved by the altar"), "It fed on Mr. Doe.", new int[0], new int[0]);
        VampAchievements.logger.log(Level.INFO, "Done");
    }
    
    private static void addAchievement(final AchievementTemplate tpl, final String desc, final int[] triggeredAchievements, final int[] requiredAchievements) {
        if (triggeredAchievements != null && triggeredAchievements.length > 0) {
            tpl.setAchievementsTriggered(triggeredAchievements);
        }
        if (requiredAchievements != null && requiredAchievements.length > 0) {
            tpl.setRequiredAchievements(requiredAchievements);
        }
        if (desc != null) {
            tpl.setDescription(desc);
        }
        try {
            final Method m = Achievement.class.getDeclaredMethod("addTemplate", AchievementTemplate.class);
            m.setAccessible(true);
            m.invoke(null, tpl);
        }
        catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException ex2) {
            final Exception ex;
            final Exception e = ex;
            throw new RuntimeException(e);
        }
    }
}
