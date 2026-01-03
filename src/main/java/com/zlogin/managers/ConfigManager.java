package com.zlogin.managers;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;

import com.zlogin.Zlogin;

public class ConfigManager {
    
    private final Zlogin plugin;
    private FileConfiguration config;
    
    public ConfigManager(Zlogin plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
    }
    
    public void reloadConfig() {
        plugin.reloadConfig();
        this.config = plugin.getConfig();
    }
    
    // Login Settings
    public int getMaxLoginAttempts() {
        return config.getInt("login.max-attempts", 3);
    }
    
    public int getMaxLoginTime() {
        return config.getInt("login.max-time", 60);
    }
    
    public int getResetAttemptsAfter() {
        return config.getInt("login.reset-attempts-after", 300);
    }
    
    // Password Settings
    public int getMinPasswordLength() {
        return config.getInt("password.min-length", 4);
    }
    
    public int getMaxPasswordLength() {
        return config.getInt("password.max-length", 32);
    }
    
    // Premium Settings
    public boolean isAutoLoginPremium() {
        return config.getBoolean("premium.auto-login-premium", true);
    }
    
    public boolean isAutoLoginBedrock() {
        return config.getBoolean("premium.auto-login-bedrock", true);
    }
    
    // Protection Settings
    public boolean isBlindnessEnabled() {
        return config.getBoolean("protection.blindness", true);
    }
    
    public boolean isTeleportToGroundEnabled() {
        return config.getBoolean("protection.teleport-to-ground", true);
    }
    
    public boolean isPreventDamageEnabled() {
        return config.getBoolean("protection.prevent-damage", true);
    }
    
    public boolean isPreventMobTargetEnabled() {
        return config.getBoolean("protection.prevent-mob-target", true);
    }
    
    // Messages
    public String getMessage(String path) {
        String message = config.getString("messages." + path, "");
        return ChatColor.translateAlternateColorCodes('&', message);
    }
    
    public String getPrefix() {
        return getMessage("prefix");
    }
    
    public String getRegisterRequiredTitle() {
        return getMessage("register-required-title");
    }
    
    public String getRegisterRequiredSubtitle() {
        return getMessage("register-required-subtitle");
    }
    
    public String getLoginRequiredTitle() {
        return getMessage("login-required-title");
    }
    
    public String getLoginRequiredSubtitle() {
        return getMessage("login-required-subtitle");
    }
    
    public String getRegisterSuccess() {
        return getPrefix() + getMessage("register-success");
    }
    
    public String getLoginSuccess() {
        return getPrefix() + getMessage("login-success");
    }
    
    public String getWrongPassword(int attemptsLeft) {
        return getPrefix() + getMessage("wrong-password").replace("{attempts}", String.valueOf(attemptsLeft));
    }
    
    public String getMaxAttemptsReached() {
        return getPrefix() + getMessage("max-attempts-reached");
    }
    
    public String getTimeoutKick() {
        return getMessage("timeout-kick");
    }
    
    public String getPasswordChanged() {
        return getPrefix() + getMessage("password-changed");
    }
    
    public String getAccountRemoved() {
        return getPrefix() + getMessage("account-removed");
    }
    
    public String getPremiumAutoLogin() {
        return getPrefix() + getMessage("premium-auto-login");
    }
}