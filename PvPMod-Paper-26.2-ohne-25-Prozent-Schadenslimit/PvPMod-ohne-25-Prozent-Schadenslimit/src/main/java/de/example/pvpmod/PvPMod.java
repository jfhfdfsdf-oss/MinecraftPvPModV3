package de.example.pvpmod;

import de.example.pvpmod.command.CommandManager;
import de.example.pvpmod.gui.SoulLiberationGUI;
import de.example.pvpmod.gui.StatsGUI;
import de.example.pvpmod.listener.*;
import de.example.pvpmod.manager.*;
import org.bukkit.plugin.java.JavaPlugin;

public final class PvPMod extends JavaPlugin {
    private PlayerDataManager data;
    private LevelManager levels;
    private HeartManager hearts;
    private BanManager bans;
    private MaceManager mace;
    private ItemManager items;
    private StatsGUI statsGui;
    private SoulLiberationGUI soulGui;
    @Override public void onEnable() {
        saveDefaultConfig();
        data = new PlayerDataManager(this); levels = new LevelManager(); hearts = new HeartManager(levels);
        bans = new BanManager(this, data); mace = new MaceManager(this); items = new ItemManager(this);
        statsGui = new StatsGUI(this); soulGui = new SoulLiberationGUI(this);
        items.registerRecipes();
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        getServer().getPluginManager().registerEvents(new CombatListener(this), this);
        getServer().getPluginManager().registerEvents(new XPListener(this), this);
        getServer().getPluginManager().registerEvents(new ItemListener(this), this);
        getServer().getPluginManager().registerEvents(new MaceListener(this), this);
        CommandManager commands = new CommandManager(this);
        getCommand("stats").setExecutor(commands); getCommand("stats").setTabCompleter(commands);
        getCommand("pvpmod").setExecutor(commands); getCommand("pvpmod").setTabCompleter(commands);
        // Existing spawn chunks may already be loaded before plugin listeners register.
        for (var world : getServer().getWorlds()) {
            for (var chunk : world.getLoadedChunks()) mace.cleanChunk(chunk);
        }
        getLogger().info("PvPMod wurde aktiviert.");
    }
    @Override public void onDisable() { if (data != null) data.saveAll(); if (mace != null) mace.save(); }
    public PlayerDataManager data() { return data; }
    public LevelManager levels() { return levels; }
    public HeartManager hearts() { return hearts; }
    public BanManager bans() { return bans; }
    public MaceManager mace() { return mace; }
    public ItemManager items() { return items; }
    public StatsGUI statsGui() { return statsGui; }
    public SoulLiberationGUI soulGui() { return soulGui; }
}
