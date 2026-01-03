package com.zlogin.managers;

import org.bukkit.entity.Player;
import com.zlogin.Zlogin;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AuthManager {
    
    private final Zlogin plugin;
    private final Map<UUID, Boolean> loggedInPlayers;
    private final Map<UUID, Integer> loginAttempts;
    private final Map<UUID, Long> lastAttemptTime;
    
    public AuthManager(Zlogin plugin) {
        this.plugin = plugin;
        this.loggedInPlayers = new HashMap<>();
        this.loginAttempts = new HashMap<>();
        this.lastAttemptTime = new HashMap<>();
    }
    
    public boolean isLoggedIn(Player player) {
        return loggedInPlayers.getOrDefault(player.getUniqueId(), false);
    }
    
    public void setLoggedIn(Player player, boolean status) {
        loggedInPlayers.put(player.getUniqueId(), status);
        if (status) {
            // Reset attempts kalau berhasil login
            loginAttempts.remove(player.getUniqueId());
            lastAttemptTime.remove(player.getUniqueId());
        }
    }
    
    public void removePlayer(Player player) {
        loggedInPlayers.remove(player.getUniqueId());
        loginAttempts.remove(player.getUniqueId());
        lastAttemptTime.remove(player.getUniqueId());
    }
    
    public boolean hasAccount(Player player) {
        return plugin.getDataManager().playerExists(player.getUniqueId());
    }
    
    public boolean register(Player player, String password) {
        String hashedPassword = hashPassword(password);
        return plugin.getDataManager().savePlayer(player.getUniqueId(), player.getName(), hashedPassword);
    }
    
    public boolean login(Player player, String password) {
        String hashedPassword = hashPassword(password);
        return plugin.getDataManager().verifyPassword(player.getUniqueId(), hashedPassword);
    }
    
    public boolean changePassword(UUID uuid, String newPassword) {
        String hashedPassword = hashPassword(newPassword);
        return plugin.getDataManager().updatePassword(uuid, hashedPassword);
    }
    
    public boolean removeAccount(UUID uuid) {
        return plugin.getDataManager().deletePlayer(uuid);
    }
    
    public int getLoginAttempts(Player player) {
        // Reset attempts kalau sudah lewat waktu reset
        long currentTime = System.currentTimeMillis();
        Long lastTime = lastAttemptTime.get(player.getUniqueId());
        
        if (lastTime != null) {
            long timeDiff = (currentTime - lastTime) / 1000; // dalam detik
            if (timeDiff > plugin.getConfigManager().getResetAttemptsAfter()) {
                loginAttempts.remove(player.getUniqueId());
                lastAttemptTime.remove(player.getUniqueId());
                return 0;
            }
        }
        
        return loginAttempts.getOrDefault(player.getUniqueId(), 0);
    }
    
    public void incrementLoginAttempts(Player player) {
        int attempts = getLoginAttempts(player) + 1;
        loginAttempts.put(player.getUniqueId(), attempts);
        lastAttemptTime.put(player.getUniqueId(), System.currentTimeMillis());
    }
    
    public boolean hasReachedMaxAttempts(Player player) {
        return getLoginAttempts(player) >= plugin.getConfigManager().getMaxLoginAttempts();
    }
    
    public int getRemainingAttempts(Player player) {
        return plugin.getConfigManager().getMaxLoginAttempts() - getLoginAttempts(player);
    }
    
    public String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }


    public String hashIP(String ipAddress) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(ipAddress.getBytes());
            StringBuilder hexString = new StringBuilder();
            
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean verifyIP(Player player, String currentIP) {
        String storedHash = plugin.getDataManager().getStoredIPHash(player.getUniqueId());
        if (storedHash == null) return false;
        
        return storedHash.equals(hashIP(currentIP));
    }
    
    public boolean isPremiumPlayer(Player player) {
        // Check kalau player pake akun premium (online mode)
        boolean isPremium = plugin.getConfigManager().isAutoLoginPremium() && 
                           player.getServer().getOnlineMode();
        
        // Check kalau player bedrock (biasanya nama dimulai dengan .)
        boolean isBedrock = plugin.getConfigManager().isAutoLoginBedrock() && 
                           player.getName().startsWith(".");
        
        return isPremium || isBedrock;
    }
}