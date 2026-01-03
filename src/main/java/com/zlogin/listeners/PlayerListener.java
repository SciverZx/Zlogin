package com.zlogin.listeners;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import com.zlogin.Zlogin;

public class PlayerListener implements Listener {
    
    private final Zlogin plugin;
    
    public PlayerListener(Zlogin plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String currentIP = player.getAddress().getAddress().getHostAddress();

        // 1. Check Bedrock (instant)
        if (plugin.getConfigManager().isAutoLoginBedrock() && player.getName().startsWith(".")) {
            autoLoginSuccess(player, "Bedrock");
            return;
        }

        // 2. Check IP Auto-Login
        if (plugin.getAuthManager().hasAccount(player) && plugin.getConfigManager().isAutoLoginPremium()) {
            if (plugin.getAuthManager().verifyIP(player, currentIP)) {
                autoLoginSuccess(player, "IP Match");
                return;
            }
        }

        // Set player belum login
        plugin.getAuthManager().setLoggedIn(player, false);
        applyRestrictions(player);
        startLoginTimer(player);

        // 3. Check Premium (seperti kodemu sebelumnya)
        // if (plugin.getConfigManager().isAutoLoginPremium()) {
        //     plugin.getMojangAPI().checkPremiumAsync(player.getName(), player.getUniqueId(), isPremium -> {
        //         if (isPremium && player.isOnline()) {
        //             autoLoginSuccess(player, "Premium");
        //         } else if (player.isOnline()) {
        //             startLoginTimer(player);
        //         }
        //     });
        // } else {
        //     startLoginTimer(player);
        // }
    }

    // Helper method biar rapi
    private void autoLoginSuccess(Player player, String reason) {
        plugin.getAuthManager().setLoggedIn(player, true);
        removeRestrictions(player);
        player.sendMessage(plugin.getConfigManager().getPrefix() + "§aOtomatis login via " + reason + "!");
    }
    
    private void startLoginTimer(Player player) {
        final int maxTime = plugin.getConfigManager().getMaxLoginTime();
        
        new BukkitRunnable() {
            int count = 0;
            
            @Override
            public void run() {
                if (plugin.getAuthManager().isLoggedIn(player) || !player.isOnline()) {
                    // Remove restrictions ketika sudah login
                    removeRestrictions(player);
                    cancel();
                    return;
                }
                
                if (plugin.getAuthManager().hasAccount(player)) {
                    player.sendTitle(plugin.getConfigManager().getLoginRequiredTitle(), 
                                   plugin.getConfigManager().getLoginRequiredSubtitle(), 
                                   10, 70, 20);
                } else {
                    player.sendTitle(plugin.getConfigManager().getRegisterRequiredTitle(), 
                                   plugin.getConfigManager().getRegisterRequiredSubtitle(), 
                                   10, 70, 20);
                }
                
                applyRestrictions(player);
                
                count++;
                if (count >= maxTime) {
                    player.kickPlayer(plugin.getConfigManager().getTimeoutKick());
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 0L, 20L); // Setiap 1 detik
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        plugin.getAuthManager().removePlayer(event.getPlayer());
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        
        if (!plugin.getAuthManager().isLoggedIn(player)) {
            
            // Cek kalau player jatuh atau di udara
            if (plugin.getConfigManager().isTeleportToGroundEnabled() && 
                !player.isOnGround() && player.getVelocity().getY() < -0.5) {
                teleportToGround(player);
                return;
            }
            
            // Cancel movement
            Location from = event.getFrom();
            Location to = event.getTo();
            
            if (to != null && (from.getX() != to.getX() || from.getZ() != to.getZ())) {
                event.setCancelled(true);
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        
        if (!plugin.getAuthManager().isLoggedIn(player)) {
            event.setCancelled(true);
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (event.getPlayer() instanceof Player) {
            Player player = (Player) event.getPlayer();
            
            if (!plugin.getAuthManager().isLoggedIn(player)) {
                event.setCancelled(true);
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            
            if (plugin.getConfigManager().isPreventDamageEnabled() &&
                !plugin.getAuthManager().isLoggedIn(player)) {
                event.setCancelled(true);
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityTarget(EntityTargetEvent event) {
        if (event.getTarget() instanceof Player) {
            Player player = (Player) event.getTarget();
            
            if (plugin.getConfigManager().isPreventMobTargetEnabled() &&
                !plugin.getAuthManager().isLoggedIn(player)) {
                event.setCancelled(true);
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        
        if (!plugin.getAuthManager().isLoggedIn(player)) {
            event.setCancelled(true);
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerPickupItem(PlayerPickupItemEvent event) {
        Player player = event.getPlayer();
        
        if (!plugin.getAuthManager().isLoggedIn(player)) {
            event.setCancelled(true);
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        
        if (!plugin.getAuthManager().isLoggedIn(player)) {
            event.setCancelled(true);
            player.sendMessage(plugin.getConfigManager().getPrefix() + ChatColor.RED + 
                             "Kamu harus login/register terlebih dahulu!");
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        String command = event.getMessage().toLowerCase();
        
        if (!plugin.getAuthManager().isLoggedIn(player)) {
            
            // Izinkan command login dan register aja
            if (!command.startsWith("/login") && !command.startsWith("/log") &&
                !command.startsWith("/register") && !command.startsWith("/reg")) {
                event.setCancelled(true);
                player.sendMessage(plugin.getConfigManager().getPrefix() + ChatColor.RED + 
                                 "Kamu harus login/register terlebih dahulu!");
            }
        }
    }
    
    private void applyRestrictions(Player player) {
        // Tambah blindness effect kalau enabled
        if (plugin.getConfigManager().isBlindnessEnabled()) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 999999, 1, false, false));
        }
        
        // Set gamemode ke adventure biar gabisa break block
        if (player.getGameMode() != GameMode.ADVENTURE) {
            player.setGameMode(GameMode.ADVENTURE);
        }
    }
    
    private void removeRestrictions(Player player) {
        // Remove blindness effect
        player.removePotionEffect(PotionEffectType.BLINDNESS);
        
        // Reset gamemode ke survival (atau bisa disesuaikan)
        if (player.getGameMode() == GameMode.ADVENTURE) {
            player.setGameMode(GameMode.SURVIVAL);
        }
    }
    
    private void teleportToGround(Player player) {
        Location loc = player.getLocation();
        
        // Cari ground di bawah player
        for (int y = loc.getBlockY(); y > player.getWorld().getMinHeight(); y--) {
            Location checkLoc = new Location(loc.getWorld(), loc.getX(), y, loc.getZ());
            Material blockType = checkLoc.getBlock().getType();
            
            if (blockType.isSolid() && blockType != Material.AIR) {
                Location safeLoc = checkLoc.clone().add(0, 1, 0);
                player.teleport(safeLoc);
                break;
            }
        }
    }
}