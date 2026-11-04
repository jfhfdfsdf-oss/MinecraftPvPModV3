package de.example.pvpmod.command;

import de.example.pvpmod.PvPMod;
import de.example.pvpmod.data.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import java.util.*;
import java.util.stream.Collectors;

public final class CommandManager implements CommandExecutor, TabCompleter {
    private final PvPMod plugin;
    public CommandManager(PvPMod plugin) { this.plugin = plugin; }
    @Override public boolean onCommand(CommandSender sender, Command command, String label, String[] a) {
        if (command.getName().equalsIgnoreCase("stats")) {
            if (!(sender instanceof Player p)) { sender.sendMessage("Nur Spieler können /stats verwenden."); return true; }
            plugin.statsGui().open(p, plugin.data().get(p.getUniqueId(), p.getName())); return true;
        }
        if (!sender.hasPermission("pvpmod.admin")) { sender.sendMessage("§cDafür hast du keine Berechtigung."); return true; }
        if (a.length == 0) { help(sender); return true; }
        try {
            switch (a[0].toLowerCase(Locale.ROOT)) {
                case "reload" -> { plugin.reloadConfig(); sender.sendMessage("§aPvPMod-Konfiguration neu geladen."); }
                case "setlevel", "addlevel", "removelevel", "stats", "reset", "unban" -> playerCommand(sender, a);
                case "mace" -> maceCommand(sender, a);
                default -> help(sender);
            }
        } catch (NumberFormatException ex) { sender.sendMessage("§cBitte eine gültige Zahl angeben."); }
        return true;
    }
    private void playerCommand(CommandSender sender, String[] a) {
        if (a.length < 2) { help(sender); return; }
        Player online = Bukkit.getPlayerExact(a[1]);
        PlayerData d;
        if (online != null) d = plugin.data().get(online.getUniqueId(), online.getName());
        else d = plugin.data().allPlayers().stream().filter(x -> x.name().equalsIgnoreCase(a[1])).findFirst().orElse(null);
        if (d == null) { sender.sendMessage("§cSpieler nicht gefunden. Er muss mindestens einmal auf dem Server gewesen sein."); return; }
        switch (a[0].toLowerCase(Locale.ROOT)) {
            case "setlevel" -> { if (a.length < 3) return; plugin.levels().set(d, Integer.parseInt(a[2])); }
            case "addlevel" -> { if (a.length < 3) return; int amount = Integer.parseInt(a[2]); if (amount < 0) { sender.sendMessage("§cDie Anzahl muss mindestens 0 sein."); return; } plugin.levels().add(d, amount); }
            case "removelevel" -> { if (a.length < 3) return; int amount = Integer.parseInt(a[2]); if (amount < 0) { sender.sendMessage("§cDie Anzahl muss mindestens 0 sein."); return; } plugin.levels().remove(d, amount); }
            case "stats" -> { if (online != null && sender instanceof Player viewer) plugin.statsGui().open(viewer, online, d); else sender.sendMessage(summary(d)); return; }
            case "reset" -> { if (d.banned()) plugin.bans().unban(d); d.level(0); d.stats(0, 0); d.banned(false); d.banTime(0); }
            case "unban" -> { if (!plugin.bans().unban(d)) { sender.sendMessage("§cSpieler ist nicht gebannt."); return; } }
        }
        plugin.data().save(d); if (online != null) plugin.hearts().apply(online, d.level());
        sender.sendMessage("§a" + d.name() + ": Level " + d.level() + ", Kills " + d.kills() + ", Tode " + d.deaths());
    }
    private void maceCommand(CommandSender sender, String[] a) {
        if (a.length < 2) { help(sender); return; }
        if (a[1].equalsIgnoreCase("reset")) {
            if (plugin.mace().reset()) sender.sendMessage("§aMace-Besitz und Crafting-Sperre zurückgesetzt.");
            else sender.sendMessage("§cState konnte nicht gespeichert werden. Die Crafting-Sperre bleibt vorsorglich aktiv.");
            return;
        }
        if (a[1].equalsIgnoreCase("give") && a.length >= 3) {
            Player target = Bukkit.getPlayerExact(a[2]); if (target == null) { sender.sendMessage("§cSpieler muss online sein."); return; }
            if (plugin.mace().locked()) { sender.sendMessage("§cDer einzigartige Mace existiert bereits."); return; }
            if (target.getInventory().firstEmpty() == -1) { sender.sendMessage("§cDas Inventar des Spielers ist voll."); return; }
            if (!plugin.mace().claim(target)) { sender.sendMessage("§cDer einzigartige Mace wurde bereits vergeben."); return; }
            ItemStack mace = plugin.items().mace();
            Map<Integer, ItemStack> overflow = target.getInventory().addItem(mace);
            if (!overflow.isEmpty()) {
                boolean reset = plugin.mace().reset();
                sender.sendMessage(reset
                        ? "§cDer Mace konnte nicht sicher übergeben werden; Besitz wurde zurückgesetzt."
                        : "§cÜbergabe fehlgeschlagen. Der Mace-State konnte nicht entsperrt werden; Crafting bleibt gesperrt.");
                return;
            }
            sender.sendMessage("§aEinzigartiger Mace an " + target.getName() + " vergeben."); return;
        }
        help(sender);
    }
    private String summary(PlayerData d) { return "§6" + d.name() + " §7Level: §f" + d.level() + " §7Kills: §f" + d.kills() + " §7Tode: §f" + d.deaths() + " §7K/D: §f" + String.format(Locale.US, "%.2f", d.kdr()); }
    private void help(CommandSender s) { s.sendMessage("§6/pvpmod reload | setlevel <spieler> <level> | addlevel <spieler> <anzahl> | removelevel <spieler> <anzahl> | stats <spieler> | unban <spieler> | reset <spieler> | mace reset | mace give <spieler>"); }
    @Override public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] a) {
        if (!sender.hasPermission("pvpmod.admin")) return List.of();
        if (a.length == 1) return filter(List.of("reload","setlevel","addlevel","removelevel","stats","unban","reset","mace"), a[0]);
        if (a[0].equalsIgnoreCase("mace") && a.length == 2) return filter(List.of("reset","give"), a[1]);
        if ((a.length == 2 && !a[0].equalsIgnoreCase("reload")) || (a.length == 3 && a[0].equalsIgnoreCase("mace") && a[1].equalsIgnoreCase("give"))) return filter(plugin.data().allPlayers().stream().map(PlayerData::name).distinct().toList(), a[a.length - 1]);
        if (a.length == 3 && List.of("setlevel","addlevel","removelevel").contains(a[0].toLowerCase(Locale.ROOT))) return filter(List.of("-10","-5","0","5","10","20","30"), a[2]);
        return List.of();
    }
    private List<String> filter(List<String> options, String prefix) { return options.stream().filter(x -> x.toLowerCase(Locale.ROOT).startsWith(prefix.toLowerCase(Locale.ROOT))).collect(Collectors.toList()); }
}
