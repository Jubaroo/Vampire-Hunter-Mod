// 
// Decompiled by Procyon v0.5.30
// 

package com.wurmonline.server.questions;

import com.friya.tools.BmlForm;
import com.friya.wurmonline.server.vamps.ChatCommands;
import com.friya.wurmonline.server.vamps.Toplist;
import com.wurmonline.server.creatures.Creature;

import java.util.Properties;

public class ToplistVampsQuestion extends Question
{
    private boolean properlySent;
    
    ToplistVampsQuestion(final Creature aResponder, final String aTitle, final String aQuestion, final int aType, final long aTarget) {
        super(aResponder, aTitle, aQuestion, aType, aTarget);
        this.properlySent = false;
    }
    
    public ToplistVampsQuestion(final Creature aResponder, final String aTitle, final String aQuestion, final long aTarget) {
        super(aResponder, aTitle, aQuestion, 79, aTarget);
        this.properlySent = false;
    }
    
    public void answer(final Properties answer) {
        if (!this.properlySent) {
            return;
        }
    }
    
    public void sendQuestion() {
        this.properlySent = true;
        final BmlForm f = new BmlForm("");
        f.addHidden("id", new StringBuilder().append(this.id).toString());
        final int listSize = 15;
        final Toplist toplist = ChatCommands.getToplistVampsData(listSize);
        f.beginTable(listSize, new String[] { "Position            ", "Vampire alias                  ", "Rating           " });
        for (int i = 0; i < toplist.added; ++i) {
            f.addLabel(new StringBuilder().append(i + 1).toString());
            f.addLabel(toplist.getName(i));
            f.addLabel(new StringBuilder().append(toplist.getScore(i)).toString());
        }
        f.endTable();
        f.addText(" \n");
        f.addText(" \n");
        f.beginHorizontalFlow();
        f.addButton("Close", "accept");
        f.endHorizontalFlow();
        f.addText(" \n");
        f.addText(" \n");
        this.getResponder().getCommunicator().sendBml(370, 520, true, true, f.toString(), 200, 150, 150, this.title);
    }
}
