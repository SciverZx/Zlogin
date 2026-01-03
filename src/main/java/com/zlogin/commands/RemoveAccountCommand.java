package com.zlogin.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.zlogin.Zlogin;

public class RemoveAccountCommand implements CommandExecutor {
    
    private final Zlogin plugin;
    
    public RemoveAccountCommand(Zlogin plugin) {
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
                             "Kamu menggunakan akun premium, tidak ada akun untuk dihapus!");
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
        if (args.length < 1) {
            player.sendMessage(plugin.getConfigManager().getPrefix() + ChatColor.RED + 
                             "Gunakan: /removeaccount <password>");
            player.sendMessage(plugin.getConfigManager().getPrefix() + ChatColor.YELLOW + 
                             "PERINGATAN: Akun akan dihapus permanen!");
            return true;
        }
        
        String password = args[0];
        
        // Verify password
        if (!plugin.getAuthManager().login(player, password)) {
            player.sendMessage(plugin.getConfigManager().getPrefix() + ChatColor.RED + 
                             "Password salah!");
            return true;
        }
        
        // Remove account
        if (plugin.getAuthManager().removeAccount(player.getUniqueId())) {
            plugin.getAuthManager().removePlayer(player);
            player.sendMessage(plugin.getConfigManager().getAccountRemoved());
            player.sendMessage(plugin.getConfigManager().getPrefix() + ChatColor.YELLOW + 
                             "Kamu harus register lagi untuk bisa bermain!");
        } else {
            player.sendMessage(plugin.getConfigManager().getPrefix() + ChatColor.RED + 
                             "Gagal menghapus akun!");
        }
        
        return true;
    }
}