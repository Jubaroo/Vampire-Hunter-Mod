// 
// Decompiled by Procyon v0.5.30
// 

package com.friya.wurmonline.server.vamps;

import com.wurmonline.server.items.Item;
import javassist.*;
import org.gotti.wurmunlimited.modloader.classhooks.HookManager;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BloodlessHusk
{
    private static Logger logger;
    
    static {
        BloodlessHusk.logger = Logger.getLogger(BloodlessHusk.class.getName());
    }
    
    public static void onItemTemplatesCreated() {
        try {
            final ClassPool classPool = HookManager.getInstance().getClassPool();
            final CtClass theClass = classPool.get("com.wurmonline.server.items.Item");
            final CtField f = CtField.make("long bloodSucker = (long)0;", theClass);
            theClass.addField(f);
            String str = "public void setBloodSucker(long vampireId)\t{\t\tbloodSucker = (long)vampireId;\t}";
            CtMethod theMethod = CtNewMethod.make(str, theClass);
            theClass.addMethod(theMethod);
            str = "public long getBloodSucker()\t{\t\treturn bloodSucker;\t}";
            theMethod = CtNewMethod.make(str, theClass);
            theClass.addMethod(theMethod);
            str = "public boolean isBloodlessHusk()\t{\t\treturn bloodSucker != (long)0;\t}";
            theMethod = CtNewMethod.make(str, theClass);
            theClass.addMethod(theMethod);
        }
        catch (NotFoundException | CannotCompileException ex2) {
            final Exception ex;
            final Exception e = ex;
            Mod.appendToFile(e);
            throw new RuntimeException(e);
        }
        BloodlessHusk.logger.log(Level.INFO, "preInit completed");
    }
    
    public static boolean isBloodlessHusk(final Item c) {
        try {
            final Method m = c.getClass().getMethod("isBloodlessHusk", (Class<?>[])new Class[0]);
            return (boolean)m.invoke(c, new Object[0]);
        }
        catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException ex2) {
            final ReflectiveOperationException ex;
            final ReflectiveOperationException e = ex;
            throw new RuntimeException(e);
        }
    }
    
    public static void setBloodSucker(final Item c, final long vampireId) {
        try {
            final Method m = c.getClass().getMethod("setBloodSucker", Long.TYPE);
            m.invoke(c, vampireId);
        }
        catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException ex2) {
            final ReflectiveOperationException ex;
            final ReflectiveOperationException e = ex;
            throw new RuntimeException(e);
        }
    }
    
    public static long getBloodSucker(final Item c) {
        try {
            final Method m = c.getClass().getMethod("getBloodSucker", (Class<?>[])new Class[0]);
            return (long)m.invoke(c, new Object[0]);
        }
        catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException ex2) {
            final ReflectiveOperationException ex;
            final ReflectiveOperationException e = ex;
            throw new RuntimeException(e);
        }
    }
}
