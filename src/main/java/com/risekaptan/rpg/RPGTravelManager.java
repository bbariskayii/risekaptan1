package com.risekaptan.rpg;

import com.risekaptan.RiseKaptan;
import com.risekaptan.utils.ColorUtils;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

public class RPGTravelManager {
    
    private final RiseKaptan plugin;
    private final Map<UUID, RPGTravelSession> activeSessions = new HashMap<>();
    private final Set<UUID> travelingPlayers = new HashSet<>();
    
    public RPGTravelManager(RiseKaptan plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Start RPG travel for a player
     */
    public void startRPGTravel(Player player, Location destination, String worldName) {
        if (activeSessions.containsKey(player.getUniqueId())) {
            return; // Already traveling
        }
        
        FileConfiguration config = plugin.getConfigManager().getConfig("rpg-travel.yml");
        
        // Get travel area location
        Location travelArea = getTravelAreaLocation(worldName);
        if (travelArea == null) {
            // Fallback to direct teleport
            player.teleport(destination);
            return;
        }
        
        // Create session
        RPGTravelSession session = new RPGTravelSession(player, destination, worldName, travelArea);
        activeSessions.put(player.getUniqueId(), session);
        travelingPlayers.add(player.getUniqueId());
        
        // Teleport to travel area
        player.teleport(travelArea);
        
        // Make player immobile
        player.setWalkSpeed(0);
        player.setFlySpeed(0);
        
        // Apply effects
        List<String> effects = config.getStringList("effects");
        for (String effectStr : effects) {
            try {
                String[] parts = effectStr.split(":");
                PotionEffectType type = PotionEffectType.getByName(parts[0]);
                int amplifier = parts.length > 1 ? Integer.parseInt(parts[1]) : 0;
                
                if (type != null) {
                    player.addPotionEffect(new PotionEffect(type, Integer.MAX_VALUE, amplifier, false, false));
                }
            } catch (Exception ignored) {}
        }
        
        // Spawn NPC if available
        spawnKaptanNPC(player, travelArea);
        
        // Start travel sequence
        startTravelSequence(session);
    }
    
    /**
     * Start the travel dialogue and animation sequence
     */
    private void startTravelSequence(RPGTravelSession session) {
        FileConfiguration config = plugin.getConfigManager().getConfig("rpg-travel.yml");
        List<String> dialogues = config.getStringList("dialogues");
        int travelDuration = config.getInt("travel-duration-seconds", 10);
        
        Player player = session.player;
        
        // Send dialogues with delays
        int dialogueDelay = 0;
        int delayIncrement = (travelDuration * 20) / Math.max(1, dialogues.size());
        
        for (String dialogue : dialogues) {
            final String msg = dialogue;
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (activeSessions.containsKey(player.getUniqueId())) {
                    player.sendMessage(ColorUtils.colorize(msg));
                    
                    // Play sound
                    String sound = config.getString("sounds.dialogue", "ENTITY_VILLAGER_YES");
                    try {
                        player.playSound(player.getLocation(), Sound.valueOf(sound), 1.0f, 1.0f);
                    } catch (Exception ignored) {}
                    
                    // Spawn particles
                    spawnTravelParticles(player);
                }
            }, dialogueDelay);
            
            dialogueDelay += delayIncrement;
        }
        
        // Complete travel after duration
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            completeTravelSession(session);
        }, travelDuration * 20L);
    }
    
    /**
     * Complete the RPG travel session
     */
    private void completeTravelSession(RPGTravelSession session) {
        Player player = session.player;
        
        if (!activeSessions.containsKey(player.getUniqueId())) {
            return;
        }
        
        // Remove session
        activeSessions.remove(player.getUniqueId());
        travelingPlayers.remove(player.getUniqueId());
        
        // Remove NPC
        removeKaptanNPC(player);
        
        // Restore player
        player.setWalkSpeed(0.2f);
        player.setFlySpeed(0.1f);
        
        // Remove potion effects
        for (PotionEffect effect : player.getActivePotionEffects()) {
            player.removePotionEffect(effect.getType());
        }
        
        // Teleport to destination
        player.teleport(session.destination);
        
        // Send completion message
        FileConfiguration messages = plugin.getConfigManager().getConfig("messages.yml");
        String teleportMsg = messages.getString("teleported")
            .replace("%x%", String.valueOf((int)session.destination.getX()))
            .replace("%y%", String.valueOf((int)session.destination.getY()))
            .replace("%z%", String.valueOf((int)session.destination.getZ()));
        player.sendMessage(ColorUtils.colorize(teleportMsg));
        
        // Save location
        plugin.getDatabaseManager().saveLastLocation(player.getUniqueId(), session.worldName, session.destination);
        
        // Mark as completed
        plugin.getDatabaseManager().setRPGTravelCompleted(player.getUniqueId(), true);
        
        // Give spawn protection
        FileConfiguration config = plugin.getConfigManager().getConfig("config.yml");
        int protectionTime = config.getInt("spawn-protection-seconds", 5);
        if (protectionTime > 0) {
            player.setNoDamageTicks(protectionTime * 20);
            
            String protectionMsg = messages.getString("spawn-protection")
                .replace("%seconds%", String.valueOf(protectionTime));
            player.sendMessage(ColorUtils.colorize(protectionMsg));
        }
    }
    
    /**
     * Get travel area location for world
     */
    private Location getTravelAreaLocation(String worldName) {
        FileConfiguration config = plugin.getConfigManager().getConfig("rpg-travel.yml");
        String locString = config.getString("travel-areas." + worldName);
        
        if (locString == null) {
            return null;
        }
        
        try {
            String[] parts = locString.split(",");
            World world = Bukkit.getWorld(parts[0]);
            if (world == null) return null;
            
            double x = Double.parseDouble(parts[1]);
            double y = Double.parseDouble(parts[2]);
            double z = Double.parseDouble(parts[3]);
            float yaw = parts.length > 4 ? Float.parseFloat(parts[4]) : 0;
            float pitch = parts.length > 5 ? Float.parseFloat(parts[5]) : 0;
            
            return new Location(world, x, y, z, yaw, pitch);
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Spawn Kaptan NPC for player
     */
    private void spawnKaptanNPC(Player player, Location location) {
        // This would integrate with Citizens or FancyNpcs
        // For now, it's a placeholder
        if (plugin.getHookManager().hasCitizens()) {
            // Spawn Citizens NPC
            // Implementation depends on Citizens API
        } else if (plugin.getHookManager().hasFancyNpcs()) {
            // Spawn FancyNpcs NPC
            // Implementation depends on FancyNpcs API
        }
    }
    
    /**
     * Remove Kaptan NPC for player
     */
    private void removeKaptanNPC(Player player) {
        // Remove spawned NPC
        // Implementation depends on NPC plugin
    }
    
    /**
     * Spawn travel particles around player
     */
    private void spawnTravelParticles(Player player) {
        FileConfiguration config = plugin.getConfigManager().getConfig("rpg-travel.yml");
        String particleType = config.getString("particles.type", "PORTAL");
        int particleCount = config.getInt("particles.count", 20);
        
        try {
            Particle particle = Particle.valueOf(particleType);
            Location loc = player.getLocation();
            
            player.getWorld().spawnParticle(particle, 
                loc.add(0, 1, 0), 
                particleCount, 
                0.5, 0.5, 0.5, 
                0.1);
        } catch (Exception ignored) {}
    }
    
    /**
     * Check if player is currently in RPG travel
     */
    public boolean isTraveling(Player player) {
        return travelingPlayers.contains(player.getUniqueId());
    }
    
    /**
     * Cancel RPG travel for player (e.g., on disconnect)
     */
    public void cancelTravel(Player player) {
        RPGTravelSession session = activeSessions.remove(player.getUniqueId());
        if (session != null) {
            travelingPlayers.remove(player.getUniqueId());
            
            // Restore player state
            player.setWalkSpeed(0.2f);
            player.setFlySpeed(0.1f);
            
            for (PotionEffect effect : player.getActivePotionEffects()) {
                player.removePotionEffect(effect.getType());
            }
            
            removeKaptanNPC(player);
        }
    }
    
    /**
     * Resume travel on rejoin
     */
    public void resumeTravel(Player player) {
        RPGTravelSession session = activeSessions.get(player.getUniqueId());
        if (session != null) {
            // Restart travel sequence
            player.teleport(session.travelArea);
            startTravelSequence(session);
        }
    }
    
    /**
     * RPG Travel Session data
     */
    public static class RPGTravelSession {
        public final Player player;
        public final Location destination;
        public final String worldName;
        public final Location travelArea;
        public final long startTime;
        
        public RPGTravelSession(Player player, Location destination, String worldName, Location travelArea) {
            this.player = player;
            this.destination = destination;
            this.worldName = worldName;
            this.travelArea = travelArea;
            this.startTime = System.currentTimeMillis();
        }
    }
}
