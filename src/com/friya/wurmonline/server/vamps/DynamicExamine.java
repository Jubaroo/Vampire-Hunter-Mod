// 
// Decompiled by Procyon v0.5.30
// 

package com.friya.wurmonline.server.vamps;

import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import javassist.CtClass;
import javassist.NotFoundException;
import javassist.bytecode.Descriptor;
import org.gotti.wurmunlimited.modloader.classhooks.HookManager;
import org.gotti.wurmunlimited.modloader.classhooks.InvocationHandlerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DynamicExamine
{
    private static Logger logger;
    private List<DynamicExaminable> listeners;
    private static DynamicExamine instance;
    
    static {
        DynamicExamine.logger = Logger.getLogger(DynamicExamine.class.getName());
    }
    
    public DynamicExamine() {
        this.listeners = new ArrayList<DynamicExaminable>();
    }
    
    static void onItemTemplatesCreated() {
        getInstance().setupExamineInterception();
    }
    
    public static DynamicExamine getInstance() {
        if (DynamicExamine.instance == null) {
            DynamicExamine.instance = new DynamicExamine();
        }
        return DynamicExamine.instance;
    }
    
    private void setupExamineInterception() {
        try {
            final String descriptor = Descriptor.ofMethod(HookManager.getInstance().getClassPool().get("java.lang.String"), new CtClass[] { HookManager.getInstance().getClassPool().get("com.wurmonline.server.creatures.Creature") });
            HookManager.getInstance().registerHook("com.wurmonline.server.items.Item", "examine", descriptor, new InvocationHandlerFactory() {
                public InvocationHandler createInvocationHandler() {
                    return new InvocationHandler() {
                        @Override
                        public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
                            final String res = DynamicExamine.this.callListeners((Item)proxy, (Creature)args[0]);
                            if (res != null) {
                                return String.valueOf(method.invoke(proxy, args)) + res;
                            }
                            final Object result = method.invoke(proxy, args);
                            return result;
                        }
                    };
                }
            });
        }
        catch (NotFoundException e) {
            DynamicExamine.logger.log(Level.SEVERE, "Failed", e);
        }
    }
    
    public void listen(final DynamicExaminable listener) {
        this.listeners.add(listener);
    }
    
    public int getListenerCount() {
        return this.listeners.size();
    }
    
    private String callListeners(final Item item, final Creature performer) {
        for (final DynamicExaminable listener : this.listeners) {
            if (listener != null && item.getTemplateId() == listener.getTemplateId()) {
                return " " + listener.examine(item, performer);
            }
        }
        return null;
    }
}
