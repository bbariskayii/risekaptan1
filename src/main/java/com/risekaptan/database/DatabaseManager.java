package com.risekaptan.database;

import com.risekaptan.RiseKaptan;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DatabaseManager {
    
    private final RiseKaptan plugin;
    private Connection connection;
    private String host, database, username, password;
    private int port;
    private boolean useMySQL;
    
    public DatabaseManager(RiseKaptan plugin) {
        this.plugin = plugin;
    }
    
    public void connect() {
        FileConfiguration config = plugin.getConfigManager().getConfig("config.yml");
        
        useMySQL = config.getBoolean("database.type", "sqlite").equalsIgnoreCase("mysql");
        
        if (useMySQL) {
            host = config.getString("database.mysql.host", "localhost");
            port = config.getInt("database.mysql.port", 3306);
            database = config.getString("database.mysql.database", "risekaptan");
            username = config.getString("database.mysql.username", "root");
            password = config.getString("database.mysql.password", "");
            
            connectMySQL();
        } else {
            connectSQLite();
        }
        
        createTables();
    }
    
    private void connectMySQL() {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            String url = "jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=false&autoReconnect=true";
            connection = DriverManager.getConnection(url, username, password);
            plugin.getLogger().info("Successfully connected to MySQL database!");
        } catch (ClassNotFoundException | SQLException e) {
            plugin.getLogger().severe("Failed to connect to MySQL database!");
            e.printStackTrace();
        }
    }
    
    private void connectSQLite() {
        try {
            Class.forName("org.sqlite.JDBC");
            File dataFolder = new File(plugin.getDataFolder(), "database.db");
            String url = "jdbc:sqlite:" + dataFolder.getAbsolutePath();
            connection = DriverManager.getConnection(url);
            plugin.getLogger().info("Successfully connected to SQLite database!");
        } catch (ClassNotFoundException | SQLException e) {
            plugin.getLogger().severe("Failed to connect to SQLite database!");
            e.printStackTrace();
        }
    }
    
    private void createTables() {
        try (Statement statement = connection.createStatement()) {
            // Player data table
            statement.executeUpdate(
                "CREATE TABLE IF NOT EXISTS player_data (" +
                "uuid VARCHAR(36) PRIMARY KEY," +
                "last_world VARCHAR(64)," +
                "last_location TEXT," +
                "last_rtp_time BIGINT," +
                "rpg_travel_completed BOOLEAN DEFAULT FALSE" +
                ")"
            );
            
            // World last locations table
            statement.executeUpdate(
                "CREATE TABLE IF NOT EXISTS world_locations (" +
                "uuid VARCHAR(36)," +
                "world VARCHAR(64)," +
                "location TEXT," +
                "PRIMARY KEY (uuid, world)" +
                ")"
            );
            
            // Galeyan areas table
            statement.executeUpdate(
                "CREATE TABLE IF NOT EXISTS galeyan_areas (" +
                "name VARCHAR(64) PRIMARY KEY," +
                "world VARCHAR(64)," +
                "target_world VARCHAR(64)," +
                "pos1 TEXT," +
                "pos2 TEXT," +
                "interval INT DEFAULT 300," +
                "pvp_protection INT DEFAULT 10" +
                ")"
            );
            
            plugin.getLogger().info("Database tables created successfully!");
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to create database tables!");
            e.printStackTrace();
        }
    }
    
    public void disconnect() {
        if (connection != null) {
            try {
                connection.close();
                plugin.getLogger().info("Database connection closed!");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    
    // Player Data Methods
    
    public void saveLastLocation(UUID uuid, String world, Location location) {
        String locString = location.getWorld().getName() + "," + 
                          location.getX() + "," + 
                          location.getY() + "," + 
                          location.getZ() + "," +
                          location.getYaw() + "," +
                          location.getPitch();
        
        try (PreparedStatement ps = connection.prepareStatement(
            "INSERT OR REPLACE INTO world_locations (uuid, world, location) VALUES (?, ?, ?)"
        )) {
            ps.setString(1, uuid.toString());
            ps.setString(2, world);
            ps.setString(3, locString);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public Location getLastLocation(UUID uuid, String world) {
        try (PreparedStatement ps = connection.prepareStatement(
            "SELECT location FROM world_locations WHERE uuid = ? AND world = ?"
        )) {
            ps.setString(1, uuid.toString());
            ps.setString(2, world);
            
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String locString = rs.getString("location");
                return parseLocation(locString);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public void saveLastRTPTime(UUID uuid, long time) {
        try (PreparedStatement ps = connection.prepareStatement(
            "INSERT OR REPLACE INTO player_data (uuid, last_rtp_time) VALUES (?, ?)"
        )) {
            ps.setString(1, uuid.toString());
            ps.setLong(2, time);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public long getLastRTPTime(UUID uuid) {
        try (PreparedStatement ps = connection.prepareStatement(
            "SELECT last_rtp_time FROM player_data WHERE uuid = ?"
        )) {
            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                return rs.getLong("last_rtp_time");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
    
    public void setRPGTravelCompleted(UUID uuid, boolean completed) {
        try (PreparedStatement ps = connection.prepareStatement(
            "INSERT OR REPLACE INTO player_data (uuid, rpg_travel_completed) VALUES (?, ?)"
        )) {
            ps.setString(1, uuid.toString());
            ps.setBoolean(2, completed);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public boolean hasCompletedRPGTravel(UUID uuid) {
        try (PreparedStatement ps = connection.prepareStatement(
            "SELECT rpg_travel_completed FROM player_data WHERE uuid = ?"
        )) {
            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                return rs.getBoolean("rpg_travel_completed");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    // Galeyan Area Methods
    
    public void saveGaleyanArea(String name, String world, String targetWorld, Location pos1, Location pos2, int interval, int pvpProtection) {
        try (PreparedStatement ps = connection.prepareStatement(
            "INSERT OR REPLACE INTO galeyan_areas (name, world, target_world, pos1, pos2, interval, pvp_protection) VALUES (?, ?, ?, ?, ?, ?, ?)"
        )) {
            ps.setString(1, name);
            ps.setString(2, world);
            ps.setString(3, targetWorld);
            ps.setString(4, serializeLoc(pos1));
            ps.setString(5, serializeLoc(pos2));
            ps.setInt(6, interval);
            ps.setInt(7, pvpProtection);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public Map<String, GaleyanAreaData> loadGaleyanAreas() {
        Map<String, GaleyanAreaData> areas = new HashMap<>();
        
        try (Statement statement = connection.createStatement()) {
            ResultSet rs = statement.executeQuery("SELECT * FROM galeyan_areas");
            
            while (rs.next()) {
                String name = rs.getString("name");
                String world = rs.getString("world");
                String targetWorld = rs.getString("target_world");
                Location pos1 = parseLocation(rs.getString("pos1"));
                Location pos2 = parseLocation(rs.getString("pos2"));
                int interval = rs.getInt("interval");
                int pvpProtection = rs.getInt("pvp_protection");
                
                areas.put(name, new GaleyanAreaData(name, world, targetWorld, pos1, pos2, interval, pvpProtection));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return areas;
    }
    
    public void deleteGaleyanArea(String name) {
        try (PreparedStatement ps = connection.prepareStatement(
            "DELETE FROM galeyan_areas WHERE name = ?"
        )) {
            ps.setString(1, name);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    // Helper methods
    
    private String serializeLoc(Location loc) {
        return loc.getWorld().getName() + "," + 
               loc.getX() + "," + 
               loc.getY() + "," + 
               loc.getZ();
    }
    
    private Location parseLocation(String locString) {
        if (locString == null) return null;
        
        String[] parts = locString.split(",");
        if (parts.length < 4) return null;
        
        org.bukkit.World world = plugin.getServer().getWorld(parts[0]);
        if (world == null) return null;
        
        try {
            double x = Double.parseDouble(parts[1]);
            double y = Double.parseDouble(parts[2]);
            double z = Double.parseDouble(parts[3]);
            
            if (parts.length == 6) {
                float yaw = Float.parseFloat(parts[4]);
                float pitch = Float.parseFloat(parts[5]);
                return new Location(world, x, y, z, yaw, pitch);
            }
            
            return new Location(world, x, y, z);
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    public Connection getConnection() {
        return connection;
    }
    
    // Data class for Galeyan areas
    public static class GaleyanAreaData {
        public final String name;
        public final String world;
        public final String targetWorld;
        public final Location pos1;
        public final Location pos2;
        public final int interval;
        public final int pvpProtection;
        
        public GaleyanAreaData(String name, String world, String targetWorld, Location pos1, Location pos2, int interval, int pvpProtection) {
            this.name = name;
            this.world = world;
            this.targetWorld = targetWorld;
            this.pos1 = pos1;
            this.pos2 = pos2;
            this.interval = interval;
            this.pvpProtection = pvpProtection;
        }
    }
}
