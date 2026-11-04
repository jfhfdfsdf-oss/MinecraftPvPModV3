package de.example.pvpmod.listener;

import de.example.pvpmod.PvPMod;
import de.example.pvpmod.data.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class XPListener implements Listener {
    private final PvPMod plugin;
    private final Map<UUID, Double> fractionalRemainder = new ConcurrentHashMap<>();
    private final Map<UUID, Integer> previousLevel = new ConcurrentHashMap<>();
    public XPListener(PvPMod plugin) { this.plugin = plugin; }
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void xp(PlayerExpChangeEvent e) {
        Player p = e.getPlayer(); PlayerData d = plugin.data().get(p.getUniqueId(), p.getName());
        int level = d.level();
        if (previousLevel.getOrDefault(p.getUniqueId(), level) != level) fractionalRemainder.remove(p.getUniqueId());
        previousLevel.put(p.getUniqueId(), level);
        int original = e.getAmount();
        double exact = original * plugin.levels().xpMultiplier(level) + fractionalRemainder.getOrDefault(p.getUniqueId(), 0.0);
        int adjusted = Math.max(0, (int) Math.floor(exact));
        fractionalRemainder.put(p.getUniqueId(), Math.max(0.0, exact - adjusted));
        e.setAmount(adjusted);
    }

    @EventHandler
    public void quit(PlayerQuitEvent event) {
        UUID id = event.getPlayer().getUniqueId();
        fractionalRemainder.remove(id);
        previousLevel.remove(id);
    }
}
