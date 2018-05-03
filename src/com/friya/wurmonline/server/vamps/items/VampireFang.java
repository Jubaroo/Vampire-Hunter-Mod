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

public class VampireFang implements ItemTypes, MiscConstants, ItemMaterials
{
    private static Logger logger;
    private static int fangId;
    
    static {
        VampireFang.logger = Logger.getLogger(VampireFang.class.getName());
    }
    
    public static int getId() {
        return VampireFang.fangId;
    }
    
    public static void onItemTemplatesCreated() {
        try {
            final ItemTemplateBuilder itemTemplateBuilder = new ItemTemplateBuilder("friya.vampirefang");
            itemTemplateBuilder.name("bloody fang", "bloody fangs", "The fang of a vampire.");
            itemTemplateBuilder.descriptions("excellent", "good", "ok", "poor");
            itemTemplateBuilder.itemTypes(new short[] { 37, 187 });
            itemTemplateBuilder.imageNumber((short)495);
            itemTemplateBuilder.behaviourType((short)1);
            itemTemplateBuilder.combatDamage(0);
            itemTemplateBuilder.decayTime(3024000L);
            itemTemplateBuilder.dimensions(1, 1, 1);
            itemTemplateBuilder.primarySkill(-10);
            itemTemplateBuilder.bodySpaces(new byte[0]);
            itemTemplateBuilder.modelName("model.resource.tooth.");
            itemTemplateBuilder.difficulty(5.0f);
            itemTemplateBuilder.weightGrams(20);
            itemTemplateBuilder.material((byte)55);
            final ItemTemplate tpl = itemTemplateBuilder.build();
            VampireFang.fangId = tpl.getTemplateId();
            VampireFang.logger.log(Level.INFO, "Using template id " + VampireFang.fangId);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
        VampireFang.logger.log(Level.INFO, "Setup completed");
    }
    
    public static void onServerStarted() {
    }
}
