package de.example.pvpmod.gui;

import de.example.pvpmod.PvPMod;
import de.example.pvpmod.data.PlayerData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitTask;
import java.util.Arrays;
import java.util.Locale;

public final class StatsGUI {
    public static final String TITLE = "PvP-Statistiken";
    private final PvPMod plugin;
    public StatsGUI(PvPMod plugin) { this.plugin = plugin; }
    public void open(Player viewer, PlayerData d) { open(viewer, viewer, d); }
    public void open(Player viewer, Player subject, PlayerData d) {
        Inventory inv = Bukkit.createInventory(null, 27, Component.text(TITLE, NamedTextColor.DARK_GRAY));
        render(subject, d, inv); viewer.openInventory(inv);
        BukkitTask[] task = new BukkitTask[1];
        task[0] = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (!viewer.isOnline() || viewer.getOpenInventory().getTopInventory() != inv) { task[0].cancel(); return; }
            render(subject, d, inv);
        }, 20L, 20L);
    }
    private void render(Player subject, PlayerData d, Inventory inv) {
        int l = d.level();
        put(inv, 10, Material.NETHER_STAR, "PvP-Level", "§f" + l);
        put(inv, 11, Material.IRON_SWORD, "Kills", "§f" + d.kills());
        put(inv, 12, Material.SKELETON_SKULL, "Tode", "§f" + d.deaths());
        put(inv, 13, Material.GOLD_INGOT, "K/D", String.format(Locale.US, "§f%.2f", d.kdr()));
        double currentHearts = subject.isOnline() ? subject.getHealth() / 2.0 : 0.0;
        put(inv, 14, Material.REDSTONE, "Aktuelle Herzen", String.format(Locale.US, "§f%.1f", currentHearts));
        put(inv, 15, Material.GOLDEN_APPLE, "Maximale Herzen", "§f" + plugin.levels().hearts(l));
        put(inv, 16, Material.DIAMOND_SWORD, "Angriffsschaden", String.format(Locale.US, "§f%.2f×", plugin.levels().damageMultiplier(l)));
        put(inv, 21, Material.EXPERIENCE_BOTTLE, "XP-Multiplikator", String.format(Locale.US, "§f%.2f×", plugin.levels().xpMultiplier(l)));
        put(inv, 22, Material.SHIELD, "Schadensreduktion", String.format(Locale.US, "§f%.1f%%", plugin.levels().damageReduction(l) * 100));
        if (l > 0) put(inv, 23, Material.EMERALD, "Aktuelle Buffs", "§aMehr Angriffsschaden", "§aMehr Minecraft-XP", plugin.levels().damageReduction(l) > 0 ? "§aSchadensreduktion" : "§7Keine weitere Reduktion");
        else if (l < 0) put(inv, 23, Material.REDSTONE, "Aktuelle Debuffs", "§cWeniger Angriffsschaden", "§cWeniger Minecraft-XP", "§cWeniger maximale Herzen");
        else put(inv, 23, Material.BOOK, "Effekte", "§7Keine levelbedingten Buffs oder Debuffs.");
    }
    private void put(Inventory inv, int slot, Material type, String name, String... lines) {
        ItemStack item = new ItemStack(type); ItemMeta meta = item.getItemMeta(); meta.displayName(Component.text(name, NamedTextColor.GOLD));
        meta.lore(Arrays.stream(lines).map(line -> Component.text(line.replace("§a", "").replace("§c", "").replace("§7", "").replace("§f", ""), line.startsWith("§a") ? NamedTextColor.GREEN : line.startsWith("§c") ? NamedTextColor.RED : NamedTextColor.WHITE)).toList());
        item.setItemMeta(meta); inv.setItem(slot, item);
    }
}
