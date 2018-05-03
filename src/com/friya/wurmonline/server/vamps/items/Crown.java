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

public class Crown implements ItemTypes, MiscConstants, ItemMaterials
{
    private static Logger logger;
    private static int crownId;
    
    static {
        Crown.logger = Logger.getLogger(Crown.class.getName());
    }
    
    public static int getId() {
        return Crown.crownId;
    }
    
    public static void onItemTemplatesCreated() {
        try {
            final ItemTemplateBuilder itemTemplateBuilder = new ItemTemplateBuilder("friya.crown");
            itemTemplateBuilder.name("Crown of Friya", "Crowns of Friya", "This abominable crown was lost in ancient times. It is made of white metal, studded with green emeralds. It can be used to find two-legged friends.");
            itemTemplateBuilder.descriptions("excellent", "good", "ok", "poor");
            itemTemplateBuilder.itemTypes(new short[] { 22, 4, 187 });
            itemTemplateBuilder.imageNumber((short)974);
            itemTemplateBuilder.behaviourType((short)1);
            itemTemplateBuilder.combatDamage(0);
            itemTemplateBuilder.decayTime(3024000L);
            itemTemplateBuilder.dimensions(20, 20, 20);
            itemTemplateBuilder.primarySkill(-10);
            itemTemplateBuilder.bodySpaces(new byte[] { 1, 28 });
            itemTemplateBuilder.modelName("model.artifact.crownmight.");
            itemTemplateBuilder.difficulty(5.0f);
            itemTemplateBuilder.weightGrams(2152);
            itemTemplateBuilder.material((byte)7);
            final ItemTemplate template = itemTemplateBuilder.build();
            Crown.crownId = template.getTemplateId();
            Crown.logger.log(Level.INFO, "Using template id " + Crown.crownId);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
        Crown.logger.log(Level.INFO, "Setup completed");
    }
    
    public static void onServerStarted() {
    }
}
