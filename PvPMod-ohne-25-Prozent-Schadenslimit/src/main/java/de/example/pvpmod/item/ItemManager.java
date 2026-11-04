package de.example.pvpmod.item;

import de.example.pvpmod.PvPMod;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.persistence.PersistentDataType;
import java.util.List;

public final class ItemManager {
    public final NamespacedKey crystalKey, soulKey, maceKey;
    private final PvPMod plugin;
    public ItemManager(PvPMod plugin) {
        this.plugin = plugin; crystalKey = new NamespacedKey(plugin, "rebirth_crystal"); soulKey = new NamespacedKey(plugin, "soul_liberation"); maceKey = new NamespacedKey(plugin, "unique_mace");
    }
    public ItemStack crystal() { return tagged(Material.AMETHYST_SHARD, "Wiedergeburts-Kristall", NamedTextColor.LIGHT_PURPLE, crystalKey, "Erhöht dein negatives PvP-Level um 1."); }
    public ItemStack soul() { return tagged(Material.NETHER_STAR, "Seelenbefreiung", NamedTextColor.AQUA, soulKey, "Öffnet die Liste permanent gebannter Spieler."); }
    public ItemStack mace() { return tagged(Material.MACE, "Einzigartiger Mace", NamedTextColor.GOLD, maceKey, "Serverweit einzigartig. Density ist verboten."); }
    private ItemStack tagged(Material type, String name, NamedTextColor color, NamespacedKey key, String lore) {
        ItemStack item = new ItemStack(type); ItemMeta meta = item.getItemMeta(); meta.displayName(Component.text(name, color));
        meta.lore(List.of(Component.text(lore, NamedTextColor.GRAY))); meta.getPersistentDataContainer().set(key, PersistentDataType.BYTE, (byte) 1); item.setItemMeta(meta); return item;
    }
    public boolean has(ItemStack item, NamespacedKey key) { return item != null && item.hasItemMeta() && item.getItemMeta().getPersistentDataContainer().has(key, PersistentDataType.BYTE); }
    public void sanitizeMace(ItemStack item) {
        if (!has(item, maceKey) || !item.hasItemMeta()) return;
        ItemMeta meta = item.getItemMeta();
        if (meta.hasEnchant(Enchantment.DENSITY)) {
            meta.removeEnchant(Enchantment.DENSITY);
            item.setItemMeta(meta);
        }
    }
    public void registerRecipes() {
        ShapedRecipe c = new ShapedRecipe(new NamespacedKey(plugin, "rebirth_crystal_recipe"), crystal()); c.shape("DGD", "GAG", "DGD");
        c.setIngredient('D', Material.DIAMOND); c.setIngredient('G', Material.GOLD_BLOCK); c.setIngredient('A', Material.GOLDEN_APPLE); plugin.getServer().addRecipe(c);
        ShapedRecipe s = new ShapedRecipe(new NamespacedKey(plugin, "soul_liberation_recipe"), soul()); s.shape("NBN", "BSB", "NBN");
        s.setIngredient('N', Material.NETHERITE_SCRAP); s.setIngredient('B', Material.DIAMOND_BLOCK); s.setIngredient('S', Material.NETHER_STAR); plugin.getServer().addRecipe(s);
    }
}
