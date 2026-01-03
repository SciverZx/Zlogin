package com.zlogin.commands;

import com.zlogin.Zlogin;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ChangePasswordCommand implements CommandExecutor {
    
    private final Zlogin plugin;
    
    public ChangePasswordCommand(Zlogin plugin) {
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
            player.sendMessage(plugin.getConfigManager().getPrefix() + ChatColor.YELLOW + 
                             "Kamu menggunakan akun premium, tidak perlu changepassword!");
            return true;
        }
        
        // Check kalau belum punya akun
        if (!plugin.getAuthManager().hasAccount(player)) {
            player.sendMessage(plugin.getConfigManager().getPrefix() + ChatColor.RED + 
                             "Kamu belum punya akun!");
            return true;
        }
        
        // Check kalau belum login
        if (!plugin.getAuthManager().isLoggedIn(player)) {
            player.sendMessage(plugin.getConfigManager().getPrefix() + ChatColor.RED + 
                             "Kamu harus login terlebih dahulu!");
            return true;
        }
        
        // Check argumen
        if (args.length < 3) {
            player.sendMessage(plugin.getConfigManager().getPrefix() + ChatColor.RED + 
                             "Gunakan: /changepassword <password-lama> <password-baru> <konfirmasi-password-baru>");
            return true;
        }
        
        String oldPassword = args[0];
        String newPassword = args[1];
        String confirmPassword = args[2];
        
        // Verify old password
        if (!plugin.getAuthManager().login(player, oldPassword)) {
            player.sendMessage(plugin.getConfigManager().getPrefix() + ChatColor.RED + 
                             "Password lama salah!");
            return true;
        }
        
        // Validasi password baru
        int minLength = plugin.getConfigManager().getMinPasswordLength();
        int maxLength = plugin.getConfigManager().getMaxPasswordLength();
        
        if (newPassword.length() < minLength) {
            player.sendMessage(plugin.getConfigManager().getPrefix() + ChatColor.RED + 
                             "Password minimal " + minLength + " karakter!");
            return true;
        }
        
        if (newPassword.length() > maxLength) {
            player.sendMessage(plugin.getConfigManager().getPrefix() + ChatColor.RED + 
                             "Password maksimal " + maxLength + " karakter!");
            return true;
        }
        
        if (!newPassword.equals(confirmPassword)) {
            player.sendMessage(plugin.getConfigManager().getPrefix() + ChatColor.RED + 
                             "Password baru tidak cocok!");
            return true;
        }
        
        if (oldPassword.equals(newPassword)) {
            player.sendMessage(plugin.getConfigManager().getPrefix() + ChatColor.RED + 
                             "Password baru tidak boleh sama dengan password lama!");
            return true;
        }
        
        // Change password
        if (plugin.getAuthManager().changePassword(player.getUniqueId(), newPassword)) {
            player.sendMessage(plugin.getConfigManager().getPasswordChanged());
        } else {
            player.sendMessage(plugin.getConfigManager().getPrefix() + ChatColor.RED + 
                             "Gagal mengubah password!");
        }
        
        return true;
    }
}