package de.example.pvpmod.manager;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;

public final class HeartManager {
    private final LevelManager levels;
    public HeartManager(LevelManager levels) { this.levels = levels; }
    public void apply(Player player, int level) {
        AttributeInstance max = player.getAttribute(Attribute.MAX_HEALTH);
        if (max == null) return;
        double oldHealth = player.getHealth();
        double newMax = levels.hearts(level) * 2.0;
        max.setBaseValue(newMax);
        // Preserve absolute health; only clamp if the new maximum is lower.
        player.setHealth(Math.max(0.1, Math.min(oldHealth, newMax)));
    }
}
