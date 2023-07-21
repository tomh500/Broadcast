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
    private String itemDisplayName = ChatColor.BOLD + " " + ChatColor.AQUA + " %s";
    private String playerDisplayName = ChatColor.BOLD + " " + ChatColor.YELLOW + " %s";
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
                sender.sendMessage(ChatColor.RED + "Usage: /broadcast <on|off> [itemDisplayName] [playerDisplayName]");
                return true;
            }
            
            if (args[0].equalsIgnoreCase("on")) {
                enabled = true;
                saveConfig();
                sender.sendMessage(ChatColor.GREEN + "Broadcast is now enabled.");
            } else if (args[0].equalsIgnoreCase("off")) {
                enabled = false;
                saveConfig();
                sender.sendMessage(ChatColor.GREEN + "Broadcast is now disabled.");
            } else {
                sender.sendMessage(ChatColor.RED + "Invalid argument. Usage: /broadcast <on|off> [itemDisplayName] [playerDisplayName]");
            }
            
            if (args.length >= 2) {
                itemDisplayName = ChatColor.translateAlternateColorCodes('&', args[1]);
            }
            
            if (args.length >= 3) {
                playerDisplayName = ChatColor.translateAlternateColorCodes('&', args[2]);
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
        
        if (weapon != null && weapon.hasItemMeta() && weapon.getItemMeta().hasDisplayName()) {
            String itemName = weapon.getItemMeta().getDisplayName();
            String killerName = killer.getName();
            String entityName = event.getEntity().getName();
            
            String message = String.format("%s使用%s击杀了%s", 
                    String.format(playerDisplayName, killerName), String.format(itemDisplayName, itemName), ChatColor.GREEN + entityName);
            
            getServer().broadcastMessage(message);
        }
    }
    
    private void loadConfig() {
        reloadConfig();
        enabled = getConfig().getBoolean("enabled", true);
        itemDisplayName = getConfig().getString("itemDisplayName", itemDisplayName);
        playerDisplayName = getConfig().getString("playerDisplayName", playerDisplayName);
        itemDisplayName = ChatColor.translateAlternateColorCodes('&', itemDisplayName);
        playerDisplayName = ChatColor.translateAlternateColorCodes('&', playerDisplayName);
    }
}
