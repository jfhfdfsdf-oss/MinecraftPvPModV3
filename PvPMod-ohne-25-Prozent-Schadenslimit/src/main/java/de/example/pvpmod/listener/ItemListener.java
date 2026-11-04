
package de.example.pvpmod.listener;

import de.example.pvpmod.PvPMod;
import org.bukkit.event.block.CrafterCraftEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentOffer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.enchantment.PrepareItemEnchantEvent;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.entity.ItemFrame;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public final class ItemListener implements Listener {
    private final PvPMod plugin;

    public ItemListener(PvPMod plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void interact(PlayerInteractEvent e) {
        sanitize(e.getItem());

        if (e.getItem() != null) {
            if (e.getHand() == org.bukkit.inventory.EquipmentSlot.OFF_HAND)
                e.getPlayer().getInventory().setItemInOffHand(e.getItem());
            else
                e.getPlayer().getInventory().setItemInMainHand(e.getItem());
        }

        if (e.getAction() != org.bukkit.event.block.Action.RIGHT_CLICK_AIR
                && e.getAction() != org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK) return;
        if (e.getItem() == null) return;

        ItemStack main = e.getPlayer().getInventory().getItemInMainHand();
        ItemStack off = e.getPlayer().getInventory().getItemInOffHand();

        if ((main.getType() == Material.BUNDLE || off.getType() == Material.BUNDLE)
                && (isSpecial(main) || isSpecial(off))) {
            e.setCancelled(true);
            return;
        }

        if (plugin.items().has(e.getItem(), plugin.items().crystalKey)) {
            e.setCancelled(true);
            Player p = e.getPlayer();
            var d = plugin.data().get(p.getUniqueId(), p.getName());

            if (d.level() >= 0) {
                p.sendMessage("§cDer Wiedergeburts-Kristall funktioniert nur auf negativen Leveln.");
                return;
            }

            plugin.levels().add(d, 1);
            plugin.hearts().apply(p, d.level());
            plugin.data().save(d);
            consumeHand(p, e.getHand());
            p.sendMessage("§aDein PvP-Level ist jetzt " + d.level() + ".");

        } else if (plugin.items().has(e.getItem(), plugin.items().soulKey)) {
            e.setCancelled(true);
            plugin.soulGui().open(e.getPlayer());
        }
    }

    private void consumeHand(Player p, org.bukkit.inventory.EquipmentSlot slot) {
        ItemStack hand = slot == org.bukkit.inventory.EquipmentSlot.OFF_HAND
                ? p.getInventory().getItemInOffHand()
                : p.getInventory().getItemInMainHand();

        if (hand.getAmount() <= 1) {
            if (slot == org.bukkit.inventory.EquipmentSlot.OFF_HAND)
                p.getInventory().setItemInOffHand(null);
            else
                p.getInventory().setItemInMainHand(null);
        } else {
            hand.setAmount(hand.getAmount() - 1);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void craft(CraftItemEvent e) {
        if (e.getRecipe().getResult().getType() != Material.MACE) return;

        if (e.isShiftClick()
                || (e.getClick() != ClickType.LEFT && e.getClick() != ClickType.RIGHT)
                || !e.getCursor().getType().isAir()) {
            e.setCancelled(true);
            if (e.getWhoClicked() instanceof Player p)
                p.sendMessage("§cDen einzigartigen Mace bitte mit leerem Cursor einzeln herstellen.");
            return;
        }

        if (!(e.getWhoClicked() instanceof Player p)) {
            e.setCancelled(true);
            return;
        }

        if (!plugin.mace().claim(p)) {
            e.setCancelled(true);
            p.sendMessage("§cDer einzigartige Mace ist bereits vergeben oder sein Zustand konnte nicht gespeichert werden.");
            return;
        }

        ItemStack unique = plugin.items().mace();
        e.getInventory().setResult(unique);
        e.setCurrentItem(unique);

        p.sendMessage("§6Du hast den einzigartigen Mace hergestellt. Stirbst du, wird er entfernt und das Crafting freigegeben.");
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void crafter(CrafterCraftEvent e) {
        if (e.getResult().getType() == Material.MACE && plugin.mace().locked())
            e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void interactEntity(PlayerInteractEntityEvent e) {
        ItemStack held = e.getPlayer().getInventory().getItem(e.getHand());
        if (isSpecial(held)
                && (e.getRightClicked() instanceof ItemFrame
                || e.getRightClicked() instanceof org.bukkit.entity.ArmorStand))
            e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void armorStand(PlayerArmorStandManipulateEvent e) {
        if (isSpecial(e.getPlayerItem()) || isSpecial(e.getArmorStandItem()))
            e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void drop(PlayerDropItemEvent e) {
        if (plugin.items().has(e.getItemDrop().getItemStack(), plugin.items().maceKey))
            e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void hopperPickup(InventoryPickupItemEvent e) {
        if (plugin.items().has(e.getItem().getItemStack(), plugin.items().maceKey))
            e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void pickup(EntityPickupItemEvent e) {
        if (plugin.items().has(e.getItem().getItemStack(), plugin.items().maceKey))
            e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void move(InventoryMoveItemEvent e) {
        if (plugin.items().has(e.getItem(), plugin.items().maceKey))
            e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void dispense(BlockDispenseEvent e) {
        if (plugin.items().has(e.getItem(), plugin.items().maceKey))
            e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void click(InventoryClickEvent e) {
        sanitize(e.getCurrentItem());
        sanitize(e.getCursor());

        if (e.getCurrentItem() != null) e.setCurrentItem(e.getCurrentItem());
        if (e.getCursor() != null) e.setCursor(e.getCursor());

        int topSize = e.getView().getTopInventory().getSize();
        int rawSlot = e.getRawSlot();
        boolean clickedTop = rawSlot >= 0 && rawSlot < topSize;
        boolean clickedOutside = rawSlot < 0;
        boolean currentIsMace = isSpecial(e.getCurrentItem());
        boolean cursorIsMace = isSpecial(e.getCursor());

        boolean hotbarIsMace = e.getClick() == ClickType.NUMBER_KEY
                && e.getHotbarButton() >= 0
                && isSpecial(e.getWhoClicked().getInventory().getItem(e.getHotbarButton()));

        boolean offhandIsMace = e.getClick() == ClickType.SWAP_OFFHAND
                && isSpecial(e.getWhoClicked().getInventory().getItemInOffHand());

        if ((currentIsMace && clickedTop)
                || (cursorIsMace && (clickedTop || clickedOutside))
                || ((hotbarIsMace || offhandIsMace) && clickedTop)
                || ((currentIsMace || cursorIsMace || hotbarIsMace || offhandIsMace)
                && (e.isShiftClick() || e.getClick() == ClickType.MIDDLE
                || (e.getWhoClicked() instanceof Player clicker
                && clicker.getGameMode() == org.bukkit.GameMode.CREATIVE)))) {
            e.setCancelled(true);
        }

        if (e.getView().getTitle().startsWith("Gebannte Spieler")
                && e.getWhoClicked() instanceof Player p) {
            e.setCancelled(true);
            plugin.soulGui().handleListClick(p, rawSlot, e.getCurrentItem());

        } else if (e.getView().getTitle().equals("Entbannung bestätigen?")
                && e.getWhoClicked() instanceof Player p) {
            e.setCancelled(true);
            if (clickedTop) plugin.soulGui().confirm(p, rawSlot);

        } else if (e.getView().getTitle().contains("PvP-Statistiken")) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void drag(InventoryDragEvent e) {
        if (!isSpecial(e.getOldCursor())) return;

        int topSize = e.getView().getTopInventory().getSize();
        if (e.getRawSlots().stream().anyMatch(slot -> slot < topSize))
            e.setCancelled(true);
    }

    private boolean isSpecial(ItemStack item) {
        return plugin.items().has(item, plugin.items().maceKey);
    }

    private void sanitize(ItemStack item) {
        plugin.items().sanitizeMace(item);
    }

    @EventHandler
    public void held(org.bukkit.event.player.PlayerItemHeldEvent e) {
        ItemStack item = e.getPlayer().getInventory().getItem(e.getNewSlot());
        sanitize(item);
        e.getPlayer().getInventory().setItem(e.getNewSlot(), item);
    }

    @EventHandler
    public void swapHands(org.bukkit.event.player.PlayerSwapHandItemsEvent e) {
        ItemStack main = e.getMainHandItem();
        sanitize(main);
        e.setMainHandItem(main);

        ItemStack off = e.getOffHandItem();
        sanitize(off);
        e.setOffHandItem(off);
    }

    @EventHandler
    public void cleanOnJoin(PlayerJoinEvent e) {
        var inventory = e.getPlayer().getInventory();

        for (int slot = 0; slot < inventory.getSize(); slot++) {
            ItemStack item = inventory.getItem(slot);
            sanitize(item);
            inventory.setItem(slot, item);
        }

        var ender = e.getPlayer().getEnderChest();

        for (int slot = 0; slot < ender.getSize(); slot++) {
            ItemStack item = ender.getItem(slot);
            sanitize(item);
            ender.setItem(slot, item);
        }
    }

    @EventHandler
    public void cleanLoadedChunk(ChunkLoadEvent e) {
        plugin.mace().cleanChunk(e.getChunk());
    }

    @EventHandler
    public void prepareEnchant(PrepareItemEnchantEvent e) {
        if (!isSpecial(e.getItem())) return;

        EnchantmentOffer[] offers = e.getOffers();
        if (offers == null) return;

        for (int i = 0; i < offers.length; i++)
            if (offers[i] != null
                    && offers[i].getEnchantment().equals(Enchantment.DENSITY))
                offers[i] = null;
    }

    @EventHandler
    public void enchant(EnchantItemEvent e) {
        if (plugin.items().has(e.getItem(), plugin.items().maceKey))
            e.getEnchantsToAdd().remove(Enchantment.DENSITY);
    }

    @EventHandler
    public void anvil(PrepareAnvilEvent e) {
        ItemStack first = e.getInventory().getItem(0);
        ItemStack result = e.getResult();
        ItemStack second = e.getInventory().getItem(1);

        if ((!isSpecial(first) && !isSpecial(second))
                || result == null || !result.hasItemMeta()) return;

        ItemMeta meta = result.getItemMeta();
        meta.removeEnchant(Enchantment.DENSITY);
        result.setItemMeta(meta);
        e.setResult(result);
    }
}
