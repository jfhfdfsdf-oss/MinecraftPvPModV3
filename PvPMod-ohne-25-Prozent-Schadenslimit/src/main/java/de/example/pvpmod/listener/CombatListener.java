package de.example.pvpmod.listener;

import de.example.pvpmod.PvPMod;
import de.example.pvpmod.data.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

public final class CombatListener implements Listener {
    private final PvPMod plugin;
    public CombatListener(PvPMod plugin) { this.plugin = plugin; }
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void damage(EntityDamageByEntityEvent e) {
        Player attacker = null;
        if (e.getDamager() instanceof Player direct) attacker = direct;
        else if (e.getDamager() instanceof Projectile projectile && projectile.getShooter() instanceof Player shooter) attacker = shooter;
        if (attacker != null) {
            PlayerData d = plugin.data().get(attacker.getUniqueId(), attacker.getName());
            e.setDamage(e.getDamage() * plugin.levels().damageMultiplier(d.level()));
        }
    }
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void reduction(EntityDamageEvent e) {
        if (e.getEntity() instanceof Player p) {
            PlayerData d = plugin.data().get(p.getUniqueId(), p.getName());
            double reduction = plugin.levels().damageReduction(d.level());
            if (reduction > 0) e.setDamage(e.getDamage() * (1.0 - reduction));
        }
    }
}
