// 
// Decompiled by Procyon v0.5.30
// 

package com.friya.wurmonline.server.vamps;

public class Vampire
{
    private long playerId;
    private String steamId;
    private String name;
    private String alias;
    private int vampireStatus;
    private long halfStartTime;
    private long fullStartTime;
    private long fullEndTime;
    
    public Vampire(final long playerId, final String steamId, final String name, final String alias, final int vampireStatus, final long halfStartTime, final long fullStartTime, final long fullEndTime) {
        this.setPlayerId(playerId);
        this.setSteamId(steamId);
        this.setName(name);
        this.setAlias(alias);
        this.setVampireStatus(vampireStatus);
        this.setHalfStartTime(halfStartTime);
        this.setFullStartTime(fullStartTime);
        this.setFullEndTime(fullEndTime);
    }
    
    @Override
    public String toString() {
        return "Vampire#" + this.playerId + " [steam:" + this.steamId + " alias:" + this.alias + " status:" + this.vampireStatus + " halfStart:" + this.halfStartTime + " fullstart:" + this.fullStartTime + " fullEnd:" + this.fullEndTime + "]";
    }
    
    long getId() {
        return this.playerId;
    }
    
    boolean isFull() {
        return this.vampireStatus == Vampires.STATUS_FULL;
    }
    
    boolean isHalf() {
        return this.vampireStatus == Vampires.STATUS_HALF;
    }
    
    boolean isFullOrHalf() {
        return this.isFull() || this.isHalf();
    }
    
    public boolean convertHalfToFull() {
        if (!this.isHalf()) {
            return false;
        }
        this.setFullStartTime(System.currentTimeMillis());
        this.setVampireStatus(Vampires.STATUS_FULL);
        Vampires.updateVampire(this);
        return true;
    }
    
    int getVampireStatus() {
        return this.vampireStatus;
    }
    
    void setVampireStatus(final int val) {
        this.vampireStatus = val;
    }
    
    String getName() {
        return this.name;
    }
    
    public String getAlias() {
        return this.alias;
    }
    
    public String getSteamId() {
        return this.steamId;
    }
    
    public void setSteamId(final String steamId) {
        this.steamId = steamId;
    }
    
    public void setName(final String name) {
        this.name = name;
    }
    
    public void setAlias(final String alias) {
        this.alias = alias;
    }
    
    public long getHalfStartTime() {
        return this.halfStartTime;
    }
    
    public void setHalfStartTime(final long halfStartTime) {
        this.halfStartTime = halfStartTime;
    }
    
    public long getFullStartTime() {
        return this.fullStartTime;
    }
    
    public void setFullStartTime(final long fullStartTime) {
        this.fullStartTime = fullStartTime;
    }
    
    public long getFullEndTime() {
        return this.fullEndTime;
    }
    
    public void setFullEndTime(final long fullEndTime) {
        this.fullEndTime = fullEndTime;
    }
    
    public long getPlayerId() {
        return this.playerId;
    }
    
    public void setPlayerId(final long playerId) {
        this.playerId = playerId;
    }
}
