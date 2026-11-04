package de.example.pvpmod.manager;

import de.example.pvpmod.data.PlayerData;

public final class LevelManager {
    public int set(PlayerData data, int level) { data.level(level); return data.level(); }
    public int add(PlayerData data, int amount) { return setLong(data, (long) data.level() + amount); }
    public int remove(PlayerData data, int amount) { return setLong(data, (long) data.level() - amount); }
    private int setLong(PlayerData data, long level) { data.level((int) Math.max(-10L, Math.min(30L, level))); return data.level(); }
    private int clampLevel(int level) { return Math.max(-10, Math.min(30, level)); }
    public double damageMultiplier(int level) { return Math.max(0.0, 1.0 + clampLevel(level) / 30.0); }
    public double xpMultiplier(int level) { return Math.max(0.0, 1.0 + clampLevel(level) * (4.0 / 30.0)); }
    public double damageReduction(int level) { return Math.max(0.0, Math.min(0.05, clampLevel(level) * (0.05 / 30.0))); }
    public double hearts(int level) {
        int l = clampLevel(level);
        if (l == -10) return 0.5;
        if (l < 0) return 10 + l;
        if (l == 0) return 10;
        if (l <= 19) return 10 + Math.floor(l / 2.0) * 0.5;
        if (l == 20 || l == 21) return 16;
        if (l <= 24) return 17;
        if (l <= 27) return 18;
        if (l <= 29) return 19;
        return 20;
    }
}
