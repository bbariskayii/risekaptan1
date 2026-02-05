package com.risekaptan.listeners;

import com.risekaptan.RiseKaptan;
import com.risekaptan.menu.KaptanMenu;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.ItemStack;

public class PlayerListener implements Listener {
    
    private final RiseKaptan plugin;
    private final KaptanMenu menu;
    
    public PlayerListener(RiseKaptan plugin) {
        this.plugin = plugin;
        this.menu = new KaptanMenu(plugin);
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        // Resume RPG travel if interrupted
        if (plugin.getRPGTravelManager().isTraveling(player)) {
            plugin.getRPGTravelManager().resumeTravel(player);
        }
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        
        // Note: RPG travel session is kept in memory
        // Will be resumed on rejoin
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getWhoClicked();
        
        // Check if it's Kaptan menu
        String title = event.getView().getTitle();
        String menuTitle = plugin.getConfigManager().getConfig("menu.yml").getString("menu.title", "Kaptan");
        
        if (!title.contains(menuTitle)) {
            return;
        }
        
        event.setCancelled(true);
        
        ItemStack item = event.getCurrentItem();
        if (item == null) {
            return;
        }
        
        boolean leftClick = event.isLeftClick();
        menu.handleClick(player, item, leftClick);
    }
    
    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        
        // Block commands during RPG travel
        if (plugin.getRPGTravelManager().isTraveling(player)) {
            event.setCancelled(true);
        }
    }
    
    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        
        // Hide chat messages from RPG traveling players
        if (plugin.getRPGTravelManager().isTraveling(player)) {
            event.getRecipients().removeIf(recipient -> 
                plugin.getRPGTravelManager().isTraveling(recipient)
            );
        }
    }
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        
        // Block interactions during RPG travel
        if (plugin.getRPGTravelManager().isTraveling(player)) {
            event.setCancelled(true);
        }
    }
}
