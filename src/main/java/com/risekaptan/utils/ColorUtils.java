package com.risekaptan.utils;

import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ColorUtils {
    
    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");
    
    /**
     * Translates color codes including HEX colors
     * Supports: &c, &6, &#FF5555, etc.
     */
    public static String colorize(String message) {
        if (message == null) return "";
        
        // Handle HEX colors (&#RRGGBB)
        Matcher matcher = HEX_PATTERN.matcher(message);
        StringBuffer buffer = new StringBuffer(message.length() + 4 * 8);
        
        while (matcher.find()) {
            String group = matcher.group(1);
            matcher.appendReplacement(buffer, "§x§" 
                + group.charAt(0) + "§" + group.charAt(1) + "§"
                + group.charAt(2) + "§" + group.charAt(3) + "§"
                + group.charAt(4) + "§" + group.charAt(5));
        }
        
        String hexed = matcher.appendTail(buffer).toString();
        
        // Handle standard color codes
        return ChatColor.translateAlternateColorCodes('&', hexed);
    }
    
    /**
     * Colorize a list of strings
     */
    public static List<String> colorize(List<String> messages) {
        List<String> colored = new ArrayList<>();
        for (String message : messages) {
            colored.add(colorize(message));
        }
        return colored;
    }
    
    /**
     * Strip all color codes from a string
     */
    public static String stripColor(String message) {
        return ChatColor.stripColor(colorize(message));
    }
}
