// 
// Decompiled by Procyon v0.5.30
// 

package com.wurmonline.server.creatures.ai;

import com.wurmonline.server.creatures.Creature;

public class AiProxy
{
    public static void clearChatManagerChats(final ChatManager cm) {
        cm.mychats.clear();
        cm.localchats.clear();
        cm.unansweredLChats.clear();
        cm.receivedchats.clear();
        cm.unansweredChats.clear();
        cm.localChats.clear();
    }
    
    public static Creature getChatManagerOwner(final ChatManager cm) {
        return cm.owner;
    }
}
