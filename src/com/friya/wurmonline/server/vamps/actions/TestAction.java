// 
// Decompiled by Procyon v0.5.30
// 

package com.friya.wurmonline.server.vamps.actions;

import com.wurmonline.server.Servers;
import com.wurmonline.server.behaviours.Action;
import com.wurmonline.server.behaviours.ActionEntry;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.skills.SkillSystem;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.CtPrimitiveType;
import javassist.NotFoundException;
import javassist.bytecode.Descriptor;
import org.gotti.wurmunlimited.modloader.classhooks.HookException;
import org.gotti.wurmunlimited.modloader.classhooks.HookManager;
import org.gotti.wurmunlimited.modloader.classhooks.InvocationHandlerFactory;
import org.gotti.wurmunlimited.modsupport.actions.ActionPerformer;
import org.gotti.wurmunlimited.modsupport.actions.BehaviourProvider;
import org.gotti.wurmunlimited.modsupport.actions.ModAction;
import org.gotti.wurmunlimited.modsupport.actions.ModActions;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TestAction implements ModAction
{
    private static Logger logger;
    private final short actionId;
    private final ActionEntry actionEntry;
    
    static {
        TestAction.logger = Logger.getLogger(TestAction.class.getName());
    }
    
    public static void preInit() {
        TestAction.logger.log(Level.INFO, "preInit completed");
    }
    
    public static void testHook() {
        TestAction.logger.log(Level.INFO, "testHook() called!");
    }
    
    public static void debugDoSkillGainNew(final double check, final double power, final double learnMod, final float times, final double skillDivider) {
        TestAction.logger.log(Level.INFO, "debugDoSkillGainNew() check: " + check);
        TestAction.logger.log(Level.INFO, "debugDoSkillGainNew() power: " + power);
        TestAction.logger.log(Level.INFO, "debugDoSkillGainNew() learnMod: " + learnMod);
        TestAction.logger.log(Level.INFO, "debugDoSkillGainNew() times: " + times);
        TestAction.logger.log(Level.INFO, "debugDoSkillGainNew() skillDivider: " + skillDivider);
    }
    
    public static void debugAlterSkill(final double advanceMultiplicator, final boolean decay, final float times, final boolean useNewSystem, final double skillDivider) {
        final DecimalFormat df = new DecimalFormat("#.########");
        TestAction.logger.log(Level.INFO, "alterSkill() advanceMultiplicator: " + df.format(advanceMultiplicator));
        TestAction.logger.log(Level.INFO, "alterSkill() decay: " + decay);
        TestAction.logger.log(Level.INFO, "alterSkill() times: " + times);
        TestAction.logger.log(Level.INFO, "alterSkill() useNewSystem: " + useNewSystem);
        TestAction.logger.log(Level.INFO, "alterSkill() skillDivider: " + skillDivider);
    }
    
    public TestAction() {
        TestAction.logger.log(Level.INFO, "SimulateAction()");
        this.actionId = (short)ModActions.getNextActionId();
        ModActions.registerAction(this.actionEntry = ActionEntry.createEntry(this.actionId, "Simulate", "simulating", new int[] { 6 }));
    }
    
    public BehaviourProvider getBehaviourProvider() {
        return new BehaviourProvider() {
            public List<ActionEntry> getBehavioursFor(final Creature performer, final Item source, final Item object) {
                return this.getBehavioursFor(performer, object);
            }
            
            public List<ActionEntry> getBehavioursFor(final Creature performer, final Item object) {
                if (performer instanceof Player && performer.getPower() > 1) {
                    return Arrays.asList(TestAction.this.actionEntry);
                }
                return null;
            }
        };
    }
    
    public ActionPerformer getActionPerformer() {
        return new ActionPerformer() {
            public short getActionId() {
                return TestAction.this.actionId;
            }
            
            public boolean action(final Action act, final Creature performer, final Item target, final short action, final float counter) {
                return TestAction.this.simulate(performer, target, counter);
            }
            
            public boolean action(final Action act, final Creature performer, final Item source, final Item target, final short action, final float counter) {
                return this.action(act, performer, target, action, counter);
            }
        };
    }
    
    private boolean simulate(final Creature performer, final Item target, final float counter) {
        performer.getCommunicator().sendNormalServerMessage("Test!");
        return true;
    }
    
    void test() {
        try {
            String descriptor = Descriptor.ofMethod(CtClass.doubleType, new CtClass[] { CtPrimitiveType.doubleType, HookManager.getInstance().getClassPool().get("com.wurmonline.server.items.Item"), CtPrimitiveType.doubleType, CtPrimitiveType.booleanType, CtPrimitiveType.floatType, CtPrimitiveType.booleanType, CtPrimitiveType.doubleType });
            HookManager.getInstance().registerHook("com.wurmonline.server.skills.Skill", "checkAdvance", descriptor, new InvocationHandlerFactory() {
                public InvocationHandler createInvocationHandler() {
                    return new InvocationHandler() {
                        @Override
                        public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
                            TestAction.logger.log(Level.INFO, "checkAdvance name: " + ((Skill)proxy).getName());
                            TestAction.logger.log(Level.INFO, "checkAdvance check: " + (double)args[0]);
                            TestAction.logger.log(Level.INFO, "checkAdvance item: " + args[1]);
                            TestAction.logger.log(Level.INFO, "checkAdvance bonus: " + (double)args[2]);
                            TestAction.logger.log(Level.INFO, "checkAdvance dryRun: " + (boolean)args[3]);
                            TestAction.logger.log(Level.INFO, "checkAdvance times: " + (float)args[4]);
                            TestAction.logger.log(Level.INFO, "checkAdvance useNewSystem: " + (boolean)args[5]);
                            TestAction.logger.log(Level.INFO, "checkAdvance skillDivider: " + (double)args[6]);
                            final Object result = method.invoke(proxy, args);
                            return result;
                        }
                    };
                }
            });
            descriptor = Descriptor.ofMethod(CtClass.voidType, new CtClass[] { CtPrimitiveType.doubleType, CtPrimitiveType.booleanType, CtPrimitiveType.floatType, CtPrimitiveType.booleanType, CtPrimitiveType.doubleType });
            HookManager.getInstance().registerHook("com.wurmonline.server.skills.Skill", "alterSkill", descriptor, new InvocationHandlerFactory() {
                public InvocationHandler createInvocationHandler() {
                    return new InvocationHandler() {
                        @Override
                        public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
                            final Skill s = (Skill)proxy;
                            TestAction.logger.log(Level.INFO, "name: " + s.getName());
                            TestAction.logger.log(Level.INFO, "type: " + s.getType());
                            TestAction.logger.log(Level.INFO, "tickTime: " + SkillSystem.getTickTimeFor(s.getNumber()));
                            TestAction.logger.log(Level.INFO, "getSkillGainRate: " + Servers.localServer.getSkillGainRate());
                            if (s.getId() == -152L) {
                                double advanceMultiplicator = (double)args[0];
                                advanceMultiplicator *= (Servers.localServer.EPIC ? 3.0 : 1.5);
                                float staminaMod = 1.0f;
                                staminaMod += (float)Math.max(0.048999999254941945, 0.0);
                                advanceMultiplicator *= staminaMod;
                                args[0] = advanceMultiplicator;
                            }
                            final Object result = method.invoke(proxy, args);
                            TestAction.debugAlterSkill((double)args[0], (boolean)args[1], (float)args[2], (boolean)args[3], (double)args[4]);
                            return result;
                        }
                    };
                }
            });
        }
        catch (Exception e) {
            throw new HookException(e);
        }
    }
    
    static String getDescriptorForClass(final CtClass c) {
        if (c.isPrimitive()) {
            return ((CtPrimitiveType)c).getGetMethodDescriptor();
        }
        if (c.isArray()) {
            return c.getName().replace('.', '/');
        }
        return (String.valueOf('L') + c.getName() + ';').replace('.', '/');
    }
    
    static String getMethodDescriptor(final CtMethod m) throws NotFoundException {
        String s = "(";
        CtClass[] parameterTypes;
        for (int length = (parameterTypes = m.getParameterTypes()).length, i = 0; i < length; ++i) {
            final CtClass c = parameterTypes[i];
            s = String.valueOf(s) + getDescriptorForClass(c);
        }
        s = String.valueOf(s) + ')';
        return String.valueOf(s) + getDescriptorForClass(m.getReturnType());
    }
}
