// 
// Decompiled by Procyon v0.5.30
// 

package com.wurmonline.server.questions;

import com.friya.tools.BmlForm;
import com.friya.wurmonline.server.vamps.EventDispatcher;
import com.friya.wurmonline.server.vamps.Vampires;
import com.friya.wurmonline.server.vamps.actions.DevampAction;
import com.friya.wurmonline.server.vamps.events.DelayedDeVamp;
import com.friya.wurmonline.server.vamps.events.DelayedMessage;
import com.friya.wurmonline.server.vamps.events.EventOnce;
import com.wurmonline.server.Servers;
import com.wurmonline.server.behaviours.Action;
import com.wurmonline.server.behaviours.NoSuchActionException;
import com.wurmonline.server.creatures.Creature;

import java.util.Properties;

public class DeVampQuestion extends Question
{
    private boolean properlySent;
    
    DeVampQuestion(final Creature aResponder, final String aTitle, final String aQuestion, final int aType, final long aTarget) {
        super(aResponder, aTitle, aQuestion, aType, aTarget);
        this.properlySent = false;
    }
    
    public DeVampQuestion(final Creature aResponder, final String aTitle, final String aQuestion, final long aTarget) {
        super(aResponder, aTitle, aQuestion, 79, aTarget);
        this.properlySent = false;
    }
    
    public void answer(final Properties answer) {
        if (!this.properlySent) {
            return;
        }
        final boolean accepted = answer.containsKey("accept") && answer.get("accept") == "true";
        if (accepted) {
            final boolean success = Vampires.deVamp(this.getResponder());
            if (success) {
                this.getResponder().getStatus().setStunned(30.0f);
                this.getResponder().getCommunicator().sendAlertServerMessage("You quaff the potion. Was this a good idea?", (byte)4);
                EventDispatcher.add(new DelayedMessage(4, EventOnce.Unit.SECONDS, this.getResponder(), "#*(&$_)@&#*&@#"));
                EventDispatcher.add(new DelayedMessage(8, EventOnce.Unit.SECONDS, this.getResponder(), "You realize with grim certainty, that you are about to die..."));
                EventDispatcher.add(new DelayedMessage(12, EventOnce.Unit.SECONDS, this.getResponder(), "There is a shattering scream from within your head as the beast is driven from your lifeless body."));
                EventDispatcher.add(new DelayedMessage(18, EventOnce.Unit.SECONDS, this.getResponder(), "Pain, the likes of which you haven't felt since you last were mortal floods your body."));
                EventDispatcher.add(new DelayedMessage(22, EventOnce.Unit.SECONDS, this.getResponder(), "Your vision fades and you slowly die as your lifeblood spills."));
                EventDispatcher.add(new DelayedDeVamp(26, EventOnce.Unit.SECONDS, this.getResponder(), "Finally your soul rises from your body, and you find peace, leaving this unnatural form behind forever!"));
            }
            else {
                this.getResponder().getCommunicator().sendNormalServerMessage("HUH! You could not be de-vamped. Please talk to an admin, there should be some errors in their logs.");
            }
        }
        else {
            this.getResponder().getCommunicator().sendNormalServerMessage("You choose not to...");
        }
    }
    
    public void sendQuestion() {
        boolean ok = true;
        if (this.getResponder().getPower() <= 0) {
            try {
                ok = false;
                final Action act = this.getResponder().getCurrentAction();
                if (act.getNumber() == DevampAction.actionId) {
                    ok = true;
                }
            }
            catch (NoSuchActionException act2) {
                throw new RuntimeException("No such action", act2);
            }
        }
        if (ok) {
            this.properlySent = true;
            final BmlForm f = new BmlForm("");
            f.addHidden("id", new StringBuilder().append(this.id).toString());
            f.addImage("http://filterbubbles.com/img/wu/devamp-potion.png?s=" + Servers.localServer.getName() + "&i=" + Servers.localServer.EXTERNALIP, 200, 200);
            f.addBoldText(this.getQuestion());
            f.addText("\nYou are one of them. A dweller of darkness. A vampire.\n\nI can cure you, but be warned, not only will it kill you, it will cost you dearly too. A lot more than you could possibly imagine.\n\n\n(Really, no joke ... you will lose a lot of skill. You may also not be able to become a vampire again for a some time! You ARE warned!)\n\n", this.getResponder().getName(), "Orlok");
            f.addBoldText("\nYou will no longer be a vampire; are you sure you want to drink van Helsing's potion?");
            f.addText(" \n");
            f.beginHorizontalFlow();
            f.addButton("No thank you (the safe answer).", "decline");
            f.addText("                          ");
            f.addButton("Yes, I want to drink the potion. Cure me!", "accept");
            f.endHorizontalFlow();
            f.addText(" \n");
            f.addText(" \n");
            this.getResponder().getCommunicator().sendBml(550, 500, true, true, f.toString(), 150, 150, 200, this.title);
        }
    }
}
