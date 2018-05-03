// 
// Decompiled by Procyon v0.5.30
// 

package com.friya.wurmonline.server.vamps;

import com.wurmonline.server.Point;
import com.wurmonline.server.zones.FocusZone;

import java.util.logging.Level;
import java.util.logging.Logger;

public class VampZones
{
    private static Logger logger;
    private static String zoneName;
    private static FocusZone covenZone;
    private static int startX;
    private static int startY;
    private static int zoneSize;
    private static int spawnPointLayer;
    private static int respawnX;
    private static int respawnY;
    
    static {
        VampZones.logger = Logger.getLogger(VampZones.class.getName());
        VampZones.zoneName = "The Coven";
        VampZones.startX = 1686;
        VampZones.startY = 1770;
        VampZones.zoneSize = 6;
        VampZones.spawnPointLayer = -1;
        VampZones.respawnX = 0;
        VampZones.respawnY = 0;
    }
    
    public VampZones() {
        if (!Mod.isTestEnv()) {
            VampZones.startX = 2290;
            VampZones.startY = 1273;
            VampZones.zoneSize = 30;
            VampZones.respawnX = 2297;
            VampZones.respawnY = 1283;
        }
    }
    
    static void onServerStarted() {
        VampZones.logger.log(Level.INFO, "Getting or creating a Vampire spawn-zone");
        FocusZone[] allZones;
        for (int length = (allZones = FocusZone.getAllZones()).length, i = 0; i < length; ++i) {
            final FocusZone z = allZones[i];
            if (z.getName().equals(VampZones.zoneName)) {
                setCovenZone(z);
                VampZones.logger.log(Level.INFO, "Found a zone called " + VampZones.zoneName + ", good.");
                VampZones.startX = z.getStartX();
                VampZones.startY = z.getStartY();
                VampZones.zoneSize = Math.max(z.getEndX() - z.getStartX(), z.getEndY() - z.getStartY());
                return;
            }
        }
        setCovenZone(new FocusZone(VampZones.startX, VampZones.startX + VampZones.zoneSize, VampZones.startY, VampZones.startY + VampZones.zoneSize, (byte)12, VampZones.zoneName, "Friya's Vamps", true));
        VampZones.logger.log(Level.INFO, "Created a zone called " + VampZones.zoneName + ", at " + VampZones.startX + ", " + VampZones.startY);
    }
    
    public static FocusZone getCovenZone() {
        return VampZones.covenZone;
    }
    
    public static Point getCovenCentre() {
        return new Point(VampZones.startX + VampZones.zoneSize / 2, VampZones.startY + VampZones.zoneSize / 2);
    }
    
    public static Point getCovenRespawnPoint() {
        if (VampZones.respawnX > 0 && VampZones.respawnY > 0) {
            return new Point(VampZones.respawnX, VampZones.respawnY);
        }
        return getCovenCentre();
    }
    
    public static int getCovenLayer() {
        return VampZones.spawnPointLayer;
    }
    
    private static void setCovenZone(final FocusZone covenZone) {
        VampZones.covenZone = covenZone;
    }
}
