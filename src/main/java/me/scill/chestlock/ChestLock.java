package me.scill.chestlock;

import lombok.Getter;
import me.scill.chestlock.commands.ReloadCommand;
import me.scill.chestlock.data.LockData;
import me.scill.chestlock.listeners.LockListener;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class ChestLock extends JavaPlugin {

	@Getter
	private LockData lockData = new LockData(this);

	@Override
	public void onEnable() {
		// Data
		saveDefaultConfig();
		lockData.loadLocks();

		// Listeners
		getServer().getPluginManager().registerEvents(new LockListener(this), this);

		// Commands
		getCommand("chestlock").setExecutor(new ReloadCommand(this));
	}
}