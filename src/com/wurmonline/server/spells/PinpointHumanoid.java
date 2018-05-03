// 
// Decompiled by Procyon v0.5.30
// 

package com.wurmonline.server.spells;

import com.friya.wurmonline.server.vamps.Mod;
import com.wurmonline.server.behaviours.ActionEntry;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.questions.PinpointHumanoidQuestion;
import com.wurmonline.server.skills.Skill;
import org.gotti.wurmunlimited.modsupport.actions.ModActions;

public class PinpointHumanoid extends ReligiousSpell
{
    public static int actionId;
    
    public static short getActionId() {
        return (short)PinpointHumanoid.actionId;
    }
    
    public PinpointHumanoid() {
        super("Pinpoint Humanoid", PinpointHumanoid.actionId = ModActions.getNextActionId(), 10, Mod.isTestEnv() ? 1 : 25, 25, 30, 60000L);
        this.targetTile = true;
        this.description = "locates a humanoid in the lands. More precise than locate soul";
        final ActionEntry actionEntry = ActionEntry.createEntry((short)this.number, this.name, "enchanting", new int[] { 2, 36, 48 });
        ModActions.registerAction(actionEntry);
    }
    
    void doEffect(final Skill castSkill, final double power, final Creature performer, final int tilex, final int tiley, final int layer, final int heightOffset) {
        final PinpointHumanoidQuestion phq = new PinpointHumanoidQuestion(performer, "Pinpoint a humanoid", "Which humanoid do you wish to locate?", performer.getWurmId(), false, power);
        phq.sendQuestion();
    }
}
