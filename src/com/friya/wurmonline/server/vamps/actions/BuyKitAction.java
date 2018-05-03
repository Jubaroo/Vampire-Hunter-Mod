// 
// Decompiled by Procyon v0.5.30
// 

package com.friya.wurmonline.server.vamps.actions;

import com.friya.wurmonline.server.vamps.VampAchievements;
import com.friya.wurmonline.server.vamps.VampTitles;
import com.friya.wurmonline.server.vamps.Vampires;
import com.friya.wurmonline.server.vamps.items.*;
import com.wurmonline.server.FailedException;
import com.wurmonline.server.Items;
import com.wurmonline.server.NoSuchItemException;
import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.behaviours.Action;
import com.wurmonline.server.behaviours.ActionEntry;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.NoSuchCreatureException;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemFactory;
import com.wurmonline.server.items.NoSuchTemplateException;
import com.wurmonline.server.players.Achievements;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.zones.NoSuchZoneException;
import com.wurmonline.shared.exceptions.WurmServerException;
import org.gotti.wurmunlimited.modsupport.actions.ActionPerformer;
import org.gotti.wurmunlimited.modsupport.actions.BehaviourProvider;
import org.gotti.wurmunlimited.modsupport.actions.ModAction;
import org.gotti.wurmunlimited.modsupport.actions.ModActions;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BuyKitAction implements ModAction
{
    private static Logger logger;
    private final short actionId;
    private final ActionEntry actionEntry;
    
    static {
        BuyKitAction.logger = Logger.getLogger(BuyKitAction.class.getName());
    }
    
    public BuyKitAction() {
        BuyKitAction.logger.log(Level.INFO, "BuyKitAction()");
        this.actionId = (short)ModActions.getNextActionId();
        ModActions.registerAction(this.actionEntry = ActionEntry.createEntry(this.actionId, "Buy a black velvet pouch (5 silver)", "buying a black velvet pouch", new int[] { 6 }));
    }
    
    public BehaviourProvider getBehaviourProvider() {
        return new BehaviourProvider() {
            public List<ActionEntry> getBehavioursFor(final Creature performer, final Creature object) {
                return this.getBehavioursFor(performer, null, object);
            }
            
            public List<ActionEntry> getBehavioursFor(final Creature performer, final Item subject, final Creature target) {
                if (performer instanceof Player && target instanceof Creature && target.getName().equals("Vampire hunter D")) {
                    return Arrays.asList(BuyKitAction.this.actionEntry);
                }
                return null;
            }
        };
    }
    
    public ActionPerformer getActionPerformer() {
        return new ActionPerformer() {
            public short getActionId() {
                return BuyKitAction.this.actionId;
            }
            
            public boolean action(final Action act, final Creature performer, final Creature target, final short action, final float counter) {
                if (performer instanceof Player && target instanceof Creature && target.getName().equals("Vampire hunter D")) {
                    performer.getCommunicator().sendNormalServerMessage("Vampire hunter D says, \"Buy a pouch with what? Think this is charity? Activate a coin of correct value. I'll accept some amulets and crowns too...\"");
                }
                return true;
            }
            
            public boolean action(final Action act, final Creature performer, final Item source, final Creature target, final short action, final float counter) {
                if (!performer.isPlayer() || target == null || !target.getName().equals("Vampire hunter D")) {
                    return true;
                }
                if (Vampires.isHalfOrFullVampire(performer.getWurmId()) && performer.getPower() < 2) {
                    performer.getCommunicator().sendNormalServerMessage("Vampire hunter D yells, \"Be GONE foul beast! I do not do business with your kind!\"");
                    return true;
                }
                if (source.getTemplateId() != 56 && source.getTemplateId() != Amulet.getId() && source.getTemplateId() != Crown.getId()) {
                    performer.getCommunicator().sendNormalServerMessage("Vampire hunter D says, \"You need to pay me and I don't have any change! I accept amulets and crowns as payment too...\"");
                    return true;
                }
                try {
                    final String creator = "Vampire hunter D";
                    final Item pouch = ItemFactory.createItem(Pouch.getId(), 5.0f, (byte)0, creator);
                    pouch.setColor(0);
                    final Item stake = ItemFactory.createItem(Stake.getId(), 10.0f, (byte)1, creator);
                    stake.setColor(255);
                    pouch.insertItem(stake);
                    final Item mallet = ItemFactory.createItem(63, 10.0f, (byte)0, creator);
                    pouch.insertItem(mallet);
                    final Item mirror = ItemFactory.createItem(Mirror.getId(), 10.0f, (byte)0, creator);
                    pouch.insertItem(mirror);
                    final Item papyrus = ItemFactory.createItem(748, 10.0f, (byte)0, creator);
                    final String str = "\";maxlines=\"0\"}text{text=\"I don't care what everyone says, there ARE vampires around!\n\nI have seen them...\n\nYou must help me rid the world of these foul creatures. You will be rewarded!\n\nThey seem to be largely unaffected by normal weapons, but these magical stakes seem to work. Mostly. Use the mallet to drive your wielded stake through the heart of the Vampire.\n\nBeware! Should you be successful in banishing one of these foul beasts, you will have their blood on your hands. They WILL hunt you as long as it is there. \nThe magical stake that you bought from me will not work on ordinary humans. In fact, using them on a human will probably kill you.\n\nOh, almost forgot... You cannot see their reflection in a mirror.\n\n \n";
                    papyrus.setInscription(str, creator);
                    papyrus.setAuxData((byte)(-104));
                    pouch.insertItem(papyrus);
                    BuyKitAction.logger.log(Level.INFO, "DESTROYING " + source.getName() + " (material: " + source.getMaterial() + ") because they bought a black velvet pouch with it");
                    Items.destroyItem(source.getWurmId());
                    if (performer.getInventory().getNumItemsNotCoins() < 100) {
                        performer.getInventory().insertItem(pouch, true);
                    }
                    else {
                        pouch.putItemInfrontof(performer);
                    }
                    performer.getCommunicator().sendNormalServerMessage("You buy a black velvet pouch.");
                    Achievements.triggerAchievement(performer.getWurmId(), VampAchievements.POUCHES);
                    if (!VampTitles.hasTitle(performer, VampTitles.VAMPIRE_HUNTER)) {
                        performer.addTitle(VampTitles.getTitle(VampTitles.VAMPIRE_HUNTER));
                    }
                }
                catch (FailedException | NoSuchTemplateException | NoSuchCreatureException | NoSuchItemException | NoSuchPlayerException | NoSuchZoneException ex2) {
                    final WurmServerException e = ex2;
                    BuyKitAction.logger.log(Level.SEVERE, "Problem selling pouch", e);
                    throw new RuntimeException(e);
                }
                return true;
            }
        };
    }
}
