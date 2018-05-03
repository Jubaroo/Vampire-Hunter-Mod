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

public class SmallRat implements ItemTypes, MiscConstants, ItemMaterials
{
    private static Logger logger;
    private static int itemId;
    
    static {
        SmallRat.logger = Logger.getLogger(SmallRat.class.getName());
    }
    
    public static int getId() {
        return SmallRat.itemId;
    }
    
    public static void onItemTemplatesCreated() {
        try {
            final ItemTemplateBuilder itemTemplateBuilder = new ItemTemplateBuilder("friya.smallrat");
            itemTemplateBuilder.name("small rat", "small rats", "A dirty rat with beady little, evil-looking eyes.");
            itemTemplateBuilder.descriptions("excellent", "good", "ok", "poor");
            itemTemplateBuilder.itemTypes(new short[] { 5, 53, 127 });
            itemTemplateBuilder.imageNumber((short)561);
            itemTemplateBuilder.behaviourType((short)1);
            itemTemplateBuilder.combatDamage(0);
            itemTemplateBuilder.decayTime(3024000L);
            itemTemplateBuilder.dimensions(1, 1, 1);
            itemTemplateBuilder.primarySkill(-10);
            itemTemplateBuilder.bodySpaces(new byte[0]);
            itemTemplateBuilder.modelName("model.creature.quadraped.rat.large.");
            itemTemplateBuilder.difficulty(5.0f);
            itemTemplateBuilder.weightGrams(200);
            itemTemplateBuilder.material((byte)55);
            itemTemplateBuilder.value(100000);
            itemTemplateBuilder.isTraded(false);
            final ItemTemplate tpl = itemTemplateBuilder.build();
            SmallRat.itemId = tpl.getTemplateId();
            SmallRat.logger.log(Level.INFO, "Using template id " + SmallRat.itemId);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
        SmallRat.logger.log(Level.INFO, "Setup completed");
    }
    
    public static void onServerStarted() {
    }
}
