package com.risekaptan;

import com.risekaptan.commands.KaptanAdminCommand;
import com.risekaptan.commands.KaptanCommand;
import com.risekaptan.config.ConfigManager;
import com.risekaptan.database.DatabaseManager;
import com.risekaptan.galeyan.GaleyanManager;
import com.risekaptan.hooks.HookManager;
import com.risekaptan.listeners.PlayerListener;
import com.risekaptan.listeners.WorldProtectionListener;
import com.risekaptan.placeholder.KaptanPlaceholder;
import com.risekaptan.rpg.RPGTravelManager;
import com.risekaptan.rtp.RTPManager;
import org.bukkit.plugin.java.JavaPlugin;

public class RiseKaptan extends JavaPlugin {
    
    private static RiseKaptan instance;
    private ConfigManager configManager;
    private DatabaseManager databaseManager;
    private RTPManager rtpManager;
    private RPGTravelManager rpgTravelManager;
    private GaleyanManager galeyanManager;
    private HookManager hookManager;
    
    @Override
    public void onEnable() {
        instance = this;
        
        // Initialize configuration
        configManager = new ConfigManager(this);
        configManager.loadConfigs();
        
        // Initialize database
        databaseManager = new DatabaseManager(this);
        databaseManager.connect();
        
        // Initialize hooks
        hookManager = new HookManager(this);
        hookManager.setupHooks();
        
        // Initialize managers
        rtpManager = new RTPManager(this);
        rpgTravelManager = new RPGTravelManager(this);
        galeyanManager = new GaleyanManager(this);
        
        // Register commands
        getCommand("kaptan").setExecutor(new KaptanCommand(this));
        getCommand("kaptanadmin").setExecutor(new KaptanAdminCommand(this));
        
        // Register listeners
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        getServer().getPluginManager().registerEvents(new WorldProtectionListener(this), this);
        
        // Register PlaceholderAPI expansion
        if (hookManager.hasPlaceholderAPI()) {
            new KaptanPlaceholder(this).register();
        }
        
        getLogger().info("RiseKaptan has been enabled!");
    }
    
    @Override
    public void onDisable() {
        if (galeyanManager != null) {
            galeyanManager.shutdown();
        }
        if (databaseManager != null) {
            databaseManager.disconnect();
        }
        getLogger().info("RiseKaptan has been disabled!");
    }
    
    public static RiseKaptan getInstance() {
        return instance;
    }
    
    public ConfigManager getConfigManager() {
        return configManager;
    }
    
    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }
    
    public RTPManager getRTPManager() {
        return rtpManager;
    }
    
    public RPGTravelManager getRPGTravelManager() {
        return rpgTravelManager;
    }
    
    public GaleyanManager getGaleyanManager() {
        return galeyanManager;
    }
    
    public HookManager getHookManager() {
        return hookManager;
    }
}
