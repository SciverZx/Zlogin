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

public class AdminRemoveAccountCommand implements CommandExecutor {
    
    private final Zlogin plugin;
    
    public AdminRemoveAccountCommand(Zlogin plugin) {
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
        if (args.length < 1) {
            sender.sendMessage(plugin.getConfigManager().getPrefix() + ChatColor.RED + 
                             "Gunakan: /adminremoveaccount <player>");
            sender.sendMessage(plugin.getConfigManager().getPrefix() + ChatColor.YELLOW + 
                             "PERINGATAN: Akun akan dihapus permanen!");
            return true;
        }
        
        String targetName = args[0];
        
        // Cari player
        OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);
        UUID targetUUID = target.getUniqueId();
        
        // Check kalau player ada akunnya
        if (!plugin.getDataManager().playerExists(targetUUID)) {
            sender.sendMessage(plugin.getConfigManager().getPrefix() + ChatColor.RED + 
                             "Player " + targetName + " tidak punya akun!");
            return true;
        }
        
        // Remove account
        if (plugin.getAuthManager().removeAccount(targetUUID)) {
            sender.sendMessage(plugin.getConfigManager().getPrefix() + ChatColor.GREEN + 
                             "Berhasil menghapus akun " + targetName + "!");
            
            // Notify player kalau online dan kick
            Player onlineTarget = Bukkit.getPlayer(targetUUID);
            if (onlineTarget != null && onlineTarget.isOnline()) {
                plugin.getAuthManager().removePlayer(onlineTarget);
                onlineTarget.kickPlayer(ChatColor.RED + "Akun kamu telah dihapus oleh admin!\n" +
                                      ChatColor.WHITE + "Silakan register kembali untuk bermain.");
            }
        } else {
            sender.sendMessage(plugin.getConfigManager().getPrefix() + ChatColor.RED + 
                             "Gagal menghapus akun!");
        }
        
        return true;
    }
}