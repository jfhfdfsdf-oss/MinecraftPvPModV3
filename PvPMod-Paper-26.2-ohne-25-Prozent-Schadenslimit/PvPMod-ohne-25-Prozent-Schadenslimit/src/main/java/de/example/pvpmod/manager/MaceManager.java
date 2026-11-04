package de.example.pvpmod.manager;

import de.example.pvpmod.PvPMod;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Inventory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/** Persistent single-owner state for the server-wide unique mace. */
public final class MaceManager {
    private final PvPMod plugin;
    private final File file;
    private final File emergencyLockFile;
    private UUID owner;
    private boolean locked;

    public MaceManager(PvPMod plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "state.yml");
        this.emergencyLockFile = new File(plugin.getDataFolder(), "mace-corrupt.lock");
        load();
    }

    private void load() {
        if (emergencyLockFile.exists()) {
            locked = true;
            plugin.getLogger().severe("Mace-Crafting bleibt wegen eines gespeicherten Sicherheits-Locks gesperrt. Nach Prüfung /pvpmod mace reset verwenden.");
            return;
        }
        if (!file.exists()) return;
        YamlConfiguration yaml = new YamlConfiguration();
        try {
            yaml.load(file);
        } catch (IOException | InvalidConfigurationException ex) {
            // Persist a sidecar lock before moving/rewriting the corrupt state. This
            // guarantees fail-closed behavior across restarts, even if the YAML rewrite fails.
            locked = true;
            try {
                Files.writeString(emergencyLockFile.toPath(), "Mace state was corrupt; manual reset required.\n");
            } catch (IOException lockError) {
                plugin.getLogger().severe("Sicherheits-Lock konnte nicht geschrieben werden; beschädigte State-Datei bleibt unverändert: " + lockError.getMessage());
                plugin.getLogger().severe("Mace-State-Datei beschädigt: " + ex.getMessage());
                return;
            }
            File backup = new File(file.getParentFile(), "state.yml.corrupt-" + System.currentTimeMillis());
            try {
                Files.move(file.toPath(), backup.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException moveError) {
                plugin.getLogger().severe("Beschädigte Mace-State-Datei konnte nicht gesichert werden: " + moveError.getMessage());
            }
            plugin.getLogger().severe("Mace-State-Datei beschädigt. Crafting bleibt vorsorglich gesperrt; nach Prüfung /pvpmod mace reset verwenden. " + ex.getMessage());
            save();
            return;
        }
        String rawOwner = yaml.getString("maceOwner", "");
        if (!rawOwner.isBlank()) {
            try {
                owner = UUID.fromString(rawOwner);
            } catch (IllegalArgumentException ex) {
                owner = null;
                locked = true;
                plugin.getLogger().severe("Ungültige Mace-Besitzer-UUID. Crafting bleibt aus Sicherheitsgründen gesperrt.");
                return;
            }
        }
        locked = yaml.getBoolean("maceLocked", owner != null);
        if (owner != null) locked = true;
    }

    public boolean save() {
        File parent = file.getParentFile();
        if (!parent.exists() && !parent.mkdirs()) {
            plugin.getLogger().severe("Plugin-Datenordner konnte nicht erstellt werden; Mace-State nicht gespeichert.");
            return false;
        }
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.set("maceOwner", owner == null ? "" : owner.toString());
        yaml.set("maceLocked", locked);
        File temp = new File(parent, "state.yml.tmp");
        try {
            yaml.save(temp);
            try {
                Files.move(temp.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (IOException atomicUnsupported) {
                Files.move(temp.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
            return true;
        } catch (IOException ex) {
            plugin.getLogger().severe("Mace-State konnte nicht gespeichert werden: " + ex.getMessage());
            return false;
        }
    }

    public boolean locked() { return locked; }
    public UUID owner() { return owner; }

    /** Atomically claims the unique mace; callers must only grant the item on true. */
    public boolean claim(Player player) {
        if (locked || owner != null) return false;
        UUID previousOwner = owner;
        boolean previousLock = locked;
        owner = player.getUniqueId();
        locked = true;
        if (save()) return true;
        owner = previousOwner;
        locked = previousLock;
        return false;
    }

    public boolean resetAfterDeath() { return reset(); }

    public boolean reset() {
        removeEverywhere();
        UUID previousOwner = owner;
        boolean previousLock = locked;
        owner = null;
        locked = false;
        if (!save()) {
            owner = previousOwner;
            locked = previousLock;
            return false;
        }
        try {
            Files.deleteIfExists(emergencyLockFile.toPath());
            return true;
        } catch (IOException ex) {
            locked = true;
            save();
            plugin.getLogger().severe("Sicherheits-Lock konnte nicht entfernt werden; Mace-Crafting bleibt gesperrt: " + ex.getMessage());
            return false;
        }
    }

    /** Removes stale copies when players join after an administrative reset. */
    public void validatePlayer(Player player) {
        boolean isOwner = owner != null && owner.equals(player.getUniqueId());
        boolean keptOwnerCopy = false;
        Inventory inventory = player.getInventory();
        for (int slot = 0; slot < inventory.getSize(); slot++) {
            ItemStack item = inventory.getItem(slot);
            if (!plugin.items().has(item, plugin.items().maceKey)) continue;
            if (!isOwner || keptOwnerCopy) inventory.setItem(slot, null);
            else keptOwnerCopy = true;
        }
        // The unique mace is never allowed in an ender chest, even for its owner.
        removeFrom(player.getEnderChest());
        player.updateInventory();
    }

    private void removeFrom(Inventory inventory) {
        for (int slot = 0; slot < inventory.getSize(); slot++) {
            if (plugin.items().has(inventory.getItem(slot), plugin.items().maceKey)) inventory.setItem(slot, null);
        }
    }

    /** Removes a stale unique mace from a chunk before its contents can be used. */
    public void cleanChunk(Chunk chunk) {
        for (BlockState state : chunk.getTileEntities(false)) {
            if (state instanceof Container container) removeFrom(container.getInventory());
        }
        for (var entity : chunk.getEntities()) {
            if (entity instanceof Item item && plugin.items().has(item.getItemStack(), plugin.items().maceKey)) item.remove();
        }
    }

    /** Cleans loaded player inventories, ender chests, world item entities and loaded containers. */
    public void removeEverywhere() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            removeFrom(player.getInventory());
            removeFrom(player.getEnderChest());
            player.updateInventory();
        }
        for (World world : Bukkit.getWorlds()) {
            for (Chunk chunk : world.getLoadedChunks()) cleanChunk(chunk);
        }
    }
}
