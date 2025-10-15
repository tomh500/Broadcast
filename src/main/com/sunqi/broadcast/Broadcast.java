package com.sunqi.petfox;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fox;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PetFoxPlugin extends JavaPlugin implements Listener {

    private final Map<UUID, Wolf> wolfMap = new HashMap<>();
    private final Map<UUID, Fox> foxMap = new HashMap<>();

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);

        // 启动定时任务检测距离
        new BukkitRunnable() {
            @Override
            public void run() {
                for (UUID uuid : wolfMap.keySet()) {
                    Player player = Bukkit.getPlayer(uuid);
                    Wolf wolf = wolfMap.get(uuid);
                    Fox fox = foxMap.get(uuid);

                    if (player == null || wolf == null || fox == null || !player.isOnline()) continue;

                    Location playerLoc = player.getLocation();
                    double distance = wolf.getLocation().distance(playerLoc);

                    // 如果距离超过 15 米，直接传送
                    if (distance > 15) {
                        wolf.teleport(playerLoc);
                    }
                }
            }
        }.runTaskTimer(this, 0L, 40L); // 每 2 秒检测一次
    }

    @Override
    public void onDisable() {
        // 插件关闭时清理所有宠物
        for (Wolf wolf : wolfMap.values()) {
            wolf.remove();
        }
        for (Fox fox : foxMap.values()) {
            fox.remove();
        }
        wolfMap.clear();
        foxMap.clear();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        // 防止重复创建
        if (wolfMap.containsKey(uuid)) return;

        Location loc = player.getLocation();

        // 创建隐形狼
        Wolf wolf = (Wolf) player.getWorld().spawnEntity(loc, EntityType.WOLF);
        wolf.setTamed(true);
        wolf.setOwner(player);
        wolf.setInvisible(true);
        wolf.setSilent(true);
        wolf.setAI(true);
        wolf.setCustomNameVisible(false);

        // 创建狐狸
        Fox fox = (Fox) player.getWorld().spawnEntity(loc, EntityType.FOX);
        fox.setAI(false);
        fox.setSilent(true);
        fox.setCustomName(player.getName() + "的小狐狸 🦊");
        fox.setCustomNameVisible(true);

        // 让狐狸骑在狼上
        wolf.addPassenger(fox);

        wolfMap.put(uuid, wolf);
        foxMap.put(uuid, fox);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();

        Wolf wolf = wolfMap.remove(uuid);
        Fox fox = foxMap.remove(uuid);

        if (wolf != null) wolf.remove();
        if (fox != null) fox.remove();
    }

    @EventHandler
    public void onFoxDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Fox) {
            // 禁止狐狸受到任何伤害
            for (Fox fox : foxMap.values()) {
                if (event.getEntity().getUniqueId().equals(fox.getUniqueId())) {
                    event.setCancelled(true);
                    break;
                }
            }
        }
    }
}
