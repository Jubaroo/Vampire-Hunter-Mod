// 
// Decompiled by Procyon v0.5.30
// 

package com.wurmonline.server.skills;

public class SkillsProxy
{
    public static Skills getParent(final Skill s) {
        return s.parent;
    }
    
    public static long getSkillOwnerId(final Skill s) {
        return getParent(s).id;
    }
}
