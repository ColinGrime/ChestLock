package me.scill.chestlock.listeners;

import me.scill.chestlock.ChestLock;
import me.scill.chestlock.utilities.Utility;
import org.bukkit.Bukkit;
import org.bukkit.block.*;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.InventoryHolder;

import java.util.UUID;

public class LockListener implements Listener {

	private final ChestLock plugin;

	public LockListener(final ChestLock plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void onLockCreate(final SignChangeEvent event) {
		if (isValidSign(event.getBlock()) && isValidLock(event.getLine(0))) {
			if (isChestLocked(getChest((Sign) event.getBlock().getState()))) {
				event.getPlayer().sendMessage(Utility.color(plugin.getConfig().getString("errors.too-many-locks")));
				event.getBlock().breakNaturally();
				return;
			}
			createLock(event);
		}
	}

	@EventHandler
	public void onBlockBreak(final BlockBreakEvent event) {
		final Block block = event.getBlock();
		final Player player = event.getPlayer();

		// Check if there are locks on the chest.
		if (block.getState() instanceof Chest && isChestLocked((Chest) block.getState())) {
			player.sendMessage(Utility.color(plugin.getConfig().getString("errors.locked-chest")));
			event.setCancelled(true);
		}

		// Check if the block is a lock.
		if (!isValidSign(block) || !isValidLock((Sign) block.getState()))
			return;

		// Owner name of the lock.
		final String playerName = Bukkit.getPlayer(getLockOwner((Sign) block.getState())).getName();

		// Check if the player owns the lock.
		if (player.hasPermission("chestlock.break") || ownsLock((Sign) block.getState(), player)) {
			player.sendMessage(Utility.color(plugin.getConfig().getString(ownsLock((Sign) block.getState(), player)
					? "lock-break" : "admin.lock-break").replace("%player%", playerName)));
			plugin.getLockData().removeLock(block.getLocation());
		} else {
			player.sendMessage(Utility.color(plugin.getConfig().getString("errors.unowned-lock").replace("%player%", playerName)));
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onChestOpenAttempt(final PlayerInteractEvent event) {
		if (event.getAction() != Action.RIGHT_CLICK_BLOCK || event.getPlayer().hasPermission("chestlock.break"))
			return;

		final Block block = event.getClickedBlock();
		if (block.getState() instanceof Chest && !canPlayerOpenChest(((Chest) block.getState()), event.getPlayer())) {
			event.setUseInteractedBlock(Event.Result.DENY);
			event.getPlayer().sendMessage(Utility.color(plugin.getConfig().getString("errors.unowned-lock")));
		}
	}

	/**
	 * Creates a chest lock for a player.
	 *
	 * @param event the sign change event
	 */
	private void createLock(final SignChangeEvent event) {
		event.setLine(0, plugin.getConfig().getString("lock-tag"));
		event.setLine(1, event.getPlayer().getName());
		plugin.getLockData().addLock(event.getBlock().getLocation(), event.getPlayer());
	}

	/**
	 * Checks if the placed down block is a sign
	 * and is attached to a chest. This confirms
	 * that the sign is able to be a lock.
	 *
	 * @param block any potential sign
	 * @return true if the block is a sign and on a chest
	 */
	private boolean isValidSign(final Block block) {
		if (!(block.getState() instanceof Sign))
			return false;
		return getChest((Sign) block.getState()) != null;
	}

	/**
	 * Checks if the sign is a valid lock
	 * by comparing the first line of text.
	 *
	 * @param sign any valid sign
	 * @return true if the sign is a lock
	 */
	private boolean isValidLock(final Sign sign) {
		return sign.getLine(0).equalsIgnoreCase(plugin.getConfig().getString("lock-tag"));
	}

	/**
	 * Checks if the sign is a valid lock
	 * by comparing the first line of text.
	 *
	 * @param lockTag first line of the sign
	 * @return true if the sign is a lock
	 */
	private boolean isValidLock(final String lockTag) {
		return lockTag.equalsIgnoreCase(plugin.getConfig().getString("lock-tag"));
	}

	/**
	 * Returns the owner of the lock.
	 *
	 * @param lock any valid lock
	 * @return true if the player owns the lock
	 */
	private UUID getLockOwner(final Sign lock) {
		return lock == null ? null : plugin.getLockData().getLocks().get(lock.getBlock().getLocation());
	}

	/**
	 * Gets the chest of the specified
	 * lock if one is available.
	 *
	 * @param lock any lock
	 * @return chest if one exists
	 */
	private Chest getChest(final Sign lock) {
		final Block signBlock = lock.getBlock();
		final org.bukkit.material.Sign signData = (org.bukkit.material.Sign) signBlock.getState().getData();
		final Block attachedBlock = signBlock.getRelative(signData.getAttachedFace());

		if (attachedBlock.getState() instanceof Chest)
			return (Chest) attachedBlock.getState();
		return null;
	}

	/**
	 * Searches a chest/double chest
	 * for any potential lock.
	 *
	 * @param chest any chest
	 * @return lock if one exists
	 */
	private Sign findLock(final Chest chest) {
		if (chest == null)
			return null;

		final InventoryHolder chestHolder = chest.getInventory().getHolder();
		Chest leftChestBlock = chest, rightChestBlock = chest;

		if (chestHolder instanceof DoubleChest) {
			final DoubleChest doubleChest = ((DoubleChest) chestHolder);
			leftChestBlock = (Chest) doubleChest.getLeftSide();
			rightChestBlock = (Chest) doubleChest.getRightSide();
		}

		if (getLock(leftChestBlock) != null)
			return getLock(leftChestBlock);
		else if (getLock(rightChestBlock) != null)
			return getLock(rightChestBlock);
		return null;
	}

	/**
	 * Gets the lock of the specified
	 * chest if one is available.
	 *
	 * @param chest any chest
	 * @return lock if one exists
	 */
	private Sign getLock(final Chest chest) {
		if (chest == null)
			return null;

		for (BlockFace face : BlockFace.values()) {
			final Block block = chest.getBlock().getRelative(face);
			if (!(block.getState() instanceof Sign))
				continue;

			final org.bukkit.material.Sign signData = (org.bukkit.material.Sign) block.getState().getData();
			final Block attachedBlock = block.getRelative(signData.getAttachedFace());

			if ((chest.getBlock().equals(attachedBlock)) && plugin.getLockData().getLocks().containsKey(block.getLocation()))
				return (Sign) block.getState();
		}
		return null;
	}

	/**
	 * Checks if the player owns the specified lock.
	 *
	 * @param lock any valid lock
	 * @param player any player
	 * @return true if the player owns the lock
	 */
	private boolean ownsLock(final Sign lock, final Player player) {
		return getLockOwner(lock) == null || getLockOwner(lock).equals(player.getUniqueId());
	}

	/**
	 * Checks if the player can open the chest
	 * by confirming there are no unowned locks.
	 *
	 * @param chest any chest
	 * @param player any player
	 * @return true if the player can open the chest
	 */
	private boolean canPlayerOpenChest(final Chest chest, final Player player) {
		return ownsLock(findLock(chest), player);
	}

	/**
	 * Checks if the specified chest
	 * has a lock attached to it.
	 *
	 * @param chest any chest
	 * @return true if the chest has a lock on it
	 */
	private boolean isChestLocked(final Chest chest) {
		return findLock(chest) != null;
	}
}