package com.risekaptan.placeholder;

import com.risekaptan.RiseKaptan;
import com.risekaptan.galeyan.GaleyanManager.GaleyanArea;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class KaptanPlaceholder extends PlaceholderExpansion {
    
    private final RiseKaptan plugin;
    
    public KaptanPlaceholder(RiseKaptan plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public @NotNull String getIdentifier() {
        return "kaptan";
    }
    
    @Override
    public @NotNull String getAuthor() {
        return "RiseKaptan";
    }
    
    @Override
    public @NotNull String getVersion() {
        return "1.0.0";
    }
    
    @Override
    public boolean persist() {
        return true;
    }
    
    @Override
    public String onPlaceholderRequest(Player player, @NotNull String identifier) {
        // %kaptan_cooldown%
        if (identifier.equals("cooldown")) {
            if (player == null) return "0";
            
            if (plugin.getRTPManager().hasCooldown(player)) {
                long remaining = plugin.getRTPManager().getRemainingCooldown(player);
                return String.valueOf(remaining / 1000);
            }
            return "0";
        }
        
        // %kaptan_cooldown_formatted%
        if (identifier.equals("cooldown_formatted")) {
            if (player == null) return "0s";
            
            if (plugin.getRTPManager().hasCooldown(player)) {
                long remaining = plugin.getRTPManager().getRemainingCooldown(player);
                return formatTime(remaining);
            }
            return "0s";
        }
        
        // %kaptan_traveling%
        if (identifier.equals("traveling")) {
            if (player == null) return "false";
            return String.valueOf(plugin.getRPGTravelManager().isTraveling(player));
        }
        
        // %kaptan_has_pvp_protection%
        if (identifier.equals("has_pvp_protection")) {
            if (player == null) return "false";
            return String.valueOf(plugin.getGaleyanManager().hasPvPProtection(player));
        }
        
        // %kaptan_galeyan_<areaname>_countdown%
        if (identifier.startsWith("galeyan_") && identifier.endsWith("_countdown")) {
            String areaName = identifier.substring(8, identifier.length() - 10);
            GaleyanArea area = plugin.getGaleyanManager().getArea(areaName);
            
            if (area != null) {
                return String.valueOf(area.getCountdown());
            }
            return "0";
        }
        
        // %kaptan_galeyan_<areaname>_players%
        if (identifier.startsWith("galeyan_") && identifier.endsWith("_players")) {
            String areaName = identifier.substring(8, identifier.length() - 8);
            GaleyanArea area = plugin.getGaleyanManager().getArea(areaName);
            
            if (area != null) {
                return String.valueOf(area.getPlayersInArea().size());
            }
            return "0";
        }
        
        return null;
    }
    
    private String formatTime(long millis) {
        long seconds = millis / 1000;
        long minutes = seconds / 60;
        seconds = seconds % 60;
        
        if (minutes > 0) {
            return minutes + "m " + seconds + "s";
        }
        return seconds + "s";
    }
}
