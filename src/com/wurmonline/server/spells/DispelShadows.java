// 
// Decompiled by Procyon v0.5.30
// 

package com.wurmonline.server.spells;

import com.wurmonline.server.behaviours.ActionEntry;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.zones.Zone;
import com.wurmonline.server.zones.Zones;
import org.gotti.wurmunlimited.modsupport.actions.ModActions;

public class DispelShadows extends ReligiousSpell
{
    public static int actionId;
    
    public static short getActionId() {
        return (short)DispelShadows.actionId;
    }
    
    public DispelShadows() {
        super("Dispel Shadows", DispelShadows.actionId = ModActions.getNextActionId(), 40, 25, 25, 30, 60000L);
        this.targetTile = true;
        this.description = "dispel stealth of creatures nearby";
        final ActionEntry actionEntry = ActionEntry.createEntry((short)this.number, this.name, "enchanting", new int[] { 2, 36, 48 });
        ModActions.registerAction(actionEntry);
    }
    
    void doEffect(final Skill castSkill, final double power, final Creature performer, final int tilex, final int tiley, final int layer, final int heightOffset) {
        performer.getCommunicator().sendNormalServerMessage("You force all hidden creatures around you into plain sight.");
        final int sx = Zones.safeTileX(performer.getTileX() - 30 - performer.getNumLinks() * 5);
        final int sy = Zones.safeTileY(performer.getTileY() - 30 - performer.getNumLinks() * 5);
        final int ex = Zones.safeTileX(performer.getTileX() + 30 + performer.getNumLinks() * 5);
        final int ey = Zones.safeTileY(performer.getTileY() + 30 + performer.getNumLinks() * 5);
        int dispelled = 0;
        Zone[] zones;
        Zone[] array;
        for (int length = (array = (zones = Zones.getZonesCoveredBy(sx, sy, ex, ey, performer.isOnSurface()))).length, i = 0; i < length; ++i) {
            final Zone lZone = array[i];
            Creature[] crets;
            Creature[] array2;
            for (int length2 = (array2 = (crets = lZone.getAllCreatures())).length, j = 0; j < length2; ++j) {
                final Creature cret = array2[j];
                if (cret.isStealth()) {
                    ++dispelled;
                    if (cret.isPlayer()) {
                        cret.getCommunicator().sendNormalServerMessage(String.valueOf(performer.getName()) + " casted dispel shadows, you are no longer hidden!");
                    }
                    cret.setStealth(false);
                }
            }
        }
        final Skill perception = performer.getSkills().getSkillOrLearn(2147483639);
        perception.skillCheck(1.0, 0.0, false, 1.0f);
        performer.getCommunicator().sendNormalServerMessage("Your magic brought " + ((dispelled > 0) ? dispelled : "no") + " creatures out of their stealth.");
    }
}
