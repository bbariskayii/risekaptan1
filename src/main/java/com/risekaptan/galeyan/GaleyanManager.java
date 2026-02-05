package com.risekaptan.galeyan;

import com.risekaptan.RiseKaptan;
import com.risekaptan.database.DatabaseManager.GaleyanAreaData;
import com.risekaptan.utils.ColorUtils;
import com.risekaptan.utils.LocationUtils;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class GaleyanManager {
    
    private final RiseKaptan plugin;
    private final Map<String, GaleyanArea> areas = new ConcurrentHashMap<>();
    private final Map<UUID, Long> pvpProtection = new ConcurrentHashMap<>();
    
    public GaleyanManager(RiseKaptan plugin) {
        this.plugin = plugin;
        loadAreas();
    }
    
    /**
     * Load all Galeyan areas from database
     */
    public void loadAreas() {
        Map<String, GaleyanAreaData> data = plugin.getDatabaseManager().loadGaleyanAreas();
        
        for (GaleyanAreaData areaData : data.values()) {
            GaleyanArea area = new GaleyanArea(areaData);
            areas.put(areaData.name, area);
            area.startCountdown();
        }
        
        plugin.getLogger().info("Loaded " + areas.size() + " Galeyan areas!");
    }
    
    /**
     * Create a new Galeyan area
     */
    public boolean createArea(String name, Location pos1, Location pos2, String targetWorld, int interval, int pvpProtectionSeconds) {
        if (areas.containsKey(name)) {
            return false;
        }
        
        String world = pos1.getWorld().getName();
        
        // Save to database
        plugin.getDatabaseManager().saveGaleyanArea(name, world, targetWorld, pos1, pos2, interval, pvpProtectionSeconds);
        
        // Load data
        GaleyanAreaData data = new GaleyanAreaData(name, world, targetWorld, pos1, pos2, interval, pvpProtectionSeconds);
        GaleyanArea area = new GaleyanArea(data);
        areas.put(name, area);
        area.startCountdown();
        
        return true;
    }
    
    /**
     * Delete a Galeyan area
     */
    public boolean deleteArea(String name) {
        GaleyanArea area = areas.remove(name);
        if (area != null) {
            area.stopCountdown();
            plugin.getDatabaseManager().deleteGaleyanArea(name);
            return true;
        }
        return false;
    }
    
    /**
     * Get Galeyan area by name
     */
    public GaleyanArea getArea(String name) {
        return areas.get(name);
    }
    
    /**
     * Get all Galeyan areas
     */
    public Collection<GaleyanArea> getAreas() {
        return areas.values();
    }
    
    /**
     * Check if location is in any Galeyan area
     */
    public GaleyanArea getAreaAt(Location location) {
        for (GaleyanArea area : areas.values()) {
            if (area.contains(location)) {
                return area;
            }
        }
        return null;
    }
    
    /**
     * Check if player has PvP protection
     */
    public boolean hasPvPProtection(Player player) {
        if (!pvpProtection.containsKey(player.getUniqueId())) {
            return false;
        }
        
        long expireTime = pvpProtection.get(player.getUniqueId());
        if (System.currentTimeMillis() > expireTime) {
            pvpProtection.remove(player.getUniqueId());
            return false;
        }
        
        return true;
    }
    
    /**
     * Give PvP protection to player
     */
    private void givePvPProtection(Player player, int seconds) {
        long expireTime = System.currentTimeMillis() + (seconds * 1000L);
        pvpProtection.put(player.getUniqueId(), expireTime);
        
        FileConfiguration messages = plugin.getConfigManager().getConfig("messages.yml");
        String msg = messages.getString("galeyan.pvp-protection")
            .replace("%seconds%", String.valueOf(seconds));
        player.sendMessage(ColorUtils.colorize(msg));
    }
    
    /**
     * Shutdown all Galeyan areas
     */
    public void shutdown() {
        for (GaleyanArea area : areas.values()) {
            area.stopCountdown();
        }
    }
    
    /**
     * Galeyan Area class
     */
    public class GaleyanArea {
        private final GaleyanAreaData data;
        private BukkitTask countdownTask;
        private int countdown;
        
        public GaleyanArea(GaleyanAreaData data) {
            this.data = data;
            this.countdown = data.interval;
        }
        
        /**
         * Start countdown for this area
         */
        public void startCountdown() {
            countdown = data.interval;
            
            countdownTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
                countdown--;
                
                // Send countdown messages
                FileConfiguration config = plugin.getConfigManager().getConfig("galeyan.yml");
                List<Integer> announceAt = config.getIntegerList("announce-countdown-at");
                
                if (announceAt.contains(countdown)) {
                    announceCountdown(countdown);
                }
                
                if (countdown <= 0) {
                    performGaleyanRTP();
                    countdown = data.interval;
                }
            }, 20L, 20L);
        }
        
        /**
         * Stop countdown for this area
         */
        public void stopCountdown() {
            if (countdownTask != null) {
                countdownTask.cancel();
                countdownTask = null;
            }
        }
        
        /**
         * Announce countdown to players in area
         */
        private void announceCountdown(int seconds) {
            FileConfiguration messages = plugin.getConfigManager().getConfig("messages.yml");
            String msg = messages.getString("galeyan.countdown")
                .replace("%seconds%", String.valueOf(seconds));
            
            for (Player player : getPlayersInArea()) {
                player.sendMessage(ColorUtils.colorize(msg));
                
                // Play sound
                try {
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.0f);
                } catch (Exception ignored) {}
            }
        }
        
        /**
         * Perform Galeyan RTP
         */
        private void performGaleyanRTP() {
            List<Player> players = getPlayersInArea();
            
            if (players.isEmpty()) {
                return;
            }
            
            // Find safe random location
            World targetWorld = Bukkit.getWorld(data.targetWorld);
            if (targetWorld == null) {
                return;
            }
            
            FileConfiguration worldsConfig = plugin.getConfigManager().getConfig("worlds.yml");
            int maxX = worldsConfig.getInt("worlds." + data.targetWorld + ".max-x", 10000);
            int maxZ = worldsConfig.getInt("worlds." + data.targetWorld + ".max-z", 10000);
            
            Location targetLoc = LocationUtils.findSafeRandomLocation(targetWorld, maxX, maxZ, 100);
            
            if (targetLoc == null) {
                return;
            }
            
            // Teleport all players to same location
            FileConfiguration messages = plugin.getConfigManager().getConfig("messages.yml");
            String teleportMsg = messages.getString("galeyan.teleported");
            
            for (Player player : players) {
                player.teleport(targetLoc);
                player.sendMessage(ColorUtils.colorize(teleportMsg));
                
                // Give PvP protection
                givePvPProtection(player, data.pvpProtection);
                
                // Play sound
                try {
                    player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
                } catch (Exception ignored) {}
                
                // Spawn particles
                try {
                    player.getWorld().spawnParticle(Particle.PORTAL, player.getLocation().add(0, 1, 0), 50, 0.5, 0.5, 0.5, 0.1);
                } catch (Exception ignored) {}
            }
        }
        
        /**
         * Get all players currently in this area
         */
        public List<Player> getPlayersInArea() {
            List<Player> players = new ArrayList<>();
            
            World world = Bukkit.getWorld(data.world);
            if (world == null) {
                return players;
            }
            
            for (Player player : world.getPlayers()) {
                if (contains(player.getLocation())) {
                    players.add(player);
                }
            }
            
            return players;
        }
        
        /**
         * Check if location is within this area
         */
        public boolean contains(Location location) {
            if (!location.getWorld().getName().equals(data.world)) {
                return false;
            }
            
            double minX = Math.min(data.pos1.getX(), data.pos2.getX());
            double maxX = Math.max(data.pos1.getX(), data.pos2.getX());
            double minY = Math.min(data.pos1.getY(), data.pos2.getY());
            double maxY = Math.max(data.pos1.getY(), data.pos2.getY());
            double minZ = Math.min(data.pos1.getZ(), data.pos2.getZ());
            double maxZ = Math.max(data.pos1.getZ(), data.pos2.getZ());
            
            return location.getX() >= minX && location.getX() <= maxX &&
                   location.getY() >= minY && location.getY() <= maxY &&
                   location.getZ() >= minZ && location.getZ() <= maxZ;
        }
        
        /**
         * Get countdown in seconds
         */
        public int getCountdown() {
            return countdown;
        }
        
        /**
         * Get area name
         */
        public String getName() {
            return data.name;
        }
        
        /**
         * Get area data
         */
        public GaleyanAreaData getData() {
            return data;
        }
        
        /**
         * Set interval
         */
        public void setInterval(int interval) {
            stopCountdown();
            
            // Update in database
            plugin.getDatabaseManager().saveGaleyanArea(
                data.name, data.world, data.targetWorld, 
                data.pos1, data.pos2, interval, data.pvpProtection
            );
            
            startCountdown();
        }
    }
}
