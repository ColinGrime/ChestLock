package me.scill.chestlock.commands;

import me.scill.chestlock.ChestLock;
import me.scill.chestlock.utilities.Utility;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class ReloadCommand implements CommandExecutor {
	
	private final ChestLock plugin;
	
	public ReloadCommand(final ChestLock plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (cmd.getName().equalsIgnoreCase("chestlock")) {
			if (sender.hasPermission("chestlock.reload")) {
				plugin.reloadConfig();
				sender.sendMessage(Utility.color(plugin.getConfig().getString("admin.reload")));
			} else
				sender.sendMessage(Utility.color(plugin.getConfig().getString("errors.insufficient-permissions")));
			return true;
		}
		return false;
	}
}
