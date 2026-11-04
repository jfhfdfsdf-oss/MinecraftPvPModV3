package de.example.pvpmod.listener;

import de.example.pvpmod.PvPMod;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;

public final class MaceListener implements Listener {
    private final PvPMod plugin;
    public MaceListener(PvPMod plugin) { this.plugin = plugin; }

    @SuppressWarnings("deprecation") // DamageModifier remains present in the pinned Paper 26.2 API.
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void handleMaceHit(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player target) || !(event.getDamager() instanceof Player attacker)) return;
        DamageType type = event.getDamageSource().getDamageType();
        if (!type.equals(DamageType.MACE_SMASH) && !type.equals(DamageType.PLAYER_ATTACK)) return;
        ItemStack main = attacker.getInventory().getItemInMainHand();
        if (!plugin.items().has(main, plugin.items().maceKey)) return;
        plugin.items().sanitizeMace(main);
        attacker.getInventory().setItemInMainHand(main);

        if (target.isInvulnerable()
                || target.getGameMode() == org.bukkit.GameMode.CREATIVE
                || target.getGameMode() == org.bukkit.GameMode.SPECTATOR
                || target.isDead()) return;

        // No artificial 25%-of-max-health damage cap: keep the full damage
        // calculated by the normal combat pipeline (including the attacker's level).
        // The original event remains active for kill attribution, totems, hurt frames,
        // death processing and knockback. Armor and magic mitigation stay neutralized
        // as in the original special-Mace behavior.
        double maceDamage = Math.max(0.0, event.getDamage());
        if (maceDamage <= 0.0) return;
        event.setDamage(EntityDamageEvent.DamageModifier.BASE, maceDamage);
        if (event.isApplicable(EntityDamageEvent.DamageModifier.ARMOR)) {
            event.setDamage(EntityDamageEvent.DamageModifier.ARMOR, 0.0);
        }
        if (event.isApplicable(EntityDamageEvent.DamageModifier.MAGIC)) {
            event.setDamage(EntityDamageEvent.DamageModifier.MAGIC, 0.0);
        }
    }
}
