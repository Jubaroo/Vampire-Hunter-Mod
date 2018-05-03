// 
// Decompiled by Procyon v0.5.30
// 

package com.friya.wurmonline.server.vamps.actions;

import com.friya.wurmonline.server.vamps.BloodlessHusk;
import com.friya.wurmonline.server.vamps.Locate;
import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.Players;
import com.wurmonline.server.WurmCalendar;
import com.wurmonline.server.behaviours.Action;
import com.wurmonline.server.behaviours.ActionEntry;
import com.wurmonline.server.behaviours.NoSuchActionException;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.skills.Skill;
import org.gotti.wurmunlimited.modsupport.actions.ActionPerformer;
import org.gotti.wurmunlimited.modsupport.actions.BehaviourProvider;
import org.gotti.wurmunlimited.modsupport.actions.ModAction;
import org.gotti.wurmunlimited.modsupport.actions.ModActions;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TraceAction implements ModAction
{
    private static Logger logger;
    private static short actionId;
    private final ActionEntry actionEntry;
    private final int castTime = 50;
    
    static {
        TraceAction.logger = Logger.getLogger(TraceAction.class.getName());
    }
    
    public static short getActionId() {
        return TraceAction.actionId;
    }
    
    public TraceAction() {
        TraceAction.logger.log(Level.INFO, "TraceAction()");
        TraceAction.actionId = (short)ModActions.getNextActionId();
        ModActions.registerAction(this.actionEntry = ActionEntry.createEntry(TraceAction.actionId, "Trace", "tracing", new int[] { 6 }));
    }
    
    public BehaviourProvider getBehaviourProvider() {
        return new BehaviourProvider() {
            public List<ActionEntry> getBehavioursFor(final Creature performer, final Item source, final Item object) {
                return this.getBehavioursFor(performer, object);
            }
            
            public List<ActionEntry> getBehavioursFor(final Creature performer, final Item object) {
                if (performer instanceof Player && object != null && object.getTemplateId() == 272 && object.getName().startsWith("bloodless husk of ")) {
                    return Arrays.asList(TraceAction.this.actionEntry);
                }
                return null;
            }
        };
    }
    
    public ActionPerformer getActionPerformer() {
        return new ActionPerformer() {
            public short getActionId() {
                return TraceAction.actionId;
            }
            
            public boolean action(final Action act, final Creature performer, final Item target, final short action, final float counter) {
                return TraceAction.this.trace(act, performer, target, counter);
            }
            
            public boolean action(final Action act, final Creature performer, final Item source, final Item target, final short action, final float counter) {
                return this.action(act, performer, target, action, counter);
            }
        };
    }
    
    private String getWurmTimeAgo(final long timeStamp, final boolean evenLowerRes) {
        if (evenLowerRes) {
            final String s = this.getWurmTimeAgo(timeStamp);
            final String[] segs = s.split(" ");
            final StringBuffer ret = new StringBuffer();
            if (segs[0].equals("1")) {
                ret.append("a");
                if (segs[1].equals("hour")) {
                    ret.append("n");
                }
                ret.append(" ");
                ret.append(segs[1]);
                ret.append(" ago");
            }
            else {
                ret.append(segs[1]);
                ret.append("s ago");
            }
            return ret.toString();
        }
        return this.getWurmTimeAgo(timeStamp);
    }
    
    private String getWurmTimeAgo(final long timeStamp) {
        final long unixTime = WurmCalendar.getCurrentTime();
        final String[] periods = { " second", " minute", " hour", " day", " week", " month", " year", " decade" };
        final double[] lengths = { 60.0, 60.0, 24.0, 7.0, 4.35, 12.0, 10.0 };
        long timeDiffernce = unixTime - timeStamp;
        final String tense = "ago";
        int j;
        for (j = 0; timeDiffernce >= lengths[j] && j < lengths.length - 1; timeDiffernce /= (long)lengths[j], ++j) {}
        return String.valueOf(timeDiffernce) + periods[j] + " " + tense;
    }
    
    private boolean trace(final Action act, final Creature performer, final Item target, final float counter) {
        if (!performer.isPlayer() || target == null || target.getTemplateId() != 272 || !target.getName().startsWith("bloodless husk of ")) {
            performer.getCommunicator().sendNormalServerMessage("You can't seem to figure out how that would work.");
            return true;
        }
        try {
            if (counter == 1.0f) {
                final int tmpTime = 50;
                performer.getCurrentAction().setTimeLeft(tmpTime);
                performer.sendActionControl("tracing", true, tmpTime);
                return false;
            }
            if (counter * 10.0f <= act.getTimeLeft()) {
                return false;
            }
        }
        catch (NoSuchActionException e) {
            return true;
        }
        try {
            final Creature vampire = Players.getInstance().getPlayer(BloodlessHusk.getBloodSucker(target));
            performer.getCommunicator().sendNormalServerMessage("This creature was killed " + this.getWurmTimeAgo(target.creationDate, true) + ".");
            performer.getCommunicator().sendNormalServerMessage("There are clues telling you that the beast who did this went " + Locate.getCompassDirection(target, vampire) + ".");
            final Skill s = performer.getSkills().getSkillOrLearn(2147483639);
            s.skillCheck(1.0, 0.0, false, 1.0f);
        }
        catch (NoSuchPlayerException e2) {
            performer.getCommunicator().sendNormalServerMessage("You can't seem to figure out which direction they went.");
            return true;
        }
        return true;
    }
}
