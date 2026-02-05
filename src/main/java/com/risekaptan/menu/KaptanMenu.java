package com.risekaptan.menu;

import com.risekaptan.RiseKaptan;
import com.risekaptan.utils.ColorUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class KaptanMenu {
    
    private final RiseKaptan plugin;
    
    public KaptanMenu(RiseKaptan plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Open main Kaptan menu for player
     */
    public void openMenu(Player player) {
        FileConfiguration menuConfig = plugin.getConfigManager().getConfig("menu.yml");
        
        String title = ColorUtils.colorize(menuConfig.getString("menu.title", "&cKaptan"));
        int size = menuConfig.getInt("menu.size", 54);
        
        Inventory inventory = Bukkit.createInventory(null, size, title);
        
        // Fill menu with items
        ConfigurationSection itemsSection = menuConfig.getConfigurationSection("menu.items");
        if (itemsSection != null) {
            for (String key : itemsSection.getKeys(false)) {
                ConfigurationSection itemSection = itemsSection.getConfigurationSection(key);
                
                String worldName = itemSection.getString("world");
                if (worldName == null) continue;
                
                // Check if player has permission for this world
                if (!plugin.getRTPManager().hasWorldPermission(player, worldName)) {
                    continue;
                }
                
                int slot = itemSection.getInt("slot", 0);
                ItemStack item = createMenuItem(player, worldName, itemSection);
                
                inventory.setItem(slot, item);
            }
        }
        
        player.openInventory(inventory);
    }
    
    /**
     * Create a menu item for a world
     */
    private ItemStack createMenuItem(Player player, String worldName, ConfigurationSection section) {
        // Get material
        String materialStr = section.getString("material", "GRASS_BLOCK");
        Material material;
        try {
            material = Material.valueOf(materialStr);
        } catch (IllegalArgumentException e) {
            material = Material.STONE;
        }
        
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        
        // Set display name
        String displayName = section.getString("name", worldName);
        meta.setDisplayName(ColorUtils.colorize(displayName));
        
        // Set lore
        List<String> lore = new ArrayList<>();
        List<String> configLore = section.getStringList("lore");
        
        for (String line : configLore) {
            // Replace placeholders
            line = replacePlaceholders(player, worldName, line);
            lore.add(ColorUtils.colorize(line));
        }
        
        meta.setLore(lore);
        
        // Set custom model data if present
        if (section.contains("custom-model-data")) {
            meta.setCustomModelData(section.getInt("custom-model-data"));
        }
        
        item.setItemMeta(meta);
        return item;
    }
    
    /**
     * Replace placeholders in string
     */
    private String replacePlaceholders(Player player, String worldName, String text) {
        FileConfiguration config = plugin.getConfigManager().getConfig("config.yml");
        FileConfiguration worldsConfig = plugin.getConfigManager().getConfig("worlds.yml");
        
        // Get world data
        ConfigurationSection worldSection = worldsConfig.getConfigurationSection("worlds." + worldName);
        
        int maxX = worldSection != null ? worldSection.getInt("max-x", 10000) : 10000;
        int maxZ = worldSection != null ? worldSection.getInt("max-z", 10000) : 10000;
        double cost = worldSection != null ? worldSection.getDouble("cost", 0) : 0;
        
        // Count players in world
        org.bukkit.World world = Bukkit.getWorld(worldName);
        int playerCount = world != null ? world.getPlayers().size() : 0;
        
        // Check if player has last location
        Location lastLoc = plugin.getDatabaseManager().getLastLocation(player.getUniqueId(), worldName);
        boolean hasLastLocation = lastLoc != null;
        
        // Replace placeholders
        text = text.replace("%world%", worldName);
        text = text.replace("%max_x%", String.valueOf(maxX));
        text = text.replace("%max_z%", String.valueOf(maxZ));
        text = text.replace("%cost%", String.format("%.2f", cost));
        text = text.replace("%player_count%", String.valueOf(playerCount));
        text = text.replace("%size%", maxX + "x" + maxZ);
        
        // Check cooldown
        if (plugin.getRTPManager().hasCooldown(player)) {
            long remaining = plugin.getRTPManager().getRemainingCooldown(player);
            text = text.replace("%cooldown%", formatTime(remaining));
        } else {
            text = text.replace("%cooldown%", "0");
        }
        
        return text;
    }
    
    /**
     * Handle menu click
     */
    public void handleClick(Player player, ItemStack item, boolean leftClick) {
        if (item == null || !item.hasItemMeta()) {
            return;
        }
        
        FileConfiguration menuConfig = plugin.getConfigManager().getConfig("menu.yml");
        ConfigurationSection itemsSection = menuConfig.getConfigurationSection("menu.items");
        
        if (itemsSection == null) {
            return;
        }
        
        // Find which world this item represents
        for (String key : itemsSection.getKeys(false)) {
            ConfigurationSection itemSection = itemsSection.getConfigurationSection(key);
            String displayName = ColorUtils.colorize(itemSection.getString("name", ""));
            
            if (item.getItemMeta().getDisplayName().equals(displayName)) {
                String worldName = itemSection.getString("world");
                if (worldName == null) continue;
                
                player.closeInventory();
                
                // Determine action
                boolean returnToLast = !leftClick; // Right click = return to last location
                
                // Check if player has last location
                Location lastLoc = plugin.getDatabaseManager().getLastLocation(player.getUniqueId(), worldName);
                
                if (returnToLast && lastLoc == null) {
                    // No last location, force random
                    returnToLast = false;
                }
                
                // Perform RTP
                plugin.getRTPManager().performRTP(player, worldName, returnToLast);
                return;
            }
        }
    }
    
    /**
     * Format time in milliseconds to readable string
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
}
