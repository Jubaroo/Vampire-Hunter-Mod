// 
// Decompiled by Procyon v0.5.30
// 

package com.friya.wurmonline.server.vamps.items;

import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.items.ItemTemplate;
import com.wurmonline.server.items.ItemTypes;
import org.gotti.wurmunlimited.modsupport.ItemTemplateBuilder;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Stake implements ItemTypes, MiscConstants
{
    private static Logger logger;
    private static int stakeId;
    public static byte STATUS_READY;
    public static byte STATUS_RECOVERING;
    public static byte STATUS_WIELDING;
    
    static {
        Stake.logger = Logger.getLogger(Stake.class.getName());
        Stake.STATUS_READY = 0;
        Stake.STATUS_RECOVERING = 126;
        Stake.STATUS_WIELDING = 127;
    }
    
    public static int getId() {
        return Stake.stakeId;
    }
    
    public static boolean handleEvent(final Object[] args) {
        return true;
    }
    
    public static void onItemTemplatesCreated() {
        try {
            final ItemTemplateBuilder itemTemplateBuilder = new ItemTemplateBuilder("friya.stake");
            itemTemplateBuilder.name("stake of vampire banishment", "stakes of vampire banishment", "This is a thick, pointed, wooden stake. It is made of a very hard wood of some type, and has been crafted into a formidable weapon for a short stick. It has been sanded smooth and chiselled to a point at one end. There are grooves carved in its shaft to improve your grip and magical runes of undead banishment spiraled around it. This would be the perfect weapon to 'stake' a vampire with. The runes will know the blood of a true vampire, and will punish you for using the stake on anything else. If you are wielding this weapon, you are considered a vampire hunter. Once wielded, the only way to get rid of it is to either stake a vampire or toss it in a garbage heap.");
            itemTemplateBuilder.descriptions("excellent", "good", "ok", "poor");
            itemTemplateBuilder.itemTypes(new short[] { 21, 112, 37, 13, 84 });
            itemTemplateBuilder.imageNumber((short)60);
            itemTemplateBuilder.behaviourType((short)41);
            itemTemplateBuilder.combatDamage(0);
            itemTemplateBuilder.decayTime(9072000L);
            itemTemplateBuilder.dimensions(3, 7, 10);
            itemTemplateBuilder.primarySkill(-10);
            itemTemplateBuilder.bodySpaces(Stake.EMPTY_BYTE_PRIMITIVE_ARRAY);
            itemTemplateBuilder.modelName("model.part.tenon.");
            itemTemplateBuilder.difficulty(5.0f);
            itemTemplateBuilder.weightGrams(3000);
            itemTemplateBuilder.material((byte)14);
            itemTemplateBuilder.imageNumber((short)646);
            final ItemTemplate stakeTemplate = itemTemplateBuilder.build();
            Stake.stakeId = stakeTemplate.getTemplateId();
            Stake.logger.log(Level.INFO, "Using template id " + Stake.stakeId);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
        Stake.logger.log(Level.INFO, "Setup completed");
    }
    
    public static void onServerStarted() {
    }
}
