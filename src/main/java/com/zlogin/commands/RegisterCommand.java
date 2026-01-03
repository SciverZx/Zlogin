package com.zlogin.commands;

import com.zlogin.Zlogin;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

public class RegisterCommand implements CommandExecutor {
    
    private final Zlogin plugin;
    
    public RegisterCommand(Zlogin plugin) {
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
            player.sendMessage(ChatColor.YELLOW + "Kamu menggunakan akun premium, tidak perlu register!");
            return true;
        }
        
        // Check kalau sudah punya akun
        if (plugin.getAuthManager().hasAccount(player)) {
            player.sendMessage(plugin.getConfigManager().getPrefix() + ChatColor.RED + 
                             "Kamu sudah punya akun! Gunakan /login <password>");
            return true;
        }
        
        // Check kalau sudah login
        if (plugin.getAuthManager().isLoggedIn(player)) {
            player.sendMessage(plugin.getConfigManager().getPrefix() + ChatColor.RED + "Kamu sudah login!");
            return true;
        }
        
        // Check argumen
        if (args.length < 2) {
            player.sendMessage(plugin.getConfigManager().getPrefix() + ChatColor.RED + 
                             "Gunakan: /register <password> <konfirmasi-password>");
            return true;
        }
        
        String password = args[0];
        String confirmPassword = args[1];
        
        // Validasi password length
        int minLength = plugin.getConfigManager().getMinPasswordLength();
        int maxLength = plugin.getConfigManager().getMaxPasswordLength();
        
        if (password.length() < minLength) {
            player.sendMessage(plugin.getConfigManager().getPrefix() + ChatColor.RED + 
                             "Password minimal " + minLength + " karakter!");
            return true;
        }
        
        if (password.length() > maxLength) {
            player.sendMessage(plugin.getConfigManager().getPrefix() + ChatColor.RED + 
                             "Password maksimal " + maxLength + " karakter!");
            return true;
        }
        
        if (!password.equals(confirmPassword)) {
            player.sendMessage(plugin.getConfigManager().getPrefix() + ChatColor.RED + "Password tidak cocok!");
            return true;
        }
        
        // Register player
        if (plugin.getAuthManager().register(player, password)) {
            plugin.getAuthManager().setLoggedIn(player, true);
            
            // Remove restrictions
            player.removePotionEffect(PotionEffectType.BLINDNESS);
            if (player.getGameMode() == GameMode.ADVENTURE) {
                player.setGameMode(GameMode.SURVIVAL);
            }
            
            player.sendMessage(plugin.getConfigManager().getRegisterSuccess());
            player.sendTitle(ChatColor.GREEN + "Berhasil Register!", 
                           ChatColor.YELLOW + "Selamat datang " + player.getName(), 
                           10, 70, 20);
        } else {
            player.sendMessage(plugin.getConfigManager().getPrefix() + ChatColor.RED + 
                             "Gagal register! Coba lagi nanti.");
        }
        
        return true;
    }
}