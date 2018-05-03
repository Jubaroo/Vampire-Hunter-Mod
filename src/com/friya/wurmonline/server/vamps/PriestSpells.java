// 
// Decompiled by Procyon v0.5.30
// 

package com.friya.wurmonline.server.vamps;

import com.wurmonline.server.deities.Deities;
import com.wurmonline.server.deities.Deity;
import com.wurmonline.server.spells.DispelShadows;
import com.wurmonline.server.spells.PinpointHumanoid;
import com.wurmonline.server.spells.Spell;
import com.wurmonline.server.spells.Spells;
import org.gotti.wurmunlimited.modloader.ReflectionUtil;

import java.lang.reflect.InvocationTargetException;

public class PriestSpells
{
    static void onServerStarted() {
        addSpell(new DispelShadows());
        addSpell(new PinpointHumanoid());
    }
    
    private static void addSpell(final Spell spell) {
        try {
            ReflectionUtil.callPrivateMethod(Spells.class, ReflectionUtil.getMethod(Spells.class, "addSpell"), spell);
        }
        catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException ex2) {
            final Exception e = ex2;
            throw new RuntimeException(e);
        }
        Deity[] deities;
        for (int length = (deities = Deities.getDeities()).length, i = 0; i < length; ++i) {
            final Deity deity = deities[i];
            deity.addSpell(spell);
        }
    }
}
