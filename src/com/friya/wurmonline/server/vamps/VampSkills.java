// 
// Decompiled by Procyon v0.5.30
// 

package com.friya.wurmonline.server.vamps;

import com.wurmonline.server.items.Item;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.skills.Skills;
import com.wurmonline.server.skills.SkillsProxy;
import javassist.*;
import javassist.bytecode.Descriptor;
import org.gotti.wurmunlimited.modloader.classhooks.HookException;
import org.gotti.wurmunlimited.modloader.classhooks.HookManager;
import org.gotti.wurmunlimited.modloader.classhooks.InvocationHandlerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.IntStream;

public class VampSkills
{
    private static Logger logger;
    public static final int BLOODLUST = 2147483641;
    public static final int DEXTERITY = 2147483640;
    public static final int PERCEPTION = 2147483639;
    public static final int ANATOMY = 2147483638;
    public static final int CRIPPLING = 2147483637;
    public static final int DISARMING = 2147483636;
    public static final int AIDING = 2147483635;
    private static ArrayList<VampSkillTemplate> tpls;
    
    static {
        VampSkills.logger = Logger.getLogger(VampSkills.class.getName());
        VampSkills.tpls = new ArrayList<VampSkillTemplate>();
    }
    
    public static void onItemTemplatesCreated() {
        onItemTemplatesCreated(true);
    }
    
    public static void onItemTemplatesCreated(final boolean enableSkillGainInterception) {
        try {
            final ClassPool classPool = HookManager.getInstance().getClassPool();
            final CtClass theClass = classPool.get("com.wurmonline.server.skills.SkillSystem");
            final CtMethod theMethod = theClass.getDeclaredMethod("addSkillTemplate");
            final String str = "{\t\tif($1.getNumber() == 10095) {\t\t\tcom.friya.wurmonline.server.vamps.VampSkillTemplate[] tpls = com.friya.wurmonline.server.vamps.VampSkills.addSkillTemplateHook();\t\t\tfor(int i = 0; i < tpls.length; i++) {\t\t\t\tcom.wurmonline.server.skills.SkillSystem.addSkillTemplate(new com.wurmonline.server.skills.SkillTemplate(\t\t\t\t\ttpls[i].number,\t\t\t\t\ttpls[i].name,\t\t\t\t\ttpls[i].difficulty,\t\t\t\t\ttpls[i].dependencies,\t\t\t\t\ttpls[i].decayTime,\t\t\t\t\ttpls[i].type,\t\t\t\t\ttpls[i].fightingSkill,\t\t\t\t\ttpls[i].ignoreEnemy\t\t\t\t));\t\t\t}\t\t}}";
            theMethod.insertAfter(str);
        }
        catch (NotFoundException | CannotCompileException ex2) {
            //final Exception ex;
            final Exception e = ex2;
            Mod.appendToFile(e);
            throw new HookException(e);
        }
        if (enableSkillGainInterception) {
            interceptSkillGains();
        }
        VampSkills.logger.log(Level.INFO, "preInit completed");
    }
    
    private static void interceptSkillGains() {
        try {
            final String descriptor = Descriptor.ofMethod(CtClass.doubleType, new CtClass[] { CtPrimitiveType.doubleType, HookManager.getInstance().getClassPool().get("com.wurmonline.server.items.Item"), CtPrimitiveType.doubleType, CtPrimitiveType.booleanType, CtPrimitiveType.floatType, CtPrimitiveType.booleanType, CtPrimitiveType.doubleType });
            HookManager.getInstance().registerHook("com.wurmonline.server.skills.Skill", "checkAdvance", descriptor, new InvocationHandlerFactory() {
                public InvocationHandler createInvocationHandler() {
                    return (proxy, method, args) -> {
                        if (Mod.logExecutionCost) {
                            Mod.tmpExecutionStartTime = System.nanoTime();
                        }
                        final Skill skill = (Skill)proxy;
                        final Skills skills = SkillsProxy.getParent(skill);
                        for (final VampSkillTemplate t : VampSkills.tpls) {
                            if (IntStream.of(t.skillUpOn).anyMatch(x -> x == skill.getNumber())) {
                                skills.getSkillOrLearn(t.number).skillCheck((double)args[0], (Item)args[1], (double)args[2], (boolean)args[3], (float)args[4], (boolean)args[5], (double)args[6]);
                            }
                        }
                        if (Mod.logExecutionCost) {
                            VampSkills.logger.log(Level.INFO, "interceptSkillGains[hook] done, spent " + Mod.executionLogDf.format((System.nanoTime() - Mod.tmpExecutionStartTime) / 1.0E9) + "s");
                        }
                        final Object result = method.invoke(proxy, args);
                        return result;
                    };
                }
            });
        }
        catch (NotFoundException e) {
            VampSkills.logger.log(Level.SEVERE, "Failed!", e);
            throw new RuntimeException("Failed to intercept checkAdvance()");
        }
    }
    
    public static VampSkillTemplate[] addSkillTemplateHook() {
        VampSkills.logger.log(Level.INFO, "addSkillTemplateHook() called, Vampire related skills will be added");
        VampSkills.tpls.add(new VampSkillTemplate(2147483641, "Bloodlust", 200000.0f, new int[] { 3 }, 1209600000L, (short)0, false, true, new int[0]));
        VampSkills.tpls.add(new VampSkillTemplate(2147483640, "Dexterity", 75000.0f, new int[0], 1209600000L, (short)4, false, true, new int[] { 1014, 10016, 10012, 10090, 10019, 10022, 10052, 10076 }));
        VampSkills.tpls.add(new VampSkillTemplate(2147483639, "Perception", 75000.0f, new int[0], 1209600000L, (short)4, false, true, new int[] { 10018, 10084, 10067 }));
        VampSkills.tpls.add(new VampSkillTemplate(2147483638, "Anatomy", 75000.0f, new int[] { 1019 }, 1209600000L, (short)4, false, true, new int[] { 10059, 10085, 10060, 10078 }));
        VampSkills.tpls.add(new VampSkillTemplate(2147483637, "Crippling", 75000.0f, new int[] { 1024 }, 1209600000L, (short)4, false, true, new int[] { 10042 }));
        VampSkills.tpls.add(new VampSkillTemplate(2147483636, "Disarming", 75000.0f, new int[0], 1209600000L, (short)4, false, true, new int[] { 10054 }));
        VampSkills.tpls.add(new VampSkillTemplate(2147483635, "Aiding", 75000.0f, new int[0], 1209600000L, (short)4, false, true, new int[] { 10056 }));
        return VampSkills.tpls.toArray(new VampSkillTemplate[0]);
    }
    
    public static void learnSkills(final Skills s) {
        for (final VampSkillTemplate t : VampSkills.tpls) {
            s.getSkillOrLearn(t.number);
        }
    }
    
    public static void onPlayerLogin(final Player p) {
        learnSkills(p.getSkills());
    }
}
