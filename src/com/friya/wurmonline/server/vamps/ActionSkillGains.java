// 
// Decompiled by Procyon v0.5.30
// 

package com.friya.wurmonline.server.vamps;

import com.wurmonline.server.Server;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.skills.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ActionSkillGains
{
    private static Logger logger;
    private static HashMap<Integer, ActionSkillGain> skillGains;
    private static SimSkills simSkills;
    
    static {
        ActionSkillGains.logger = Logger.getLogger(ActionSkillGains.class.getName());
        ActionSkillGains.skillGains = new HashMap<Integer, ActionSkillGain>();
    }
    
    static void onServerStarted() {
        VampSkills.learnSkills(ActionSkillGains.simSkills = new SimSkills(null));
        ActionSkillGains.simSkills.priest = true;
        createSimulatedSkillGains();
    }
    
    public static ActionSkillGain getSkill(final int skillNumber) {
        return ActionSkillGains.skillGains.get(skillNumber);
    }
    
    private static Skill getSimulatedSkill(final int skillNumber) {
        final Skill simSkill = new SimSkill(-152L, ActionSkillGains.simSkills, skillNumber, 1.0, 99.9999008178711, 1479035543351L);
        int[] uniqueDependencies;
        for (int length = (uniqueDependencies = simSkill.getUniqueDependencies()).length, i = 0; i < length; ++i) {
            final int p = uniqueDependencies[i];
            ActionSkillGains.simSkills.learn(p, 30.0f, false);
        }
        return simSkill;
    }
    
    private static boolean isSkillExcluded(final int s) {
        return s == 10053 || s == 10054 || s == 10055 || s == 1023 || s == 10052 || s == 2147483644 || s == 1 || s == 2 || s == 3 || s == 10075 || s == 10076 || s == 2147483641;
    }
    
    private static float getSkillDifficultyMultiplier(final int skillNumber) {
        switch (skillNumber) {
            case 1007:
            case 1009: {
                return 1.2f;
            }
            case 1008:
            case 1016:
            case 10044:
            case 10073: {
                return 0.5f;
            }
            case 10067: {
                return 0.3f;
            }
            case 100:
            case 101:
            case 102:
            case 103:
            case 104:
            case 105:
            case 106: {
                return 0.01f;
            }
            case 2147483645: {
                return 0.006f;
            }
            case 10076:
            case 10086: {
                return 0.003f;
            }
            default: {
                return 1.0f;
            }
        }
    }
    
    static int randomGaussian(final int max, final float scale) {
        double v;
        do {
            v = Math.abs(Server.rand.nextGaussian() / scale);
        } while (v >= 1.0);
        return (int)(v * max);
    }
    
    private static ArrayList<Skill> getSortedSkills(final Skill[] skills) {
        final ArrayList<Skill> s = new ArrayList<Skill>();
        Collections.addAll(s, skills);
        s.sort(Comparator.comparing(u -> u.getKnowledge()).reversed());
        return s;
    }
    
    public static ActionSkillGain getRandomSkillToPunish(final Creature c, final int highChanceSkillNum) {
        if (Server.rand.nextInt(100) < 40) {
            return getSkill(highChanceSkillNum);
        }
        return getRandomSkillToPunish(c);
    }
    
    public static ActionSkillGain getRandomHighSkillToPunish(final Creature c) {
        return getRandomHighSkillToPunish(c, 0);
    }
    
    public static ActionSkillGain getRandomHighSkillToPunish(final Creature c, final int highChanceSkillNum) {
        if (highChanceSkillNum > 0 && Server.rand.nextInt(100) < 20) {
            return getSkill(highChanceSkillNum);
        }
        final ArrayList<Skill> targetSkills = getSortedSkills(c.getSkills().getSkills());
        int i = 0;
        while (i++ <= 200) {
            final Skill skill = targetSkills.get(randomGaussian(targetSkills.size(), 3.5f));
            if (!isSkillExcluded(skill.getNumber()) && skill.getKnowledge() > 1.0) {
                return getSkill(skill.getNumber());
            }
        }
        return null;
    }
    
    public static ActionSkillGain getRandomSkillToPunish(final Creature c) {
        final Skill[] targetSkills = c.getSkills().getSkills();
        return getRandomSkillToPunish(c, targetSkills);
    }
    
    private static ActionSkillGain getRandomSkillToPunish(final Creature c, final Skill[] targetSkills) {
        int i = 200;
        while (i-- > 0) {
            final Skill skill = targetSkills[Server.rand.nextInt(targetSkills.length)];
            if (!isSkillExcluded(skill.getNumber()) && skill.getKnowledge() > 1.0) {
                return getSkill(skill.getNumber());
            }
        }
        return null;
    }
    
    private static void createSimulatedSkillGains() {
        ActionSkillGains.logger.log(Level.INFO, "createSimulatedSkillGains() starting...");
        final SkillTemplate[] templates = SkillSystem.getAllSkillTemplates();
        SkillTemplate[] array;
        for (int length = (array = templates).length, i = 0; i < length; ++i) {
            final SkillTemplate tpl = array[i];
            if (!isSkillExcluded(tpl.getNumber())) {
                final Skill skill = getSimulatedSkill(tpl.getNumber());
                final ActionSkillGain asg = new ActionSkillGain(skill.getNumber(), skill.getName(), getSkillDifficultyMultiplier(tpl.getNumber()));
                asg.simulate(skill);
                asg.getModifiedLostActionCount(90.0, 1000, 0.25f);
                ActionSkillGains.skillGains.put(skill.getNumber(), asg);
            }
        }
        ActionSkillGains.logger.log(Level.INFO, "createSimulatedSkillGains() done");
    }
}
