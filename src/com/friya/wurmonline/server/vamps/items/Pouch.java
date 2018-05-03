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

public class Pouch implements ItemTypes, MiscConstants, ItemMaterials
{
    private static Logger logger;
    private static int pouchId;
    
    static {
        Pouch.logger = Logger.getLogger(Pouch.class.getName());
    }
    
    public static int getId() {
        return Pouch.pouchId;
    }
    
    public static void onItemTemplatesCreated() {
        try {
            final ItemTemplateBuilder itemTemplateBuilder = new ItemTemplateBuilder("friya.velvetpouch");
            itemTemplateBuilder.name("black velvet pouch", "black pouches", "A black velvet pouch.");
            itemTemplateBuilder.descriptions("excellent", "good", "ok", "poor");
            itemTemplateBuilder.itemTypes(new short[] { 24, 1, 92 });
            itemTemplateBuilder.imageNumber((short)242);
            itemTemplateBuilder.behaviourType((short)1);
            itemTemplateBuilder.combatDamage(0);
            itemTemplateBuilder.decayTime(3024000L);
            itemTemplateBuilder.dimensions(50, 50, 50);
            itemTemplateBuilder.primarySkill(-10);
            itemTemplateBuilder.bodySpaces(Pouch.EMPTY_BYTE_PRIMITIVE_ARRAY);
            itemTemplateBuilder.modelName("model.container.satchel.");
            itemTemplateBuilder.difficulty(5.0f);
            itemTemplateBuilder.weightGrams(500);
            itemTemplateBuilder.material((byte)21);
            final ItemTemplate pouchTemplate = itemTemplateBuilder.build();
            Pouch.pouchId = pouchTemplate.getTemplateId();
            Pouch.logger.log(Level.INFO, "Using template id " + Pouch.pouchId);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
        Pouch.logger.log(Level.INFO, "Setup completed");
    }
    
    public static void onServerStarted() {
    }
}
