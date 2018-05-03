// 
// Decompiled by Procyon v0.5.30
// 

package com.friya.wurmonline.server.vamps.items;

import com.friya.wurmonline.server.vamps.DynamicExaminable;
import com.friya.wurmonline.server.vamps.DynamicExamine;
import com.friya.wurmonline.server.vamps.VampZones;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.behaviours.Terraforming;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.*;
import com.wurmonline.server.zones.FocusZone;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zones;
import com.wurmonline.shared.constants.ItemMaterials;
import org.gotti.wurmunlimited.modsupport.ItemTemplateBuilder;

import java.io.IOException;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AltarOfSouls implements ItemTypes, MiscConstants, ItemMaterials, DynamicExaminable
{
    private static Logger logger;
    private static int itemId;
    private static AltarOfSouls instance;
    
    static {
        AltarOfSouls.logger = Logger.getLogger(AltarOfSouls.class.getName());
    }
    
    public static int getId() {
        return AltarOfSouls.itemId;
    }
    
    public int getTemplateId() {
        return getId();
    }
    
    public static AltarOfSouls getInstance() {
        if (AltarOfSouls.instance == null) {
            AltarOfSouls.instance = new AltarOfSouls();
        }
        return AltarOfSouls.instance;
    }
    
    public static void onItemTemplatesCreated() {
        try {
            final ItemTemplateBuilder itemTemplateBuilder = new ItemTemplateBuilder("friya.altarsouls");
            itemTemplateBuilder.name("altar of souls", "altars of souls", "Clearly dark magic. When placed on an uncluttered and flattened area inside a cave you can sacrifice corpses at it. When standing next to a charged altar, vampire soulfeed can never kill you.");
            itemTemplateBuilder.descriptions("excellent", "good", "ok", "poor");
            itemTemplateBuilder.itemTypes(new short[] { 108, 48, 119, 52, 44, 67, 109, 135 });
            itemTemplateBuilder.imageNumber((short)462);
            itemTemplateBuilder.behaviourType((short)1);
            itemTemplateBuilder.combatDamage(0);
            itemTemplateBuilder.decayTime(3024000L);
            itemTemplateBuilder.dimensions(100, 100, 50);
            itemTemplateBuilder.bodySpaces(new byte[0]);
            itemTemplateBuilder.modelName("model.structure.rift.altar.1.");
            itemTemplateBuilder.weightGrams(95000);
            itemTemplateBuilder.material((byte)15);
            itemTemplateBuilder.difficulty(90.0f);
            itemTemplateBuilder.primarySkill(-10);
            final ItemTemplate tpl = itemTemplateBuilder.build();
            AltarOfSouls.itemId = tpl.getTemplateId();
            AltarOfSouls.logger.log(Level.INFO, "Using template id " + AltarOfSouls.itemId);
            DynamicExamine.getInstance().listen(getInstance());
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
        AltarOfSouls.logger.log(Level.INFO, "Setup completed");
    }
    
    public static void onServerStarted() {
        if (AltarOfSouls.itemId > 0) {
            final AdvancedCreationEntry creationEntry = CreationEntryCreator.createAdvancedEntry(1013, 146, 130, AltarOfSouls.itemId, false, false, 0.0f, true, true, CreationCategories.ALTAR);
            creationEntry.addRequirement(new CreationRequirement(1, 146, 400, true));
            creationEntry.addRequirement(new CreationRequirement(2, 26, 20, true));
            creationEntry.addRequirement(new CreationRequirement(3, 782, 42, true));
            creationEntry.addRequirement(new CreationRequirement(4, 130, 200, true));
            creationEntry.addRequirement(new CreationRequirement(5, 204, 75, true));
            creationEntry.addRequirement(new CreationRequirement(6, 380, 20, true));
            creationEntry.addRequirement(new CreationRequirement(7, 378, 20, true));
            creationEntry.addRequirement(new CreationRequirement(8, 379, 1, true));
        }
    }
    
    public static byte getCharge(final Item altar) {
        if (isInTheCoven(altar)) {
            return 127;
        }
        return ((altar.getAuxData() & 0xFF) < 0) ? 0 : altar.getAuxData();
    }
    
    public static void setCharge(final Item altar, byte amount) {
        if ((amount & 0xFF) < 0) {
            amount = 0;
        }
        if ((amount & 0xFF) > 127) {
            amount = 127;
        }
        altar.setAuxData(amount);
    }
    
    public static boolean isCharged(final Item altar) {
        return getCharge(altar) > 0 || isInTheCoven(altar);
    }
    
    public static boolean isInTheCoven(final Item item) {
        final Set<FocusZone> zones = FocusZone.getZonesAt(item.getTileX(), item.getTileY());
        for (final FocusZone fz : zones) {
            if (fz.getName().equals(VampZones.getCovenZone().getName())) {
                return true;
            }
        }
        return false;
    }
    
    public static boolean isCleanArea(final Item altar) {
        Item[] tmpItems = null;
        VolaTile tmpTile = null;
        for (int x = -2; x <= 2; ++x) {
            for (int y = -2; y <= 2; ++y) {
                tmpTile = Zones.getTileOrNull(altar.getTileX() + x, altar.getTileY() + y, false);
                if (tmpTile != null) {
                    if (!Terraforming.isFlat(tmpTile.getTileX(), tmpTile.getTileY(), false, 0)) {
                        return false;
                    }
                    tmpItems = tmpTile.getItems();
                    if (tmpItems.length > 0 && tmpItems[0] != null && tmpItems[0].getTemplateId() != getId()) {
                        AltarOfSouls.logger.info("Unclean. Bailing. Found a disallowed item around Altar of Souls: " + tmpItems[0]);
                        return false;
                    }
                }
            }
        }
        return true;
    }
    
    public String examine(final Item altar, final Creature performer) {
        String ret = "";
        if (!isCleanArea(altar)) {
            ret = "The surrounding area is too cluttered or choopy for it to function properly. ";
        }
        final byte charge = getCharge(altar);
        if (charge > 120) {
            ret = String.valueOf(ret) + "It can hold no more souls.";
        }
        else if (charge > 80) {
            ret = String.valueOf(ret) + "It's at near capacity.";
        }
        else if (charge > 40) {
            ret = String.valueOf(ret) + "It's not nearly full.";
        }
        else if (charge > 10) {
            ret = String.valueOf(ret) + "It's running out of souls.";
        }
        else if (charge > 0) {
            ret = String.valueOf(ret) + "It's nearly out of souls.";
        }
        else {
            ret = String.valueOf(ret) + "It has no souls.";
        }
        return String.valueOf(ret) + ((performer.getPower() > 2) ? (" [" + charge + "]") : "");
    }
}
