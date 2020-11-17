package me.scill.chestlock.data;

import lombok.Getter;
import me.scill.chestlock.ChestLock;
import me.scill.chestlock.utilities.Utility;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.HashMap;
import java.util.UUID;

public class LockData {

	@Getter
	private final HashMap<Location, UUID> locks = new HashMap<>();
	private final File configFile;
	private final FileConfiguration config;

	public LockData(final ChestLock plugin) {
		configFile = new File(plugin.getDataFolder(), "locks.yml");
		config = YamlConfiguration.loadConfiguration(configFile);
	}

	private void saveConfig() {
		try {
			config.save(configFile);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * Loads all the locks into the server.
	 */
	public void loadLocks() {
		for (String lockLocation : config.getConfigurationSection("").getKeys(false)) {
			final Location potentialLock = Utility.stringToLocation(lockLocation);
			if (potentialLock.getBlock().getType().name().contains("SIGN"))
				locks.put(potentialLock, UUID.fromString(config.getString(lockLocation)));
			else
				config.set(lockLocation, null);
		}
		saveConfig();
	}

	/**
	 * Adds a player chest lock.
	 *
	 * @param location the location of the lock
	 * @param player the owner of the lock
	 */
	public void addLock(final Location location, final Player player) {
		if (location == null || player == null)
			return;

		config.set(Utility.locationToString(location), player.getUniqueId().toString());
		saveConfig();

		locks.put(location, player.getUniqueId());
	}

	/**
	 * Removes a player chest lock.
	 *
	 * @param location the location of the lock.
	 */
	public void removeLock(final Location location) {
		if (location == null)
			return;

		config.set(Utility.locationToString(location), null);
		saveConfig();

		locks.remove(location);
	}
}