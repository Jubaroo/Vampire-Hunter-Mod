// 
// Decompiled by Procyon v0.5.30
// 

package com.friya.tools;

import sun.reflect.FieldAccessor;
import sun.reflect.ReflectionFactory;

import java.lang.reflect.Field;

public class ReflectionHelper
{
    private static final String MODIFIERS_FIELD = "modifiers";
    private static final ReflectionFactory reflection;
    
    static {
        reflection = ReflectionFactory.getReflectionFactory();
    }
    
    public static void setStaticFinalField(final Field field, final Object value) throws NoSuchFieldException, IllegalAccessException {
        field.setAccessible(true);
        final Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        int modifiers = modifiersField.getInt(field);
        modifiers &= 0xFFFFFFEF;
        modifiersField.setInt(field, modifiers);
        final FieldAccessor fa = ReflectionHelper.reflection.newFieldAccessor(field, false);
        fa.set(null, value);
    }
}
