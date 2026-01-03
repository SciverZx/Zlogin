package com.zlogin;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import com.zlogin.commands.*;
import com.zlogin.listeners.PlayerListener;
import com.zlogin.managers.AuthManager;
import com.zlogin.managers.DataManager;
import com.zlogin.managers.ConfigManager;
import com.zlogin.managers.MojangAPI;

import java.io.File;

public class Zlogin extends JavaPlugin {
    
    private static Zlogin instance;
    private AuthManager authManager;
    private DataManager dataManager;
    private ConfigManager configManager;
    private MojangAPI mojangAPI;
    
    @Override
    public void onEnable() {
        instance = this;
        
        // Buat folder data kalau belum ada
        File dataFolder = new File(getDataFolder(), "data");
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
        
        // Save default config
        saveDefaultConfig();
        
        // Initialize managers
        this.configManager = new ConfigManager(this);
        this.dataManager = new DataManager(this);
        this.authManager = new AuthManager(this);
        this.mojangAPI = new MojangAPI(this);
        
        // Register commands
        getCommand("register").setExecutor(new RegisterCommand(this));
        getCommand("login").setExecutor(new LoginCommand(this));
        getCommand("changepassword").setExecutor(new ChangePasswordCommand(this));
        getCommand("removeaccount").setExecutor(new RemoveAccountCommand(this));
        getCommand("adminchangepassword").setExecutor(new AdminChangePasswordCommand(this));
        getCommand("adminremoveaccount").setExecutor(new AdminRemoveAccountCommand(this));
        getCommand("autologin").setExecutor(new AutoLoginCommand(this));
        
        // Register listeners
        Bukkit.getPluginManager().registerEvents(new PlayerListener(this), this);
        
        getLogger().info("Zlogin has been enabled!");
        getLogger().info("Compatible with Minecraft 1.21 - 1.21.11+");
    }
    
    @Override
    public void onDisable() {
        getLogger().info("Zlogin has been disabled!");
    }
    
    public static Zlogin getInstance() {
        return instance;
    }
    
    public AuthManager getAuthManager() {
        return authManager;
    }
    
    public DataManager getDataManager() {
        return dataManager;
    }
    
    public ConfigManager getConfigManager() {
        return configManager;
    }
    
    public MojangAPI getMojangAPI() {
        return mojangAPI;
    }
}