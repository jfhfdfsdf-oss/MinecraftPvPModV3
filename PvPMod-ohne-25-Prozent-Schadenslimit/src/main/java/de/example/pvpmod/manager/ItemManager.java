
package de.example.pvpmod.manager;

import de.example.pvpmod.PvPMod;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

public final class ItemManager {

    private final PvPMod plugin;

    public final NamespacedKey maceKey;
    public final NamespacedKey crystalKey;
    public final NamespacedKey soulKey;

    public ItemManager(PvPMod plugin) {
        this.plugin = plugin;

        this.maceKey = new NamespacedKey(plugin, "unique_mace");
        this.crystalKey = new NamespacedKey(plugin, "rebirth_crystal");
        this.soulKey = new NamespacedKey(plugin, "soul_liberation");
    }

    /**
     * Prüft, ob ein Item den angegebenen Plugin-Schlüssel besitzt.
     */
    public boolean has(ItemStack item, NamespacedKey key) {
        if (item == null || item.getType().isAir() || key == null) {
            return false;
        }

        if (!item.hasItemMeta()) {
            return false;
        }

        return item.getItemMeta()
                .getPersistentDataContainer()
                .has(key, PersistentDataType.BYTE);
    }

    /**
     * Erstellt den einzigartigen Mace.
     */
    public ItemStack mace() {
        ItemStack item = new ItemStack(Material.MACE);

        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName("§6§lEinzigartiger Mace");

        meta.setLore(List.of(
                "§7Ein einzigartiger Gegenstand.",
                "§7Kann nur einmal gleichzeitig existieren.",
                "§7Wird beim Tod seines Besitzers entfernt.",
                "",
                "§6PvPMod"
        ));

        meta.getPersistentDataContainer().set(
                maceKey,
                PersistentDataType.BYTE,
                (byte) 1
        );

        item.setItemMeta(meta);

        return item;
    }

    /**
     * Entfernt unerlaubte Density-Verzauberungen
     * vom einzigartigen Mace.
     *
     * Andere Gegenstände werden nicht verändert.
     */
    public void sanitizeMace(ItemStack item) {
        if (item == null || item.getType().isAir()) {
            return;
        }

        if (!has(item, maceKey)) {
            return;
        }

        ItemMeta meta = item.getItemMeta();

        if (meta == null) {
            return;
        }

        if (meta.hasEnchant(Enchantment.DENSITY)) {
            meta.removeEnchant(Enchantment.DENSITY);
            item.setItemMeta(meta);
        }
    }

    /**
     * Wird beim Aktivieren des Plugins aufgerufen.
     *
     * Das normale Minecraft-Mace-Rezept bleibt bestehen.
     * Zusätzliche Rezepte werden erst registriert, wenn
     * ihre Zutaten und gewünschten Ergebnisse feststehen.
     */
    public void registerRecipes() {
        plugin.getLogger().info(
                "ItemManager aktiviert. Einzigartiger Mace und Item-Schutz geladen."
        );
    }
}
