// 
// Decompiled by Procyon v0.5.30
// 

package com.friya.wurmonline.server.vamps;

import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;

public class Locate
{
    private static String formatBearing(double bearing) {
        if (bearing < 0.0 && bearing > -180.0) {
            bearing += 360.0;
        }
        if (bearing > 360.0 || bearing < -180.0) {
            return "Unknown";
        }
        final String[] directions = { "north", "north-northeast", "northeast", "east-northeast", "east", "east-southeast", "southeast", "south-southeast", "south", "south-southwest", "southwest", "west-southwest", "west", "west-northwest", "northwest", "north-northwest", "north" };
        final String cardinal = directions[(int)Math.floor((bearing + 11.25) % 360.0 / 22.5)];
        return cardinal;
    }
    
    public static String getCompassDirection(final Creature fromCreature, final Creature toCreature) {
        return getCompassDirection(new int[] { fromCreature.getTileX(), fromCreature.getTileY() }, new int[] { toCreature.getTileX(), toCreature.getTileY() });
    }
    
    public static String getCompassDirection(final int[] from, final int[] to) {
        final double myBearing = 90.0 - 57.29577951308232 * Math.atan2(from[1] - to[1], to[0] - from[0]);
        return formatBearing(myBearing);
    }
    
    public static String getCompassDirection(final Item fromItem, final Creature toCreature) {
        return getCompassDirection(new int[] { fromItem.getTileX(), fromItem.getTileY() }, new int[] { toCreature.getTileX(), toCreature.getTileY() });
    }
    
    public static String getCompassDirection(final int fromX, final int fromY, final int toX, final int toY) {
        return getCompassDirection(new int[] { fromX, fromY }, new int[] { toX, toY });
    }
}
