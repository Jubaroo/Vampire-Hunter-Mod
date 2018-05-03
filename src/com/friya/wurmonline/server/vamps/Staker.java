// 
// Decompiled by Procyon v0.5.30
// 

package com.friya.wurmonline.server.vamps;

import com.wurmonline.server.creatures.Creature;
import org.gotti.wurmunlimited.modsupport.ModSupportDb;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Staker
{
    private static Logger logger;
    private long id;
    private long playerId;
    private String playerName;
    private long startTime;
    private long lastPoll;
    private long lastSave;
    private long elapsedTime;
    private boolean huntOver;
    private int bitten;
    private int affectedSkill;
    private static final String insertSlayerSql = "INSERT INTO FriyaVampireSlayers( slayerid, slayersteamid, slayername, vampireid, vampirename, vampirestat, vampirestatname, vampireloststatlevel,  vampirelostamount, vampirelostactions, slayerstatlevel, slayergainedamount, staketime, timeelapsed, huntover) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
    static long SAVE_INTERVAL;
    
    static {
        Staker.logger = Logger.getLogger(Staker.class.getName());
        Staker.SAVE_INTERVAL = 120000L;
    }
    
    public Staker() {
        this.id = 0L;
        this.playerId = 0L;
        this.playerName = null;
        this.startTime = 0L;
        this.lastPoll = 0L;
        this.lastSave = 0L;
        this.elapsedTime = 0L;
        this.huntOver = false;
        this.bitten = 0;
        this.affectedSkill = 0;
    }
    
    Staker(final Creature slayer, final Creature vampire, final int exchangedStatNum, final String exchangedStatName, final double vampireStatBefore, final double vampireLostAmount, final int vampireLostActions, final double slayerStatLevelBefore, final double slayerGainedAmount) {
        this.id = 0L;
        this.playerId = 0L;
        this.playerName = null;
        this.startTime = 0L;
        this.lastPoll = 0L;
        this.lastSave = 0L;
        this.elapsedTime = 0L;
        this.huntOver = false;
        this.bitten = 0;
        this.affectedSkill = 0;
        this.setPlayerId(slayer.getWurmId());
        this.setPlayerName(slayer.getName());
        this.setStartTime(System.currentTimeMillis());
        this.setLastPoll(this.getStartTime());
        this.setLastSave(this.getStartTime());
        this.setElapsedTime(0L);
        this.setAffectedSkill(exchangedStatNum);
        try {
            final Connection dbcon = ModSupportDb.getModSupportDb();
            final PreparedStatement ps = dbcon.prepareStatement("INSERT INTO FriyaVampireSlayers( slayerid, slayersteamid, slayername, vampireid, vampirename, vampirestat, vampirestatname, vampireloststatlevel,  vampirelostamount, vampirelostactions, slayerstatlevel, slayergainedamount, staketime, timeelapsed, huntover) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)", 1);
            int i = 1;
            ps.setLong(i++, slayer.getWurmId());
            ps.setString(i++, slayer.SteamId);
            ps.setString(i++, slayer.getName());
            ps.setLong(i++, vampire.getWurmId());
            ps.setString(i++, vampire.getName());
            ps.setInt(i++, exchangedStatNum);
            ps.setString(i++, exchangedStatName);
            ps.setDouble(i++, vampireStatBefore);
            ps.setDouble(i++, vampireLostAmount);
            ps.setInt(i++, vampireLostActions);
            ps.setDouble(i++, slayerStatLevelBefore);
            ps.setDouble(i++, slayerGainedAmount);
            ps.setLong(i++, System.currentTimeMillis());
            ps.setLong(i++, 0L);
            ps.setByte(i++, (byte)0);
            ps.execute();
            final ResultSet rs = ps.getGeneratedKeys();
            if (rs != null) {
                rs.next();
                this.setId(rs.getLong(1));
                Staker.logger.log(Level.FINE, "Inserted item as: " + this.id);
            }
            else {
                Staker.logger.log(Level.SEVERE, "no resultset back from getGeneratedKeys(), probably means nothing was created!");
            }
            rs.close();
            ps.close();
        }
        catch (SQLException e) {
            Staker.logger.log(Level.SEVERE, "Failed to insert staker");
            throw new RuntimeException(e);
        }
    }
    
    void increaseElapsedTime() {
        if (this.isHuntOver()) {
            Staker.logger.log(Level.INFO, "Hunt is over, why are we calling increaseElapsedTime() on " + this.getPlayerName() + "?");
        }
        final long ts = System.currentTimeMillis();
        if (this.getLastPoll() <= 0L) {
            Staker.logger.log(Level.WARNING, "lastPoll was set to 0 for hunter (probably just loaded?), setting it to 'now'");
            this.setLastPoll(ts);
        }
        this.setElapsedTime(this.elapsedTime + ts - this.getLastPoll());
        Staker.logger.log(Level.FINE, "Staker's elapsed hunted time now: " + Math.max(0L, this.getElapsedTime() / 1000L) + " seconds");
        this.setLastPoll(ts);
        if (this.getElapsedTime() >= Stakers.HUNTED_TIME) {
            this.setHuntOver(true);
        }
        if (ts > this.getLastSave() + Staker.SAVE_INTERVAL) {
            this.save();
        }
    }
    
    void save() {
        Staker.logger.log(Level.INFO, "Updating hunted staker " + this.getPlayerName());
        final String sql = "UPDATE FriyaVampireSlayers SET  timeelapsed = ?, huntover = ? WHERE id = ?";
        try {
            final Connection dbcon = ModSupportDb.getModSupportDb();
            final PreparedStatement ps = dbcon.prepareStatement(sql);
            int i = 1;
            ps.setLong(i++, this.getElapsedTime());
            ps.setByte(i++, (byte)(this.huntOver ? 1 : 0));
            ps.setLong(i++, this.getId());
            ps.executeUpdate();
            ps.close();
        }
        catch (SQLException e) {
            Staker.logger.log(Level.SEVERE, "Failed to update staker: " + this.getPlayerName());
            throw new RuntimeException(e);
        }
        this.setLastSave(System.currentTimeMillis());
    }
    
    long getPlayerId() {
        return this.playerId;
    }
    
    void setPlayerId(final long playerId) {
        this.playerId = playerId;
    }
    
    String getPlayerName() {
        return this.playerName;
    }
    
    void setPlayerName(final String playerName) {
        this.playerName = playerName;
    }
    
    long getStartTime() {
        return this.startTime;
    }
    
    void setStartTime(final long startTime) {
        this.startTime = startTime;
    }
    
    long getLastPoll() {
        return this.lastPoll;
    }
    
    void setLastPoll(final long lastPoll) {
        this.lastPoll = lastPoll;
    }
    
    long getElapsedTime() {
        return this.elapsedTime;
    }
    
    void setElapsedTime(final long elapsedTime) {
        this.elapsedTime = elapsedTime;
    }
    
    long getLastSave() {
        return this.lastSave;
    }
    
    void setLastSave(final long lastSave) {
        this.lastSave = lastSave;
    }
    
    boolean isHuntOver() {
        return this.huntOver;
    }
    
    void setHuntOver(final boolean huntOver) {
        if (huntOver && this.huntOver != huntOver) {
            final Creature staker = Stakers.getPlayer(this.getPlayerName());
            staker.getCommunicator().sendAlertServerMessage("Your hands finally wash clean of the blood. You are no longer marked as a vampire slayer.", (byte)4);
            Vampires.broadcast(String.valueOf(this.getPlayerName()) + " is no longer marked as a vampire slayer. The time of the hunt will now cease!", true, true, false);
            if (this.bitten == 0 && !VampTitles.hasTitle(staker, VampTitles.ESCAPIST)) {
                staker.addTitle(VampTitles.getTitle(VampTitles.ESCAPIST));
            }
        }
        this.huntOver = huntOver;
        this.save();
    }
    
    void setHuntOverNoSave(final boolean huntOver) {
        this.huntOver = huntOver;
    }
    
    public long getId() {
        return this.id;
    }
    
    public void setId(final long id) {
        this.id = id;
    }
    
    public void addBitten() {
        ++this.bitten;
    }
    
    public boolean mayBite() {
        return this.bitten < 50;
    }
    
    public int getAffectedSkill() {
        return this.affectedSkill;
    }
    
    public void setAffectedSkill(final int num) {
        this.affectedSkill = num;
    }
}
