// 
// Decompiled by Procyon v0.5.30
// 

package com.friya.wurmonline.server.vamps;

import com.wurmonline.server.players.Player;

public class Toplist
{
    private String[] names;
    private long[] scores;
    public int added;
    
    public Toplist(final int listSize) {
        this.added = 0;
        this.names = new String[listSize];
        this.scores = new long[listSize];
    }
    
    public void sendTo(final Player p) {
        for (int i = 0; i < this.added; ++i) {
            p.getCommunicator().sendNormalServerMessage(String.valueOf(i + 1) + "    " + this.names[i] + ", " + this.scores[i] + " rating");
        }
    }
    
    @Override
    public String toString() {
        final StringBuffer s = new StringBuffer();
        for (int i = 0; i < this.added; ++i) {
            s.append(String.valueOf(i + 1) + " " + String.format("%1$20s %2$20s", this.names[i], this.scores[i]));
        }
        return s.toString();
    }
    
    void addNameScore(final String name, final long score) {
        if (this.added == this.names.length) {
            throw new IndexOutOfBoundsException("Tried to add too many scores to toplist, bailing");
        }
        this.names[this.added] = name;
        this.scores[this.added] = score;
        ++this.added;
    }
    
    public String getName(final int index) {
        return this.names[index];
    }
    
    public long getScore(final int index) {
        return this.scores[index];
    }
    
    public String[] getNames() {
        return this.names;
    }
    
    public long[] getScores() {
        return this.scores;
    }
}
