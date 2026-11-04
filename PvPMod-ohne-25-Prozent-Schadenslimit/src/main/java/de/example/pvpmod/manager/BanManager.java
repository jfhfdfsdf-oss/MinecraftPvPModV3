package de.example.pvpmod.manager;

import de.example.pvpmod.PvPMod;
import de.example.pvpmod.data.PlayerData;
import io.papermc.paper.ban.BanListType;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import java.util.Date;

public final class BanManager {
    private final PlayerDataManager data;
    public BanManager(PvPMod plugin, PlayerDataManager data) { this.data = data; }
    public void ban(Player victim) {
        PlayerData d = data.get(victim.getUniqueId(), victim.getName());
        OfflinePlayer profile = Bukkit.getOfflinePlayer(d.uuid());
        d.wasWhitelisted(profile.isWhitelisted());
        d.banned(true); d.banTime(System.currentTimeMillis()); data.save(d);
        // Paper 26.2 uses profile/UUID bans; name-based BanList.Type.NAME entries are no longer supported.
        profile.ban("PvP-Level -10: automatischer Bann", (Date) null, "PvPMod");
        profile.setWhitelisted(false);
        victim.kickPlayer("§cDu wurdest permanent gebannt: PvP-Tod auf Level -10.");
    }
    public boolean unban(PlayerData d) {
        if (!d.banned()) return false;
        OfflinePlayer profile = Bukkit.getOfflinePlayer(d.uuid());
        Bukkit.getBanList(BanListType.PROFILE).pardon(profile.getPlayerProfile());
        profile.setWhitelisted(true);
        d.banned(false); d.banTime(0); d.level(0); data.save(d); return true;
    }
}
