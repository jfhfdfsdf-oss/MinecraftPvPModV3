package de.example.pvpmod.listener;

import de.example.pvpmod.PvPMod;
import de.example.pvpmod.data.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import com.destroystokyo.paper.profile.PlayerProfile;
import io.papermc.paper.connection.PlayerConfigurationConnection;
import io.papermc.paper.connection.PlayerLoginConnection;
import io.papermc.paper.event.connection.PlayerConnectionValidateLoginEvent;
import net.kyori.adventure.text.Component;
import java.util.UUID;

public final class PlayerListener implements Listener {
    private final PvPMod plugin;
    public PlayerListener(PvPMod plugin) { this.plugin = plugin; }
    @EventHandler(priority = EventPriority.HIGHEST)
    public void validateLogin(PlayerConnectionValidateLoginEvent event) {
        PlayerProfile profile = null;
        if (event.getConnection() instanceof PlayerLoginConnection login) {
            // Never trust the unauthenticated requested profile for a UUID ban check.
            // If authentication is not complete yet, the configuration phase checks it.
            profile = login.getAuthenticatedProfile();
        } else if (event.getConnection() instanceof PlayerConfigurationConnection configuration) {
            profile = configuration.getProfile();
        }
        if (profile == null) return;
        UUID uuid = profile.getId();
        if (uuid == null) return;
        String name = profile.getName() == null ? "Unbekannt" : profile.getName();
        PlayerData data = plugin.data().get(uuid, name);
        if (data.banned()) event.kickMessage(Component.text("Du bist permanent durch PvPMod gebannt."));
    }
    @EventHandler public void join(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        PlayerData d = plugin.data().get(p.getUniqueId(), p.getName());
        d.name(p.getName());
        plugin.data().save(d);
        plugin.hearts().apply(p, d.level());
        plugin.mace().validatePlayer(p);
    }
    @EventHandler public void respawn(PlayerRespawnEvent e) { Player p = e.getPlayer(); plugin.getServer().getScheduler().runTaskLater(plugin, () -> plugin.hearts().apply(p, plugin.data().get(p.getUniqueId(), p.getName()).level()), 1L); }
    @EventHandler public void quit(PlayerQuitEvent e) { plugin.data().save(plugin.data().get(e.getPlayer().getUniqueId(), e.getPlayer().getName())); }
    @EventHandler(priority = EventPriority.HIGHEST)
    public void death(PlayerDeathEvent e) {
        Player victim = e.getEntity(); PlayerData vd = plugin.data().get(victim.getUniqueId(), victim.getName());
        Player killer = victim.getKiller();
        vd.addDeath();
        if (killer != null && !killer.getUniqueId().equals(victim.getUniqueId())) {
            PlayerData kd = plugin.data().get(killer.getUniqueId(), killer.getName()); kd.addKill(); plugin.levels().add(kd, 1); plugin.hearts().apply(killer, kd.level()); plugin.data().save(kd);
            if (vd.level() <= -10) { if (plugin.getConfig().getBoolean("settings.ban-at-minus-ten", true)) plugin.bans().ban(victim); }
            else { plugin.levels().remove(vd, 1); }
        }
        plugin.data().save(vd);
        if (plugin.mace().owner() != null && plugin.mace().owner().equals(victim.getUniqueId())) { e.getDrops().removeIf(item -> plugin.items().has(item, plugin.items().maceKey)); plugin.mace().resetAfterDeath(); }
    }
}
