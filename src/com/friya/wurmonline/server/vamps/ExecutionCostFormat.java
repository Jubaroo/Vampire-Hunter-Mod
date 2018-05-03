// 
// Decompiled by Procyon v0.5.30
// 

package com.friya.wurmonline.server.vamps;

import java.text.DecimalFormat;
import java.text.FieldPosition;

public class ExecutionCostFormat extends DecimalFormat
{
    @Override
    public StringBuffer format(final double number, final StringBuffer result, final FieldPosition fieldPosition) {
        Mod.totalExecutionCost += number;
        return super.format(number, result, fieldPosition);
    }
}
