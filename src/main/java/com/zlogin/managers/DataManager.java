package com.zlogin.managers;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import org.bukkit.configuration.file.YamlConfiguration;

import com.zlogin.Zlogin;

public class DataManager {
    
    private final Zlogin plugin;
    private final File dataFolder;
    
    public DataManager(Zlogin plugin) {
        this.plugin = plugin;
        this.dataFolder = new File(plugin.getDataFolder(), "data");
    }
    
    public boolean playerExists(UUID uuid) {
        File playerFile = new File(dataFolder, uuid.toString() + ".yml");
        return playerFile.exists();
    }
    
    public boolean savePlayer(UUID uuid, String name, String hashedPassword) {
        File playerFile = new File(dataFolder, uuid.toString() + ".yml");
        YamlConfiguration config = YamlConfiguration.loadConfiguration(playerFile);
        
        config.set("uuid", uuid.toString());
        config.set("name", name);
        config.set("password", hashedPassword);
        config.set("registered", System.currentTimeMillis());
        
        try {
            config.save(playerFile);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void saveIPHash(UUID uuid, String hashedIP) {
        File playerFile = new File(dataFolder, uuid.toString() + ".yml");
        YamlConfiguration config = YamlConfiguration.loadConfiguration(playerFile);
        
        config.set("last-ip-hash", hashedIP);
        try {
            config.save(playerFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getStoredIPHash(UUID uuid) {
        File playerFile = new File(dataFolder, uuid.toString() + ".yml");
        if (!playerFile.exists()) return null;
        
        YamlConfiguration config = YamlConfiguration.loadConfiguration(playerFile);
        return config.getString("last-ip-hash");
    }
    
    public boolean verifyPassword(UUID uuid, String hashedPassword) {
        File playerFile = new File(dataFolder, uuid.toString() + ".yml");
        if (!playerFile.exists()) {
            return false;
        }
        
        YamlConfiguration config = YamlConfiguration.loadConfiguration(playerFile);
        String storedPassword = config.getString("password");
        
        return storedPassword != null && storedPassword.equals(hashedPassword);
    }
    
    public String getPlayerName(UUID uuid) {
        File playerFile = new File(dataFolder, uuid.toString() + ".yml");
        if (!playerFile.exists()) {
            return null;
        }
        
        YamlConfiguration config = YamlConfiguration.loadConfiguration(playerFile);
        return config.getString("name");
    }
    
    public boolean updatePassword(UUID uuid, String hashedPassword) {
        File playerFile = new File(dataFolder, uuid.toString() + ".yml");
        if (!playerFile.exists()) {
            return false;
        }
        
        YamlConfiguration config = YamlConfiguration.loadConfiguration(playerFile);
        config.set("password", hashedPassword);
        config.set("last-password-change", System.currentTimeMillis());
        
        try {
            config.save(playerFile);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean deletePlayer(UUID uuid) {
        File playerFile = new File(dataFolder, uuid.toString() + ".yml");
        if (!playerFile.exists()) {
            return false;
        }
        
        return playerFile.delete();
    }
}