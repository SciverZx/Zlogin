package com.zlogin.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.zlogin.Zlogin;

public class AutoLoginCommand implements CommandExecutor {
    private final Zlogin plugin;

    public AutoLoginCommand(Zlogin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) return true;
        Player player = (Player) sender;

        if (!plugin.getAuthManager().isLoggedIn(player)) {
            player.sendMessage("§cKamu harus login dulu sebelum mengaktifkan autologin!");
            return true;
        }

        String ip = player.getAddress().getAddress().getHostAddress();
        String hashed = plugin.getAuthManager().hashIP(ip);
        
        plugin.getDataManager().saveIPHash(player.getUniqueId(), hashed);
        player.sendMessage("§a[Zlogin] IP kamu telah didaftarkan. Masuk selanjutnya akan otomatis!");
        
        return true;
    }
}