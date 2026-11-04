package de.example.pvpmod.manager;

import de.example.pvpmod.PvPMod;
import de.example.pvpmod.data.PlayerData;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.InvalidConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class PlayerDataManager {
    private final PvPMod plugin;
    private final Map<UUID, PlayerData> cache = new ConcurrentHashMap<>();
    private final File folder;
    public PlayerDataManager(PvPMod plugin) {
        this.plugin = plugin; folder = new File(plugin.getDataFolder(), "players");
        if (!folder.exists()) folder.mkdirs();
    }
    public PlayerData get(UUID id, String name) {
        return cache.computeIfAbsent(id, key -> load(key, name));
    }
    public PlayerData get(UUID id) { return get(id, "Unbekannt"); }
    private PlayerData load(UUID id, String fallback) {
        File file = file(id); PlayerData d = new PlayerData(id, fallback);
        if (!file.exists()) return d;
        YamlConfiguration y = new YamlConfiguration();
        try { y.load(file); } catch (IOException | InvalidConfigurationException ex) {
            File backup = new File(folder, file.getName() + ".corrupt-" + System.currentTimeMillis());
            try { java.nio.file.Files.move(file.toPath(), backup.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING); } catch (IOException moveError) { plugin.getLogger().severe("Beschädigte Spielerdaten konnten nicht gesichert werden: " + file.getName()); }
            plugin.getLogger().severe("Beschädigte Spielerdaten für " + id + " erkannt; Originaldatei wurde soweit möglich gesichert. " + ex.getMessage());
            return d;
        }
        d.name(y.getString("name", fallback)); d.level(y.getInt("level", 0));
        d.stats(y.getLong("kills", 0), y.getLong("deaths", 0));
        d.banned(y.getBoolean("banned", false)); d.banTime(y.getLong("banTime", 0));
        d.wasWhitelisted(y.getBoolean("wasWhitelisted", false)); return d;
    }
    public Collection<PlayerData> allLoaded() { return Collections.unmodifiableCollection(cache.values()); }
    public List<PlayerData> allPlayers() {
        Map<UUID, PlayerData> all = new HashMap<>(cache);
        File[] files = folder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files != null) for (File f : files) {
            try { UUID id = UUID.fromString(f.getName().replace(".yml", "")); all.putIfAbsent(id, load(id, "Unbekannt")); }
            catch (IllegalArgumentException ignored) { plugin.getLogger().warning("Ungültige Spielerdatei: " + f.getName()); }
        }
        return new ArrayList<>(all.values());
    }
    public List<PlayerData> allBanned() {
        Map<UUID, PlayerData> all = new HashMap<>(cache);
        File[] files = folder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files != null) for (File f : files) {
            try { UUID id = UUID.fromString(f.getName().replace(".yml", "")); all.putIfAbsent(id, load(id, "Unbekannt")); }
            catch (IllegalArgumentException ignored) { plugin.getLogger().warning("Ungültige Spielerdatei: " + f.getName()); }
        }
        return all.values().stream().filter(PlayerData::banned).sorted(Comparator.comparing(PlayerData::name, String.CASE_INSENSITIVE_ORDER)).toList();
    }
    public void save(PlayerData d) {
        YamlConfiguration y = new YamlConfiguration(); y.set("uuid", d.uuid().toString()); y.set("name", d.name());
        y.set("level", d.level()); y.set("kills", d.kills()); y.set("deaths", d.deaths());
        y.set("banned", d.banned()); y.set("banTime", d.banTime()); y.set("wasWhitelisted", d.wasWhitelisted());
        File target = file(d.uuid()), temp = new File(folder, d.uuid() + ".tmp");
        try { y.save(temp.toPath().toFile()); try { java.nio.file.Files.move(temp.toPath(), target.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING, java.nio.file.StandardCopyOption.ATOMIC_MOVE); }
            catch (IOException ex) { java.nio.file.Files.move(temp.toPath(), target.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING); }
        } catch (IOException ex) { plugin.getLogger().severe("Spielerdaten konnten nicht gespeichert werden: " + d.uuid() + " – " + ex.getMessage()); }
    }
    public void saveAll() { cache.values().forEach(this::save); }
    private File file(UUID id) { return new File(folder, id + ".yml"); }
}
