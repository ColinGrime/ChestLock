package me.scill.chestlock.utilities;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;

public class Utility {

	/**
	 * If the message contains any valid
	 * color codes, they will be applied.
	 *
	 * @param message any string
	 * @return colored version of the string
	 */
	public static String color(final String message) {
		if (message == null)
			return "";
		return ChatColor.translateAlternateColorCodes('&', message);
	}

	/**
	 * Turns a valid location object into a location string.
	 *
	 * @param location any location object
	 * @return the string of the given location object
	 */
	public static String locationToString(final Location location) {
		if (location == null)
			return "invalid-location";
		return location.getWorld().getName() + ":" + location.getBlockX() + ":" + location.getBlockY() + ":" + location.getBlockZ();
	}

	/**
	 * Turns a valid location string into a location object.
	 *
	 * @param locationString any location in string form
	 * @return the location object of the given string
	 */
	public static Location stringToLocation(final String locationString) {
		final String[] args = locationString.split(":");
		return new Location(Bukkit.getWorld(args[0]) == null ?
				Bukkit.getWorld("world") : Bukkit.getWorld(args[0]),
				Integer.parseInt(args[1]),
				Integer.parseInt(args[2]),
				Integer.parseInt(args[3]));
	}
}