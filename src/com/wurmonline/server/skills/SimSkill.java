// 
// Decompiled by Procyon v0.5.30
// 

package com.wurmonline.server.skills;

import java.io.IOException;

public class SimSkill extends DbSkill
{
    public SimSkill(final long aId, final Skills aParent) throws IOException {
        super(aId, aParent);
    }
    
    public SimSkill(final long aId, final Skills aParent, final int aNumber, final double aKnowledge, final double aMinimum, final long aLastused) {
        super(aId, aParent, aNumber, aKnowledge, aMinimum, aLastused);
    }
    
    public SimSkill(final long aId, final int aNumber, final double aKnowledge, final double aMinimum, final long aLastused) {
        super(aId, aNumber, aKnowledge, aMinimum, aLastused);
    }
    
    public Skills getParent() {
        return this.parent;
    }
}
