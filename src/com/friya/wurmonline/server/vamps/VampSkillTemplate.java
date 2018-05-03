// 
// Decompiled by Procyon v0.5.30
// 

package com.friya.wurmonline.server.vamps;

public class VampSkillTemplate
{
    public int number;
    public String name;
    public float difficulty;
    public int[] dependencies;
    public long decayTime;
    public short type;
    public boolean fightingSkill;
    public boolean ignoreEnemy;
    public int[] skillUpOn;
    
    VampSkillTemplate(final int aNumber, final String aName, final float aDifficulty, final int[] aDependencies, final long aDecayTime, final short aType, final boolean aFightingSkill, final boolean aIgnoreEnemy, final int[] skillUpOn) {
        this.number = aNumber;
        this.name = aName;
        this.difficulty = aDifficulty;
        this.dependencies = aDependencies;
        this.decayTime = aDecayTime;
        this.type = aType;
        this.fightingSkill = aFightingSkill;
        this.ignoreEnemy = aIgnoreEnemy;
        this.skillUpOn = skillUpOn;
    }
}
