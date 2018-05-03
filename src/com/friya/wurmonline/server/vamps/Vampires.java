// 
// Decompiled by Procyon v0.5.30
// 

package com.friya.wurmonline.server.vamps;

import com.friya.wurmonline.server.vamps.actions.*;
import com.friya.wurmonline.server.vamps.events.EventOnce;
import com.friya.wurmonline.server.vamps.events.RemoveStakedTeleportEvent;
import com.wurmonline.server.Message;
import com.wurmonline.server.Players;
import com.wurmonline.server.Servers;
import com.wurmonline.server.behaviours.Action;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.CreaturesProxy;
import com.wurmonline.server.creatures.MovementScheme;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.modifiers.DoubleValueModifier;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.players.PlayerInfo;
import com.wurmonline.server.players.PlayerInfoFactory;
import com.wurmonline.server.skills.AffinitiesTimed;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.zones.VolaTile;
import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtPrimitiveType;
import javassist.NotFoundException;
import javassist.bytecode.Descriptor;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;
import org.gotti.wurmunlimited.modloader.classhooks.HookManager;
import org.gotti.wurmunlimited.modloader.classhooks.InvocationHandlerFactory;
import org.gotti.wurmunlimited.modsupport.ModSupportDb;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Vampires
{
    private static Logger logger;
    private static HashMap<Long, Vampire> vampires;
    public static final String kitSalesManName = "Vampire hunter D";
    public static final String headVampireName = "Orlok";
    public static final String deVampManName = "van Helsing";
    public static final String halfVampMakerName = "Dhampira the Ponderer";
    public static int STATUS_NONE;
    public static int STATUS_HALF;
    public static int STATUS_FULL;
    public static final int DISARM_BITABLE_DURATION = 25;
    public static final int AMULET_HIT_BITABLE_DURATION = 300;
    public static int VAMPIRE_RETIREMENT_COST;
    public static int HALF_VAMPIRE_RETIREMENT_COST;
    public static int BITE_ACTION_COUNT_REWARD;
    private static boolean createTestCharacters;
    static ArrayList<String> fakeGMs;
    private static VolaTile lastStakedTile;
    
    static {
        Vampires.logger = Logger.getLogger(Vampires.class.getName());
        Vampires.vampires = new HashMap<Long, Vampire>();
        Vampires.STATUS_NONE = 0;
        Vampires.STATUS_HALF = 1;
        Vampires.STATUS_FULL = 2;
        Vampires.VAMPIRE_RETIREMENT_COST = 5000;
        Vampires.HALF_VAMPIRE_RETIREMENT_COST = 800;
        Vampires.BITE_ACTION_COUNT_REWARD = 250;
        Vampires.createTestCharacters = false;
        Vampires.fakeGMs = new ArrayList<String>();
        Vampires.lastStakedTile = null;
    }
    
    public static void onItemTemplatesCreated() {
        setupStealthModInterception();
        setupEatInterception();
        setupBuryInterception();
        setupStealthActionInterception();
    }
    
    private static void setupStealthModInterception() {
        final String descriptor = Descriptor.ofMethod(CtPrimitiveType.voidType, new CtClass[] { CtPrimitiveType.booleanType });
        HookManager.getInstance().registerHook("com.wurmonline.server.creatures.MovementScheme", "setStealthMod", descriptor, new InvocationHandlerFactory() {
            public InvocationHandler createInvocationHandler() {
                return new InvocationHandler() {
                    @Override
                    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
                        if (Mod.logExecutionCost) {
                            Mod.tmpExecutionStartTime = System.nanoTime();
                        }
                        if (args[0]) {
                            final MovementScheme mover = (MovementScheme)proxy;
                            Field field = MovementScheme.class.getDeclaredField("creature");
                            field.setAccessible(true);
                            final Creature creature = (Creature)field.get(mover);
                            if (Vampires.isVampire(creature.getWurmId())) {
                                field = MovementScheme.class.getDeclaredField("stealthMod");
                                field.setAccessible(true);
                                final DoubleValueModifier stealthMod = (DoubleValueModifier)field.get(mover);
                                stealthMod.setModifier(1.0);
                            }
                        }
                        if (Mod.logExecutionCost) {
                            Vampires.logger.log(Level.INFO, "setupStealthModInterception[hook] done, spent " + Mod.executionLogDf.format((System.nanoTime() - Mod.tmpExecutionStartTime) / 1.0E9) + "s");
                        }
                        final Object result = method.invoke(proxy, args);
                        return result;
                    }
                };
            }
        });
    }
    
    private static void setupEatInterception() {
        try {
            String descriptor = Descriptor.ofMethod(CtPrimitiveType.intType, new CtClass[] { HookManager.getInstance().getClassPool().get("com.wurmonline.server.creatures.Creature"), HookManager.getInstance().getClassPool().get("com.wurmonline.server.items.Item") });
            HookManager.getInstance().registerHook("com.wurmonline.server.behaviours.MethodsItems", "eat", descriptor, new InvocationHandlerFactory() {
                public InvocationHandler createInvocationHandler() {
                    return new InvocationHandler() {
                        @Override
                        public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
                            if (Mod.logExecutionCost) {
                                Mod.tmpExecutionStartTime = System.nanoTime();
                            }
                            if (stopEat((Creature)args[0], (Item)args[1])) {
                                return 0;
                            }
                            if (Mod.logExecutionCost) {
                                Vampires.logger.log(Level.INFO, "setupEatInterception[hook1] done, spent " + Mod.executionLogDf.format((System.nanoTime() - Mod.tmpExecutionStartTime) / 1.0E9) + "s");
                            }
                            final Object result = method.invoke(proxy, args);
                            return result;
                        }
                    };
                }
            });
            descriptor = Descriptor.ofMethod(CtPrimitiveType.booleanType, new CtClass[] { HookManager.getInstance().getClassPool().get("com.wurmonline.server.behaviours.Action"), HookManager.getInstance().getClassPool().get("com.wurmonline.server.creatures.Creature"), HookManager.getInstance().getClassPool().get("com.wurmonline.server.items.Item"), CtPrimitiveType.floatType });
            HookManager.getInstance().registerHook("com.wurmonline.server.behaviours.MethodsItems", "eat", descriptor, new InvocationHandlerFactory() {
                public InvocationHandler createInvocationHandler() {
                    return new InvocationHandler() {
                        @Override
                        public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
                            if (Mod.logExecutionCost) {
                                Mod.tmpExecutionStartTime = System.nanoTime();
                            }
                            if (stopEat((Creature)args[1], (Item)args[2])) {
                                return true;
                            }
                            if (Mod.logExecutionCost) {
                                Vampires.logger.log(Level.INFO, "setupEatInterception[hook2] done, spent " + Mod.executionLogDf.format((System.nanoTime() - Mod.tmpExecutionStartTime) / 1.0E9) + "s");
                            }
                            final Object result = method.invoke(proxy, args);
                            return result;
                        }
                    };
                }
            });
        }
        catch (NotFoundException e) {
            Vampires.logger.log(Level.SEVERE, "Failed to intercept 'eat', this probably means vampires can eat normal food this restart");
            throw new RuntimeException(e);
        }
    }
    
    private static void setupBuryInterception() {
        try {
            final String descriptor = Descriptor.ofMethod(CtPrimitiveType.booleanType, new CtClass[] { HookManager.getInstance().getClassPool().get("com.wurmonline.server.behaviours.Action"), HookManager.getInstance().getClassPool().get("com.wurmonline.server.creatures.Creature"), HookManager.getInstance().getClassPool().get("com.wurmonline.server.items.Item"), HookManager.getInstance().getClassPool().get("com.wurmonline.server.items.Item"), CtPrimitiveType.floatType, CtPrimitiveType.shortType });
            HookManager.getInstance().registerHook("com.wurmonline.server.behaviours.CorpseBehaviour", "bury", descriptor, new InvocationHandlerFactory() {
                public InvocationHandler createInvocationHandler() {
                    return new InvocationHandler() {
                        @Override
                        public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
                            final Object result = method.invoke(proxy, args);
                            if (Mod.logExecutionCost) {
                                Mod.tmpExecutionStartTime = System.nanoTime();
                            }
                            if ((float)args[4] == 1.0f && BloodlessHusk.isBloodlessHusk((Item)args[3])) {
                                final int time = 900;
                                ((Action)args[0]).setTimeLeft(time);
                                ((Creature)args[1]).sendActionControl("Ritual of burying", true, time);
                            }
                            if (Mod.logExecutionCost) {
                                Vampires.logger.log(Level.INFO, "setupBuryInterception[hook1] done, spent " + Mod.executionLogDf.format((System.nanoTime() - Mod.tmpExecutionStartTime) / 1.0E9) + "s");
                            }
                            return result;
                        }
                    };
                }
            });
        }
        catch (NotFoundException e) {
            Vampires.logger.log(Level.SEVERE, "Failed to intercept 'bury'");
            throw new RuntimeException(e);
        }
    }
    
    private static void setupStealthActionInterception() {
        try {
            final CtClass ctc = HookManager.getInstance().getClassPool().get("com.wurmonline.server.behaviours.Action");
            ctc.getDeclaredMethod("poll").instrument(new ExprEditor() {
                public void edit(final MethodCall m) throws CannotCompileException {
                    if (m.getMethodName().equals("isStealth")) {
                        m.replace("$_ = (performer.isStealth() && com.friya.wurmonline.server.vamps.Vampires.isVampire(performer.getWurmId()) ? com.friya.wurmonline.server.vamps.Vampires.isAllowedVampireStealthAction(this) == false : performer.isStealth());");
                        Vampires.logger.log(Level.INFO, "Applied interception of isStealth in Action.poll()");
                    }
                }
            });
        }
        catch (NotFoundException | CannotCompileException ex2) {
            final Exception ex;
            final Exception e = ex;
            Vampires.logger.log(Level.SEVERE, "Failed to apply Action.poll() means Vampire actions will never be performed in stealth", e);
        }
    }
    
    public static boolean isAllowedVampireStealthAction(final Action a) {
        if (Mod.logExecutionCost) {
            Mod.tmpExecutionStartTime = System.nanoTime();
        }
        final short aNum = a.getActionEntry().getNumber();
        if (aNum == AidAction.getActionId() || aNum == BiteAction.getActionId() || aNum == CrippleAction.getActionId() || aNum == CrownFindAction.getActionId() || aNum == DevourAction.getActionId() || aNum == DisarmAction.getActionId() || aNum == FlyAction.getActionId() || aNum == SprintAction.getActionId() || aNum == TraceAction.getActionId() || aNum == SmashAction.getActionId() || aNum == AssistSlainAction.getActionId()) {
            return true;
        }
        if (Mod.logExecutionCost) {
            Vampires.logger.log(Level.INFO, "isAllowedVampireStealthAction done, spent " + Mod.executionLogDf.format((System.nanoTime() - Mod.tmpExecutionStartTime) / 1.0E9) + "s");
        }
        return false;
    }
    
    private static boolean stopEat(final Creature creature, final Item item) {
        if (item != null && item.getTemplateId() == 666) {
            return false;
        }
        if (isHalfOrFullVampire(creature.getWurmId())) {
            creature.getCommunicator().sendNormalServerMessage("You have a lust for blood, normal food will not sustain you.");
            return true;
        }
        return false;
    }
    
    public static void createBite(final Creature vampire, final Creature slayer, final int exchangedStatNum, final String exchangedStatName, final double vampireSkillLevelBefore, final double skillLoss, final int actionCount, final double slayerSkillLevelBefore, final double skillGain, final long staking) {
        try {
            final String sql = "INSERT INTO FriyaVampireBites (vampireid, vampiresteamid, vampirename, slayerid, slayerstat, slayerstatname, slayerloststatlevel, slayerlostamount, slayerlostactions, vampirestatlevel, vampiregainedamount, bitetime, stakingid, slayersteamid, slayername) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
            final Connection dbcon = ModSupportDb.getModSupportDb();
            final PreparedStatement ps = dbcon.prepareStatement(sql);
            int i = 1;
            ps.setLong(i++, vampire.getWurmId());
            ps.setString(i++, vampire.SteamId);
            ps.setString(i++, vampire.getName());
            ps.setLong(i++, slayer.getWurmId());
            ps.setInt(i++, exchangedStatNum);
            ps.setString(i++, exchangedStatName);
            ps.setDouble(i++, slayerSkillLevelBefore);
            ps.setDouble(i++, skillLoss);
            ps.setInt(i++, actionCount);
            ps.setDouble(i++, vampireSkillLevelBefore);
            ps.setDouble(i++, skillGain);
            ps.setLong(i++, System.currentTimeMillis());
            ps.setLong(i++, staking);
            ps.setString(i++, slayer.SteamId);
            ps.setString(i++, slayer.getName());
            ps.execute();
            ps.close();
        }
        catch (SQLException e) {
            Vampires.logger.log(Level.SEVERE, "Failed to insert bite");
            throw new RuntimeException(e);
        }
    }
    
    public static Player[] getAll() {
        final ArrayList<Player> ret = new ArrayList<Player>();
        final Player[] players = Players.getInstance().getPlayers();
        Player[] array;
        for (int length = (array = players).length, i = 0; i < length; ++i) {
            final Player p = array[i];
            if (isVampire(p)) {
                ret.add(p);
            }
        }
        return ret.toArray(new Player[0]);
    }
    
    static void onPlayerLogin(final Player p) {
        if (Mod.isTestEnv()) {
            Vampires.logger.warning("Forcing hun-nut-sta-ccfp-bl-affs to test-values on testenv");
            CreaturesProxy.setHunNutSta(p, 0, 0.0f, 0, 0.0f);
            final Skill bl = p.getSkills().getSkillOrLearn(2147483641);
            bl.setKnowledge(80.0, false);
            AffinitiesTimed.deleteTimedAffinitiesForPlayer(p.getWurmId());
        }
        if (isVampire(p)) {
            p.getCommunicator().sendAlertServerMessage("Welcome, dweller of darkness.", (byte)4);
            p.getCommunicator().sendNormalServerMessage("You are a vampire.");
            ChatCommands.cmdSlayers(p.getCommunicator(), "/slayers");
        }
        if (isHalfVampire(p)) {
            p.getCommunicator().sendNormalServerMessage("Welcome, dark one.", (byte)4);
            p.getCommunicator().sendNormalServerMessage("You lead the life of one who is entranced by the night.");
            p.getCommunicator().sendNormalServerMessage("You are infected by bloodlust.");
            p.getCommunicator().sendNormalServerMessage("You are half vampire.");
            p.getCommunicator().sendAlertServerMessage("You should find Orlok. Dhampira the Ponderer's clue might help...", (byte)4);
        }
    }
    
    public static void broadcast(final String message) {
        broadcast(message, false, false, false);
    }
    
    public static void broadcast(final String message, final boolean includeChat) {
        broadcast(message, includeChat, false, false);
    }
    
    public static void broadcast(final String message, final boolean includeChat, final boolean playSound) {
        broadcast(message, includeChat, playSound, false);
    }
    
    public static void broadcast(final String message, final boolean includeChat, final boolean playSound, final boolean emptyLines) {
        final Player[] players = Players.getInstance().getPlayers();
        final Message emptyMsg = new Message(null, (byte)10, "Coven", "", 255, 90, 90);
        Player[] array;
        for (int length = (array = players).length, i = 0; i < length; ++i) {
            final Player p = array[i];
            if (isVampire(p) || Vampires.fakeGMs.contains(p.getName()) || p.getPower() > 2) {
                p.getCommunicator().sendAlertServerMessage(message, (byte)4);
                if (includeChat) {
                    if (emptyLines) {
                        p.getCommunicator().sendMessage(emptyMsg);
                    }
                    final Message covenMsg = new Message(null, (byte)10, "Coven", "      " + message, 255, 90, 90);
                    p.getCommunicator().sendMessage(covenMsg);
                    if (emptyLines) {
                        p.getCommunicator().sendMessage(emptyMsg);
                    }
                }
                if (playSound) {
                    p.playPersonalSound("sound.spawn.item.central");
                }
            }
        }
    }
    
    public static void broadcastLight(final String message, final boolean includeChat) {
        final Player[] players = Players.getInstance().getPlayers();
        Player[] array;
        for (int length = (array = players).length, i = 0; i < length; ++i) {
            final Player p = array[i];
            if (isVampire(p) || Vampires.fakeGMs.contains(p.getName())) {
                p.getCommunicator().sendAlertServerMessage(message, (byte)0);
                if (includeChat) {
                    final Message covenMsg = new Message(null, (byte)10, "Coven", "      " + message, 255, 90, 90);
                    p.getCommunicator().sendMessage(covenMsg);
                }
            }
        }
    }
    
    public static void poll() {
        final Player[] players = Players.getInstance().getPlayers();
        Player[] array;
        for (int length = (array = players).length, i = 0; i < length; ++i) {
            final Player p = array[i];
            if (p.isFullyLoaded()) {
                if (!p.loggedout) {
                    if (isHalfOrFullVampire(p.getWurmId())) {
                        BloodLust.poll(p);
                    }
                }
            }
        }
    }
    
    static void saveBitingStats() {
    }
    
    static void onServerStarted() {
        try {
            final Connection con = ModSupportDb.getModSupportDb();
            String sql = "";
            if (!ModSupportDb.hasTable(con, "FriyaVampires")) {
                sql = "CREATE TABLE FriyaVampires (\t\tplayerid\t\t\t\tBIGINT\t\t\tNOT NULL PRIMARY KEY,\t\tsteamid\t\t\t\t\tVARCHAR(40)\t\tNOT NULL DEFAULT '',\t\tname\t\t\t\t\tVARCHAR(40)\t\tNOT NULL DEFAULT 'Unknown',\t\talias\t\t\t\t\tVARCHAR(40)\t\tNOT NULL DEFAULT 'Unknown',\t\tvampirestatus\t\t\tINT\t\t\t\tNOT NULL DEFAULT 0,\t\thalfstarttime\t\t\tBIGINT\t\t\tNOT NULL DEFAULT 0,\t\tfullstarttime\t\t\tBIGINT\t\t\tNOT NULL DEFAULT 0,\t\tfullendtime\t\t\t\tBIGINT\t\t\tNOT NULL DEFAULT 0)";
                final PreparedStatement ps = con.prepareStatement(sql);
                ps.execute();
                ps.close();
            }
            if (!ModSupportDb.hasTable(con, "FriyaVampireBites")) {
                sql = "CREATE TABLE FriyaVampireBites (\t\tid\t\t\t\t\t\tINTEGER\t\t\tPRIMARY KEY AUTOINCREMENT,\t\tvampireid\t\t\t\tBIGINT\t\t\tNOT NULL,\t\tvampiresteamid\t\t\tVARCHAR(40)\t\tNOT NULL DEFAULT '',\t\tvampirename\t\t\t\tVARCHAR(40)\t\tNOT NULL DEFAULT 'Unknown',\t\tslayersteamid\t\t\tVARCHAR(40)\t\tNOT NULL DEFAULT '',\t\tslayername\t\t\t\tVARCHAR(40)\t\tNOT NULL DEFAULT 'Unknown',\t\tslayerid\t\t\t\tBIGINT\t\t\tNOT NULL DEFAULT 0,\t\tslayerstat\t\t\t\tINT\t\t\t\tNOT NULL DEFAULT 0,\t\tslayerstatname\t\t\tVARCHAR(40)\t\tNOT NULL DEFAULT '',\t\tslayerloststatlevel\t\tFLOAT\t\t\tNOT NULL DEFAULT 0,\t\tslayerlostamount\t\tFLOAT\t\t\tNOT NULL DEFAULT 0,\t\tslayerlostactions\t\tINT\t\t\t\tNOT NULL DEFAULT 0,\t\tvampirestatlevel\t\tFLOAT\t\t\tNOT NULL DEFAULT 0,\t\tvampiregainedamount\t\tFLOAT\t\t\tNOT NULL DEFAULT 0,\t\tbitetime\t\t\t\tBIGINT\t\t\tNOT NULL DEFAULT 0,\t\tstakingid\t\t\t\tINTEGER\t\t\tNOT NULL DEFAULT -1)";
                final PreparedStatement ps = con.prepareStatement(sql);
                ps.execute();
                ps.close();
            }
            if (!columnExists(con, "FriyaVampireBites", "slayersteamid")) {
                PreparedStatement ps = con.prepareStatement("ALTER TABLE FriyaVampireBites ADD COLUMN slayersteamid\tVARCHAR(40)\tNOT NULL DEFAULT ''");
                ps.execute();
                ps.close();
                ps = con.prepareStatement("ALTER TABLE FriyaVampireBites ADD COLUMN slayername VARCHAR(40) NOT NULL DEFAULT 'Unknown'");
                ps.execute();
                ps.close();
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        loadAll();
        if (Mod.isTestEnv() && Vampires.createTestCharacters) {
            PlayerInfo pinf = PlayerInfoFactory.createPlayerInfo("Friya");
            if (pinf != null && !isHalfOrFullVampire(pinf.getPlayerId())) {
                Vampires.logger.log(Level.INFO, "Creating Friya the Vampire");
                createOfflineVampire(pinf, false);
            }
            pinf = PlayerInfoFactory.createPlayerInfo("Artemis");
            if (pinf != null && !isHalfOrFullVampire(pinf.getPlayerId())) {
                Vampires.logger.log(Level.INFO, "Creating Artemis the Vampire");
                createOfflineVampire(pinf, false);
            }
            pinf = PlayerInfoFactory.createPlayerInfo("Aurora");
            if (pinf != null && !isHalfOrFullVampire(pinf.getPlayerId())) {
                Vampires.logger.log(Level.INFO, "Creating Aurora the Half Vampire");
                createOfflineVampire(pinf, true);
            }
        }
        if (Servers.localServer.getName().equals("Zenath")) {
            Vampires.fakeGMs.add("Friya");
            Vampires.fakeGMs.add("Jaygriff");
            Vampires.fakeGMs.add("Raidsoft");
            Vampires.logger.log(Level.INFO, "These characters will have Coven channel without being vampire: " + String.join(", ", Vampires.fakeGMs));
        }
    }
    
    private static boolean columnExists(final Connection con, final String table, final String column) {
        boolean found = false;
        ResultSet rs = null;
        PreparedStatement ps = null;
        try {
            ps = con.prepareStatement("PRAGMA table_info(" + table + ")");
            rs = ps.executeQuery();
            while (rs.next()) {
                if (rs.getString("name").equals(column)) {
                    found = true;
                    break;
                }
            }
        }
        catch (SQLException e) {
            Vampires.logger.log(Level.SEVERE, "Could not determine whether a column existed in a table, this might cause problems later on....", e);
            try {
                rs.close();
                ps.close();
            }
            catch (SQLException e2) {
                e2.printStackTrace();
            }
            return found;
        }
        finally {
            try {
                rs.close();
                ps.close();
            }
            catch (SQLException e2) {
                e2.printStackTrace();
            }
        }
        try {
            rs.close();
            ps.close();
        }
        catch (SQLException e2) {
            e2.printStackTrace();
        }
        return found;
    }
    
    public static boolean deVamp(final Creature creature) {
        if (creature == null) {
            return false;
        }
        final Vampire vampire = getVampire(creature.getWurmId());
        if (vampire == null) {
            return false;
        }
        int allocatedActions = 0;
        int deVampActionCost = 0;
        if (isVampire(creature.getWurmId())) {
            deVampActionCost = Vampires.VAMPIRE_RETIREMENT_COST;
        }
        else if (isHalfVampire(creature.getWurmId())) {
            deVampActionCost = Vampires.HALF_VAMPIRE_RETIREMENT_COST;
        }
        final HashMap<Integer, Integer> skillsToPunish = new HashMap<Integer, Integer>();
        int i = 0;
        while (i++ < 50) {
            final ActionSkillGain actionSkillGain = ActionSkillGains.getRandomHighSkillToPunish(creature);
            final Skill vampireSkill = creature.getSkills().getSkillOrLearn(actionSkillGain.getId());
            final int actionCount = actionSkillGain.getModifiedLostActionCount(vampireSkill.getKnowledge(), deVampActionCost / 5, 0.25f);
            allocatedActions += actionCount;
            if (skillsToPunish.containsKey(actionSkillGain.getId())) {
                skillsToPunish.put(actionSkillGain.getId(), skillsToPunish.get(actionSkillGain.getId()) + actionCount);
            }
            else {
                skillsToPunish.put(actionSkillGain.getId(), actionCount);
            }
            if (allocatedActions >= deVampActionCost) {
                break;
            }
        }
        Vampires.logger.log(Level.INFO, "deVamp() about to devamp " + creature.getName());
        Vampires.logger.log(Level.INFO, "deVamp() is half vampire: " + isHalfVampire(creature.getWurmId()));
        Vampires.logger.log(Level.INFO, "deVamp() iterations to get to " + deVampActionCost + " actions: " + i);
        Vampires.logger.log(Level.INFO, "deVamp() actions found: " + allocatedActions);
        Vampires.logger.log(Level.INFO, "deVamp() skill losses: " + skillsToPunish.toString());
        for (final int skillNum : skillsToPunish.keySet()) {
            final Skill vampireSkill2 = creature.getSkills().getSkillOrLearn(skillNum);
            final double vampireSkillLevelBefore = vampireSkill2.getKnowledge();
            final double skillLoss = ActionSkillGains.getSkill(skillNum).getRawSkillLossForActionCount(vampireSkill2.getKnowledge(), skillsToPunish.get(skillNum));
            Vampires.logger.log(Level.INFO, "Skill: " + vampireSkill2.getName() + " Actions: " + skillsToPunish.get(skillNum) + " Before: " + vampireSkillLevelBefore + " Loss: " + skillLoss);
            vampireSkill2.setKnowledge(vampireSkill2.getKnowledge() - skillLoss, false, true);
        }
        deVampWithoutLoss(creature);
        return true;
    }
    
    public static boolean deVampWithoutLoss(final Creature creature) {
        final Vampire vampire = getVampire(creature.getWurmId());
        if (vampire == null) {
            return false;
        }
        vampire.setFullEndTime(System.currentTimeMillis());
        vampire.setVampireStatus(Vampires.STATUS_NONE);
        updateVampire(vampire);
        Vampires.vampires.remove(creature.getWurmId());
        return true;
    }
    
    private static Vampire createOfflineVampire(final PlayerInfo p, final boolean half) {
        final Vampire v = new Vampire(p.getPlayerId(), "unknown", p.getName(), CovenChat.generateAlias(p.getPlayerId(), p.creationDate), half ? Vampires.STATUS_HALF : Vampires.STATUS_FULL, System.currentTimeMillis(), half ? 0L : System.currentTimeMillis(), 0L);
        if (insertVampire(v)) {
            Vampires.vampires.put(v.getId(), v);
            return v;
        }
        return null;
    }
    
    public static Vampire createVampire(final Player p, final boolean half) {
        final Vampire v = new Vampire(p.getWurmId(), p.SteamId, p.getName(), CovenChat.generateAlias(p), half ? Vampires.STATUS_HALF : Vampires.STATUS_FULL, System.currentTimeMillis(), half ? 0L : System.currentTimeMillis(), 0L);
        if (insertVampire(v)) {
            Vampires.vampires.put(v.getId(), v);
            return v;
        }
        return null;
    }
    
    public static void updateVampire(final Vampire v) {
        try {
            final Connection dbcon = ModSupportDb.getModSupportDb();
            final PreparedStatement ps = dbcon.prepareStatement("UPDATE FriyaVampires SET playerid = ?, steamid = ?, name = ?, alias = ?, vampirestatus = ?, halfstarttime = ?, fullstarttime = ?, fullendtime  = ? WHERE playerid = ?");
            ps.setLong(1, v.getId());
            ps.setString(2, v.getSteamId());
            ps.setString(3, v.getName());
            ps.setString(4, v.getAlias());
            ps.setInt(5, v.getVampireStatus());
            ps.setLong(6, v.getHalfStartTime());
            ps.setLong(7, v.getFullStartTime());
            ps.setLong(8, v.getFullEndTime());
            ps.setLong(9, v.getId());
            ps.executeUpdate();
            ps.close();
        }
        catch (SQLException e) {
            Vampires.logger.log(Level.SEVERE, "Failed to update database entry for existing vampire");
            throw new RuntimeException(e);
        }
    }
    
    private static boolean existsInDatabase(final long playerId) {
        Connection dbcon = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        int foundCount = 0;
        try {
            dbcon = ModSupportDb.getModSupportDb();
            ps = dbcon.prepareStatement("SELECT COUNT(*) AS cnt FROM FriyaVampires WHERE playerid = " + playerId);
            rs = ps.executeQuery();
            if (rs.next()) {
                foundCount = rs.getInt("cnt");
            }
            rs.close();
            ps.close();
        }
        catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return foundCount > 0;
    }
    
    private static boolean insertVampire(final Vampire v) {
        Vampires.logger.log(Level.INFO, "About to insert a vampire: " + v.toString());
        if (getVampire(v.getId()) != null && v.getVampireStatus() != 0) {
            Vampires.logger.log(Level.SEVERE, "Can't create a vampire that already exists in database and are still a vampire; refusing...");
            return false;
        }
        if (existsInDatabase(v.getId())) {
            deleteVampire(v);
        }
        try {
            final Connection dbcon = ModSupportDb.getModSupportDb();
            final PreparedStatement ps = dbcon.prepareStatement("INSERT INTO FriyaVampires (playerid, steamid, name, alias, vampirestatus, halfstarttime, fullstarttime, fullendtime) VALUES(?,?,?,?,?,?,?,?)");
            ps.setLong(1, v.getId());
            ps.setString(2, v.getSteamId());
            ps.setString(3, v.getName());
            ps.setString(4, v.getAlias());
            ps.setInt(5, v.getVampireStatus());
            ps.setLong(6, v.getHalfStartTime());
            ps.setLong(7, v.getFullStartTime());
            ps.setLong(8, v.getFullEndTime());
            ps.execute();
            ps.close();
        }
        catch (SQLException e) {
            Vampires.logger.log(Level.SEVERE, "Failed to create database entry for new vampire");
            throw new RuntimeException(e);
        }
        return true;
    }
    
    private static void deleteVampire(final Vampire v) {
        Vampires.logger.log(Level.INFO, "Deleting vampire: " + v.getName() + ", " + v.getId());
        try {
            final Connection dbcon = ModSupportDb.getModSupportDb();
            final PreparedStatement ps = dbcon.prepareStatement("DELETE FROM FriyaVampires WHERE playerid = ?");
            ps.setLong(1, v.getId());
            ps.execute();
            ps.close();
        }
        catch (SQLException e) {
            Vampires.logger.log(Level.SEVERE, "Failed to delete vampire");
            throw new RuntimeException(e);
        }
    }
    
    private static void loadAll() {
        Vampires.logger.log(Level.INFO, "Loading all Vampires...");
        Connection dbcon = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            dbcon = ModSupportDb.getModSupportDb();
            ps = dbcon.prepareStatement("SELECT * FROM FriyaVampires WHERE fullendtime = 0 AND vampirestatus != 0");
            rs = ps.executeQuery();
            while (rs.next()) {
                Vampires.vampires.put(rs.getLong("playerid"), new Vampire(rs.getLong("playerid"), rs.getString("steamid"), rs.getString("name"), rs.getString("alias"), rs.getInt("vampirestatus"), rs.getLong("halfstarttime"), rs.getLong("fullstarttime"), rs.getLong("fullendtime")));
            }
            rs.close();
            ps.close();
        }
        catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    
    public static Vampire getVampire(final long id) {
        if (Vampires.vampires.containsKey(id)) {
            return Vampires.vampires.get(id);
        }
        return null;
    }
    
    public static boolean isVampire(final long id) {
        final Vampire vamp = getVampire(id);
        return vamp != null && vamp.isFull();
    }
    
    public static boolean isHalfVampire(final long id) {
        final Vampire vamp = getVampire(id);
        return vamp != null && vamp.isHalf();
    }
    
    public static boolean isHalfOrFullVampire(final long id) {
        return isVampire(id) || isHalfVampire(id);
    }
    
    public static boolean isVampire(final Player p) {
        return isVampire(p.getWurmId());
    }
    
    public static boolean isHalfVampire(final Player p) {
        return isHalfVampire(p.getWurmId());
    }
    
    public static boolean isHalfOrFullVampire(final Player p) {
        return isHalfOrFullVampire(p.getWurmId());
    }
    
    public static void setStakedTeleportPosition(final VolaTile t, final int validSeconds) {
        EventDispatcher.add(new RemoveStakedTeleportEvent(validSeconds, EventOnce.Unit.SECONDS));
        Vampires.lastStakedTile = t;
        Vampires.logger.log(Level.INFO, "Setting teleport point for staking: " + Vampires.lastStakedTile);
    }
    
    public static void clearStakedTeleportPosition() {
        Vampires.logger.log(Level.INFO, "Clearing teleport point for staking: " + Vampires.lastStakedTile);
        Vampires.lastStakedTile = null;
    }
    
    public static VolaTile getStakedTeleportPosition() {
        return Vampires.lastStakedTile;
    }
}
