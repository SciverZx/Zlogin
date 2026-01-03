package com.zlogin.managers;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.zlogin.Zlogin;

public class MojangAPI {
    
    private final Zlogin plugin;
    private final Map<String, CachedResult> cache;
    private static final long CACHE_DURATION = 300000; // 5 menit
    
    public MojangAPI(Zlogin plugin) {
        this.plugin = plugin;
        this.cache = new HashMap<>();
    }
    
    /**
     * Check apakah player premium secara async
     */
    public void checkPremiumAsync(String playerName, UUID playerUUID, PremiumCheckCallback callback) {
        // plugin.getLogger().info("[Zlogin] Checking premium status for: " + playerName);
        
        // Check cache dulu
        CachedResult cached = cache.get(playerName.toLowerCase());
        if (cached != null && !cached.isExpired()) {
            // plugin.getLogger().info("[Zlogin] Using cached result for " + playerName + ": " + (cached.isPremium ? "PREMIUM" : "CRACKED"));
            callback.onResult(cached.isPremium);
            return;
        }
        
        // Check async biar gak lag
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            boolean isPremium = checkPremium(playerName, playerUUID);
            
            // Cache hasil
            cache.put(playerName.toLowerCase(), new CachedResult(isPremium));
            
            // plugin.getLogger().info("[Zlogin] Result for " + playerName + ": " + (isPremium ? "PREMIUM" : "CRACKED"));
            
            // Callback di main thread
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                callback.onResult(isPremium);
            });
        });
    }
    
    /**
     * Check apakah player premium (blocking)
     */
    private boolean checkPremium(String playerName, UUID playerUUID) {
        try {
            // plugin.getLogger().info("[Zlogin] Querying Mojang API for: " + playerName);
            // plugin.getLogger().info("[Zlogin] Player UUID: " + playerUUID.toString());
            
            // Query Mojang API
            String urlString = "https://api.mojang.com/users/profiles/minecraft/" + playerName;
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            
            int responseCode = connection.getResponseCode();
            // plugin.getLogger().info("[Zlogin] API Response Code: " + responseCode);
            
            // 204 = Player tidak ada (cracked)
            if (responseCode == 204) {
                // plugin.getLogger().info("[Zlogin] " + playerName + " is CRACKED (204 - Not Found)");
                return false;
            }
            
            // 200 = Player ada (premium)
            if (responseCode == 200) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String inputLine;
                
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                
                // plugin.getLogger().info("[Zlogin] API Response: " + response.toString());
                
                // Parse JSON response
                JsonObject jsonObject = JsonParser.parseString(response.toString()).getAsJsonObject();
                String mojangUUIDString = jsonObject.get("id").getAsString();
                
                // Format UUID dari Mojang (tanpa dash)
                String formattedUUID = mojangUUIDString.replaceFirst(
                    "(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})", 
                    "$1-$2-$3-$4-$5"
                );
                
                UUID mojangUUID = UUID.fromString(formattedUUID);
                
                // plugin.getLogger().info("[Zlogin] Mojang UUID: " + mojangUUID.toString());
                // plugin.getLogger().info("[Zlogin] Player UUID: " + playerUUID.toString());
                
                // Bandingkan UUID
                boolean matches = playerUUID.equals(mojangUUID);
                // plugin.getLogger().info("[Zlogin] UUID Match: " + matches);
                
                // if (matches) {
                //     plugin.getLogger().info("[Zlogin] " + playerName + " is PREMIUM (UUID verified)");
                // } else {
                //     plugin.getLogger().info("[Zlogin] " + playerName + " is CRACKED (UUID mismatch)");
                // }
                
                return matches;
            }
            
            // Rate limit atau error lain
            // plugin.getLogger().warning("[Zlogin] Unexpected response code: " + responseCode);
            return false;
            
        } catch (Exception e) {
            plugin.getLogger().warning("[Zlogin] Error checking premium for " + playerName + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Clear cache untuk player tertentu
     */
    public void clearCache(String playerName) {
        cache.remove(playerName.toLowerCase());
    }
    
    /**
     * Clear semua cache
     */
    public void clearAllCache() {
        cache.clear();
    }
    
    /**
     * Callback interface
     */
    public interface PremiumCheckCallback {
        void onResult(boolean isPremium);
    }
    
    /**
     * Cache result class
     */
    private static class CachedResult {
        private final boolean isPremium;
        private final long timestamp;
        
        public CachedResult(boolean isPremium) {
            this.isPremium = isPremium;
            this.timestamp = System.currentTimeMillis();
        }
        
        public boolean isExpired() {
            return System.currentTimeMillis() - timestamp > CACHE_DURATION;
        }
    }
}