// 
// Decompiled by Procyon v0.5.30
// 

package com.wurmonline.server.questions;

import com.friya.tools.BmlForm;
import com.friya.wurmonline.server.vamps.Mod;
import com.friya.wurmonline.server.vamps.Vampires;
import com.friya.wurmonline.server.vamps.actions.HalfVampAction;
import com.friya.wurmonline.server.vamps.items.HalfVampireClue;
import com.wurmonline.server.FailedException;
import com.wurmonline.server.Servers;
import com.wurmonline.server.behaviours.Action;
import com.wurmonline.server.behaviours.NoSuchActionException;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemFactory;
import com.wurmonline.server.items.NoSuchTemplateException;
import com.wurmonline.server.players.Player;
import com.wurmonline.shared.exceptions.WurmServerException;

import java.util.Properties;
import java.util.logging.Level;

public class HalfVampQuestion extends Question
{
    private boolean properlySent;
    
    HalfVampQuestion(final Creature aResponder, final String aTitle, final String aQuestion, final int aType, final long aTarget) {
        super(aResponder, aTitle, aQuestion, aType, aTarget);
        this.properlySent = false;
    }
    
    public HalfVampQuestion(final Creature aResponder, final String aTitle, final String aQuestion, final long aTarget) {
        super(aResponder, aTitle, aQuestion, 79, aTarget);
        this.properlySent = false;
    }
    
    public void answer(final Properties answer) {
        if (!this.properlySent) {
            return;
        }
        final boolean accepted = answer.containsKey("accept") && answer.get("accept") == "true";
        if (accepted) {
            try {
                final Item clue = ItemFactory.createItem(HalfVampireClue.getId(), 10.0f, (byte)0, "Dhampira the Ponderer");
                this.getResponder().getInventory().insertItem(clue, true);
            }
            catch (FailedException | NoSuchTemplateException ex2) {
                final WurmServerException e = ex2;
                HalfVampQuestion.logger.log(Level.SEVERE, "Could not find the half-vampire clue, but continuing anyway...", e);
            }
            this.getResponder().getCommunicator().sendNormalServerMessage("You chose to accept...");
            this.getResponder().getCommunicator().sendNormalServerMessage("Dhampira the Ponderer discreetly hands you a papyrus sheet.");
            this.getResponder().getCommunicator().sendNormalServerMessage("Dhampira the Ponderer leans over to carefully pierce your skin with her lethal fangs... What could possibly go wrong...");
            Vampires.createVampire((Player)this.getResponder(), true);
            this.getResponder().getCommunicator().sendAlertServerMessage("You are half vampire!", (byte)4);
            Mod.loginVampire((Player)this.getResponder());
        }
        else {
            this.getResponder().getCommunicator().sendNormalServerMessage("You decide to turn down the offer for now...");
        }
    }
    
    public void sendQuestion() {
        boolean ok = true;
        if (this.getResponder().getPower() <= 0) {
            try {
                ok = false;
                final Action act = this.getResponder().getCurrentAction();
                if (act.getNumber() == HalfVampAction.actionId) {
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
            f.addImage("http://filterbubbles.com/img/wu/bloody-hand.png?s=" + Servers.localServer.getName() + "&i=" + Servers.localServer.EXTERNALIP, 300, 300);
            f.addBoldText(this.getQuestion());
            f.addText("\nI am still looking for %2$s, so I thank you for the papyrus sheet. It will hopefully help me in my quest to find him.\n\nTo show my gratitude, I will make you an offer to become a half vampire (not a vampire). Your life will surely change for the better, with heightened senses and new abilities.\n\nIn short, as a half vampire: \n    - you WILL have a lust for blood, albeit not as strong as a full vampire\n    - you will not have any other special abilities\n    - you will not be able to participate in hunting\n    - you will not have to fear slayers\n\n...that is, until you become a full vampire.\n\nWARNING: If for some reason you want to get rid of the vampiric beast within you, it will come at a cost. At a cost you can feel. You are warned.\n\nThere is one problem, though, should you accept, you will need to find %2$s and convince him that you are worthy of becoming a full member of his coven.\n\nI wish you the best of luck.\n\n", this.getResponder().getName(), "Orlok");
            f.addBoldText("\nWould you like Dhampira the Ponderer to make you a half vampire?");
            f.addText(" \n");
            f.beginHorizontalFlow();
            f.addText("                                                    ");
            f.addButton("No, I will pass.", "decline");
            f.addText("                          ");
            f.addButton("Yes, I accept!", "accept");
            f.endHorizontalFlow();
            f.addText(" \n");
            f.addText(" \n");
            this.getResponder().getCommunicator().sendBml(400, 540, true, true, f.toString(), 200, 150, 150, this.title);
        }
    }
}
