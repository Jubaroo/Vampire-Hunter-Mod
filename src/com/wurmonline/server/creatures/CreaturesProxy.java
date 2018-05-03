// 
// Decompiled by Procyon v0.5.30
// 

package com.wurmonline.server.creatures;

import com.wurmonline.server.creatures.ai.ChatManager;

public class CreaturesProxy
{
    public static long getTraits(final Offspring offspring) {
        return offspring.getTraits();
    }
    
    public static long getFather(final Offspring offspring) {
        return offspring.getFather();
    }
    
    public static void setHunNutSta(final Creature creature, final int hunger, final float nutrition, final int stamina, final float ccfp) {
        final CreatureStatus cs = creature.getStatus();
        final int oldStam = cs.stamina;
        cs.hunger = hunger;
        cs.nutrition = nutrition;
        cs.stamina = stamina;
        cs.calories = ccfp;
        cs.carbs = ccfp;
        cs.fats = ccfp;
        cs.proteins = ccfp;
        cs.setChanged(true);
        cs.sendStamina();
        cs.sendStateString();
        cs.checkStaminaEffects(oldStam);
        cs.sendHunger();
    }
    
    public static Creature[] getCreaturesWithName(final String name) {
        return Creatures.getInstance().getCreaturesWithName(name);
    }
    
    public static ChatManager getChatManager(final Creature npc) {
        return npc.isNpc() ? ((Npc)npc).chatManager : null;
    }
    
    public static void deleteOffspringSettings(final long motherid) {
        Offspring.deleteSettings(motherid);
    }
}
