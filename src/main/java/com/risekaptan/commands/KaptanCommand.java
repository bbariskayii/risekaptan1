package com.risekaptan.commands;

import com.risekaptan.RiseKaptan;
import com.risekaptan.menu.KaptanMenu;
import com.risekaptan.utils.ColorUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class KaptanCommand implements CommandExecutor {
    
    private final RiseKaptan plugin;
    private final KaptanMenu menu;
    
    public KaptanCommand(RiseKaptan plugin) {
        this.plugin = plugin;
        this.menu = new KaptanMenu(plugin);
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by players!");
            return true;
        }
        
        Player player = (Player) sender;
        FileConfiguration config = plugin.getConfigManager().getConfig("config.yml");
        FileConfiguration messages = plugin.getConfigManager().getConfig("messages.yml");
        
        // Check if command is disabled
        if (!config.getBoolean("commands.kaptan.enabled", true)) {
            player.sendMessage(ColorUtils.colorize(messages.getString("command-disabled")));
            return true;
        }
        
        // Check permission
        if (!player.hasPermission("risekaptan.use")) {
            player.sendMessage(ColorUtils.colorize(messages.getString("no-permission")));
            return true;
        }
        
        // Check if in disabled world
        String currentWorld = player.getWorld().getName();
        if (config.getStringList("disabled-command-worlds").contains(currentWorld)) {
            player.sendMessage(ColorUtils.colorize(messages.getString("command-disabled-in-world")));
            return true;
        }
        
        // Check if RTP command is disabled (menu only)
        boolean rtpCommandDisabled = config.getBoolean("disable-rtp-command", false);
        
        if (args.length == 0 || label.equalsIgnoreCase("kaptan")) {
            // Open menu
            menu.openMenu(player);
            return true;
        }
        
        // RTP command to current world
        if (label.equalsIgnoreCase("rtp")) {
            if (rtpCommandDisabled) {
                player.sendMessage(ColorUtils.colorize(messages.getString("rtp-command-disabled")));
                return true;
            }
            
            // Check if current world has RTP enabled
            FileConfiguration worldsConfig = plugin.getConfigManager().getConfig("worlds.yml");
            if (!worldsConfig.contains("worlds." + currentWorld)) {
                player.sendMessage(ColorUtils.colorize(messages.getString("rtp-not-enabled-in-world")));
                return true;
            }
            
            // Perform RTP in current world
            plugin.getRTPManager().performRTP(player, currentWorld, false);
            return true;
        }
        
        return true;
    }
}
