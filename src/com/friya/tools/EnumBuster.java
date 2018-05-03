// 
// Decompiled by Procyon v0.5.30
// 

package com.friya.tools;

import sun.reflect.ConstructorAccessor;
import sun.reflect.ReflectionFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.*;

public class EnumBuster<E extends Enum<E>>
{
    private static final Class[] EMPTY_CLASS_ARRAY;
    private static final Object[] EMPTY_OBJECT_ARRAY;
    private static final String VALUES_FIELD = "$VALUES";
    private static final String ORDINAL_FIELD = "ordinal";
    private final ReflectionFactory reflection;
    private final Class<E> clazz;
    private final Collection<Field> switchFields;
    private final Deque<Memento> undoStack;
    
    static {
        EMPTY_CLASS_ARRAY = new Class[0];
        EMPTY_OBJECT_ARRAY = new Object[0];
    }
    
    public EnumBuster(final Class<E> clazz, final Class... switchUsers) {
        this.reflection = ReflectionFactory.getReflectionFactory();
        this.undoStack = new LinkedList<Memento>();
        try {
            this.clazz = clazz;
            this.switchFields = this.findRelatedSwitchFields(switchUsers);
        }
        catch (Exception e) {
            throw new IllegalArgumentException("Could not create the class", e);
        }
    }
    
    public E make(final String value) {
        return this.make(value, 0, EnumBuster.EMPTY_CLASS_ARRAY, EnumBuster.EMPTY_OBJECT_ARRAY);
    }
    
    public E make(final String value, final int ordinal) {
        return this.make(value, ordinal, EnumBuster.EMPTY_CLASS_ARRAY, EnumBuster.EMPTY_OBJECT_ARRAY);
    }
    
    public E make(final String value, final int ordinal, final Class[] additionalTypes, final Object[] additional) {
        try {
            this.undoStack.push(new Memento((Memento)null));
            final ConstructorAccessor ca = this.findConstructorAccessor(additionalTypes, this.clazz);
            return this.constructEnum(this.clazz, ca, value, ordinal, additional);
        }
        catch (Exception e) {
            throw new IllegalArgumentException("Could not create enum", e);
        }
    }
    
    public void addByValue(final E e) {
        try {
            this.undoStack.push(new Memento((Memento)null));
            final Field valuesField = this.findValuesField();
            final Enum[] values = this.values();
            for (int i = 0; i < values.length; ++i) {
                final E value = (E)values[i];
                if (value.name().equals(e.name())) {
                    this.setOrdinal(e, value.ordinal());
                    this.replaceConstant((E)(values[i] = e));
                    return;
                }
            }
            final Enum[] newValues = Arrays.copyOf(values, values.length + 1);
            newValues[newValues.length - 1] = e;
            ReflectionHelper.setStaticFinalField(valuesField, newValues);
            final int ordinal = newValues.length - 1;
            this.setOrdinal(e, ordinal);
            this.addSwitchCase();
        }
        catch (Exception ex) {
            throw new IllegalArgumentException("Could not set the enum", ex);
        }
    }
    
    public boolean deleteByValue(final E e) {
        if (e == null) {
            throw new NullPointerException();
        }
        try {
            this.undoStack.push(new Memento((Memento)null));
            final Enum[] values = this.values();
            for (int i = 0; i < values.length; ++i) {
                final E value = (E)values[i];
                if (value.name().equals(e.name())) {
                    final Enum[] newValues = Arrays.copyOf(values, values.length - 1);
                    System.arraycopy(values, i + 1, newValues, i, values.length - i - 1);
                    for (int j = i; j < newValues.length; ++j) {
                        this.setOrdinal((E)newValues[j], j);
                    }
                    final Field valuesField = this.findValuesField();
                    ReflectionHelper.setStaticFinalField(valuesField, newValues);
                    this.removeSwitchCase(i);
                    this.blankOutConstant(e);
                    return true;
                }
            }
        }
        catch (Exception ex) {
            throw new IllegalArgumentException("Could not set the enum", ex);
        }
        return false;
    }
    
    public void restore() {
        while (this.undo()) {}
    }
    
    public boolean undo() {
        try {
            final Memento memento = this.undoStack.poll();
            if (memento == null) {
                return false;
            }
            memento.undo();
            return true;
        }
        catch (Exception e) {
            throw new IllegalStateException("Could not undo", e);
        }
    }
    
    private ConstructorAccessor findConstructorAccessor(final Class[] additionalParameterTypes, final Class<E> clazz) throws NoSuchMethodException {
        final Class[] parameterTypes = new Class[additionalParameterTypes.length + 2];
        parameterTypes[0] = String.class;
        parameterTypes[1] = Integer.TYPE;
        System.arraycopy(additionalParameterTypes, 0, parameterTypes, 2, additionalParameterTypes.length);
        final Constructor<E> cstr = clazz.getDeclaredConstructor((Class<?>[])parameterTypes);
        return this.reflection.newConstructorAccessor(cstr);
    }
    
    private E constructEnum(final Class<E> clazz, final ConstructorAccessor ca, final String value, final int ordinal, final Object[] additional) throws Exception {
        final Object[] parms = new Object[additional.length + 2];
        parms[0] = value;
        parms[1] = ordinal;
        System.arraycopy(additional, 0, parms, 2, additional.length);
        return clazz.cast(ca.newInstance(parms));
    }
    
    private void addSwitchCase() {
        try {
            for (final Field switchField : this.switchFields) {
                int[] switches = (int[])switchField.get(null);
                switches = Arrays.copyOf(switches, switches.length + 1);
                ReflectionHelper.setStaticFinalField(switchField, switches);
            }
        }
        catch (Exception e) {
            throw new IllegalArgumentException("Could not fix switch", e);
        }
    }
    
    private void replaceConstant(final E e) throws IllegalAccessException, NoSuchFieldException {
        final Field[] fields = this.clazz.getDeclaredFields();
        Field[] array;
        for (int length = (array = fields).length, i = 0; i < length; ++i) {
            final Field field = array[i];
            if (field.getName().equals(e.name())) {
                ReflectionHelper.setStaticFinalField(field, e);
            }
        }
    }
    
    private void blankOutConstant(final E e) throws IllegalAccessException, NoSuchFieldException {
        final Field[] fields = this.clazz.getDeclaredFields();
        Field[] array;
        for (int length = (array = fields).length, i = 0; i < length; ++i) {
            final Field field = array[i];
            if (field.getName().equals(e.name())) {
                ReflectionHelper.setStaticFinalField(field, null);
            }
        }
    }
    
    private void setOrdinal(final E e, final int ordinal) throws NoSuchFieldException, IllegalAccessException {
        final Field ordinalField = Enum.class.getDeclaredField("ordinal");
        ordinalField.setAccessible(true);
        ordinalField.set(e, ordinal);
    }
    
    private Field findValuesField() throws NoSuchFieldException {
        final Field valuesField = this.clazz.getDeclaredField("$VALUES");
        valuesField.setAccessible(true);
        return valuesField;
    }
    
    private Collection<Field> findRelatedSwitchFields(final Class[] switchUsers) {
        final Collection<Field> result = new ArrayList<Field>();
        try {
            for (final Class switchUser : switchUsers) {
                final Class[] clazzes = switchUser.getDeclaredClasses();
                Class[] array;
                for (int length2 = (array = clazzes).length, j = 0; j < length2; ++j) {
                    final Class suspect = array[j];
                    final Field[] fields = suspect.getDeclaredFields();
                    Field[] array2;
                    for (int length3 = (array2 = fields).length, k = 0; k < length3; ++k) {
                        final Field field = array2[k];
                        if (field.getName().startsWith("$SwitchMap$" + this.clazz.getSimpleName())) {
                            field.setAccessible(true);
                            result.add(field);
                        }
                    }
                }
            }
        }
        catch (Exception e) {
            throw new IllegalArgumentException("Could not fix switch", e);
        }
        return result;
    }
    
    private void removeSwitchCase(final int ordinal) {
        try {
            for (final Field switchField : this.switchFields) {
                final int[] switches = (int[])switchField.get(null);
                final int[] newSwitches = Arrays.copyOf(switches, switches.length - 1);
                System.arraycopy(switches, ordinal + 1, newSwitches, ordinal, switches.length - ordinal - 1);
                ReflectionHelper.setStaticFinalField(switchField, newSwitches);
            }
        }
        catch (Exception e) {
            throw new IllegalArgumentException("Could not fix switch", e);
        }
    }
    
    private E[] values() throws NoSuchFieldException, IllegalAccessException {
        final Field valuesField = this.findValuesField();
        return (E[])valuesField.get(null);
    }
    
    private class Memento
    {
        private final E[] values;
        private final Map<Field, int[]> savedSwitchFieldValues;
        
        private Memento() {
            this.savedSwitchFieldValues = new HashMap<Field, int[]>();
            try {
                this.values = EnumBuster.this.values().clone();
                for (final Field switchField : EnumBuster.this.switchFields) {
                    final int[] switchArray = (int[])switchField.get(null);
                    this.savedSwitchFieldValues.put(switchField, switchArray.clone());
                }
            }
            catch (Exception e) {
                throw new IllegalArgumentException("Could not create the class", e);
            }
        }
        
        private void undo() throws NoSuchFieldException, IllegalAccessException {
            final Field valuesField = EnumBuster.this.findValuesField();
            ReflectionHelper.setStaticFinalField(valuesField, this.values);
            for (int i = 0; i < this.values.length; ++i) {
                EnumBuster.this.setOrdinal(this.values[i], i);
            }
            final Map<String, E> valuesMap = new HashMap<String, E>();
            Enum[] values;
            for (int length = (values = this.values).length, j = 0; j < length; ++j) {
                final E e = (E)values[j];
                valuesMap.put(e.name(), e);
            }
            final Field[] constantEnumFields = EnumBuster.this.clazz.getDeclaredFields();
            Field[] array;
            for (int length2 = (array = constantEnumFields).length, k = 0; k < length2; ++k) {
                final Field constantEnumField = array[k];
                final E en = valuesMap.get(constantEnumField.getName());
                if (en != null) {
                    ReflectionHelper.setStaticFinalField(constantEnumField, en);
                }
            }
            for (final Map.Entry<Field, int[]> entry : this.savedSwitchFieldValues.entrySet()) {
                final Field field = entry.getKey();
                final int[] mappings = entry.getValue();
                ReflectionHelper.setStaticFinalField(field, mappings);
            }
        }
    }
}
