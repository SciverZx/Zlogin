package com.zlogin.commands;

import com.zlogin.Zlogin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class AdminChangePasswordCommand implements CommandExecutor {
    
    private final Zlogin plugin;
    
    public AdminChangePasswordCommand(Zlogin plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Check permission
        if (!sender.hasPermission("zlogin.admin")) {
            sender.sendMessage(plugin.getConfigManager().getPrefix() + ChatColor.RED + 
                             "Kamu tidak punya permission untuk command ini!");
            return true;
        }
        
        // Check argumen
        if (args.length < 2) {
            sender.sendMessage(plugin.getConfigManager().getPrefix() + ChatColor.RED + 
                             "Gunakan: /adminchangepassword <player> <password-baru>");
            return true;
        }
        
        String targetName = args[0];
        String newPassword = args[1];
        
        // Cari player
        OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);
        UUID targetUUID = target.getUniqueId();
        
        // Check kalau player ada akunnya
        if (!plugin.getDataManager().playerExists(targetUUID)) {
            sender.sendMessage(plugin.getConfigManager().getPrefix() + ChatColor.RED + 
                             "Player " + targetName + " tidak punya akun!");
            return true;
        }
        
        // Validasi password baru
        int minLength = plugin.getConfigManager().getMinPasswordLength();
        int maxLength = plugin.getConfigManager().getMaxPasswordLength();
        
        if (newPassword.length() < minLength) {
            sender.sendMessage(plugin.getConfigManager().getPrefix() + ChatColor.RED + 
                             "Password minimal " + minLength + " karakter!");
            return true;
        }
        
        if (newPassword.length() > maxLength) {
            sender.sendMessage(plugin.getConfigManager().getPrefix() + ChatColor.RED + 
                             "Password maksimal " + maxLength + " karakter!");
            return true;
        }
        
        // Change password
        if (plugin.getAuthManager().changePassword(targetUUID, newPassword)) {
            sender.sendMessage(plugin.getConfigManager().getPrefix() + ChatColor.GREEN + 
                             "Berhasil mengubah password untuk " + targetName + "!");
            
            // Notify player kalau online
            Player onlineTarget = Bukkit.getPlayer(targetUUID);
            if (onlineTarget != null && onlineTarget.isOnline()) {
                onlineTarget.sendMessage(plugin.getConfigManager().getPrefix() + ChatColor.YELLOW + 
                                       "Password kamu telah diubah oleh admin!");
                // Kick player untuk re-login dengan password baru
                plugin.getAuthManager().setLoggedIn(onlineTarget, false);
                onlineTarget.kickPlayer(ChatColor.YELLOW + "Password kamu telah diubah oleh admin!\n" +
                                      ChatColor.WHITE + "Silakan login kembali dengan password baru.");
            }
        } else {
            sender.sendMessage(plugin.getConfigManager().getPrefix() + ChatColor.RED + 
                             "Gagal mengubah password!");
        }
        
        return true;
    }
}