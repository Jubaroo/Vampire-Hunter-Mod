// 
// Decompiled by Procyon v0.5.30
// 

package com.wurmonline.server.questions;

import com.friya.wurmonline.server.vamps.Locate;
import com.friya.wurmonline.server.vamps.Stakers;
import com.friya.wurmonline.server.vamps.actions.CrownFindAction;
import com.wurmonline.server.Items;
import com.wurmonline.server.NoSuchItemException;
import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.Server;
import com.wurmonline.server.behaviours.Action;
import com.wurmonline.server.behaviours.NoSuchActionException;
import com.wurmonline.server.behaviours.Terraforming;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.NoSuchCreatureException;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.players.PlayerInfo;
import com.wurmonline.server.players.PlayerInfoFactory;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.spells.PinpointHumanoid;
import com.wurmonline.server.villages.Village;
import com.wurmonline.server.villages.Villages;
import com.wurmonline.shared.exceptions.WurmServerException;

import java.util.Properties;
import java.util.logging.Level;

public class PinpointHumanoidQuestion extends Question
{
    private boolean properlySent;
    private double power;
    public boolean ignoreNoLo;
    public boolean reverseFind;
    public String extraQuestionNote;
    private Item locateItem;
    
    public PinpointHumanoidQuestion(final Creature aResponder, final String aTitle, final String aQuestion, final int aType, final long aTarget) {
        super(aResponder, aTitle, aQuestion, (aType != 79) ? 79 : 79, aTarget);
        this.properlySent = false;
        this.ignoreNoLo = false;
        this.reverseFind = false;
        this.extraQuestionNote = "";
        this.locateItem = null;
        try {
            this.locateItem = Items.getItem(aTarget);
        }
        catch (NoSuchItemException e) {
            PinpointHumanoidQuestion.logger.log(Level.SEVERE, "Passed in item id into ctor is null");
        }
    }
    
    public PinpointHumanoidQuestion(final Creature aResponder, final String aTitle, final String aQuestion, final long aTarget, final boolean eyeVyn, final double power) {
        super(aResponder, aTitle, aQuestion, 79, aTarget);
        this.properlySent = false;
        this.ignoreNoLo = false;
        this.reverseFind = false;
        this.extraQuestionNote = "";
        this.locateItem = null;
        this.power = power;
    }
    
    private Village getNearestDeedOf(final int tileX, final int tileY) {
        final Village[] villages = Villages.getVillages();
        int closestX = 100000;
        int closestY = 100000;
        Village deed = null;
        Village[] array;
        for (int length = (array = villages).length, i = 0; i < length; ++i) {
            final Village v = array[i];
            final int dx = Math.abs(v.getTokenX() - tileX);
            final int dy = Math.abs(v.getTokenY() - tileY);
            if (dx <= closestX && dy <= closestY) {
                closestX = dx;
                closestY = dy;
                deed = v;
            }
        }
        return deed;
    }
    
    private String getDistanceString(final int mindist, final String name, final String direction, final boolean includeThe) {
        String toReturn = "";
        if (mindist < 1) {
            toReturn = "You are practically standing on " + name;
        }
        else if (mindist < 4) {
            toReturn = String.valueOf(name) + " is " + direction + " a few steps away";
        }
        else if (mindist < 6) {
            toReturn = String.valueOf(name) + " is " + direction + " a stone's throw away";
        }
        else if (mindist < 10) {
            toReturn = String.valueOf(name) + " is " + direction + " very close";
        }
        else if (mindist < 20) {
            toReturn = String.valueOf(name) + " is " + direction + " fairly close by";
        }
        else if (mindist < 50) {
            toReturn = String.valueOf(name) + " is some distance away " + direction;
        }
        else if (mindist < 200) {
            toReturn = String.valueOf(name) + " is quite some distance away " + direction;
        }
        else if (mindist < 500) {
            toReturn = String.valueOf(name) + " is rather a long distance away " + direction;
        }
        else if (mindist < 1000) {
            toReturn = String.valueOf(name) + " is pretty far away " + direction;
        }
        else if (mindist < 2000) {
            toReturn = String.valueOf(name) + " is far away " + direction;
        }
        else {
            toReturn = String.valueOf(name) + " is very far away " + direction;
        }
        return toReturn;
    }
    
    private String getLocationStringFor(final float rot, final int dir, final String performername) {
        int turnDir = 0;
        final float lRot = Creature.normalizeAngle(rot);
        if (lRot >= 337.5 || lRot < 22.5f) {
            turnDir = 0;
        }
        else {
            for (int x = 0; x < 8; ++x) {
                if (lRot < 22.5f + 45 * x) {
                    turnDir = x;
                    break;
                }
            }
        }
        String direction = "in front of " + performername;
        if (dir == turnDir + 1 || dir == turnDir - 7) {
            direction = "ahead of " + performername + " to the right";
        }
        else if (dir == turnDir + 2 || dir == turnDir - 6) {
            direction = "to the right of " + performername;
        }
        else if (dir == turnDir + 3 || dir == turnDir - 5) {
            direction = "behind " + performername + " to the right";
        }
        else if (dir == turnDir + 4 || dir == turnDir - 4) {
            direction = "behind " + performername;
        }
        else if (dir == turnDir + 5 || dir == turnDir - 3) {
            direction = "behind " + performername + " to the left";
        }
        else if (dir == turnDir + 6 || dir == turnDir - 2) {
            direction = "to the left of " + performername;
        }
        else if (dir == turnDir + 7 || dir == turnDir - 1) {
            direction = "ahead of " + performername + " to the left";
        }
        return direction;
    }
    
    private int getDir(final Creature performer, final int targetX, final int targetY) {
        final double newrot = Math.atan2((targetY << 2) + 2 - (int)performer.getStatus().getPositionY(), (targetX << 2) + 2 - (int)performer.getStatus().getPositionX());
        float attAngle = (float)(newrot * 57.29577951308232) + 90.0f;
        attAngle = Creature.normalizeAngle(attAngle);
        if (attAngle >= 337.5 || attAngle < 22.5f) {
            return 0;
        }
        for (int x = 0; x < 8; ++x) {
            if (attAngle < 22.5f + 45 * x) {
                return x;
            }
        }
        return 0;
    }
    
    private int getMeterDistance(final int fromX, final int fromY, final int toX, final int toY) {
        final int dx = Math.abs(fromX - toX);
        final int dy = Math.abs(fromY - toY);
        final int ret = (int)Math.sqrt(dx * dx + dy * dy) * 4;
        return ret;
    }
    
    private boolean isOnWaterTile(final Creature player) {
        return Terraforming.isTileUnderWater(player.getCurrentTileNum(), player.getTileX(), player.getTileY(), player.isOnSurface());
    }
    
    private String getHeightDifference(final Creature performer, final Creature target) {
        final int diff = (int)((performer.getPosZDirts() - target.getPosZDirts()) / 10.0f);
        if (diff > 0) {
            return String.valueOf(Math.abs(diff)) + " meters below";
        }
        if (diff < 0) {
            return String.valueOf(Math.abs(diff)) + " meters above";
        }
        return "at same altitude as";
    }
    
    private boolean pinpointHumanoid(final String name, final Creature performer, final double power) {
        boolean found = false;
        final Skill perception = performer.getSkills().getSkillOrLearn(2147483639);
        final double perceptionLevel = perception.getKnowledge();
        perception.skillCheck(1.0, 0.0, false, 1.0f);
        Creature target = null;
        final PlayerInfo pinf = PlayerInfoFactory.createPlayerInfo(name);
        if (pinf == null || !pinf.loaded) {
            return false;
        }
        try {
            target = Server.getInstance().getCreature(pinf.wurmId);
        }
        catch (NoSuchPlayerException | NoSuchCreatureException ex2) {
            final WurmServerException ex;
            final WurmServerException e = ex;
            e.printStackTrace();
        }
        if (target == null) {
            if (perceptionLevel >= 60.0) {
                performer.getCommunicator().sendNormalServerMessage(String.valueOf(name) + " is not logged in.");
            }
            return found;
        }
        final int centerx = target.getTileX();
        final int centery = target.getTileY();
        final int dx = Math.abs(centerx - performer.getTileX());
        final int dy = Math.abs(centery - performer.getTileY());
        final int mindist = (int)Math.sqrt(dx * dx + dy * dy);
        final int dir = this.getDir(performer, centerx, centery);
        final String simpleDirection = this.getLocationStringFor(performer.getStatus().getRotation(), dir, "you");
        final String simpleDistance = this.getDistanceString(mindist, target.getName(), simpleDirection, false);
        final StringBuffer str = new StringBuffer();
        found = true;
        if (this.ignoreNoLo || target.getBonusForSpellEffect((byte)29) <= power) {
            if (perceptionLevel >= 5.0) {
                str.append(String.valueOf(target.getName()) + " ");
                if (!target.isOnSurface()) {
                    str.append("is in a cave");
                }
                else if (perceptionLevel >= 25.0 && this.isOnWaterTile(target)) {
                    str.append("is in the water");
                }
                else {
                    str.append("is not in a cave");
                }
            }
            if (perceptionLevel >= 10.0) {
                final boolean legalLoc = Stakers.isAtLegalLocation(target);
                if (!legalLoc) {
                    str.append(" but is in a safe area. ");
                }
                else {
                    str.append(" but is in the wilderness. ");
                }
            }
            else {
                str.append(". ");
            }
            if (perceptionLevel < 30.0) {
                str.append(simpleDistance);
            }
            else {
                str.append(String.valueOf(target.isNotFemale() ? "He" : "She") + " is " + Locate.getCompassDirection(performer, target) + " of you");
            }
            if (perceptionLevel < 50.0) {
                if (perceptionLevel >= 5.0 && str.length() > 0) {
                    str.append(". ");
                }
            }
            else {
                str.append(", " + this.getMeterDistance(performer.getTileX(), performer.getTileY(), target.getTileX(), target.getTileY()) + " meters away, " + this.getHeightDifference(performer, target) + " you. ");
            }
            final Village deed = this.getNearestDeedOf(target.getTileX(), target.getTileY());
            if (deed == null && perceptionLevel >= 70.0) {
                str.append(String.valueOf(target.getName()) + " is near no known settlement.");
            }
            else if (perceptionLevel >= 90.0) {
                str.append(String.valueOf(target.getName()) + " is " + this.getMeterDistance(target.getTileX(), target.getTileY(), deed.getTokenX(), deed.getTokenY()) + " meters " + Locate.getCompassDirection(deed.getTokenX(), deed.getTokenY(), target.getTileX(), target.getTileY()) + " of the closest settlement, " + deed.getName() + ".");
            }
            else if (perceptionLevel >= 80.0) {
                str.append(String.valueOf(target.getName()) + " is " + this.getMeterDistance(target.getTileX(), target.getTileY(), deed.getTokenX(), deed.getTokenY()) + " meters from " + deed.getName() + ", the closest settlement.");
            }
            else if (perceptionLevel >= 70.0) {
                str.append(String.valueOf(target.getName()) + " is in the proximity of the settlement " + deed.getName() + ".");
            }
            final Skill s = target.getSkills().getSkillOrLearn(2147483639);
            s.skillCheck(1.0, 0.0, false, 1.0f);
            performer.getCommunicator().sendNormalServerMessage(str.toString());
            return found;
        }
        if (perceptionLevel >= 60.0) {
            str.append(String.valueOf(target.getName()) + " is around, but could not be found this time.");
            return true;
        }
        return false;
    }
    
    public void answer(final Properties aAnswers) {
        if (!this.properlySent) {
            return;
        }
        boolean found = false;
        final String name = aAnswers.getProperty("name");
        if (name != null && name.length() > 1) {
            found = this.pinpointHumanoid(name, this.getResponder(), this.power);
            if (found && this.reverseFind) {
                final PlayerInfo pinf = PlayerInfoFactory.createPlayerInfo(name);
                if (pinf != null && pinf.loaded) {
                    try {
                        final Creature newTarget = Server.getInstance().getCreature(pinf.wurmId);
                        this.pinpointHumanoid(this.getResponder().getName(), newTarget, this.power);
                        if (this.locateItem != null) {
                            float dmg = 0.0f;
                            if (Server.rand.nextBoolean()) {
                                dmg = Server.rand.nextInt(8);
                            }
                            else {
                                dmg = Server.rand.nextInt((int)this.locateItem.getDamage() + 1);
                            }
                            this.locateItem.setDamage(this.locateItem.getDamage() + dmg);
                        }
                    }
                    catch (NoSuchPlayerException | NoSuchCreatureException ex2) {
                        final WurmServerException ex;
                        final WurmServerException e = ex;
                        e.printStackTrace();
                    }
                }
            }
        }
        if (!found) {
            this.getResponder().getCommunicator().sendNormalServerMessage("Could not find " + name);
        }
    }
    
    public void sendQuestion() {
        boolean ok = true;
        if (this.getResponder().getPower() <= 0) {
            try {
                ok = false;
                final Action act = this.getResponder().getCurrentAction();
                if (act.getNumber() == PinpointHumanoid.actionId || act.getNumber() == CrownFindAction.getActionId()) {
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
            if (!this.extraQuestionNote.equals("")) {
                sb.append("text{text='" + this.extraQuestionNote + "'};");
            }
            sb.append("label{text='Name:'};input{id='name';maxchars='40';text=\"\"};");
            sb.append(this.createAnswerButton2());
            this.getResponder().getCommunicator().sendBml(300, 300, true, true, sb.toString(), 200, 200, 200, this.title);
        }
    }
}
