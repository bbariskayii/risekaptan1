package com.risekaptan.hooks;

import com.risekaptan.RiseKaptan;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;

public class HookManager {
    
    private final RiseKaptan plugin;
    private Economy economy = null;
    private boolean placeholderAPI = false;
    private boolean worldEdit = false;
    private boolean citizens = false;
    private boolean fancyNpcs = false;
    private boolean griefPrevention = false;
    private boolean towny = false;
    private boolean lands = false;
    private boolean coinsEngine = false;
    private boolean playerPoints = false;
    private boolean itemsAdder = false;
    private boolean oraxen = false;
    
    public HookManager(RiseKaptan plugin) {
        this.plugin = plugin;
    }
    
    public void setupHooks() {
        // Vault Economy
        if (plugin.getServer().getPluginManager().getPlugin("Vault") != null) {
            setupEconomy();
        }
        
        // PlaceholderAPI
        if (plugin.getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            placeholderAPI = true;
            plugin.getLogger().info("Hooked into PlaceholderAPI!");
        }
        
        // WorldEdit
        if (plugin.getServer().getPluginManager().getPlugin("WorldEdit") != null) {
            worldEdit = true;
            plugin.getLogger().info("Hooked into WorldEdit!");
        }
        
        // Citizens
        if (plugin.getServer().getPluginManager().getPlugin("Citizens") != null) {
            citizens = true;
            plugin.getLogger().info("Hooked into Citizens!");
        }
        
        // FancyNpcs
        if (plugin.getServer().getPluginManager().getPlugin("FancyNpcs") != null) {
            fancyNpcs = true;
            plugin.getLogger().info("Hooked into FancyNpcs!");
        }
        
        // GriefPrevention
        if (plugin.getServer().getPluginManager().getPlugin("GriefPrevention") != null) {
            griefPrevention = true;
            plugin.getLogger().info("Hooked into GriefPrevention!");
        }
        
        // Towny
        if (plugin.getServer().getPluginManager().getPlugin("Towny") != null) {
            towny = true;
            plugin.getLogger().info("Hooked into Towny!");
        }
        
        // Lands
        if (plugin.getServer().getPluginManager().getPlugin("Lands") != null) {
            lands = true;
            plugin.getLogger().info("Hooked into Lands!");
        }
        
        // CoinsEngine
        if (plugin.getServer().getPluginManager().getPlugin("CoinsEngine") != null) {
            coinsEngine = true;
            plugin.getLogger().info("Hooked into CoinsEngine!");
        }
        
        // PlayerPoints
        if (plugin.getServer().getPluginManager().getPlugin("PlayerPoints") != null) {
            playerPoints = true;
            plugin.getLogger().info("Hooked into PlayerPoints!");
        }
        
        // ItemsAdder
        if (plugin.getServer().getPluginManager().getPlugin("ItemsAdder") != null) {
            itemsAdder = true;
            plugin.getLogger().info("Hooked into ItemsAdder!");
        }
        
        // Oraxen
        if (plugin.getServer().getPluginManager().getPlugin("Oraxen") != null) {
            oraxen = true;
            plugin.getLogger().info("Hooked into Oraxen!");
        }
    }
    
    private void setupEconomy() {
        RegisteredServiceProvider<Economy> rsp = plugin.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp != null) {
            economy = rsp.getProvider();
            plugin.getLogger().info("Hooked into Vault Economy!");
        }
    }
    
    // Getters
    
    public Economy getEconomy() {
        return economy;
    }
    
    public boolean hasVault() {
        return economy != null;
    }
    
    public boolean hasPlaceholderAPI() {
        return placeholderAPI;
    }
    
    public boolean hasWorldEdit() {
        return worldEdit;
    }
    
    public boolean hasCitizens() {
        return citizens;
    }
    
    public boolean hasFancyNpcs() {
        return fancyNpcs;
    }
    
    public boolean hasGriefPrevention() {
        return griefPrevention;
    }
    
    public boolean hasTowny() {
        return towny;
    }
    
    public boolean hasLands() {
        return lands;
    }
    
    public boolean hasCoinsEngine() {
        return coinsEngine;
    }
    
    public boolean hasPlayerPoints() {
        return playerPoints;
    }
    
    public boolean hasItemsAdder() {
        return itemsAdder;
    }
    
    public boolean hasOraxen() {
        return oraxen;
    }
}
