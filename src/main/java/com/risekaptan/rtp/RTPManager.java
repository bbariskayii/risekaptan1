package com.risekaptan.rtp;

import com.risekaptan.RiseKaptan;
import com.risekaptan.utils.ColorUtils;
import com.risekaptan.utils.LocationUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.*;

public class RTPManager {
    
    private final RiseKaptan plugin;
    private final Map<UUID, Long> cooldowns = new HashMap<>();
    private final Map<UUID, RTPTask> activeTasks = new HashMap<>();
    
    public RTPManager(RiseKaptan plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Perform RTP for a player to a specific world
     */
    public void performRTP(Player player, String worldName, boolean returnToLast) {
        FileConfiguration config = plugin.getConfigManager().getConfig("config.yml");
        FileConfiguration messages = plugin.getConfigManager().getConfig("messages.yml");
        
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            player.sendMessage(ColorUtils.colorize(messages.getString("world-not-found")));
            return;
        }
        
        // Check if player has permission for this world
        if (!hasWorldPermission(player, worldName)) {
            player.sendMessage(ColorUtils.colorize(messages.getString("no-world-permission")));
            return;
        }
        
        // Check if world is disabled
        List<String> disabledWorlds = config.getStringList("disabled-worlds");
        if (disabledWorlds.contains(worldName)) {
            player.sendMessage(ColorUtils.colorize(messages.getString("world-disabled")));
            return;
        }
        
        // Check if returning to last location
        if (returnToLast) {
            Location lastLoc = plugin.getDatabaseManager().getLastLocation(player.getUniqueId(), worldName);
            if (lastLoc != null) {
                teleportPlayer(player, lastLoc, worldName, false);
                return;
            }
        }
        
        // Check cooldown
        if (!player.hasPermission("risekaptan.bypass.cooldown") && hasCooldown(player)) {
            long remaining = getRemainingCooldown(player);
            String cooldownMsg = messages.getString("cooldown-active")
                .replace("%time%", formatTime(remaining));
            player.sendMessage(ColorUtils.colorize(cooldownMsg));
            return;
        }
        
        // Check cost
        if (!player.hasPermission("risekaptan.bypass.cost")) {
            double cost = getWorldCost(worldName, player);
            if (cost > 0) {
                if (!plugin.getHookManager().hasVault()) {
                    player.sendMessage(ColorUtils.colorize(messages.getString("economy-not-available")));
                    return;
                }
                
                if (!plugin.getHookManager().getEconomy().has(player, cost)) {
                    String insufficientMsg = messages.getString("insufficient-money")
                        .replace("%amount%", String.format("%.2f", cost));
                    player.sendMessage(ColorUtils.colorize(insufficientMsg));
                    return;
                }
            }
        }
        
        // Start RTP process
        player.sendMessage(ColorUtils.colorize(messages.getString("rtp-preparing")));
        
        // Find safe location asynchronously
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            ConfigurationSection worldConfig = config.getConfigurationSection("worlds." + worldName);
            int maxX = worldConfig != null ? worldConfig.getInt("max-x", 10000) : 10000;
            int maxZ = worldConfig != null ? worldConfig.getInt("max-z", 10000) : 10000;
            int maxAttempts = config.getInt("max-attempts", 100);
            
            Location safeLoc = LocationUtils.findSafeRandomLocation(world, maxX, maxZ, maxAttempts);
            
            Bukkit.getScheduler().runTask(plugin, () -> {
                if (safeLoc == null) {
                    player.sendMessage(ColorUtils.colorize(messages.getString("rtp-failed")));
                    return;
                }
                
                // Charge the player
                if (!player.hasPermission("risekaptan.bypass.cost")) {
                    double cost = getWorldCost(worldName, player);
                    if (cost > 0 && plugin.getHookManager().hasVault()) {
                        plugin.getHookManager().getEconomy().withdrawPlayer(player, cost);
                        String paidMsg = messages.getString("money-paid")
                            .replace("%amount%", String.format("%.2f", cost));
                        player.sendMessage(ColorUtils.colorize(paidMsg));
                    }
                }
                
                // Set cooldown
                if (!player.hasPermission("risekaptan.bypass.cooldown")) {
                    setCooldown(player);
                }
                
                // Teleport player
                teleportPlayer(player, safeLoc, worldName, true);
            });
        });
    }
    
    /**
     * Teleport player with RPG travel if enabled
     */
    private void teleportPlayer(Player player, Location location, String worldName, boolean isNewLocation) {
        FileConfiguration rpgConfig = plugin.getConfigManager().getConfig("rpg-travel.yml");
        
        boolean rpgEnabled = rpgConfig.getBoolean("enabled", true);
        boolean onlyFirstTime = rpgConfig.getBoolean("only-first-time", true);
        List<String> rpgWorlds = rpgConfig.getStringList("worlds");
        
        boolean shouldUseRPG = rpgEnabled && 
                              rpgWorlds.contains(worldName) &&
                              isNewLocation &&
                              (!onlyFirstTime || !plugin.getDatabaseManager().hasCompletedRPGTravel(player.getUniqueId()));
        
        if (shouldUseRPG) {
            plugin.getRPGTravelManager().startRPGTravel(player, location, worldName);
        } else {
            player.teleport(location);
            
            FileConfiguration messages = plugin.getConfigManager().getConfig("messages.yml");
            String teleportMsg = messages.getString("teleported")
                .replace("%x%", String.valueOf((int)location.getX()))
                .replace("%y%", String.valueOf((int)location.getY()))
                .replace("%z%", String.valueOf((int)location.getZ()));
            player.sendMessage(ColorUtils.colorize(teleportMsg));
            
            // Save location
            plugin.getDatabaseManager().saveLastLocation(player.getUniqueId(), worldName, location);
            
            // Give damage protection
            giveSpawnProtection(player);
        }
    }
    
    /**
     * Give spawn protection to player
     */
    private void giveSpawnProtection(Player player) {
        FileConfiguration config = plugin.getConfigManager().getConfig("config.yml");
        FileConfiguration messages = plugin.getConfigManager().getConfig("messages.yml");
        
        int protectionTime = config.getInt("spawn-protection-seconds", 5);
        if (protectionTime > 0) {
            player.setNoDamageTicks(protectionTime * 20);
            
            String protectionMsg = messages.getString("spawn-protection")
                .replace("%seconds%", String.valueOf(protectionTime));
            player.sendMessage(ColorUtils.colorize(protectionMsg));
        }
    }
    
    /**
     * Check if player has world permission
     */
    public boolean hasWorldPermission(Player player, String worldName) {
        FileConfiguration config = plugin.getConfigManager().getConfig("config.yml");
        
        if (!config.getBoolean("world-permissions.enabled", false)) {
            return true;
        }
        
        String permission = config.getString("world-permissions.worlds." + worldName);
        if (permission == null || permission.isEmpty()) {
            return true;
        }
        
        return player.hasPermission(permission);
    }
    
    /**
     * Get world RTP cost for player
     */
    private double getWorldCost(String worldName, Player player) {
        FileConfiguration config = plugin.getConfigManager().getConfig("config.yml");
        ConfigurationSection worldConfig = config.getConfigurationSection("worlds." + worldName);
        
        if (worldConfig == null) {
            return 0;
        }
        
        // Check for permission-based costs
        ConfigurationSection costsSection = worldConfig.getConfigurationSection("costs");
        if (costsSection != null) {
            for (String key : costsSection.getKeys(false)) {
                if (player.hasPermission("risekaptan.cost." + key)) {
                    return costsSection.getDouble(key);
                }
            }
        }
        
        return worldConfig.getDouble("cost", 0);
    }
    
    /**
     * Check if player has cooldown
     */
    public boolean hasCooldown(Player player) {
        if (!cooldowns.containsKey(player.getUniqueId())) {
            return false;
        }
        
        long lastUse = cooldowns.get(player.getUniqueId());
        long cooldownTime = getCooldownTime(player) * 1000;
        
        return System.currentTimeMillis() - lastUse < cooldownTime;
    }
    
    /**
     * Get remaining cooldown in milliseconds
     */
    public long getRemainingCooldown(Player player) {
        if (!cooldowns.containsKey(player.getUniqueId())) {
            return 0;
        }
        
        long lastUse = cooldowns.get(player.getUniqueId());
        long cooldownTime = getCooldownTime(player) * 1000;
        long elapsed = System.currentTimeMillis() - lastUse;
        
        return Math.max(0, cooldownTime - elapsed);
    }
    
    /**
     * Get cooldown time for player in seconds
     */
    private long getCooldownTime(Player player) {
        FileConfiguration config = plugin.getConfigManager().getConfig("config.yml");
        
        // Check permission-based cooldowns
        ConfigurationSection cooldownSection = config.getConfigurationSection("cooldowns");
        if (cooldownSection != null) {
            for (String key : cooldownSection.getKeys(false)) {
                if (player.hasPermission("risekaptan.cooldown." + key)) {
                    return cooldownSection.getLong(key);
                }
            }
        }
        
        return config.getLong("default-cooldown", 300);
    }
    
    /**
     * Set cooldown for player
     */
    private void setCooldown(Player player) {
        cooldowns.put(player.getUniqueId(), System.currentTimeMillis());
        plugin.getDatabaseManager().saveLastRTPTime(player.getUniqueId(), System.currentTimeMillis());
    }
    
    /**
     * Format time in seconds to readable string
     */
    private String formatTime(long millis) {
        long seconds = millis / 1000;
        long minutes = seconds / 60;
        seconds = seconds % 60;
        
        if (minutes > 0) {
            return minutes + " dakika " + seconds + " saniye";
        }
        return seconds + " saniye";
    }
    
    // RTP Task for tracking
    public static class RTPTask {
        public final UUID playerUUID;
        public final Location targetLocation;
        public final long startTime;
        
        public RTPTask(UUID playerUUID, Location targetLocation) {
            this.playerUUID = playerUUID;
            this.targetLocation = targetLocation;
            this.startTime = System.currentTimeMillis();
        }
    }
}
