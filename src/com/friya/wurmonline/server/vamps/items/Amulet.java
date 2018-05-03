// 
// Decompiled by Procyon v0.5.30
// 

package com.friya.wurmonline.server.vamps.items;

import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.items.ItemTemplate;
import com.wurmonline.server.items.ItemTypes;
import com.wurmonline.shared.constants.ItemMaterials;
import org.gotti.wurmunlimited.modsupport.ItemTemplateBuilder;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Amulet implements ItemTypes, MiscConstants, ItemMaterials
{
    private static Logger logger;
    private static int amuletId;
    
    static {
        Amulet.logger = Logger.getLogger(Amulet.class.getName());
    }
    
    public static int getId() {
        return Amulet.amuletId;
    }
    
    public static void onItemTemplatesCreated() {
        try {
            final ItemTemplateBuilder itemTemplateBuilder = new ItemTemplateBuilder("friya.ancientamulet");
            itemTemplateBuilder.name("ancient amulet", "ancient amulets", "The amulet is of archaic design beaten into a thick coin of hammered bronze and hanging from a chain. Its time worn surface still shows a myriad of magical protection runes.");
            itemTemplateBuilder.descriptions("excellent", "good", "ok", "poor");
            itemTemplateBuilder.itemTypes(new short[] { 22, 187 });
            itemTemplateBuilder.imageNumber((short)779);
            itemTemplateBuilder.behaviourType((short)1);
            itemTemplateBuilder.combatDamage(0);
            itemTemplateBuilder.decayTime(3024000L);
            itemTemplateBuilder.dimensions(20, 20, 20);
            itemTemplateBuilder.primarySkill(-10);
            itemTemplateBuilder.bodySpaces(new byte[] { 36 });
            itemTemplateBuilder.modelName("model.magic.amulet.farwalker.");
            itemTemplateBuilder.difficulty(5.0f);
            itemTemplateBuilder.weightGrams(20);
            itemTemplateBuilder.material((byte)67);
            final ItemTemplate mirrorTemplate = itemTemplateBuilder.build();
            Amulet.amuletId = mirrorTemplate.getTemplateId();
            Amulet.logger.log(Level.INFO, "Using template id " + Amulet.amuletId);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
        Amulet.logger.log(Level.INFO, "Setup completed");
    }
    
    public static void onServerStarted() {
    }
}
