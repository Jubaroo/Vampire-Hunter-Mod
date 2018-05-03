// 
// Decompiled by Procyon v0.5.30
// 

package com.friya.tools;

import java.util.ArrayList;
import java.util.Scanner;

public class SqlHelper
{
    public static ArrayList<String> getStatementsFromBatch(final String sqlBatch) {
        final ArrayList<String> ret = new ArrayList<String>();
        final Scanner s = new Scanner(sqlBatch);
        s.useDelimiter("/\\*[\\s\\S]*?\\*/|--[^\\r\\n]*");
        try {
            final StringBuffer currentStatement = new StringBuffer();
            while (s.hasNext()) {
                String line = s.next();
                if (line.startsWith("/*!") && line.endsWith("*/")) {
                    final int i = line.indexOf(32);
                    line = line.substring(i + 1, line.length() - " */".length());
                }
                if (line.trim().length() > 0) {
                    currentStatement.append(line);
                    if (!line.contains(";")) {
                        continue;
                    }
                    final String[] tmp = currentStatement.toString().split(";");
                    String[] array;
                    for (int length = (array = tmp).length, j = 0; j < length; ++j) {
                        final String ln = array[j];
                        if (ln.trim().length() != 0) {
                            ret.add(ln);
                        }
                    }
                    currentStatement.setLength(0);
                }
            }
        }
        finally {
            s.close();
        }
        s.close();
        return ret;
    }
}
