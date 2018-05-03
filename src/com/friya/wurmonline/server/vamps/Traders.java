// 
// Decompiled by Procyon v0.5.30
// 

package com.friya.wurmonline.server.vamps;

import com.friya.wurmonline.server.vamps.items.SmallRat;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import javassist.CtClass;
import javassist.NotFoundException;
import javassist.bytecode.Descriptor;
import org.gotti.wurmunlimited.modloader.classhooks.HookManager;
import org.gotti.wurmunlimited.modloader.classhooks.InvocationHandlerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Traders
{
    private static Logger logger;
    
    static {
        Traders.logger = Logger.getLogger(Mod.class.getName());
    }
    
    public static void onServerStarted() {
        addItemToTrader(SmallRat.getId());
    }
    
    private static void addItemToTrader(final int itemId) {
        try {
            final String descriptor = Descriptor.ofMethod(CtClass.voidType, new CtClass[] { HookManager.getInstance().getClassPool().get("com.wurmonline.server.creatures.Creature") });
            HookManager.getInstance().registerHook("com.wurmonline.server.economy.Shop", "createShop", descriptor, () -> new InvocationHandler() {
                @Override
                public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
                    final Object result = method.invoke(proxy, args);
                    final Item inventory = ((Creature)args[0]).getInventory();
                    for (int x = 0; x < 3; ++x) {
                        final Item item = Creature.createItem(itemId, 50.0f);
                        inventory.insertItem(item);
                    }
                    return result;
                }
            });
        }
        catch (NotFoundException e) {
            Traders.logger.log(Level.SEVERE, "Failed to add item to shop", e);
        }
    }
}
