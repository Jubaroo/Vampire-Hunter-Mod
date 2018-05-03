// 
// Decompiled by Procyon v0.5.30
// 

package com.friya.wurmonline.server.vamps;

import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;

public interface DynamicExaminable
{
    String examine(final Item p0, final Creature p1);
    
    int getTemplateId();
}
