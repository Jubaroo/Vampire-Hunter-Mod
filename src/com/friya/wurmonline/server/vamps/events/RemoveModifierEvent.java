// 
// Decompiled by Procyon v0.5.30
// 

package com.friya.wurmonline.server.vamps.events;

import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.SpellEffectsEnum;
import com.wurmonline.server.modifiers.DoubleValueModifier;

import java.util.logging.Level;
import java.util.logging.Logger;

public class RemoveModifierEvent extends EventOnce
{
    private static Logger logger;
    private Creature creature;
    private DoubleValueModifier modifier;
    private SpellEffectsEnum spellEffect;
    
    static {
        RemoveModifierEvent.logger = Logger.getLogger(RemoveModifierEvent.class.getName());
    }
    
    public RemoveModifierEvent(final int fromNow, final Unit unit, final Creature c, final DoubleValueModifier modifier, final SpellEffectsEnum effectEnum) {
        super(fromNow, unit);
        this.spellEffect = null;
        this.creature = c;
        this.modifier = modifier;
        this.spellEffect = effectEnum;
        RemoveModifierEvent.logger.log(Level.INFO, "RemoveModifierEvent created");
    }
    
    @Override
    public boolean invoke() {
        this.creature.getMovementScheme().removeModifier(this.modifier);
        if (this.spellEffect != null) {
            this.creature.getCommunicator().sendRemoveSpellEffect(this.spellEffect);
        }
        if (this.creature.isPlayer()) {
            this.creature.getCommunicator().sendNormalServerMessage("The crippling effect fades.");
        }
        return true;
    }
}
