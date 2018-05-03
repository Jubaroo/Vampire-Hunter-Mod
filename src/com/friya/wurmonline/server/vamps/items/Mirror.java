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

public class Mirror implements ItemTypes, MiscConstants, ItemMaterials
{
    private static Logger logger;
    private static int mirrorId;
    
    static {
        Mirror.logger = Logger.getLogger(Mirror.class.getName());
    }
    
    public static int getId() {
        return Mirror.mirrorId;
    }
    
    public static void onItemTemplatesCreated() {
        try {
            final ItemTemplateBuilder itemTemplateBuilder = new ItemTemplateBuilder("friya.slayermirror");
            itemTemplateBuilder.name("silver mirror", "silver mirrors", "This is a very shiny silver mirror and you look marvellous. You can probably use it to check other individuals' reflections too.");
            itemTemplateBuilder.descriptions("excellent", "good", "ok", "poor");
            itemTemplateBuilder.itemTypes(new short[] { 22, 187 });
            itemTemplateBuilder.imageNumber((short)920);
            itemTemplateBuilder.behaviourType((short)1);
            itemTemplateBuilder.combatDamage(0);
            itemTemplateBuilder.decayTime(3024000L);
            itemTemplateBuilder.dimensions(7, 7, 7);
            itemTemplateBuilder.primarySkill(-10);
            itemTemplateBuilder.bodySpaces(Mirror.EMPTY_BYTE_PRIMITIVE_ARRAY);
            itemTemplateBuilder.modelName("model.tool.handmirror.");
            itemTemplateBuilder.difficulty(5.0f);
            itemTemplateBuilder.weightGrams(500);
            itemTemplateBuilder.material((byte)8);
            final ItemTemplate mirrorTemplate = itemTemplateBuilder.build();
            Mirror.mirrorId = mirrorTemplate.getTemplateId();
            Mirror.logger.log(Level.INFO, "Using template id " + Mirror.mirrorId);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
        Mirror.logger.log(Level.INFO, "Setup completed");
    }
    
    public static void onServerStarted() {
    }
}
