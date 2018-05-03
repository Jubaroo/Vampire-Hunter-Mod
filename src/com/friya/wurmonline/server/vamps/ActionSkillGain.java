// 
// Decompiled by Procyon v0.5.30
// 

package com.friya.wurmonline.server.vamps;

import com.wurmonline.server.skills.Skill;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ActionSkillGain
{
    private static Logger logger;
    private int id;
    private String name;
    private float difficultyMultiplier;
    HashMap<Integer, Double> gains;
    
    static {
        ActionSkillGain.logger = Logger.getLogger(ActionSkillGain.class.getName());
    }
    
    public ActionSkillGain(final int skillId, final String skillName) {
        this.id = -1;
        this.difficultyMultiplier = 1.0f;
        this.gains = new HashMap<Integer, Double>();
        this.setId(skillId);
        this.setName(skillName);
    }
    
    public ActionSkillGain(final int skillId, final String skillName, final float difficultyMultiplier) {
        this.id = -1;
        this.difficultyMultiplier = 1.0f;
        this.gains = new HashMap<Integer, Double>();
        this.setId(skillId);
        this.setName(skillName);
        this.setDifficultyMultiplier(difficultyMultiplier);
    }
    
    public void simulate(final Skill skill) {
        boolean gained = false;
        int attempts = 0;
        for (int n = 1; n <= 100; ++n) {
            if (n == 100) {
                skill.setKnowledge(99.99, false);
            }
            else {
                skill.setKnowledge(n + 0.5, false);
            }
            double before;
            double diff;
            for (gained = false, attempts = 100; !gained && attempts-- > 0; gained = true) {
                before = skill.getKnowledge();
                skill.skillCheck(50.0, 0.0, false, 1.0f);
                diff = skill.getKnowledge() - before;
                if (diff > 0.0) {
                    this.add(n, diff);
                }
            }
            if (attempts == 0) {
                ActionSkillGain.logger.log(Level.SEVERE, "Could not get skill gain of " + skill.getName() + " it could be because it is on cooldown");
                throw new RuntimeException("Could not get a skill gain out of " + skill.getName());
            }
        }
    }
    
    public void add(final int level, final double gain) {
        this.gains.put(level, gain);
    }
    
    private double getActionCountForLoss(final double skillLevel, final double skillLossAmount) {
        double actionCount = 0.0;
        ActionSkillGain.logger.log(Level.FINE, "getActionCountForLoss(): " + skillLevel + ", " + skillLossAmount + " in skill " + this.getName());
        if (skillLossAmount < 0.0 || skillLossAmount > 99.0 || skillLevel < 1.0) {
            return 0.0;
        }
        double tmpLoss = 0.0;
        for (double endLevel = Math.max(1.0, skillLevel - skillLossAmount), currentLevel = Math.max(1.0, skillLevel); currentLevel >= endLevel; --currentLevel) {
            if (currentLevel < 1.0) {
                break;
            }
            tmpLoss = this.gains.get((int)currentLevel);
            if (endLevel - currentLevel > 0.0) {
                actionCount += (endLevel - currentLevel) / tmpLoss;
                break;
            }
            actionCount += 1.0 / tmpLoss;
        }
        return actionCount;
    }
    
    private double getActionCountForGain(final double skillLevel, final double skillGainAmount) {
        double actionCount = 0.0;
        if (skillGainAmount < 0.0 || skillGainAmount > 99.0 || skillLevel < 1.0) {
            return 0.0;
        }
        double tmpGain = 0.0;
        final double endLevel = skillLevel + skillGainAmount;
        double tmpLevel = skillLevel;
        while (true) {
            tmpGain = this.gains.get((int)tmpLevel);
            if (endLevel - tmpLevel < 1.0) {
                actionCount += (endLevel - tmpLevel) / tmpGain;
                break;
            }
            actionCount += 1.0 / tmpGain;
            if (tmpLevel > endLevel) {
                break;
            }
            if (tmpLevel >= 100.0) {
                break;
            }
            ++tmpLevel;
        }
        return actionCount;
    }
    
    private double getSkillGainOrLossForActionCount(final double skillLevel, final int actionCount, final boolean isLoss) {
        double ret = 0.0;
        double tmpLevel = skillLevel;
        int actionsStillNeeded = actionCount;
        while (true) {
            final double tmpGain = this.gains.get((int)tmpLevel);
            final double tmpActionCount = 1.0 / tmpGain;
            if (actionsStillNeeded - tmpActionCount <= 0.0) {
                ret += actionsStillNeeded / tmpActionCount;
                break;
            }
            ++ret;
            if (isLoss) {
                --tmpLevel;
                if (tmpLevel <= 1.0) {
                    break;
                }
            }
            else {
                ++tmpLevel;
                if (tmpLevel > 100.0) {
                    break;
                }
            }
            actionsStillNeeded -= (int)tmpActionCount;
        }
        return ret;
    }
    
    public int getRawGainedActionCount(final double skillLevel, final double skillGainAmount) {
        return (int)Math.max(1.0, this.getActionCountForGain(skillLevel, skillGainAmount));
    }
    
    public int getRawLostActionCount(final double skillLevel, final double skillLossAmount) {
        return (int)Math.max(1.0, this.getActionCountForLoss(skillLevel, skillLossAmount));
    }
    
    public double getRawSkillGainForActionCount(final double skillLevel, final int actionCount) {
        return this.getSkillGainOrLossForActionCount(skillLevel, actionCount, false);
    }
    
    public double getRawSkillLossForActionCount(final double skillLevel, final int actionCount) {
        return this.getSkillGainOrLossForActionCount(skillLevel, actionCount, true);
    }
    
    public double getGainedSkillLevelForActionCount(final double skillLevel, final int actionCount) {
        return skillLevel + this.getRawSkillGainForActionCount(skillLevel, actionCount);
    }
    
    public double getLostSkillLevelForActionCount(final double skillLevel, final int actionCount) {
        return skillLevel - this.getRawSkillLossForActionCount(skillLevel, actionCount);
    }
    
    public int getModifiedLostActionCount(final double skillLevel, final int actionCount, final float capSkillLossAt) {
        int modifiedActionCount = (int)(actionCount * this.getDifficultyMultiplier());
        double skillPointLoss = 0.0;
        final double cappedSkillPointLoss = skillLevel * capSkillLossAt;
        final double rawSkillPointLoss = this.getRawSkillLossForActionCount(skillLevel, modifiedActionCount);
        if (rawSkillPointLoss > cappedSkillPointLoss) {
            skillPointLoss = cappedSkillPointLoss;
        }
        else {
            skillPointLoss = rawSkillPointLoss;
        }
        modifiedActionCount = Math.min(modifiedActionCount, this.getRawLostActionCount(skillLevel, skillPointLoss));
        final DecimalFormat df = new DecimalFormat("#.#######");
        if (modifiedActionCount == (int)(actionCount * this.getDifficultyMultiplier())) {
            ActionSkillGain.logger.log(Level.FINE, String.valueOf(this.getName()) + " (" + this.getDifficultyMultiplier() + " difficulty) " + " at skill " + skillLevel + ", kept lost action count at " + modifiedActionCount + " (" + actionCount + ")" + " which means a skill loss of " + df.format(skillPointLoss));
        }
        else {
            ActionSkillGain.logger.log(Level.FINE, String.valueOf(this.getName()) + " (" + this.getDifficultyMultiplier() + " difficulty) " + " at skill " + skillLevel + ", modified lost action count from " + (int)(actionCount * this.getDifficultyMultiplier()) + " (" + actionCount + ") to " + modifiedActionCount + " which means a skill loss of " + df.format(skillPointLoss) + " pts instead of " + df.format(rawSkillPointLoss));
        }
        return modifiedActionCount;
    }
    
    public int getId() {
        return this.id;
    }
    
    private void setId(final int id) {
        this.id = id;
    }
    
    public String getName() {
        return this.name;
    }
    
    private void setName(final String name) {
        this.name = name;
    }
    
    public float getDifficultyMultiplier() {
        return this.difficultyMultiplier;
    }
    
    private void setDifficultyMultiplier(final float difficultyMultiplier) {
        this.difficultyMultiplier = difficultyMultiplier;
    }
}
