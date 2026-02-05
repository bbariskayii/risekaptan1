package com.risekaptan.listeners;

import com.risekaptan.RiseKaptan;
import com.risekaptan.utils.ColorUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.configuration.file.FileConfiguration;

public class WorldProtectionListener implements Listener {
    
    private final RiseKaptan plugin;
    
    public WorldProtectionListener(RiseKaptan plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onWorldChange(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        String worldName = player.getWorld().getName();
        
        // Check if player has permission for this world
        if (!plugin.getRTPManager().hasWorldPermission(player, worldName)) {
            FileConfiguration messages = plugin.getConfigManager().getConfig("messages.yml");
            player.sendMessage(ColorUtils.colorize(messages.getString("world-kicked")));
            
            // Kick player back to previous world or spawn
            player.teleport(event.getFrom().getSpawnLocation());
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        String worldName = player.getWorld().getName();
        
        // Check world permission
        if (!plugin.getRTPManager().hasWorldPermission(player, worldName)) {
            event.setCancelled(true);
            
            FileConfiguration messages = plugin.getConfigManager().getConfig("messages.yml");
            player.sendMessage(ColorUtils.colorize(messages.getString("no-action-permission")));
            
            // Kick player from world
            player.teleport(player.getWorld().getSpawnLocation());
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        String worldName = player.getWorld().getName();
        
        // Check world permission
        if (!plugin.getRTPManager().hasWorldPermission(player, worldName)) {
            event.setCancelled(true);
            
            FileConfiguration messages = plugin.getConfigManager().getConfig("messages.yml");
            player.sendMessage(ColorUtils.colorize(messages.getString("no-action-permission")));
            
            // Kick player from world
            player.teleport(player.getWorld().getSpawnLocation());
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        
        if (!(event.getDamager() instanceof Player)) {
            return;
        }
        
        Player victim = (Player) event.getEntity();
        Player attacker = (Player) event.getDamager();
        
        // Check Galeyan PvP protection
        if (plugin.getGaleyanManager().hasPvPProtection(victim)) {
            event.setCancelled(true);
            
            FileConfiguration messages = plugin.getConfigManager().getConfig("messages.yml");
            attacker.sendMessage(ColorUtils.colorize(messages.getString("target-has-pvp-protection")));
            return;
        }
        
        if (plugin.getGaleyanManager().hasPvPProtection(attacker)) {
            event.setCancelled(true);
            
            FileConfiguration messages = plugin.getConfigManager().getConfig("messages.yml");
            attacker.sendMessage(ColorUtils.colorize(messages.getString("you-have-pvp-protection")));
            return;
        }
    }
}
