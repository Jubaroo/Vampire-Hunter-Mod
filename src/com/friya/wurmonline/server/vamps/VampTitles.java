// 
// Decompiled by Procyon v0.5.30
// 

package com.friya.wurmonline.server.vamps;

import com.friya.tools.EnumBuster;
import com.wurmonline.server.DbConnector;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.players.PlayerInfo;
import com.wurmonline.server.players.PlayersProxy;
import com.wurmonline.server.players.Titles;
import com.wurmonline.server.utils.DbUtilities;
import javassist.CtClass;
import javassist.CtPrimitiveType;
import javassist.bytecode.Descriptor;
import org.gotti.wurmunlimited.modloader.classhooks.HookManager;
import org.gotti.wurmunlimited.modloader.classhooks.InvocationHandlerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class VampTitles
{
    private static Logger logger;
    private static Titles.Title[] titleArray;
    public static int VAMPIRE_SLAYER;
    public static int VAMPIRE_HUNTER;
    public static int VAN_HELSING;
    public static int ESCAPIST;
    
    static {
        VampTitles.logger = Logger.getLogger(VampTitles.class.getName());
        VampTitles.VAMPIRE_SLAYER = 818801;
        VampTitles.VAMPIRE_HUNTER = 818802;
        VampTitles.VAN_HELSING = 818803;
        VampTitles.ESCAPIST = 818804;
    }
    
    public static boolean hasTitle(final Creature c, final int titleId) {
        if (c.isPlayer()) {
            final Titles.Title[] titles = ((Player)c).getTitles();
            Titles.Title[] array;
            for (int length = (array = titles).length, i = 0; i < length; ++i) {
                final Titles.Title title = array[i];
                if (title == null) {
                    throw new RuntimeException("We have NULL in titles collection, that is not nice at all!");
                }
                if (title.getTitleId() == titleId) {
                    return true;
                }
            }
            return false;
        }
        return true;
    }
    
    public static void onItemTemplatesCreated() {
        interceptLoadTitles();
        final EnumBuster<Titles.Title> buster = new EnumBuster<Titles.Title>(Titles.Title.class, Titles.Title.class);
        createTitle(buster, "VAMPIRE_SLAYER", VampTitles.VAMPIRE_SLAYER, "Vampire Slayer", -1, Titles.TitleType.MINOR);
        createTitle(buster, "VAMPIRE_HUNTER", VampTitles.VAMPIRE_HUNTER, "Vampire Hunter", -1, Titles.TitleType.MINOR);
        createTitle(buster, "VAN_HELSING", VampTitles.VAN_HELSING, "van Helsing", -1, Titles.TitleType.MINOR);
        createTitle(buster, "ESCAPIST", VampTitles.ESCAPIST, "Escapist", -1, Titles.TitleType.MINOR);
        VampTitles.titleArray = Titles.Title.values();
    }
    
    private static void createTitle(final EnumBuster<Titles.Title> buster, final String enumName, final int id, final String title, final int skillId, final Titles.TitleType type) {
        final Titles.Title testTitle = buster.make(enumName, 0, new Class[] { Integer.TYPE, String.class, String.class, Integer.TYPE, Titles.TitleType.class }, new Object[] { id, title, title, skillId, type });
        buster.addByValue(testTitle);
        VampTitles.logger.log(Level.INFO, "Created title: " + title);
    }
    
    private static void interceptLoadTitles() {
        final String descriptor = Descriptor.ofMethod(CtPrimitiveType.voidType, new CtClass[] { CtClass.longType });
        HookManager.getInstance().registerHook("com.wurmonline.server.players.DbPlayerInfo", "loadTitles", descriptor, new InvocationHandlerFactory() {
            public InvocationHandler createInvocationHandler() {
                return new InvocationHandler() {
                    @Override
                    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
                        if (Mod.logExecutionCost) {
                            Mod.tmpExecutionStartTime = System.nanoTime();
                        }
                        final Object result = method.invoke(proxy, args);
                        final PlayerInfo pi = (PlayerInfo)proxy;
                        final Set<Titles.Title> titles = PlayersProxy.getTitles(pi);
                        titles.remove(null);
                        Connection dbcon = null;
                        PreparedStatement ps = null;
                        ResultSet rs = null;
                        Label_0217: {
                            try {
                                dbcon = DbConnector.getPlayerDbCon();
                                ps = dbcon.prepareStatement("select TITLEID from TITLES where WURMID=?");
                                ps.setLong(1, pi.getPlayerId());
                                rs = ps.executeQuery();
                                while (rs.next()) {
                                    if (Titles.Title.getTitle(rs.getInt("TITLEID")) == null) {
                                        titles.add(VampTitles.getTitle(rs.getInt("TITLEID")));
                                    }
                                }
                            }
                            catch (SQLException ex) {
                                VampTitles.logger.log(Level.INFO, "Failed to load titles for  " + pi.getPlayerId(), ex);
                                break Label_0217;
                            }
                            finally {
                                DbUtilities.closeDatabaseObjects(ps, rs);
                                DbConnector.returnConnection(dbcon);
                            }
                            DbUtilities.closeDatabaseObjects(ps, rs);
                            DbConnector.returnConnection(dbcon);
                        }
                        if (Mod.logExecutionCost) {
                            VampTitles.logger.log(Level.INFO, "interceptLoadTitles[hook] done, spent " + Mod.executionLogDf.format((System.nanoTime() - Mod.tmpExecutionStartTime) / 1.0E9) + "s");
                        }
                        return result;
                    }
                };
            }
        });
    }
    
    public static Titles.Title getTitle(final int titleAsInt) {
        for (int i = 0; i < VampTitles.titleArray.length; ++i) {
            if (titleAsInt == VampTitles.titleArray[i].getTitleId()) {
                return VampTitles.titleArray[i];
            }
        }
        throw new RuntimeException("Could not find title: " + titleAsInt);
    }
}
