package com.zlogin.commands;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import com.zlogin.Zlogin;

public class LoginCommand implements CommandExecutor {
    
    private final Zlogin plugin;
    
    public LoginCommand(Zlogin plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Command ini hanya bisa digunakan oleh player!");
            return true;
        }
        
        Player player = (Player) sender;
        
        // Check kalau player premium
        if (plugin.getAuthManager().isPremiumPlayer(player)) {
            player.sendMessage(ChatColor.YELLOW + "Kamu menggunakan akun premium, tidak perlu login!");
            return true;
        }
        
        // Check kalau belum punya akun
        if (!plugin.getAuthManager().hasAccount(player)) {
            player.sendMessage(plugin.getConfigManager().getPrefix() + ChatColor.RED + 
                             "Kamu belum punya akun! Gunakan /register <password> <konfirmasi-password>");
            return true;
        }
        
        // Check kalau sudah login
        if (plugin.getAuthManager().isLoggedIn(player)) {
            player.sendMessage(plugin.getConfigManager().getPrefix() + ChatColor.RED + "Kamu sudah login!");
            return true;
        }
        
        // Check kalau sudah max attempts
        if (plugin.getAuthManager().hasReachedMaxAttempts(player)) {
            player.kickPlayer(plugin.getConfigManager().getMaxAttemptsReached());
            return true;
        }
        
        // Check argumen
        if (args.length < 1) {
            player.sendMessage(plugin.getConfigManager().getPrefix() + ChatColor.RED + "Gunakan: /login <password>");
            return true;
        }
        
        String password = args[0];
        
        // Verify password
        if (plugin.getAuthManager().login(player, password)) {
            plugin.getAuthManager().setLoggedIn(player, true);
            
            // Remove restrictions
            player.removePotionEffect(PotionEffectType.BLINDNESS);
            if (player.getGameMode() == GameMode.ADVENTURE) {
                player.setGameMode(GameMode.SURVIVAL);
            }
            
            player.sendMessage(plugin.getConfigManager().getLoginSuccess());
            player.sendTitle(ChatColor.GREEN + "Berhasil Login!", 
                           ChatColor.YELLOW + "Selamat datang kembali " + player.getName(), 
                           10, 70, 20);
        } else {
            plugin.getAuthManager().incrementLoginAttempts(player);
            int remaining = plugin.getAuthManager().getRemainingAttempts(player);
            
            if (remaining <= 0) {
                player.kickPlayer(plugin.getConfigManager().getMaxAttemptsReached());
            } else {
                player.sendMessage(plugin.getConfigManager().getWrongPassword(remaining));
            }
        }
        
        return true;
    }
}