// 
// Decompiled by Procyon v0.5.30
// 

package com.wurmonline.server.players;

import java.util.Set;

public class PlayersProxy
{
    public static Set<Titles.Title> getTitles(final PlayerInfo pi) {
        return pi.titles;
    }
}
