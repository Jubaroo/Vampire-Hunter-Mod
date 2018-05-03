// 
// Decompiled by Procyon v0.5.30
// 

package com.friya.wurmonline.server.vamps;

import com.friya.wurmonline.server.vamps.events.EventOnce;
import com.friya.wurmonline.server.vamps.events.StakeWieldedEvent;
import com.friya.wurmonline.server.vamps.items.Stake;
import com.wurmonline.server.*;
import com.wurmonline.server.behaviours.Terraforming;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.NoSuchCreatureException;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.players.Cultist;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.players.PlayerInfo;
import com.wurmonline.server.players.PlayerInfoFactory;
import com.wurmonline.server.questions.KarmaQuestion;
import com.wurmonline.server.questions.VillageTeleportQuestion;
import com.wurmonline.server.utils.DbUtilities;
import com.wurmonline.server.villages.Village;
import com.wurmonline.server.villages.Villages;
import com.wurmonline.shared.exceptions.WurmServerException;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.CtPrimitiveType;
import javassist.NotFoundException;
import javassist.bytecode.Descriptor;
import org.gotti.wurmunlimited.modloader.classhooks.HookManager;
import org.gotti.wurmunlimited.modloader.classhooks.InvocationHandlerFactory;
import org.gotti.wurmunlimited.modsupport.ModSupportDb;

import javax.annotation.Nullable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Stakers
{
    private static Logger logger;
    public static int HUNTED_TIME;
    public static final int POLL_INTERVAL = 5000;
    public static final int BITE_CAP = 50;
    public static final double STAKER_REQUIRED_FS = 35.0;
    private static HashMap<Long, Staker> stakers;
    private static HashMap<Long, Long> bitables;
    
    static {
        Stakers.logger = Logger.getLogger(Stakers.class.getName());
        Stakers.HUNTED_TIME = 5400000;
        Stakers.stakers = new HashMap<Long, Staker>();
        Stakers.bitables = new HashMap<Long, Long>();
    }
    
    static void onItemTemplatesCreated() {
        if (Mod.isTestEnv()) {
            Stakers.HUNTED_TIME = 20000;
        }
        setUpEquipInterception();
        setUpUnequipInterception();
        setUpTeleportInterception();
    }
    
    public static void createStaker(final Creature slayer, final Creature vampire, final int exchangedStatNum, final String exchangedStatName, final double vampireStatBefore, final double vampireLostAmount, final int vampireLostActions, final double slayerStatLevelBefore, final double slayerGainedAmount) {
        final Staker s = new Staker(slayer, vampire, exchangedStatNum, exchangedStatName, vampireStatBefore, vampireLostAmount, vampireLostActions, slayerStatLevelBefore, slayerGainedAmount);
        Stakers.stakers.put(slayer.getWurmId(), s);
    }
    
    static void onServerStarted() {
        try {
            final Connection con = ModSupportDb.getModSupportDb();
            if (!ModSupportDb.hasTable(con, "FriyaVampireSlayers")) {
                final String sql = "CREATE TABLE FriyaVampireSlayers (\t\tid\t\t\t\t\t\tINTEGER\t\t\tPRIMARY KEY AUTOINCREMENT,\t\tslayerid\t\t\t\tBIGINT\t\t\tNOT NULL,\t\tslayersteamid\t\t\tVARCHAR(40)\t\tNOT NULL DEFAULT '',\t\tslayername\t\t\t\tVARCHAR(40)\t\tNOT NULL DEFAULT 'Unknown',\t\tvampirename\t\t\t\tVARCHAR(40)\t\tNOT NULL DEFAULT 'Unknown',\t\tvampireid\t\t\t\tBIGINT\t\t\tNOT NULL DEFAULT 0,\t\tvampirestat\t\t\t\tINT\t\t\t\tNOT NULL DEFAULT 0,\t\tvampirestatname\t\t\tVARCHAR(40)\t\tNOT NULL DEFAULT '',\t\tvampireloststatlevel\tFLOAT\t\t\tNOT NULL DEFAULT 0,\t\tvampirelostamount\t\tFLOAT\t\t\tNOT NULL DEFAULT 0,\t\tvampirelostactions\t\tINT\t\t\t\tNOT NULL DEFAULT 0,\t\tslayerstatlevel\t\t\tFLOAT\t\t\tNOT NULL DEFAULT 0,\t\tslayergainedamount\t\tFLOAT\t\t\tNOT NULL DEFAULT 0,\t\tstaketime\t\t\t\tBIGINT\t\t\tNOT NULL DEFAULT 0,\t\ttimeelapsed\t\t\t\tBIGINT\t\t\tNOT NULL DEFAULT 0,\t\thuntover\t\t\t\tTINYINT\t\t\tNOT NULL DEFAULT 0)";
                final PreparedStatement ps = con.prepareStatement(sql);
                ps.execute();
                ps.close();
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        loadAll();
    }
    
    private static void loadAll() {
        Stakers.logger.log(Level.INFO, "Loading all hunted stakers...");
        Connection dbcon = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            dbcon = ModSupportDb.getModSupportDb();
            ps = dbcon.prepareStatement("SELECT * FROM FriyaVampireSlayers WHERE timeelapsed < ? AND huntover = 0");
            ps.setInt(1, Stakers.HUNTED_TIME);
            rs = ps.executeQuery();
            while (rs.next()) {
                final Staker s = new Staker();
                s.setId(rs.getLong("id"));
                s.setPlayerId(rs.getLong("slayerid"));
                s.setPlayerName(rs.getString("slayername"));
                s.setStartTime(rs.getLong("staketime"));
                s.setLastPoll(System.currentTimeMillis() - 5000L);
                s.setLastSave(System.currentTimeMillis());
                s.setElapsedTime(rs.getLong("timeelapsed"));
                s.setHuntOverNoSave(rs.getByte("huntover") == 1);
                s.setAffectedSkill(rs.getInt("vampirestat"));
                Stakers.stakers.put(s.getPlayerId(), s);
                Stakers.logger.log(Level.INFO, "Loaded staker " + s.getPlayerName() + " (slaying ID " + s.getId() + ")");
            }
            rs.close();
            ps.close();
        }
        catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    
    static HashMap<Long, Staker> getStakers() {
        return Stakers.stakers;
    }
    
    public static boolean isHunted(final Creature player) {
        return isHunted(player.getWurmId());
    }
    
    public static boolean mayPunish(final long wurmId) {
        return isHunted(wurmId) || Stakers.bitables.containsKey(wurmId) || isWieldingStake(wurmId);
    }
    
    public static boolean isWieldingStake(final long wurmId) {
        final Creature player = Players.getInstance().getPlayerOrNull(wurmId);
        return player != null && ((player.getRighthandItem() != null && player.getRighthandItem().getTemplateId() == Stake.getId()) || (player.getLefthandItem() != null && player.getLefthandItem().getTemplateId() == Stake.getId()));
    }
    
    public static Item getWieldedStake(final Creature creature) {
        Item stake = null;
        if (creature.getRighthandItem() != null && creature.getRighthandItem().getTemplateId() == Stake.getId()) {
            stake = creature.getRighthandItem();
        }
        else if (creature.getLefthandItem() != null && creature.getLefthandItem().getTemplateId() == Stake.getId()) {
            stake = creature.getLefthandItem();
        }
        return stake;
    }
    
    public static boolean isHunted(final long wurmId) {
        return Stakers.stakers.containsKey(wurmId) && !Stakers.stakers.get(wurmId).isHuntOver();
    }
    
    public static boolean isHuntedMount(final Creature creature) {
        return creature != null && creature.isVehicle() && creature.getMountVehicle() != null && creature.getMountVehicle().getPilotId() > 0L && isHunted(creature.getMountVehicle().getPilotId());
    }
    
    public static Staker getStaker(final long wurmId) throws NoSuchPlayerException {
        if (!Stakers.stakers.containsKey(wurmId)) {
            throw new NoSuchPlayerException("Staker not found");
        }
        return Stakers.stakers.get(wurmId);
    }
    
    public static boolean isStaker(final long wurmId) {
        return Stakers.stakers.containsKey(wurmId);
    }
    
    static Creature getPlayer(final String name) {
        try {
            final PlayerInfo pinf = PlayerInfoFactory.createPlayerInfo(name.toLowerCase());
            final Creature target = Server.getInstance().getCreature(pinf.wurmId);
            return target.isPlayer() ? target : null;
        }
        catch (NoSuchPlayerException | NoSuchCreatureException ex2) {
            final WurmServerException ex;
            final WurmServerException e = ex;
            return null;
        }
    }
    
    public static void onPlayerLogin(final Player p) {
        if (isHunted(p.getWurmId())) {
            Vampires.broadcastLight("", true);
            Vampires.broadcastLight(String.valueOf(p.getName()) + " returned to this world.  Let the hunt continue!", true);
            Vampires.broadcastLight("", true);
            p.getCommunicator().sendAlertServerMessage("You are a hunted a vampire slayer.", (byte)4);
        }
    }
    
    public static void onPlayerLogout(final Player p) {
        if (isHunted(p.getWurmId())) {
            Vampires.broadcastLight(String.valueOf(p.getName()) + " has fled this world, but will surely return.  The hunt will continue then.", false);
        }
    }
    
    public static void poll() {
        final long ts = System.currentTimeMillis();
        if (ts / 1000L % 2L == 1L) {
            final Player[] players = Players.getInstance().getPlayers();
            Player[] array;
            for (int length = (array = players).length, i = 0; i < length; ++i) {
                final Player p = array[i];
                if (isWieldingStake(p.getWurmId()) || isHunted(p.getWurmId())) {
                    final Creature[] followers = p.getFollowers();
                    Creature[] array2;
                    for (int length2 = (array2 = followers).length, j = 0; j < length2; ++j) {
                        final Creature f = array2[j];
                        Server.getInstance().broadCastAction(String.valueOf(f.getName()) + " refuses to follow " + p.getName() + " further.", p, 5);
                        if (isHunted(p.getWurmId())) {
                            p.getCommunicator().sendNormalServerMessage(String.valueOf(f.getName()) + " looks nervously at your bloodstained hands and stops in its tracks.");
                        }
                        else {
                            p.getCommunicator().sendNormalServerMessage("You are too busy wielding a stake, " + f.getName() + " looks disinterested in following you.");
                        }
                        f.setLeader(null);
                    }
                }
            }
        }
        for (final Staker s : Stakers.stakers.values()) {
            if (s.isHuntOver()) {
                continue;
            }
            if (ts - s.getLastPoll() < 5000L) {
                continue;
            }
            final Creature player = getPlayer(s.getPlayerName());
            if (player == null) {
                s.setLastPoll(ts);
            }
            else if (isAtLegalLocation(player)) {
                s.increaseElapsedTime();
            }
            else {
                if (Server.rand.nextInt(100) < 10) {
                    if (!player.isOnGround()) {
                        player.getCommunicator().sendNormalServerMessage("It will take the purity of nature to wear the vampiric blood from your hands. As long as you remain on your mount, you will remain marked as a vampire slayer.");
                    }
                    else {
                        player.getCommunicator().sendNormalServerMessage("It will take the purity of nature to wear the vampiric blood from your hands. As long as you remain here, you will remain marked as a vampire slayer.");
                    }
                }
                Stakers.logger.log(Level.FINE, "Disallowed location for staker " + s.getPlayerName() + " " + player.getTileX() + ", " + player.getTileY() + ". Elapsed hunted timer remains at: " + Math.max(0L, s.getElapsedTime() / 1000L));
                s.setLastPoll(ts);
            }
        }
    }
    
    public static boolean isAtLegalLocation(final Creature player) {
        return player != null && player.isOnSurface() && !player.isOnDeed() && player.isOnGround() && player.isAlive() && !player.isDead() && !player.isFloating() && !player.isGhost() && !player.isLoggedOut() && !player.isTeleporting() && !Terraforming.isTileUnderWater(player.getCurrentTileNum(), player.getTileX(), player.getTileY(), player.isOnSurface()) && !isWithinBannedZone(player) && !isOnAPerimeter(player) && !isWithinEnclosure(player);
    }
    
    public static boolean isWithinBannedZone(final Creature c) {
        return false;
    }
    
    private static boolean isWithinEnclosure(final Creature c) {
        final long startTime = System.currentTimeMillis();
        final int radius = 75;
        final int diameter = radius * 2;
        final int plrX = radius;
        final int plrY = radius;
        final boolean[][] bits = getBuiltTilesSlow(c, radius);
        int raysChecked = 0;
        int obstaclesFound = 0;
        final float enclosureThreshold = 0.85f;
        if (!hasObstacleAlongRay(bits, plrX, plrY, 0, 0) || !hasObstacleAlongRay(bits, plrX, plrY, diameter, 0) || !hasObstacleAlongRay(bits, plrX, plrY, 0, diameter) || !hasObstacleAlongRay(bits, plrX, plrY, diameter, diameter)) {
            return false;
        }
        for (int destX = 0; destX < diameter; destX += 4) {
            for (int destY = 0; destY < diameter; destY += 4) {
                ++raysChecked;
                if (hasObstacleAlongRay(bits, plrX, plrY, destX, destY)) {
                    ++obstaclesFound;
                }
            }
        }
        final long cost = System.currentTimeMillis() - startTime;
        final float amountSurrounded = obstaclesFound / raysChecked;
        Stakers.logger.log(Level.FINE, "Location: " + c.getTileX() + "," + c.getTileY() + " Rays: " + raysChecked + " Obstacles: " + obstaclesFound + " Amount surrounded: " + amountSurrounded + " Threshold: " + enclosureThreshold + " inEnclosure Verdict: " + (amountSurrounded > enclosureThreshold) + " Cost: " + cost);
        return amountSurrounded > enclosureThreshold;
    }
    
    private static boolean hasObstacleAlongRay(final boolean[][] obstacles, int x1, int y1, final int destX, final int destY) {
        final int w = destX - x1;
        final int h = destY - y1;
        int dx1 = 0;
        int dy1 = 0;
        int dx2 = 0;
        int dy2 = 0;
        if (w < 0) {
            dx1 = -1;
        }
        else if (w > 0) {
            dx1 = 1;
        }
        if (h < 0) {
            dy1 = -1;
        }
        else if (h > 0) {
            dy1 = 1;
        }
        if (w < 0) {
            dx2 = -1;
        }
        else if (w > 0) {
            dx2 = 1;
        }
        int longest = Math.abs(w);
        int shortest = Math.abs(h);
        if (longest <= shortest) {
            longest = Math.abs(h);
            shortest = Math.abs(w);
            if (h < 0) {
                dy2 = -1;
            }
            else if (h > 0) {
                dy2 = 1;
            }
            dx2 = 0;
        }
        int numerator = longest >> 1;
        for (int i = 0; i <= longest; ++i) {
            if (obstacles[x1][y1]) {
                return true;
            }
            numerator += shortest;
            if (numerator >= longest) {
                numerator -= longest;
                x1 += dx1;
                y1 += dy1;
            }
            else {
                x1 += dx2;
                y1 += dy2;
            }
        }
        return false;
    }
    
    private static boolean[][] getBuiltTilesSlow(final Creature c, final int radius) {
        Connection dbcon = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        boolean[][] obstacles;
        try {
            dbcon = DbConnector.getZonesDbCon();
            ps = dbcon.prepareStatement("SELECT TILEX, TILEY FROM FENCES WHERE TILEX <= ? AND TILEX >= ? AND TILEY <= ? AND TILEY >= ? ORDER BY TILEX,TILEY");
            ps.setInt(1, c.getTileX() + radius);
            ps.setInt(2, c.getTileX() - radius);
            ps.setInt(3, c.getTileY() + radius);
            ps.setInt(4, c.getTileY() - radius);
            rs = ps.executeQuery();
            obstacles = new boolean[radius * 2 + 1][radius * 2 + 1];
            while (rs.next()) {
                final int normalizedX = rs.getInt("TILEX") - c.getTileX() + radius;
                final int normalizedY = rs.getInt("TILEY") - c.getTileY() + radius;
                obstacles[normalizedX][normalizedY] = true;
            }
            rs.close();
            ps.close();
            ps = dbcon.prepareStatement("SELECT TILEX, TILEY FROM WALLS WHERE TILEX <= ? AND TILEX >= ? AND TILEY <= ? AND TILEY >= ?");
            ps.setInt(1, c.getTileX() + radius);
            ps.setInt(2, c.getTileX() - radius);
            ps.setInt(3, c.getTileY() + radius);
            ps.setInt(4, c.getTileY() - radius);
            rs = ps.executeQuery();
            while (rs.next()) {
                final int normalizedX = rs.getInt("TILEX") - c.getTileX() + radius;
                final int normalizedY = rs.getInt("TILEY") - c.getTileY() + radius;
                obstacles[normalizedX][normalizedY] = true;
            }
            rs.close();
            ps.close();
            ps = dbcon.prepareStatement("SELECT TILEX, TILEY FROM BRIDGEPARTS WHERE TILEX <= ? AND TILEX >= ? AND TILEY <= ? AND TILEY >= ?");
            ps.setInt(1, c.getTileX() + radius);
            ps.setInt(2, c.getTileX() - radius);
            ps.setInt(3, c.getTileY() + radius);
            ps.setInt(4, c.getTileY() - radius);
            rs = ps.executeQuery();
            while (rs.next()) {
                final int normalizedX = rs.getInt("TILEX") - c.getTileX() + radius;
                final int normalizedY = rs.getInt("TILEY") - c.getTileY() + radius;
                obstacles[normalizedX][normalizedY] = true;
            }
        }
        catch (SQLException sqx) {
            throw new RuntimeException(sqx);
        }
        finally {
            DbUtilities.closeDatabaseObjects(ps, rs);
            DbConnector.returnConnection(dbcon);
        }
        DbUtilities.closeDatabaseObjects(ps, rs);
        DbConnector.returnConnection(dbcon);
        return obstacles;
    }
    
    private static boolean isOnAPerimeter(final Creature c) {
        final Village[] villages = Villages.getVillages();
        Village[] array;
        for (int length = (array = villages).length, i = 0; i < length; ++i) {
            final Village v = array[i];
            if (v.isWithinMinimumPerimeter(c.getTileX(), c.getTileY())) {
                return true;
            }
        }
        return false;
    }
    
    static void setUpEquipInterception() {
        Stakers.logger.log(Level.INFO, "doing setUpEquipInterception()");
        try {
            final String descriptor = Descriptor.ofMethod(CtClass.booleanType, new CtClass[] { HookManager.getInstance().getClassPool().get("com.wurmonline.server.items.Item"), HookManager.getInstance().getClassPool().get("com.wurmonline.server.creatures.Creature"), CtPrimitiveType.byteType, CtPrimitiveType.booleanType });
            HookManager.getInstance().registerHook("com.wurmonline.server.behaviours.AutoEquipMethods", "autoEquipWeapon", descriptor, new InvocationHandlerFactory() {
                public InvocationHandler createInvocationHandler() {
                    return new InvocationHandler() {
                        @Override
                        public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
                            final Object result = method.invoke(proxy, args);
                            if (Mod.logExecutionCost) {
                                Mod.tmpExecutionStartTime = System.nanoTime();
                            }
                            if (result) {
                                Stakers.afterWieldHook((Creature)args[1], (Item)args[0]);
                            }
                            if (Mod.logExecutionCost) {
                                Stakers.logger.log(Level.INFO, "setUpEquipInterception[hook] done, spent " + Mod.executionLogDf.format((System.nanoTime() - Mod.tmpExecutionStartTime) / 1.0E9) + "s");
                            }
                            return result;
                        }
                    };
                }
            });
        }
        catch (NotFoundException e) {
            Stakers.logger.log(Level.SEVERE, "Failed!", e);
            throw new RuntimeException("Failed to set up equip-interception");
        }
    }
    
    static void setUpUnequipInterception() {
        Stakers.logger.log(Level.INFO, "doing setUpUnequipInterception()");
        try {
            final CtClass c = HookManager.getInstance().getClassPool().get("com.wurmonline.server.behaviours.MethodsItems");
            final CtMethod m = c.getDeclaredMethod("drop");
            String descriptor = Descriptor.ofMethod(m.getReturnType(), new CtClass[] { HookManager.getInstance().getClassPool().get("com.wurmonline.server.creatures.Creature"), HookManager.getInstance().getClassPool().get("com.wurmonline.server.items.Item"), CtPrimitiveType.booleanType });
            HookManager.getInstance().registerHook("com.wurmonline.server.behaviours.MethodsItems", "drop", descriptor, new InvocationHandlerFactory() {
                public InvocationHandler createInvocationHandler() {
                    return new InvocationHandler() {
                        @Override
                        public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
                            if (Mod.logExecutionCost) {
                                Mod.tmpExecutionStartTime = System.nanoTime();
                            }
                            final Creature performer = (Creature)args[0];
                            final Item item = (Item)args[1];
                            if (performer instanceof Player && ((performer.getRighthandItem() != null && performer.getRighthandItem().getWurmId() == item.getWurmId()) || (performer.getLefthandItem() != null && performer.getLefthandItem().getWurmId() == item.getWurmId())) && !Stakers.allowUnequip(performer, item, null)) {
                                return new String[0];
                            }
                            if (Mod.logExecutionCost) {
                                Stakers.logger.log(Level.INFO, "setUpUnequipInterception[hook1] done, spent " + Mod.executionLogDf.format((System.nanoTime() - Mod.tmpExecutionStartTime) / 1.0E9) + "s");
                            }
                            final Object result = method.invoke(proxy, args);
                            return result;
                        }
                    };
                }
            });
            descriptor = Descriptor.ofMethod(CtClass.booleanType, new CtClass[] { HookManager.getInstance().getClassPool().get("com.wurmonline.server.creatures.Creature"), CtPrimitiveType.longType, CtPrimitiveType.booleanType });
            HookManager.getInstance().registerHook("com.wurmonline.server.items.Item", "moveToItem", descriptor, new InvocationHandlerFactory() {
                public InvocationHandler createInvocationHandler() {
                    return new InvocationHandler() {
                        @Override
                        public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
                            if (Mod.logExecutionCost) {
                                Mod.tmpExecutionStartTime = System.nanoTime();
                            }
                            final Creature mover = (Creature)args[0];
                            if (mover instanceof Player) {
                                final Item item = (Item)proxy;
                                final Item target = Items.getItem((long)args[1]);
                                if ((mover.getRighthandItem() != null && mover.getRighthandItem().getWurmId() == item.getWurmId()) || (mover.getLefthandItem() != null && mover.getLefthandItem().getWurmId() == item.getWurmId())) {
                                    if (!Stakers.allowUnequip(mover, (Item)proxy, Items.getItem((long)args[1]))) {
                                        return false;
                                    }
                                    final Object result = method.invoke(proxy, args);
                                    return result;
                                }
                                else if (target != null && target.isBodyPart() && item.isWeapon() && item.getTemplateId() == Stake.getId()) {
                                    final Object result = method.invoke(proxy, args);
                                    if (result) {
                                        Stakers.afterWieldHook(mover, item);
                                    }
                                    return result;
                                }
                            }
                            if (Mod.logExecutionCost) {
                                Stakers.logger.log(Level.INFO, "setUpUnequipInterception[hook2] done, spent " + Mod.executionLogDf.format((System.nanoTime() - Mod.tmpExecutionStartTime) / 1.0E9) + "s");
                            }
                            final Object result2 = method.invoke(proxy, args);
                            return result2;
                        }
                    };
                }
            });
            descriptor = Descriptor.ofMethod(CtClass.booleanType, new CtClass[] { HookManager.getInstance().getClassPool().get("com.wurmonline.server.items.Item"), HookManager.getInstance().getClassPool().get("com.wurmonline.server.creatures.Creature") });
            HookManager.getInstance().registerHook("com.wurmonline.server.behaviours.AutoEquipMethods", "unequip", descriptor, new InvocationHandlerFactory() {
                public InvocationHandler createInvocationHandler() {
                    return new InvocationHandler() {
                        @Override
                        public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
                            if (Mod.logExecutionCost) {
                                Mod.tmpExecutionStartTime = System.nanoTime();
                            }
                            final Item item = (Item)args[0];
                            final Creature mover = (Creature)args[1];
                            if (mover instanceof Player && ((mover.getRighthandItem() != null && mover.getRighthandItem().getWurmId() == item.getWurmId()) || (mover.getLefthandItem() != null && mover.getLefthandItem().getWurmId() == item.getWurmId())) && !Stakers.allowUnequip(mover, item, null)) {
                                return false;
                            }
                            if (Mod.logExecutionCost) {
                                Stakers.logger.log(Level.INFO, "setUpUnequipInterception[hook3] done, spent " + Mod.executionLogDf.format((System.nanoTime() - Mod.tmpExecutionStartTime) / 1.0E9) + "s");
                            }
                            final Object result = method.invoke(proxy, args);
                            return result;
                        }
                    };
                }
            });
            descriptor = Descriptor.ofMethod(CtClass.booleanType, new CtClass[] { HookManager.getInstance().getClassPool().get("com.wurmonline.server.items.Item"), HookManager.getInstance().getClassPool().get("com.wurmonline.server.creatures.Creature") });
            HookManager.getInstance().registerHook("com.wurmonline.server.behaviours.AutoEquipMethods", "dropToInventory", descriptor, new InvocationHandlerFactory() {
                public InvocationHandler createInvocationHandler() {
                    return new InvocationHandler() {
                        @Override
                        public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
                            if (Mod.logExecutionCost) {
                                Mod.tmpExecutionStartTime = System.nanoTime();
                            }
                            final Item item = (Item)args[0];
                            final Creature mover = (Creature)args[1];
                            if (mover instanceof Player && ((mover.getRighthandItem() != null && mover.getRighthandItem().getWurmId() == item.getWurmId()) || (mover.getLefthandItem() != null && mover.getLefthandItem().getWurmId() == item.getWurmId())) && !Stakers.allowUnequip(mover, item, null)) {
                                return false;
                            }
                            if (Mod.logExecutionCost) {
                                Stakers.logger.log(Level.INFO, "setUpUnequipInterception[hook4] done, spent " + Mod.executionLogDf.format((System.nanoTime() - Mod.tmpExecutionStartTime) / 1.0E9) + "s");
                            }
                            final Object result = method.invoke(proxy, args);
                            return result;
                        }
                    };
                }
            });
        }
        catch (NotFoundException e) {
            Stakers.logger.log(Level.SEVERE, "Failed!", e);
            throw new RuntimeException("Failed to set up unequip-interception");
        }
    }
    
    static void setUpTeleportInterception() {
        Stakers.logger.log(Level.INFO, "doing setUpTeleportInterception()");
        try {
            String descriptor = Descriptor.ofMethod(CtClass.voidType, new CtClass[] { HookManager.getInstance().getClassPool().get("java.util.Properties") });
            HookManager.getInstance().registerHook("com.wurmonline.server.questions.VillageTeleportQuestion", "answer", descriptor, new InvocationHandlerFactory() {
                public InvocationHandler createInvocationHandler() {
                    return new InvocationHandler() {
                        @Override
                        public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
                            if (Mod.logExecutionCost) {
                                Mod.tmpExecutionStartTime = System.nanoTime();
                            }
                            Object result = null;
                            final Properties answers = (Properties)args[0];
                            final VillageTeleportQuestion q = (VillageTeleportQuestion)proxy;
                            final boolean teleport = answers.getProperty("teleport") != null && answers.getProperty("teleport").equals("true");
                            if (teleport && Stakers.isHunted(q.getResponder())) {
                                q.getResponder().getCommunicator().sendNormalServerMessage("You are hunted. The blood on your hands prevent you from teleporting now.");
                                result = null;
                            }
                            else {
                                result = method.invoke(proxy, args);
                            }
                            if (Mod.logExecutionCost) {
                                Stakers.logger.log(Level.INFO, "setUpTeleportInterception[hook1] done, spent " + Mod.executionLogDf.format((System.nanoTime() - Mod.tmpExecutionStartTime) / 1.0E9) + "s");
                            }
                            return result;
                        }
                    };
                }
            });
            HookManager.getInstance().registerHook("com.wurmonline.server.questions.KarmaQuestion", "answer", descriptor, new InvocationHandlerFactory() {
                public InvocationHandler createInvocationHandler() {
                    return new InvocationHandler() {
                        @Override
                        public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
                            if (Mod.logExecutionCost) {
                                Mod.tmpExecutionStartTime = System.nanoTime();
                            }
                            final Properties answers = (Properties)args[0];
                            final KarmaQuestion q = (KarmaQuestion)proxy;
                            final boolean teleport = answers.getProperty("val") != null && answers.getProperty("val").equals("townportal");
                            Object result;
                            if (teleport && Stakers.isHunted(q.getResponder())) {
                                q.getResponder().getCommunicator().sendNormalServerMessage("You are hunted. The blood on your hands prevent you from teleporting now.");
                                result = null;
                            }
                            else {
                                result = method.invoke(proxy, args);
                            }
                            if (Mod.logExecutionCost) {
                                Stakers.logger.log(Level.INFO, "setUpTeleportInterception[hook2] done, spent " + Mod.executionLogDf.format((System.nanoTime() - Mod.tmpExecutionStartTime) / 1.0E9) + "s");
                            }
                            return result;
                        }
                    };
                }
            });
            descriptor = Descriptor.ofMethod(CtClass.booleanType, new CtClass[0]);
            HookManager.getInstance().registerHook("com.wurmonline.server.players.Cultist", "mayTeleport", descriptor, new InvocationHandlerFactory() {
                public InvocationHandler createInvocationHandler() {
                    return new InvocationHandler() {
                        @Override
                        public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
                            if (Mod.logExecutionCost) {
                                Mod.tmpExecutionStartTime = System.nanoTime();
                            }
                            if (Stakers.isHunted(((Cultist)proxy).getWurmId())) {
                                final Player p = Players.getInstance().getPlayer(((Cultist)proxy).getWurmId());
                                p.getCommunicator().sendNormalServerMessage("You are hunted. The blood on your hands prevent you from teleporting now.");
                                return false;
                            }
                            if (Mod.logExecutionCost) {
                                Stakers.logger.log(Level.INFO, "setUpTeleportInterception[hook3] done, spent " + Mod.executionLogDf.format((System.nanoTime() - Mod.tmpExecutionStartTime) / 1.0E9) + "s");
                            }
                            final Object result = method.invoke(proxy, args);
                            return result;
                        }
                    };
                }
            });
        }
        catch (NotFoundException e) {
            Stakers.logger.log(Level.SEVERE, "Failed!", e);
            throw new RuntimeException("Failed to setUpTeleportInterception");
        }
    }
    
    public static boolean allowUnequip(final Creature performer, final Item item, @Nullable final Item destination) {
        if (item.getTemplateId() != Stake.getId()) {
            return true;
        }
        if (destination != null && destination.getTemplateId() == 670) {
            return true;
        }
        Mod.actionNotify(performer, "The magical runes of the stake are fused to you. You can get rid of the stake is by throwing it in a trash heap or using it on a Vampire.", "%NAME tries to get rid of a magical stake in frustration.", "You hear some muffled sounds, almost as if someone is grumbling.");
        performer.getCommunicator().sendNormalServerMessage("The magical runes of the stake are fused to you. You can get rid of the stake is by throwing it in a trash heap or using it on a Vampire.");
        return false;
    }
    
    public static void afterWieldHook(final Creature performer, final Item item) {
        if (item.getTemplateId() == Stake.getId()) {
            final EventOnce ev = new StakeWieldedEvent(10, EventOnce.Unit.SECONDS, performer, item);
            EventDispatcher.add(ev);
        }
    }
    
    public static void addBitable(final long wurmId) {
        Stakers.bitables.put(wurmId, System.currentTimeMillis());
    }
    
    public static void removeBitable(final long wurmId) {
        Stakers.bitables.remove(wurmId);
    }
}
