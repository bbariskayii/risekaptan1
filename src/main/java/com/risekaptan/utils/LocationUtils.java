package com.risekaptan.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public class LocationUtils {
    
    private static final Set<Material> UNSAFE_BLOCKS = new HashSet<>();
    
    static {
        // Dangerous blocks
        UNSAFE_BLOCKS.add(Material.LAVA);
        UNSAFE_BLOCKS.add(Material.FIRE);
        UNSAFE_BLOCKS.add(Material.CACTUS);
        UNSAFE_BLOCKS.add(Material.MAGMA_BLOCK);
        
        // Try to add 1.13+ materials
        try {
            UNSAFE_BLOCKS.add(Material.valueOf("CAMPFIRE"));
            UNSAFE_BLOCKS.add(Material.valueOf("SOUL_CAMPFIRE"));
        } catch (IllegalArgumentException ignored) {}
    }
    
    /**
     * Find a safe random location in a world
     */
    public static Location findSafeRandomLocation(World world, int maxX, int maxZ, int maxAttempts) {
        for (int i = 0; i < maxAttempts; i++) {
            int x = ThreadLocalRandom.current().nextInt(-maxX, maxX);
            int z = ThreadLocalRandom.current().nextInt(-maxZ, maxZ);
            
            Location location = findSafeY(world, x, z);
            if (location != null) {
                return location;
            }
        }
        return null;
    }
    
    /**
     * Find safe Y coordinate for given X and Z
     */
    private static Location findSafeY(World world, int x, int z) {
        int highestY = world.getHighestBlockYAt(x, z);
        
        // Check from highest block down
        for (int y = highestY; y >= 1; y--) {
            Location loc = new Location(world, x + 0.5, y, z + 0.5);
            if (isSafeLocation(loc)) {
                return loc;
            }
        }
        
        return null;
    }
    
    /**
     * Check if a location is safe for teleportation
     */
    public static boolean isSafeLocation(Location location) {
        Block feet = location.getBlock();
        Block head = feet.getRelative(0, 1, 0);
        Block ground = feet.getRelative(0, -1, 0);
        
        // Check if feet and head are passable
        if (!isPassable(feet) || !isPassable(head)) {
            return false;
        }
        
        // Check if ground is solid
        if (!ground.getType().isSolid()) {
            return false;
        }
        
        // Check for dangerous blocks
        if (UNSAFE_BLOCKS.contains(ground.getType()) || 
            UNSAFE_BLOCKS.contains(feet.getType()) || 
            UNSAFE_BLOCKS.contains(head.getType())) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Check if a block is passable
     */
    private static boolean isPassable(Block block) {
        Material type = block.getType();
        return type.isTransparent() || 
               type == Material.AIR || 
               !type.isSolid();
    }
    
    /**
     * Serialize location to string
     */
    public static String serializeLocation(Location location) {
        if (location == null) return null;
        return location.getWorld().getName() + "," +
               location.getX() + "," +
               location.getY() + "," +
               location.getZ() + "," +
               location.getYaw() + "," +
               location.getPitch();
    }
    
    /**
     * Deserialize location from string
     */
    public static Location deserializeLocation(String locationString) {
        if (locationString == null) return null;
        
        String[] parts = locationString.split(",");
        if (parts.length != 6) return null;
        
        World world = Bukkit.getWorld(parts[0]);
        if (world == null) return null;
        
        try {
            double x = Double.parseDouble(parts[1]);
            double y = Double.parseDouble(parts[2]);
            double z = Double.parseDouble(parts[3]);
            float yaw = Float.parseFloat(parts[4]);
            float pitch = Float.parseFloat(parts[5]);
            
            return new Location(world, x, y, z, yaw, pitch);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
