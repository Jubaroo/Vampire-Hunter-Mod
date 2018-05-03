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

public class HalfVampireClue implements ItemTypes, MiscConstants, ItemMaterials
{
    private static Logger logger;
    private static int halfVampClueId;
    
    static {
        HalfVampireClue.logger = Logger.getLogger(HalfVampireClue.class.getName());
    }
    
    public static int getId() {
        return HalfVampireClue.halfVampClueId;
    }
    
    public static void onItemTemplatesCreated() {
        try {
            final ItemTemplateBuilder itemTemplateBuilder = new ItemTemplateBuilder("friya.halfvampclue");
            itemTemplateBuilder.name("Dhampira the Ponderer's clue", "Dhampira the Ponderer's clues", "This is a papyrus sheet given to you by the half vampire Dhampira the Ponderer.");
            itemTemplateBuilder.descriptions("excellent", "good", "ok", "poor");
            itemTemplateBuilder.itemTypes(new short[] { 22, 187 });
            itemTemplateBuilder.imageNumber((short)640);
            itemTemplateBuilder.behaviourType((short)1);
            itemTemplateBuilder.combatDamage(0);
            itemTemplateBuilder.decayTime(3024000L);
            itemTemplateBuilder.dimensions(20, 20, 20);
            itemTemplateBuilder.primarySkill(-10);
            itemTemplateBuilder.bodySpaces(new byte[0]);
            itemTemplateBuilder.modelName("model.resource.sheet.");
            itemTemplateBuilder.difficulty(5.0f);
            itemTemplateBuilder.weightGrams(20);
            itemTemplateBuilder.material((byte)33);
            final ItemTemplate template = itemTemplateBuilder.build();
            HalfVampireClue.halfVampClueId = template.getTemplateId();
            HalfVampireClue.logger.log(Level.INFO, "Using template id " + HalfVampireClue.halfVampClueId);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
        HalfVampireClue.logger.log(Level.INFO, "Setup completed");
    }
    
    public static void onServerStarted() {
    }
}
