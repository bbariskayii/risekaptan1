package com.risekaptan.commands;

import com.risekaptan.RiseKaptan;
import com.risekaptan.utils.ColorUtils;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.regions.Region;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class KaptanAdminCommand implements CommandExecutor, TabCompleter {
    
    private final RiseKaptan plugin;
    
    public KaptanAdminCommand(RiseKaptan plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        FileConfiguration messages = plugin.getConfigManager().getConfig("messages.yml");
        
        if (!sender.hasPermission("risekaptan.admin")) {
            sender.sendMessage(ColorUtils.colorize(messages.getString("no-permission")));
            return true;
        }
        
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "reload":
                handleReload(sender);
                break;
                
            case "rtp":
                if (args.length < 3) {
                    sender.sendMessage(ColorUtils.colorize("&cUsage: /kaptanadmin rtp <player> <world>"));
                    return true;
                }
                handleRTP(sender, args[1], args[2]);
                break;
                
            case "travel":
                if (args.length < 2) {
                    sender.sendMessage(ColorUtils.colorize("&cUsage: /kaptanadmin travel <setplayer|setnpc>"));
                    return true;
                }
                handleTravel(sender, args[1]);
                break;
                
            case "galeyan":
                if (args.length < 2) {
                    sender.sendMessage(ColorUtils.colorize("&cUsage: /kaptanadmin galeyan <setarea|delete|list>"));
                    return true;
                }
                handleGaleyan(sender, args);
                break;
                
            default:
                sendHelp(sender);
                break;
        }
        
        return true;
    }
    
    private void handleReload(CommandSender sender) {
        plugin.getConfigManager().reloadConfigs();
        sender.sendMessage(ColorUtils.colorize("&aRiseKaptan configuration reloaded!"));
    }
    
    private void handleRTP(CommandSender sender, String playerName, String worldName) {
        Player target = Bukkit.getPlayer(playerName);
        if (target == null) {
            sender.sendMessage(ColorUtils.colorize("&cPlayer not found!"));
            return;
        }
        
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            sender.sendMessage(ColorUtils.colorize("&cWorld not found!"));
            return;
        }
        
        plugin.getRTPManager().performRTP(target, worldName, false);
        sender.sendMessage(ColorUtils.colorize("&aStarted RTP for " + playerName + " to " + worldName));
    }
    
    private void handleTravel(CommandSender sender, String action) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by players!");
            return;
        }
        
        Player player = (Player) sender;
        Location location = player.getLocation();
        FileConfiguration config = plugin.getConfigManager().getConfig("rpg-travel.yml");
        
        String locString = location.getWorld().getName() + "," +
                          location.getX() + "," +
                          location.getY() + "," +
                          location.getZ() + "," +
                          location.getYaw() + "," +
                          location.getPitch();
        
        if (action.equalsIgnoreCase("setplayer")) {
            // Set player spawn location for RPG travel
            String worldName = player.getWorld().getName();
            config.set("travel-areas." + worldName, locString);
            plugin.getConfigManager().saveConfig("rpg-travel.yml");
            
            sender.sendMessage(ColorUtils.colorize("&aRPG travel player location set for world: " + worldName));
        } else if (action.equalsIgnoreCase("setnpc")) {
            // Set NPC spawn location for RPG travel
            String worldName = player.getWorld().getName();
            config.set("npc-locations." + worldName, locString);
            plugin.getConfigManager().saveConfig("rpg-travel.yml");
            
            sender.sendMessage(ColorUtils.colorize("&aRPG travel NPC location set for world: " + worldName));
        }
    }
    
    private void handleGaleyan(CommandSender sender, String[] args) {
        String action = args[1].toLowerCase();
        
        switch (action) {
            case "setarea":
                if (args.length < 3) {
                    sender.sendMessage(ColorUtils.colorize("&cUsage: /kaptanadmin galeyan setarea <name>"));
                    return;
                }
                handleGaleyanSetArea(sender, args[2]);
                break;
                
            case "delete":
                if (args.length < 3) {
                    sender.sendMessage(ColorUtils.colorize("&cUsage: /kaptanadmin galeyan delete <name>"));
                    return;
                }
                handleGaleyanDelete(sender, args[2]);
                break;
                
            case "list":
                handleGaleyanList(sender);
                break;
                
            case "interval":
                if (args.length < 4) {
                    sender.sendMessage(ColorUtils.colorize("&cUsage: /kaptanadmin galeyan interval <name> <seconds>"));
                    return;
                }
                handleGaleyanInterval(sender, args[2], args[3]);
                break;
                
            default:
                sender.sendMessage(ColorUtils.colorize("&cUnknown Galeyan subcommand!"));
                break;
        }
    }
    
    private void handleGaleyanSetArea(CommandSender sender, String name) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by players!");
            return;
        }
        
        if (!plugin.getHookManager().hasWorldEdit()) {
            sender.sendMessage(ColorUtils.colorize("&cWorldEdit is required for this command!"));
            return;
        }
        
        Player player = (Player) sender;
        
        try {
            // Get WorldEdit selection
            LocalSession session = WorldEdit.getInstance().getSessionManager().get(BukkitAdapter.adapt(player));
            Region region = session.getSelection(BukkitAdapter.adapt(player.getWorld()));
            
            com.sk89q.worldedit.math.BlockVector3 min = region.getMinimumPoint();
            com.sk89q.worldedit.math.BlockVector3 max = region.getMaximumPoint();
            
            Location pos1 = new Location(player.getWorld(), min.getX(), min.getY(), min.getZ());
            Location pos2 = new Location(player.getWorld(), max.getX(), max.getY(), max.getZ());
            
            // Ask for target world
            sender.sendMessage(ColorUtils.colorize("&aGaleyan area created! Set target world with: /kaptanadmin galeyan settarget <name> <world>"));
            
            // Create area with default settings
            FileConfiguration config = plugin.getConfigManager().getConfig("galeyan.yml");
            int defaultInterval = config.getInt("default-interval", 300);
            int defaultPvP = config.getInt("default-pvp-protection", 10);
            
            plugin.getGaleyanManager().createArea(name, pos1, pos2, player.getWorld().getName(), defaultInterval, defaultPvP);
            
        } catch (IncompleteRegionException e) {
            sender.sendMessage(ColorUtils.colorize("&cYou must select a region with WorldEdit first!"));
        }
    }
    
    private void handleGaleyanDelete(CommandSender sender, String name) {
        if (plugin.getGaleyanManager().deleteArea(name)) {
            sender.sendMessage(ColorUtils.colorize("&aGaleyan area '" + name + "' deleted!"));
        } else {
            sender.sendMessage(ColorUtils.colorize("&cGaleyan area '" + name + "' not found!"));
        }
    }
    
    private void handleGaleyanList(CommandSender sender) {
        sender.sendMessage(ColorUtils.colorize("&6=== Galeyan Areas ==="));
        
        if (plugin.getGaleyanManager().getAreas().isEmpty()) {
            sender.sendMessage(ColorUtils.colorize("&7No Galeyan areas found."));
            return;
        }
        
        for (com.risekaptan.galeyan.GaleyanManager.GaleyanArea area : plugin.getGaleyanManager().getAreas()) {
            sender.sendMessage(ColorUtils.colorize("&e" + area.getName() + " &7- Countdown: &f" + area.getCountdown() + "s"));
        }
    }
    
    private void handleGaleyanInterval(CommandSender sender, String name, String secondsStr) {
        try {
            int seconds = Integer.parseInt(secondsStr);
            
            com.risekaptan.galeyan.GaleyanManager.GaleyanArea area = plugin.getGaleyanManager().getArea(name);
            if (area == null) {
                sender.sendMessage(ColorUtils.colorize("&cGaleyan area '" + name + "' not found!"));
                return;
            }
            
            area.setInterval(seconds);
            sender.sendMessage(ColorUtils.colorize("&aGaleyan area '" + name + "' interval set to " + seconds + " seconds!"));
            
        } catch (NumberFormatException e) {
            sender.sendMessage(ColorUtils.colorize("&cInvalid number!"));
        }
    }
    
    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ColorUtils.colorize("&6=== RiseKaptan Admin Commands ==="));
        sender.sendMessage(ColorUtils.colorize("&e/kaptanadmin reload &7- Reload configuration"));
        sender.sendMessage(ColorUtils.colorize("&e/kaptanadmin rtp <player> <world> &7- Force RTP player"));
        sender.sendMessage(ColorUtils.colorize("&e/kaptanadmin travel setplayer &7- Set RPG travel location"));
        sender.sendMessage(ColorUtils.colorize("&e/kaptanadmin travel setnpc &7- Set RPG NPC location"));
        sender.sendMessage(ColorUtils.colorize("&e/kaptanadmin galeyan setarea <name> &7- Create Galeyan area"));
        sender.sendMessage(ColorUtils.colorize("&e/kaptanadmin galeyan delete <name> &7- Delete Galeyan area"));
        sender.sendMessage(ColorUtils.colorize("&e/kaptanadmin galeyan list &7- List all Galeyan areas"));
        sender.sendMessage(ColorUtils.colorize("&e/kaptanadmin galeyan interval <name> <seconds> &7- Set interval"));
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            completions.addAll(Arrays.asList("reload", "rtp", "travel", "galeyan"));
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("travel")) {
                completions.addAll(Arrays.asList("setplayer", "setnpc"));
            } else if (args[0].equalsIgnoreCase("galeyan")) {
                completions.addAll(Arrays.asList("setarea", "delete", "list", "interval"));
            }
        }
        
        return completions;
    }
}
