package com.risekaptan.config;

import com.risekaptan.RiseKaptan;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class ConfigManager {
    
    private final RiseKaptan plugin;
    private final Map<String, FileConfiguration> configs = new HashMap<>();
    private final Map<String, File> configFiles = new HashMap<>();
    
    public ConfigManager(RiseKaptan plugin) {
        this.plugin = plugin;
    }
    
    public void loadConfigs() {
        // Save and load all configuration files
        saveDefaultConfig("config.yml");
        saveDefaultConfig("menu.yml");
        saveDefaultConfig("messages.yml");
        saveDefaultConfig("rpg-travel.yml");
        saveDefaultConfig("galeyan.yml");
        saveDefaultConfig("worlds.yml");
        
        reloadConfigs();
    }
    
    public void reloadConfigs() {
        configs.clear();
        
        loadConfig("config.yml");
        loadConfig("menu.yml");
        loadConfig("messages.yml");
        loadConfig("rpg-travel.yml");
        loadConfig("galeyan.yml");
        loadConfig("worlds.yml");
    }
    
    private void saveDefaultConfig(String fileName) {
        File file = new File(plugin.getDataFolder(), fileName);
        if (!file.exists()) {
            plugin.saveResource(fileName, false);
        }
    }
    
    private void loadConfig(String fileName) {
        File file = new File(plugin.getDataFolder(), fileName);
        if (!file.exists()) {
            plugin.saveResource(fileName, false);
        }
        
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        
        // Load defaults from resources
        InputStreamReader reader = new InputStreamReader(
            plugin.getResource(fileName), StandardCharsets.UTF_8
        );
        FileConfiguration defaultConfig = YamlConfiguration.loadConfiguration(reader);
        config.setDefaults(defaultConfig);
        
        configs.put(fileName, config);
        configFiles.put(fileName, file);
    }
    
    public FileConfiguration getConfig(String fileName) {
        return configs.get(fileName);
    }
    
    public void saveConfig(String fileName) {
        try {
            configs.get(fileName).save(configFiles.get(fileName));
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save " + fileName + "!");
            e.printStackTrace();
        }
    }
}
