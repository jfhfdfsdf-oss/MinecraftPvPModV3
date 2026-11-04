package de.example.pvpmod.data;

import java.util.UUID;

public final class PlayerData {
    private final UUID uuid;
    private String name;
    private int level;
    private long kills;
    private long deaths;
    private boolean banned;
    private long banTime;
    private boolean wasWhitelisted;

    public PlayerData(UUID uuid, String name) { this.uuid = uuid; this.name = name; }
    public UUID uuid() { return uuid; }
    public String name() { return name; }
    public void name(String name) { this.name = name; }
    public int level() { return level; }
    public void level(int level) { this.level = Math.max(-10, Math.min(30, level)); }
    public long kills() { return kills; }
    public void addKill() { kills++; }
    public long deaths() { return deaths; }
    public void addDeath() { deaths++; }
    public void stats(long kills, long deaths) { this.kills = Math.max(0, kills); this.deaths = Math.max(0, deaths); }
    public boolean banned() { return banned; }
    public void banned(boolean banned) { this.banned = banned; }
    public long banTime() { return banTime; }
    public void banTime(long banTime) { this.banTime = banTime; }
    public boolean wasWhitelisted() { return wasWhitelisted; }
    public void wasWhitelisted(boolean value) { wasWhitelisted = value; }
    public double kdr() { return deaths == 0 ? kills : (double) kills / deaths; }
}
