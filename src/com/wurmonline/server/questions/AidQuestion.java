// 
// Decompiled by Procyon v0.5.30
// 

package com.wurmonline.server.questions;

import com.friya.wurmonline.server.vamps.Mod;
import com.friya.wurmonline.server.vamps.Vampires;
import com.friya.wurmonline.server.vamps.actions.AidAction;
import com.wurmonline.server.NoSuchItemException;
import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.Server;
import com.wurmonline.server.behaviours.Action;
import com.wurmonline.server.behaviours.NoSuchActionException;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.NoSuchCreatureException;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.players.PlayerInfo;
import com.wurmonline.server.players.PlayerInfoFactory;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.zones.NoSuchZoneException;
import com.wurmonline.shared.exceptions.WurmServerException;

import java.util.Properties;
import java.util.logging.Level;

public class AidQuestion extends Question
{
    private boolean properlySent;
    private double power;
    private Item rat;
    
    AidQuestion(final Creature aResponder, final String aTitle, final String aQuestion, final int aType, final long aTarget) {
        super(aResponder, aTitle, aQuestion, aType, aTarget);
        this.properlySent = false;
        this.rat = null;
    }
    
    public AidQuestion(final Creature aResponder, final String aTitle, final String aQuestion, final long aTarget, final double power, final Item rat) {
        super(aResponder, aTitle, aQuestion, 79, aTarget);
        this.properlySent = false;
        this.rat = null;
        this.power = power;
        this.rat = rat;
    }
    
    private boolean aid(final String name, final Creature performer, final double power) {
        final PlayerInfo pinf = PlayerInfoFactory.createPlayerInfo(name);
        if (pinf == null || !pinf.loaded) {
            return false;
        }
        Creature target = null;
        try {
            target = Server.getInstance().getCreature(pinf.wurmId);
        }
        catch (NoSuchPlayerException | NoSuchCreatureException ex3) {
            final WurmServerException ex;
            final WurmServerException e = ex;
            performer.getCommunicator().sendNormalServerMessage("Your vampire senses reach out into the world, but you can't feel the presence of " + name + " anywhere.");
            return true;
        }
        try {
            if (this.rat == null || performer.getInventory() == null || this.rat.getParent() == null || this.rat.getParent().getWurmId() != performer.getInventory().getWurmId()) {
                performer.getCommunicator().sendNormalServerMessage("You must carry it to be able to send it.");
                return true;
            }
        }
        catch (NoSuchItemException e2) {
            performer.getCommunicator().sendNormalServerMessage("You must carry it to be able to send it.");
            return true;
        }
        performer.getStatus().modifyStamina((float)(int)(performer.getStatus().getStamina() * 0.3f));
        try {
            this.rat.putItemInfrontof(target);
            performer.getCommunicator().sendNormalServerMessage("You argue a bit with the vermin and request its help. Disgruntledly, the rat begins to change form as you see wings sprout from it's body.");
            Mod.actionNotify(performer, "Transformed into a vampire bat, it quickly flies away in search of " + target.getName() + ".", "A vampire bat swoops away from " + performer.getName() + ".", "A vampire bat swoops away from a shadowy form.");
            Mod.actionNotify(target, "You see a vampire bat flying towards you from a distance.  After circling several times, it abruptly lands in front of you. Before your eyes, it transforms into a small rat. You see that it's been branded by " + Vampires.getVampire(performer.getWurmId()).getAlias() + ".", "A vampire bat swoops in and lands in front of %NAME.", "A vampire bat swoops in and lands in front of a shadowy form.");
            final Skill s = performer.getSkills().getSkillOrLearn(2147483635);
            s.skillCheck(1.0, 0.0, false, 1.0f);
        }
        catch (NoSuchCreatureException | NoSuchItemException | NoSuchPlayerException | NoSuchZoneException ex4) {
            final WurmServerException ex2;
            final WurmServerException e = ex2;
            performer.getCommunicator().sendNormalServerMessage("Could not send the vermin. This could be a Friya booboo, tell admins or something!");
            AidQuestion.logger.log(Level.SEVERE, "Could not send rat, for some reason", e);
            e.printStackTrace();
        }
        return true;
    }
    
    public void answer(final Properties aAnswers) {
        if (!this.properlySent) {
            return;
        }
        boolean found = false;
        final String name = aAnswers.getProperty("name");
        if (name != null && name.length() > 1) {
            found = this.aid(name, this.getResponder(), this.power);
        }
        if (!found) {
            this.getResponder().getCommunicator().sendNormalServerMessage("Not found.");
        }
    }
    
    public void sendQuestion() {
        boolean ok = true;
        if (this.getResponder().getPower() <= 0) {
            try {
                ok = false;
                final Action act = this.getResponder().getCurrentAction();
                if (act.getNumber() == AidAction.actionId) {
                    ok = true;
                }
            }
            catch (NoSuchActionException act2) {
                throw new RuntimeException("No such action", act2);
            }
        }
        if (ok) {
            this.properlySent = true;
            final StringBuilder sb = new StringBuilder();
            sb.append(this.getBmlHeader());
            sb.append("text{text='Who are you looking to aid?'};");
            sb.append("label{text='Name:'};input{id='name';maxchars='40';text=\"\"};");
            sb.append(this.createAnswerButton2());
            this.getResponder().getCommunicator().sendBml(300, 300, true, true, sb.toString(), 200, 200, 200, this.title);
        }
    }
}
