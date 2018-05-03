// 
// Decompiled by Procyon v0.5.30
// 

package com.friya.wurmonline.server.vamps;

import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.players.Player;
import org.gotti.wurmunlimited.modsupport.ModSupportDb;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Stream;

public class ChatCommands
{
    static boolean onPlayerMessage(final Communicator com, final String msg) {
        if (msg.startsWith("/")) {
            if (msg.startsWith("/slayers")) {
                return cmdSlayers(com, msg);
            }
            if (msg.startsWith("/hunted")) {
                return cmdHunted(com, msg);
            }
            if (msg.startsWith("/toplist")) {
                return cmdToplistVamps(com, msg);
            }
        }
        return false;
    }
    
    static boolean cmdToplistVamps(final Communicator com, final String msg) {
        if (com.player.getPower() == 0 && !Vampires.isVampire(com.player.getWurmId())) {
            return false;
        }
        com.sendNormalServerMessage("The highest rated hunting vampires");
        final Toplist toplist = getToplistVampsData(15);
        toplist.sendTo(com.player);
        return true;
    }
    
    public static Toplist getToplistVampsData(final int listSize) {
        Connection dbcon = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        final HashMap<String, Long> scores = new HashMap<String, Long>();
        try {
            dbcon = ModSupportDb.getModSupportDb();
            ps = dbcon.prepareStatement("SELECT playerid, alias, slayerlostactions, stakingid, slayersteamid, vampiresteamid FROM FriyaVampires AS vamps INNER JOIN FriyaVampireBites AS bites ON (bites.vampireid = vamps.playerid)");
            rs = ps.executeQuery();
            String alias = null;
            long score = 0L;
            while (rs.next()) {
                alias = rs.getString("alias");
                if (alias == null) {
                    continue;
                }
                if (rs.getString("slayersteamid").equals(rs.getString("vampiresteamid"))) {
                    continue;
                }
                score = rs.getLong("slayerlostactions");
                if (rs.getLong("stakingid") > 0L) {
                    score *= 3L;
                }
                if (scores.containsKey(alias)) {
                    scores.put(alias, scores.get(alias) + score);
                }
                else {
                    scores.put(alias, score);
                }
            }
        }
        catch (SQLException e) {
            throw new RuntimeException(e);
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
        final Stream<Map.Entry<String, Long>> sorted = scores.entrySet().stream().sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()));
        final Toplist toplist = new Toplist(listSize);
        final Iterator<Map.Entry<String, Long>> it = sorted.iterator();
        int i = 0;
        while (it.hasNext()) {
            final Map.Entry<String, Long> entry = it.next();
            toplist.addNameScore(entry.getKey(), entry.getValue());
            if (i++ >= listSize) {
                break;
            }
        }
        sorted.close();
        return toplist;
    }
    
    static boolean cmdSlayers(final Communicator com, final String msg) {
        final Player p = com.player;
        if (p.getPower() == 0 && !Vampires.isVampire(p.getWurmId())) {
            return false;
        }
        boolean found = false;
        final HashMap<Long, Staker> stakers = Stakers.getStakers();
        for (final Staker s : stakers.values()) {
            if (Stakers.getPlayer(s.getPlayerName()) != null && !s.isHuntOver()) {
                found = true;
                com.sendNormalServerMessage(String.valueOf(s.getPlayerName()) + " is a hunted vampire slayer and is in this world.");
            }
        }
        if (!found) {
            com.sendNormalServerMessage("There are currently no hunted vampire slayers in the world.");
        }
        return true;
    }
    
    private static boolean cmdHunted(final Communicator com, final String msg) {
        final Player p = com.player;
        if (Stakers.isHunted(p)) {
            try {
                final long minutes = (Stakers.HUNTED_TIME - Stakers.getStaker(p.getWurmId()).getElapsedTime()) / 1000L / 60L;
                if (minutes < 5L) {
                    com.sendNormalServerMessage("You are marked as a vampire slayer and is hunted for a few more minutes.");
                }
                else {
                    com.sendNormalServerMessage("You are marked as a vampire slayer and is hunted for approximately " + 5 * Math.round(minutes / 5L) + " more minutes.");
                }
            }
            catch (NoSuchPlayerException e) {
                com.sendNormalServerMessage("You are marked as a vampire slayer and is hunted by all vampires.");
            }
        }
        else {
            com.sendNormalServerMessage("Your hands are clean, you are not hunted.");
        }
        return true;
    }
}
