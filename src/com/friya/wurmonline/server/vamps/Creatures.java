// 
// Decompiled by Procyon v0.5.30
// 

package com.friya.wurmonline.server.vamps;

import com.wurmonline.server.Server;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.CreatureTemplate;
import com.wurmonline.server.creatures.CreatureTemplateFactory;
import com.wurmonline.server.creatures.CreaturesProxy;
import com.wurmonline.server.creatures.ai.AiProxy;
import com.wurmonline.server.creatures.ai.ChatManager;
import com.wurmonline.server.skills.Skills;
import com.wurmonline.server.skills.SkillsFactory;
import com.wurmonline.server.villages.Village;
import com.wurmonline.server.villages.Villages;
import com.wurmonline.server.zones.Zones;
import javassist.CtClass;
import javassist.CtPrimitiveType;
import javassist.NotFoundException;
import javassist.bytecode.Descriptor;
import org.gotti.wurmunlimited.modloader.classhooks.HookManager;
import org.gotti.wurmunlimited.modloader.classhooks.InvocationHandlerFactory;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Creatures
{
    public static int vampireId;
    public static int vampireGuardId;
    public static int protectedHumanId;
    public static int blackPetDragonId;
    private static Logger logger;
    
    static {
        Creatures.vampireId = 8712300;
        Creatures.vampireGuardId = 8712301;
        Creatures.protectedHumanId = 8712302;
        Creatures.blackPetDragonId = 8712303;
        Creatures.logger = Logger.getLogger(Creatures.class.getName());
    }
    
    static void onTemplatesCreated() {
        String name = "Vampire";
        String longDesc = "I would not mess with this one...";
        setupShutUpHook();
        final Skills skills = SkillsFactory.createSkills(name);
        skills.learnTemp(102, 20.0f);
        skills.learnTemp(104, 20.0f);
        skills.learnTemp(103, 20.0f);
        skills.learnTemp(100, 20.0f);
        skills.learnTemp(101, 20.0f);
        skills.learnTemp(105, 20.0f);
        skills.learnTemp(106, 20.0f);
        try {
            CreatureTemplate tmp = CreatureTemplateFactory.getInstance().createCreatureTemplate(Creatures.vampireId, name, longDesc, "model.creature.humanoid.avenger.light", new int[] { 22, 23, 12, 13, 55, 4 }, (byte)0, skills, (short)5, (byte)0, (short)180, (short)20, (short)35, "sound.death.spirit.male", "sound.death.spirit.female", "sound.combat.hit.spirit.male", "sound.combat.hit.spirit.female", 0.1f, 70.0f, 70.0f, 70.0f, 70.0f, 0.0f, 1.5f, 100, new int[0], 40, 100, (byte)0);
            tmp.setSizeModX(10);
            tmp.setSizeModY(10);
            tmp.setSizeModZ(10);
            name = "Orlok's watchman";
            longDesc = "I would not mess with this one either...";
            skills.learnTemp(102, 30.0f);
            skills.learnTemp(104, 30.0f);
            skills.learnTemp(103, 35.0f);
            skills.learnTemp(100, 17.0f);
            skills.learnTemp(101, 27.0f);
            skills.learnTemp(105, 24.0f);
            skills.learnTemp(106, 24.0f);
            skills.learnTemp(10052, 80.0f);
            tmp = CreatureTemplateFactory.getInstance().createCreatureTemplate(Creatures.vampireGuardId, name, longDesc, "model.creature.humanoid.human.spirit.shadow", new int[] { 4 }, (byte)0, skills, (short)5, (byte)0, (short)180, (short)20, (short)35, "sound.death.spirit.male", "sound.death.spirit.female", "sound.combat.hit.spirit.male", "sound.combat.hit.spirit.female", 0.4f, 3.0f, 5.0f, 0.0f, 0.0f, 0.0f, 1.5f, 0, new int[0], 100, 100, (byte)0);
            tmp.setHandDamString("claw");
            tmp.setKickDamString("claw");
            tmp.setAlignment(-70.0f);
            tmp.setBaseCombatRating(25.0f);
            tmp.combatDamageType = 1;
            tmp.setMaxGroupAttackSize(4);
            tmp.setSizeModX(40);
            tmp.setSizeModY(40);
            tmp.setSizeModZ(40);
            name = "Domestic Black Dragon";
            skills.learnTemp(102, 20.0f);
            skills.learnTemp(104, 20.0f);
            skills.learnTemp(103, 30.0f);
            skills.learnTemp(100, 5.0f);
            skills.learnTemp(101, 4.0f);
            skills.learnTemp(105, 10.0f);
            skills.learnTemp(106, 1.0f);
            skills.learnTemp(10052, 8.0f);
            final int[] types = { 7, 41, 3, 43, 14, 9, 28, 32 };
            final CreatureTemplate temp = CreatureTemplateFactory.getInstance().createCreatureTemplate(Creatures.blackPetDragonId, name, longDesc, "model.creature.dragon.black", types, (byte)1, skills, (short)3, (byte)0, (short)180, (short)50, (short)250, "sound.death.dragon", "sound.death.dragon", "sound.combat.hit.dragon", "sound.combat.hit.dragon", 1.0f, 2.0f, 2.0f, 3.0f, 4.0f, 0.0f, 0.5f, 100, new int[] { 307, 306, 140, 71, 309, 308, 312, 312 }, 5, 10, (byte)0);
            temp.keepSex = true;
            temp.setMaxAge(100);
            temp.setBaseCombatRating(1.0f);
            temp.setChildTemplateId(50);
            temp.setMateTemplateId(49);
            temp.setMaxGroupAttackSize(2);
            temp.combatDamageType = 0;
            temp.setMaxPercentOfCreatures(0.0f);
            temp.setSizeModX(35);
            temp.setSizeModY(35);
            temp.setSizeModZ(35);
        }
        catch (IOException e) {
            Creatures.logger.log(Level.SEVERE, "Could not create creature template(s)", e);
        }
    }
    
    private static void setupShutUpHook() {
        try {
            String descriptor = Descriptor.ofMethod(CtPrimitiveType.voidType, new CtClass[] { HookManager.getInstance().getClassPool().get("com.wurmonline.server.players.Player"), HookManager.getInstance().getClassPool().get("java.lang.String"), CtPrimitiveType.booleanType });
            HookManager.getInstance().registerHook("com.wurmonline.server.creatures.ai.ChatManager", "createAndSendMessage", descriptor, new InvocationHandlerFactory() {
                public InvocationHandler createInvocationHandler() {
                    return new InvocationHandler() {
                        @Override
                        public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
                            if (Mod.logExecutionCost) {
                                Mod.tmpExecutionStartTime = System.nanoTime();
                            }
                            if (Creatures.shutUpNpcHook(AiProxy.getChatManagerOwner((ChatManager)proxy))) {
                                AiProxy.clearChatManagerChats((ChatManager)proxy);
                                if (Mod.logExecutionCost) {
                                    Creatures.logger.log(Level.INFO, "setupShutUpHook[hook1.1] done, spent " + Mod.executionLogDf.format((System.nanoTime() - Mod.tmpExecutionStartTime) / 1.0E9) + "s");
                                }
                                return null;
                            }
                            if (Mod.logExecutionCost) {
                                Creatures.logger.log(Level.INFO, "setupShutUpHook[hook1.2] done, spent " + Mod.executionLogDf.format((System.nanoTime() - Mod.tmpExecutionStartTime) / 1.0E9) + "s");
                            }
                            final Object result = method.invoke(proxy, args);
                            return result;
                        }
                    };
                }
            });
            descriptor = Descriptor.ofMethod(CtPrimitiveType.voidType, new CtClass[] { HookManager.getInstance().getClassPool().get("com.wurmonline.server.Message"), HookManager.getInstance().getClassPool().get("java.lang.String") });
            HookManager.getInstance().registerHook("com.wurmonline.server.creatures.ai.ChatManager", "answerLocalChat", descriptor, new InvocationHandlerFactory() {
                public InvocationHandler createInvocationHandler() {
                    return new InvocationHandler() {
                        @Override
                        public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
                            if (Mod.logExecutionCost) {
                                Mod.tmpExecutionStartTime = System.nanoTime();
                            }
                            if (Creatures.shutUpNpcHook(AiProxy.getChatManagerOwner((ChatManager)proxy))) {
                                AiProxy.clearChatManagerChats((ChatManager)proxy);
                                if (Mod.logExecutionCost) {
                                    Creatures.logger.log(Level.INFO, "setupShutUpHook[hook2.1] done, spent " + Mod.executionLogDf.format((System.nanoTime() - Mod.tmpExecutionStartTime) / 1.0E9) + "s");
                                }
                                return null;
                            }
                            if (Mod.logExecutionCost) {
                                Creatures.logger.log(Level.INFO, "setupShutUpHook[hook2.2] done, spent " + Mod.executionLogDf.format((System.nanoTime() - Mod.tmpExecutionStartTime) / 1.0E9) + "s");
                            }
                            final Object result = method.invoke(proxy, args);
                            return result;
                        }
                    };
                }
            });
            descriptor = Descriptor.ofMethod(CtPrimitiveType.voidType, new CtClass[0]);
            HookManager.getInstance().registerHook("com.wurmonline.server.creatures.ai.ChatManager", "startLocalChat", descriptor, new InvocationHandlerFactory() {
                public InvocationHandler createInvocationHandler() {
                    return new InvocationHandler() {
                        @Override
                        public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
                            if (Mod.logExecutionCost) {
                                Mod.tmpExecutionStartTime = System.nanoTime();
                            }
                            if (Creatures.shutUpNpcHook(AiProxy.getChatManagerOwner((ChatManager)proxy))) {
                                AiProxy.clearChatManagerChats((ChatManager)proxy);
                                if (Mod.logExecutionCost) {
                                    Creatures.logger.log(Level.INFO, "setupShutUpHook[hook3.1] done, spent " + Mod.executionLogDf.format((System.nanoTime() - Mod.tmpExecutionStartTime) / 1.0E9) + "s");
                                }
                                return null;
                            }
                            if (Mod.logExecutionCost) {
                                Creatures.logger.log(Level.INFO, "setupShutUpHook[hook3.2] done, spent " + Mod.executionLogDf.format((System.nanoTime() - Mod.tmpExecutionStartTime) / 1.0E9) + "s");
                            }
                            final Object result = method.invoke(proxy, args);
                            return result;
                        }
                    };
                }
            });
        }
        catch (NotFoundException e) {
            Creatures.logger.log(Level.SEVERE, "Failed to intercept 'setupShutUpHook', this probably means some Vampire related NPC's will chat in local");
            throw new RuntimeException(e);
        }
    }
    
    public static boolean stopNpcMoveHook(final Creature npc) {
        return npc.getName().equals("Orlok") || npc.getName().equals("Vampire hunter D") || npc.getName().equals("van Helsing") || npc.getName().equals("Dhampira the Ponderer");
    }
    
    public static boolean shutUpNpcHook(final Creature npc) {
        return npc.getName().equals("Orlok") || npc.getName().equals("Vampire hunter D") || npc.getName().equals("van Helsing") || npc.getName().equals("Dhampira the Ponderer");
    }
    
    private static void spawnHeadVampire() {
        try {
            final CreatureTemplate template = CreatureTemplateFactory.getInstance().getTemplate(Creatures.vampireId);
            final int xPos = VampZones.getCovenCentre().getX() + 1;
            final int yPos = VampZones.getCovenCentre().getY() + 1;
            if (!Zones.isGoodTileForSpawn(xPos, yPos, VampZones.getCovenLayer() != -1)) {
                Creatures.logger.log(Level.SEVERE, "Could not spawn Orlok, designated tile is not suitable for spawning: " + xPos + ", " + yPos);
                return;
            }
            final Creature newCreature = Creature.doNew(template.getTemplateId(), true, xPos * 4.0f, yPos * 4.0f, 0.0f, VampZones.getCovenLayer(), "Orlok", (byte)0, (byte)4, (byte)0, false, (byte)3);
            newCreature.shouldStandStill = true;
        }
        catch (Exception e) {
            Creatures.logger.log(Level.SEVERE, "Could not spawn Orlok", e);
        }
    }
    
    private static void spawnAtVillageIfNotExists(final int templateId, final String npcName, final byte kingdom, final boolean standStill, final boolean female) {
        final Creature[] creatures = CreaturesProxy.getCreaturesWithName(npcName);
        if (creatures.length == 0) {
            Village v = Villages.getCapital(kingdom);
            if (v == null) {
                v = Villages.getFirstPermanentVillageForKingdom(kingdom);
            }
            if (v == null && Villages.getVillages().length > 0) {
                v = Villages.getVillages()[0];
            }
            if (v != null) {
                Creatures.logger.log(Level.INFO, String.valueOf(npcName) + " did not exist, so spawning it near a village: " + v.getTokenX() + ", " + v.getTokenY());
                final int xPos = v.getTokenX() - 20 + Server.rand.nextInt(40);
                final int yPos = v.getTokenY() - 20 + Server.rand.nextInt(40);
                try {
                    final Creature newCreature = Creature.doNew(templateId, true, xPos * 4.0f, yPos * 4.0f, Server.rand.nextFloat(), 0, npcName, (byte)0, kingdom, (byte)0, false, (byte)3);
                    newCreature.setSex((byte)(female ? 1 : 0));
                    if (standStill) {
                        newCreature.shouldStandStill = true;
                    }
                }
                catch (Exception e) {
                    Creatures.logger.log(Level.SEVERE, "Could not spawn " + npcName + " -- TODO FOR YOU: spawn manually!", e);
                }
            }
            else {
                Creatures.logger.log(Level.SEVERE, "Could not find a village to spawn " + npcName + " TODO FOR YOU: spawn manually!");
            }
        }
        else {
            Creatures.logger.log(Level.INFO, String.valueOf(npcName) + " Exists. Good. There are " + creatures.length + " of them: " + Arrays.toString(creatures));
        }
    }
    
    static void onServerStarted() {
        Creatures.logger.log(Level.INFO, "onServerStarted");
        if (!com.wurmonline.server.creatures.Creatures.getInstance().creatureWithTemplateExists(Creatures.vampireId)) {
            Creatures.logger.log(Level.INFO, "Orlok did not exist, so spawning him near: " + VampZones.getCovenCentre().getX() + ", " + VampZones.getCovenCentre().getY());
            spawnHeadVampire();
        }
        else {
            Creatures.logger.log(Level.INFO, "Orlok existed. Good.");
        }
        spawnAtVillageIfNotExists(113, "Vampire hunter D", (byte)4, false, false);
        spawnAtVillageIfNotExists(113, "van Helsing", (byte)4, true, false);
        spawnAtVillageIfNotExists(113, "Dhampira the Ponderer", (byte)4, false, true);
    }
}
