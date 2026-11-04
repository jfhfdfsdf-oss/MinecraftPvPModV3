package de.example.pvpmod.gui;

import de.example.pvpmod.PvPMod;
import de.example.pvpmod.data.PlayerData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import java.util.*;

public final class SoulLiberationGUI {
    private final PvPMod plugin;
    private final Map<UUID, UUID> pending = new HashMap<>();
    private final Map<UUID, Integer> pages = new HashMap<>();
    public SoulLiberationGUI(PvPMod plugin) { this.plugin = plugin; }
    public void open(Player player) { open(player, 0); }
    private void open(Player player, int page) {
        List<PlayerData> banned = plugin.data().allBanned();
        if (banned.isEmpty()) { player.sendMessage("§aEs sind keine Spieler gebannt."); return; }
        int pagesTotal = Math.max(1, (banned.size() + 44) / 45); page = Math.max(0, Math.min(page, pagesTotal - 1)); pages.put(player.getUniqueId(), page);
        Inventory inv = Bukkit.createInventory(null, 54, Component.text("Gebannte Spieler " + (page + 1) + "/" + pagesTotal));
        int start = page * 45, end = Math.min(banned.size(), start + 45);
        for (int i = start; i < end; i++) {
            PlayerData d = banned.get(i); ItemStack head = new ItemStack(Material.PLAYER_HEAD); SkullMeta meta = (SkullMeta) head.getItemMeta();
            OfflinePlayer offline = Bukkit.getOfflinePlayer(d.uuid()); meta.setOwningPlayer(offline); meta.displayName(Component.text(d.name(), NamedTextColor.RED));
            meta.lore(List.of(Component.text("Klicken zum Auswählen", NamedTextColor.GRAY))); head.setItemMeta(meta); inv.setItem(i - start, head);
        }
        if (page > 0) inv.setItem(45, button(Material.ARROW, "Vorherige Seite"));
        if (page + 1 < pagesTotal) inv.setItem(53, button(Material.ARROW, "Nächste Seite"));
        player.openInventory(inv);
    }
    public void handleListClick(Player player, int slot, ItemStack clicked) {
        int page = pages.getOrDefault(player.getUniqueId(), 0);
        if (slot == 45) { open(player, page - 1); return; }
        if (slot == 53) { open(player, page + 1); return; }
        if (slot < 0 || slot >= 45) return;
        select(player, clicked);
    }
    public void select(Player player, ItemStack clicked) {
        if (clicked == null || !(clicked.getItemMeta() instanceof SkullMeta meta) || meta.getOwningPlayer() == null) return;
        UUID target = meta.getOwningPlayer().getUniqueId(); PlayerData d = plugin.data().get(target);
        if (!d.banned()) { player.sendMessage("§cDieser Spieler ist nicht mehr gebannt."); return; }
        pending.put(player.getUniqueId(), target);
        Inventory confirm = Bukkit.createInventory(null, 27, Component.text("Entbannung bestätigen?"));
        confirm.setItem(11, button(Material.LIME_CONCRETE, "BESTÄTIGEN")); confirm.setItem(15, button(Material.RED_CONCRETE, "ABBRECHEN"));
        player.openInventory(confirm);
    }
    private ItemStack button(Material type, String name) { ItemStack i = new ItemStack(type); var m = i.getItemMeta(); m.displayName(Component.text(name)); i.setItemMeta(m); return i; }
    public void confirm(Player player, int slot) {
        UUID target = pending.remove(player.getUniqueId()); if (target == null) return;
        if (slot != 11) { player.sendMessage("§7Entbannung abgebrochen."); return; }
        PlayerData d = plugin.data().get(target);
        if (!hasSoul(player)) { player.sendMessage("§cDu musst die Seelenbefreiung noch besitzen."); return; }
        if (!plugin.bans().unban(d)) { player.sendMessage("§cEntbannung fehlgeschlagen."); return; }
        consumeSoul(player); player.sendMessage("§a" + d.name() + " wurde entbannt. Das Item wurde verbraucht.");
    }
    private boolean hasSoul(Player player) {
        for (ItemStack item : player.getInventory().getContents()) if (plugin.items().has(item, plugin.items().soulKey)) return true;
        return plugin.items().has(player.getInventory().getItemInOffHand(), plugin.items().soulKey);
    }
    private void consumeSoul(Player player) {
        var key = plugin.items().soulKey;
        for (int slot = 0; slot < player.getInventory().getSize(); slot++) { ItemStack item = player.getInventory().getItem(slot); if (plugin.items().has(item, key)) { if (item.getAmount() <= 1) player.getInventory().setItem(slot, null); else item.setAmount(item.getAmount() - 1); return; } }
        ItemStack off = player.getInventory().getItemInOffHand(); if (plugin.items().has(off, key)) { if (off.getAmount() <= 1) player.getInventory().setItemInOffHand(null); else off.setAmount(off.getAmount() - 1); }
    }
    public boolean isPending(Player p) { return pending.containsKey(p.getUniqueId()); }
}
