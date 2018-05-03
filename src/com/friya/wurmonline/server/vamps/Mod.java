// 
// Decompiled by Procyon v0.5.30
// 

package com.friya.wurmonline.server.vamps;

import com.friya.wurmonline.server.loot.LootSystem;
import com.friya.wurmonline.server.vamps.actions.*;
import com.friya.wurmonline.server.vamps.items.*;
import com.wurmonline.server.Server;
import com.wurmonline.server.Servers;
import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.skills.Skills;
import com.wurmonline.shared.constants.Version;
import javassist.*;
import javassist.bytecode.Descriptor;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;
import org.gotti.wurmunlimited.modloader.classhooks.HookException;
import org.gotti.wurmunlimited.modloader.classhooks.HookManager;
import org.gotti.wurmunlimited.modloader.classhooks.InvocationHandlerFactory;
import org.gotti.wurmunlimited.modloader.interfaces.*;
import org.gotti.wurmunlimited.modsupport.actions.ModActions;
import org.gotti.wurmunlimited.modsupport.vehicles.ModVehicleBehaviours;

import javax.annotation.Nullable;
import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.text.DecimalFormat;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Mod implements WurmServerMod, Initable, Configurable, ServerStartedListener, PreInitable, ItemTemplatesCreatedListener, ServerPollListener, PlayerLoginListener, PlayerMessageListener
{
    private static Logger logger;
    private static long lastPoll;
    private static boolean enableSkillsOnly;
    private static boolean executionCostLogging;
    public static String halfVampClueUrl;
    public static boolean logExecutionCost;
    public static long tmpExecutionStartTime;
    public static DecimalFormat executionLogDf;
    public static double totalExecutionCost;
    public static long totalExecutionCostStartTime;
    
    static {
        Mod.logger = Logger.getLogger(Mod.class.getName());
        Mod.lastPoll = 0L;
        Mod.enableSkillsOnly = false;
        Mod.executionCostLogging = false;
        Mod.halfVampClueUrl = "http://filterbubbles.com/img/wu/half-vamp-clue2.png";
        Mod.logExecutionCost = true;
        Mod.tmpExecutionStartTime = 0L;
        Mod.executionLogDf = new DecimalFormat("#.#########");
        Mod.totalExecutionCost = 0.0;
        Mod.totalExecutionCostStartTime = -1L;
    }
    
    public void configure(final Properties properties) {
        Mod.logger.log(Level.INFO, "configure called");
        Mod.enableSkillsOnly = Boolean.valueOf(properties.getProperty("enableSkillsOnly", String.valueOf(Mod.enableSkillsOnly)));
        Mod.executionCostLogging = Boolean.valueOf(properties.getProperty("executionCostLogging", String.valueOf(Mod.executionCostLogging)));
        Mod.logExecutionCost = Mod.executionCostLogging;
        Mod.halfVampClueUrl = String.valueOf(properties.getProperty("halfVampClueUrl", String.valueOf(Mod.halfVampClueUrl)));
        if (!Mod.enableSkillsOnly) {
            Mod.logger.log(Level.INFO, "          _   ,_,   _       ");
            Mod.logger.log(Level.INFO, "         / `'=) (='` \\       Configuring Vampires for Wurm Unlimited");
            Mod.logger.log(Level.INFO, "        /.-.-.\\ /.-.-.\\      (c)2016-2017 Friya (aka Friyanouce) <dgdhttpd@gmail.com>");
            Mod.logger.log(Level.INFO, "        `      \"      `      ");
        }
        Mod.logger.log(Level.INFO, "enableSkillsOnly: " + Mod.enableSkillsOnly);
        Mod.logger.log(Level.INFO, "executionCostLogging: " + Mod.executionCostLogging);
        Mod.logger.log(Level.INFO, "halfVampClueUrl: " + Mod.halfVampClueUrl);
        Mod.logger.log(Level.INFO, "all configure completed");
        Mod.logger.info("Major version: " + Version.getMajor());
    }
    
    public void preInit() {
        if (Mod.enableSkillsOnly) {
            return;
        }
        Mod.logger.log(Level.INFO, "preInit called");
        Mod.logger.log(Level.INFO, "all preInits completed");
    }
    
    public void init() {
        if (Mod.enableSkillsOnly) {
            return;
        }
        Mod.logger.log(Level.INFO, "init called");
        setUpLogoutTimeInterception();
        setUpNpcMovementPrevention();
        setUpMountSpeedInterception();
        setUpStealthDetectInterception();
        setupCovenChatHook();
        ModActions.init();
        ModVehicleBehaviours.init();
        Mod.logger.log(Level.INFO, "all init completed");
    }
    
    public void onItemTemplatesCreated() {
        Mod.logger.log(Level.INFO, "onItemTemplatesCreated called");
        thaw("com.wurmonline.server.players.Player");
        if (Mod.enableSkillsOnly) {
            VampSkills.onItemTemplatesCreated(false);
            VampTitles.onItemTemplatesCreated();
            this.addItems();
            return;
        }
        DynamicExamine.onItemTemplatesCreated();
        this.startLogoutListener();
        this.startAlterSkillListener();
        BloodlessHusk.onItemTemplatesCreated();
        VampSkills.onItemTemplatesCreated();
        Vampires.onItemTemplatesCreated();
        BloodLust.onItemTemplatesCreated();
        VampTitles.onItemTemplatesCreated();
        Creatures.onTemplatesCreated();
        Stakers.onItemTemplatesCreated();
        this.addItems();
        Traders.onServerStarted();
        Mod.logger.log(Level.INFO, "all onItemTemplatesCreated completed");
    }
    
    private void addItems() {
        SmallRat.onItemTemplatesCreated();
        Amulet.onItemTemplatesCreated();
        Stake.onItemTemplatesCreated();
        Pouch.onItemTemplatesCreated();
        Mirror.onItemTemplatesCreated();
        VampireFang.onItemTemplatesCreated();
        HalfVampireClue.onItemTemplatesCreated();
        Crown.onItemTemplatesCreated();
        AltarOfSouls.onItemTemplatesCreated();
    }
    
    private void setupItems() {
        Amulet.onServerStarted();
        Stake.onServerStarted();
        Pouch.onServerStarted();
        HalfVampireClue.onServerStarted();
        Crown.onServerStarted();
        AltarOfSouls.onServerStarted();
    }
    
    public void onServerStarted() {
        Mod.logger.log(Level.INFO, "onServerStarted called");
        if (Mod.enableSkillsOnly) {
            VampAchievements.onServerStarted();
            return;
        }
        ActionSkillGains.onServerStarted();
        Vampires.onServerStarted();
        Stakers.onServerStarted();
        this.setupItems();
        PriestSpells.onServerStarted();
        VampZones.onServerStarted();
        VampAchievements.onServerStarted();
        Creatures.onServerStarted();
        if (isTestEnv()) {
            ModActions.registerAction(new TestAction());
        }
        ModActions.registerAction(new DevourAction());
        ModActions.registerAction(new BuyKitAction());
        ModActions.registerAction(new StakeAction());
        ModActions.registerAction(new SenseAction());
        ModActions.registerAction(new SprintAction());
        ModActions.registerAction(new BiteAction());
        ModActions.registerAction(new CrippleAction());
        ModActions.registerAction(new DisarmAction());
        ModActions.registerAction(new AidAction());
        ModActions.registerAction(new TraceAction());
        ModActions.registerAction(new FlyAction());
        ModActions.registerAction(new MirrorAction());
        ModActions.registerAction(new PolishMirrorAction());
        ModActions.registerAction(new MakeSeryllStakeAction());
        ModActions.registerAction(new SmashAction());
        ModActions.registerAction(new HalfVampAction());
        ModActions.registerAction(new SacrificeOrlokAction());
        ModActions.registerAction(new HalfVampClueAction());
        ModActions.registerAction(new DevampAction());
        ModActions.registerAction(new AdminDevampAction());
        ModActions.registerAction(new AdminVampAction());
        ModActions.registerAction(new CrownFindAction());
        ModActions.registerAction(new AssistSlainAction());
        ModActions.registerAction(new AbortAction());
        ModActions.registerAction(new ToplistVampsAction());
        ModActions.registerAction(new SacrificeAltarOfSoulsAction());
        ModActions.registerAction(new StealthAction());
        ModActions.registerAction(new LabyrinthAction());
        ModActions.registerAction(new LabyrinthRemoveAction());
        LootSystem.getInstance();
        CreatureLoot.onServerStarted();
        Mod.logger.log(Level.INFO, "all onServerStarted completed");
    }
    
    public void onPlayerLogin(final Player p) {
        Mod.logger.info("Player login. Name: " + p.getName() + " SteamID: " + p.SteamId);
        if (Mod.enableSkillsOnly) {
            return;
        }
        if (Mod.logExecutionCost) {
            Mod.logger.log(Level.INFO, "onPlayerLogin called");
            Mod.tmpExecutionStartTime = System.nanoTime();
        }
        loginVampire(p);
        Stakers.onPlayerLogin(p);
        if (Mod.logExecutionCost) {
            Mod.logger.log(Level.INFO, "onPlayerLogin done, spent " + Mod.executionLogDf.format((System.nanoTime() - Mod.tmpExecutionStartTime) / 1.0E9) + "s");
        }
    }
    
    public static void loginVampire(final Player p) {
        if (Mod.enableSkillsOnly) {
            return;
        }
        if (Mod.logExecutionCost) {
            Mod.logger.log(Level.INFO, "loginVampire called");
            Mod.tmpExecutionStartTime = System.nanoTime();
        }
        VampSkills.onPlayerLogin(p);
        CovenChat.onPlayerLogin(p);
        Vampires.onPlayerLogin(p);
        if (Mod.logExecutionCost) {
            Mod.logger.log(Level.INFO, "loginVampire done, spent " + Mod.executionLogDf.format((System.nanoTime() - Mod.tmpExecutionStartTime) / 1.0E9) + "s");
        }
    }
    
    public void onPlayerLogout(final Player p) {
        if (Mod.enableSkillsOnly) {
            return;
        }
        if (Mod.logExecutionCost) {
            Mod.logger.log(Level.INFO, "onPlayerLogout called");
            Mod.tmpExecutionStartTime = System.nanoTime();
        }
        Stakers.onPlayerLogout(p);
        if (Mod.logExecutionCost) {
            Mod.logger.log(Level.INFO, "onPlayerLogout done, spent " + Mod.executionLogDf.format((System.nanoTime() - Mod.tmpExecutionStartTime) / 1.0E9) + "s");
        }
    }
    
    public void onServerPoll() {
        if (Mod.enableSkillsOnly) {
            return;
        }
        if (System.currentTimeMillis() - Mod.lastPoll < 1000L) {
            return;
        }
        if (Mod.logExecutionCost) {
            Mod.tmpExecutionStartTime = System.nanoTime();
        }
        EventDispatcher.poll();
        Vampires.poll();
        Stakers.poll();
        Mod.lastPoll = System.currentTimeMillis();
        if (Mod.logExecutionCost) {
            Mod.logger.log(Level.INFO, "onServerPoll done, spent " + Mod.executionLogDf.format((System.nanoTime() - Mod.tmpExecutionStartTime) / 1.0E9) + "s");
        }
    }
    
    public boolean onPlayerMessage(final Communicator c, final String msg) {
        if (Mod.enableSkillsOnly) {
            return false;
        }
        if (Mod.logExecutionCost) {
            Mod.logger.log(Level.INFO, "onPlayerMessage called");
            Mod.tmpExecutionStartTime = System.nanoTime();
        }
        boolean intercepted = false;
        if (ChatCommands.onPlayerMessage(c, msg)) {
            intercepted = true;
        }
        if (Mod.logExecutionCost) {
            Mod.logger.log(Level.INFO, "onPlayerMessage done, spent " + Mod.executionLogDf.format((System.nanoTime() - Mod.tmpExecutionStartTime) / 1.0E9) + "s");
        }
        return intercepted;
    }
    
    static void stopPrune(final String className) {
        try {
            final ClassPool cp = HookManager.getInstance().getClassPool();
            final CtClass c = cp.get(className);
            c.stopPruning(true);
            Mod.logger.log(Level.INFO, "Stopped pruning " + className);
        }
        catch (NotFoundException e) {
            throw new HookException(e);
        }
    }
    
    static void thaw(final String className) {
        try {
            final ClassPool cp = HookManager.getInstance().getClassPool();
            final CtClass c = cp.get(className);
            c.stopPruning(true);
            c.writeFile();
            c.defrost();
            Mod.logger.log(Level.INFO, "Thawed " + className);
        }
        catch (CannotCompileException | IOException | NotFoundException ex2) {
            final Exception e = ex2;
            throw new HookException(e);
        }
    }
    
    static void appendToFile(final Exception e) {
        try {
            final FileWriter fstream = new FileWriter("VampsException.txt", true);
            final BufferedWriter out = new BufferedWriter(fstream);
            final PrintWriter pWriter = new PrintWriter(out, true);
            e.printStackTrace(pWriter);
            pWriter.close();
        }
        catch (Exception ie) {
            throw new RuntimeException("Could not write Exception to file", ie);
        }
    }
    
    String loadString(final String id) {
        try {
            final String path = System.getProperty("user.dir").replace(" ", "%20").replace("\\", "/");
            final URLClassLoader myLoader = URLClassLoader.newInstance(new URL[] { new URL("jar:file:/" + path + "/mods/vamps/vamps.jar!/com/friya/wurmonline/server/vamps/") });
            final InputStream is = myLoader.getResourceAsStream(id);
            if (is == null) {
                throw new RuntimeException("Failed to load resource: " + id);
            }
            final BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            final StringBuilder builder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
                builder.append(System.lineSeparator());
            }
            final String ret = builder.toString();
            return ret;
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    private void startLogoutListener() {
        Mod.logger.log(Level.INFO, "startLogoutListener()");
        final String descriptor = Descriptor.ofMethod(CtClass.voidType, new CtClass[0]);
        HookManager.getInstance().registerHook("com.wurmonline.server.players.Player", "logout", descriptor, new InvocationHandlerFactory() {
            public InvocationHandler createInvocationHandler() {
                return new InvocationHandler() {
                    @Override
                    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
                        final Object result = method.invoke(proxy, args);
                        if (proxy instanceof Player) {
                            Mod.this.onPlayerLogout((Player)proxy);
                        }
                        return result;
                    }
                };
            }
        });
    }
    
    private void setupFreeDeathInterception() {
        Mod.logger.log(Level.INFO, "setupFreeDeathInterception()");
        final String descriptor = Descriptor.ofMethod(CtClass.voidType, new CtClass[] { CtPrimitiveType.booleanType, CtPrimitiveType.intType, CtPrimitiveType.intType });
        HookManager.getInstance().registerHook("com.wurmonline.server.players.Player", "setDeathEffects", descriptor, new InvocationHandlerFactory() {
            public InvocationHandler createInvocationHandler() {
                return new InvocationHandler() {
                    @Override
                    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
                        final Object result = method.invoke(proxy, args);
                        return result;
                    }
                };
            }
        });
    }
    
    private void startAlterSkillListener() {
        final String descriptor = Descriptor.ofMethod(CtClass.voidType, new CtClass[] { CtPrimitiveType.doubleType, CtPrimitiveType.booleanType, CtPrimitiveType.floatType, CtPrimitiveType.booleanType, CtPrimitiveType.doubleType });
        HookManager.getInstance().registerHook("com.wurmonline.server.skills.Skill", "alterSkill", descriptor, new InvocationHandlerFactory() {
            public InvocationHandler createInvocationHandler() {
                return new InvocationHandler() {
                    @Override
                    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
                        if (Mod.logExecutionCost) {
                            Mod.tmpExecutionStartTime = System.nanoTime();
                        }
                        final Skill s = (Skill)proxy;
                        if (s.getNumber() == 10086 && s.getId() != -152L) {
                            final Field field = Skill.class.getDeclaredField("parent");
                            field.setAccessible(true);
                            final Skills skills = (Skills)field.get(s);
                            final long playerId = skills.getId();
                            if (Vampires.isVampire(playerId)) {
                                final Double multiplier = (Double)args[0];
                                args[0] = multiplier * 2.5;
                            }
                        }
                        if (s.getId() == -152L) {
                            double advanceMultiplicator = (double)args[0];
                            advanceMultiplicator *= (Servers.localServer.EPIC ? 3.0 : 1.5);
                            float staminaMod = 1.0f;
                            staminaMod += (float)Math.max(0.048999999254941945, 0.0);
                            advanceMultiplicator *= staminaMod;
                            args[0] = advanceMultiplicator;
                        }
                        if (Mod.logExecutionCost) {
                            Mod.logger.log(Level.INFO, "startAlterSkillListener[hook] done, spent " + Mod.executionLogDf.format((System.nanoTime() - Mod.tmpExecutionStartTime) / 1.0E9) + "s");
                        }
                        final Object result = method.invoke(proxy, args);
                        return result;
                    }
                };
            }
        });
    }
    
    private static void setUpLogoutTimeInterception() {
        Mod.logger.log(Level.INFO, "doing setUpLogoutTimeInterception()");
        final String descriptor = Descriptor.ofMethod(CtClass.intType, new CtClass[0]);
        HookManager.getInstance().registerHook("com.wurmonline.server.players.Player", "getSecondsToLogout", descriptor, new InvocationHandlerFactory() {
            public InvocationHandler createInvocationHandler() {
                return new InvocationHandler() {
                    @Override
                    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
                        Object result = method.invoke(proxy, args);
                        if (Mod.logExecutionCost) {
                            Mod.tmpExecutionStartTime = System.nanoTime();
                        }
                        if ((int)result < 60 && Stakers.isHunted((Creature)proxy)) {
                            result = 60;
                        }
                        if (Mod.logExecutionCost) {
                            Mod.logger.log(Level.INFO, "setUpLogoutTimeInterception[hook] done, spent " + Mod.executionLogDf.format((System.nanoTime() - Mod.tmpExecutionStartTime) / 1.0E9) + "s");
                        }
                        return result;
                    }
                };
            }
        });
    }
    
    private static void setUpNpcMovementPrevention() {
        final String descriptor = Descriptor.ofMethod(CtPrimitiveType.voidType, new CtClass[] { CtPrimitiveType.intType });
        HookManager.getInstance().registerHook("com.wurmonline.server.creatures.Creature", "startPathing", descriptor, new InvocationHandlerFactory() {
            public InvocationHandler createInvocationHandler() {
                return new InvocationHandler() {
                    @Override
                    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
                        if (Mod.logExecutionCost) {
                            Mod.tmpExecutionStartTime = System.nanoTime();
                        }
                        if (((Creature)proxy).isNpc() && Creatures.stopNpcMoveHook((Creature)proxy)) {
                            return null;
                        }
                        if (Mod.logExecutionCost) {
                            Mod.logger.log(Level.INFO, "setUpNpcMovementPrevention[hook] done, spent " + Mod.executionLogDf.format((System.nanoTime() - Mod.tmpExecutionStartTime) / 1.0E9) + "s");
                        }
                        final Object result = method.invoke(proxy, args);
                        return result;
                    }
                };
            }
        });
    }
    
    private static void setUpMountSpeedInterception() {
        final String descriptor = Descriptor.ofMethod(CtPrimitiveType.floatType, new CtClass[] { CtPrimitiveType.booleanType });
        HookManager.getInstance().registerHook("com.wurmonline.server.creatures.Creature", "getTraitMovePercent", descriptor, new InvocationHandlerFactory() {
            public InvocationHandler createInvocationHandler() {
                return new InvocationHandler() {
                    @Override
                    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
                        Object result = method.invoke(proxy, args);
                        if ((float)result > 0.0f && Stakers.isHunted(((Creature)proxy).getMountVehicle().pilotId)) {
                            result = 0.0f;
                        }
                        return result;
                    }
                };
            }
        });
    }
    
    private static void setUpStealthDetectInterception() {
        try {
            final String descriptor = Descriptor.ofMethod(CtPrimitiveType.booleanType, new CtClass[] { HookManager.getInstance().getClassPool().get("com.wurmonline.server.creatures.Creature"), CtPrimitiveType.floatType });
            HookManager.getInstance().registerHook("com.wurmonline.server.creatures.Creature", "visibilityCheck", descriptor, new InvocationHandlerFactory() {
                public InvocationHandler createInvocationHandler() {
                    return new InvocationHandler() {
                        @Override
                        public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
                            final Creature me;
                            final Creature watcher;
                            if ((me = (Creature)proxy) instanceof Player && me.isStealth() && (watcher = (Creature)args[0]) instanceof Player && me.getPower() == 0 && watcher.getPower() == 0 && Vampires.isVampire(me.getWurmId())) {
                                return false;
                            }
                            final Object result = method.invoke(proxy, args);
                            return result;
                        }
                    };
                }
            });
        }
        catch (NotFoundException e) {
            throw new HookException(e);
        }
    }
    
    private static void setupCovenChatHook() {
        final ClassPool cp = HookManager.getInstance().getClassPool();
        try {
            final CtClass c = cp.get("com.wurmonline.server.creatures.Communicator");
            c.getDeclaredMethod("reallyHandle_CMD_MESSAGE").instrument(new ExprEditor() {
                public void edit(final MethodCall m) throws CannotCompileException {
                    if (m.getMethodName().equals("isGlobalKingdomChat")) {
                        m.replace("com.friya.wurmonline.server.vamps.CovenChat.message(title, message, this.player);$_ = $proceed($$);");
                        Mod.logger.log(Level.INFO, "applied hook for channel chat");
                    }
                }
            });
        }
        catch (CannotCompileException | NotFoundException ex2) {
            final Exception e = ex2;
            throw new HookException(e);
        }
    }
    
    public static String fixActionString(final Creature c, String s) {
        s = s.replace("%HIS", c.isNotFemale() ? "his" : "her");
        s = s.replace("%NAME", c.getName());
        s = s.replace("%NAME'S", String.valueOf(c.getName()) + "'s");
        s = s.replace("%HIMSELF", c.isNotFemale() ? "himself" : "herself");
        s = s.replace("%HIM", c.isNotFemale() ? "him" : "her");
        return s;
    }
    
    public static void actionNotify(final Creature c, @Nullable String myMsg, @Nullable String othersMsg, @Nullable String stealthOthersMsg, @Nullable final Creature[] excludeFromBroadCast) {
        if (excludeFromBroadCast != null) {
            final int length = excludeFromBroadCast.length;
        }
        if (myMsg != null) {
            myMsg = fixActionString(c, myMsg);
            c.getCommunicator().sendNormalServerMessage(myMsg);
        }
        if (stealthOthersMsg != null && c.isStealth()) {
            stealthOthersMsg = fixActionString(c, stealthOthersMsg);
            Server.getInstance().broadCastAction(stealthOthersMsg, c, 8);
        }
        else if (othersMsg != null) {
            othersMsg = fixActionString(c, othersMsg);
            Server.getInstance().broadCastAction(othersMsg, c, 8);
        }
    }
    
    public static void actionNotify(final Creature c, @Nullable final String myMsg, @Nullable final String othersMsg, @Nullable final String stealthOthersMsg) {
        actionNotify(c, myMsg, othersMsg, stealthOthersMsg, null);
    }
    
    public static void debug(final String s) {
        Mod.logger.log(Level.INFO, s);
    }
    
    public static boolean isTestEnv() {
        return Servers.localServer.getName().equals("Friya");
    }
}
