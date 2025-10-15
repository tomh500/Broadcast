package com.sunqi.broadcast;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class Broadcast extends JavaPlugin implements CommandExecutor, Listener {

    private boolean enabled = true;
    private String itemDisplayName;
    private String playerDisplayName;
    private String prefix;

    @Override
    public void onEnable() {
        getLogger().info("Broadcast plugin enabled!");
        getCommand("broadcast").setExecutor(this);
        getServer().getPluginManager().registerEvents(this, this);
        saveDefaultConfig();
        loadConfig();
    }

    @Override
    public void onDisable() {
        getLogger().info("Broadcast plugin disabled!");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("broadcast")) {
            if (args.length == 0) {
                sender.sendMessage(ChatColor.RED + "Usage: /broadcast <on|off>");
                return true;
            }

            if (args[0].equalsIgnoreCase("on")) {
                enabled = true;
                getConfig().set("enabled", true);
                saveConfig();
                sender.sendMessage(ChatColor.GREEN + "击杀广播已开启！");
            } else if (args[0].equalsIgnoreCase("off")) {
                enabled = false;
                getConfig().set("enabled", false);
                saveConfig();
                sender.sendMessage(ChatColor.RED + "击杀广播已关闭！");
            } else {
                sender.sendMessage(ChatColor.RED + "用法: /broadcast <on|off>");
            }
            return true;
        }
        return false;
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (!enabled || !(event.getEntity().getKiller() instanceof Player)) {
            return;
        }

        Player killer = event.getEntity().getKiller();
        ItemStack weapon = killer.getInventory().getItemInMainHand();
        String itemName;

        if (weapon != null && weapon.hasItemMeta() && weapon.getItemMeta().hasDisplayName()) {
            itemName = weapon.getItemMeta().getDisplayName() + ChatColor.RESET;
        } else {
            itemName = ChatColor.GRAY + "空手" + ChatColor.RESET;
        }

        String killerName = String.format(playerDisplayName, killer.getName()) + ChatColor.RESET;
        String entityName = ChatColor.RED + event.getEntity().getName() + ChatColor.RESET;

        String message = prefix + " " + killerName + ChatColor.WHITE + " 使用 " + itemName + ChatColor.WHITE + " 击杀了 " + entityName;
        getServer().broadcastMessage(message);
    }

    private void loadConfig() {
        reloadConfig();
        enabled = getConfig().getBoolean("enabled", true);
        itemDisplayName = ChatColor.translateAlternateColorCodes('&',
                getConfig().getString("itemDisplayName", "&l&b%s&r"));
        playerDisplayName = ChatColor.translateAlternateColorCodes('&',
                getConfig().getString("playerDisplayName", "&l&e%s&r"));
        prefix = ChatColor.translateAlternateColorCodes('&',
                getConfig().getString("prefix", "&6[&e击杀广播&6]&r"));
    }
}
