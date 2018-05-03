// 
// Decompiled by Procyon v0.5.30
// 

package com.friya.wurmonline.server.vamps;

import com.friya.wurmonline.server.loot.*;
import com.friya.wurmonline.server.vamps.items.Amulet;
import com.friya.wurmonline.server.vamps.items.Crown;
import com.friya.wurmonline.server.vamps.items.SmallRat;
import com.wurmonline.server.items.Item;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CreatureLoot implements BeforeDropListener
{
    private static Logger logger;
    private static CreatureLoot instance;
    
    static {
        CreatureLoot.logger = Logger.getLogger(CreatureLoot.class.getName());
    }
    
    public static CreatureLoot getInstance() {
        if (CreatureLoot.instance == null) {
            CreatureLoot.instance = new CreatureLoot();
        }
        return CreatureLoot.instance;
    }
    
    public boolean onBeforeDrop(final LootResult lootResult) {
        if (Mod.logExecutionCost) {
            CreatureLoot.logger.log(Level.INFO, "onBeforeDrop called");
            Mod.tmpExecutionStartTime = System.nanoTime();
        }
        final Item[] items = lootResult.getItems();
        Item[] array;
        for (int length = (array = items).length, j = 0; j < length; ++j) {
            final Item i = array[j];
            if (i.getTemplateId() == Crown.getId()) {
                i.setRarity((byte)1);
                i.setQualityLevel(99.0f);
                i.setOriginalQualityLevel(99.0f);
            }
        }
        if (items.length > 0) {
            CreatureLoot.logger.log(Level.INFO, "LootTable drops: " + Arrays.toString(lootResult.getItems()) + " at " + lootResult.getCreature().getTileX() + ", " + lootResult.getCreature().getTileY());
        }
        if (Mod.logExecutionCost) {
            CreatureLoot.logger.log(Level.INFO, "onBeforeDrop done, spent " + Mod.executionLogDf.format((System.nanoTime() - Mod.tmpExecutionStartTime) / 1.0E9) + "s");
        }
        return true;
    }
    
    public static void onServerStarted() {
        createLootRules();
        LootSystem.getInstance().listen((BeforeDropListener)getInstance());
    }
    
    static void createLootRules() {
        final LootSystem ls = LootSystem.getInstance();
        String ruleName = "[all NPCs] mod:vamps, Ancient Amulet";
        if (!ls.hasLootRule(ruleName)) {
            CreatureLoot.logger.log(Level.INFO, "Adding loot rule: " + ruleName);
            final LootRule lr = new LootRule(ruleName);
            final LootItem[] li = { new LootItem(new StringBuilder().append(Amulet.getId()).toString(), (byte)67, 0.25, "Ceno") };
            ls.addLootRule(lr, li);
        }
        ruleName = "[all NPCs] mod:vamps, Crown";
        if (!ls.hasLootRule(ruleName)) {
            CreatureLoot.logger.log(Level.INFO, "Adding loot rule: " + ruleName);
            final LootRule lr = new LootRule(ruleName);
            final LootItem[] li = { new LootItem(new StringBuilder().append(Crown.getId()).toString(), (byte)7, 0.25, "Zenath") };
            ls.addLootRule(lr, li);
        }
        ruleName = "[all NPCs] mod:vamps, Rat";
        if (!ls.hasLootRule(ruleName)) {
            CreatureLoot.logger.log(Level.INFO, "Adding loot rule: " + ruleName);
            final LootRule lr = new LootRule(ruleName);
            final LootItem[] li = { new LootItem(new StringBuilder().append(SmallRat.getId()).toString(), (byte)55, 0.30000001192092896, "Friya") };
            ls.addLootRule(lr, li);
        }
    }
}
