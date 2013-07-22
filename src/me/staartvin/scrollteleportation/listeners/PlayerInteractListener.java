package me.staartvin.scrollteleportation.listeners;

import me.staartvin.scrollteleportation.ScrollTeleportation;
import me.staartvin.scrollteleportation.exceptions.DestinationInvalidException;
import me.staartvin.scrollteleportation.tasks.TeleportRunnable;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class PlayerInteractListener implements Listener {

	private ScrollTeleportation plugin;

	public PlayerInteractListener(ScrollTeleportation instance) {
		plugin = instance;
	}

	@EventHandler
	public void onItemClick(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		ItemStack item = event.getItem();

		// Did the player right click
		if (!event.getAction().equals(Action.RIGHT_CLICK_AIR)
				&& !event.getAction().equals(Action.RIGHT_CLICK_BLOCK))
			return;


		// Is there an item in the player's hand
		if (item == null)
			return;

		// Is the item in hand a paper
		if (!item.getType().equals(Material.getMaterial(plugin.getMainConfig().getScrollItemId())))
			return;
		
		// If item doesn't have item meta it can never be a scroll
		if (!item.hasItemMeta())
			return;
		
		ItemMeta im = item.getItemMeta();

		// Does the scroll have a name
		if (!im.hasDisplayName())
			return;

		// Change displayname
		im.setDisplayName(plugin.fixName(im.getDisplayName()));
		// Is there a scroll defined in the config with this name?
		if (plugin.getMainConfig().getScroll(im.getDisplayName()) == null)
			return;

		String scroll = plugin.getMainConfig().getScroll(im.getDisplayName());

		if (!player.hasPermission("scrollteleportation.teleport")) {
			player.sendMessage(ChatColor.RED
					+ "You are not allowed to use scrolls!");
			return;
		}
		Location destination = null;

		try {
			destination = plugin.getMainConfig().getDestination(scroll);
		} catch (DestinationInvalidException e) {
			e.printStackTrace();
		}
		if (destination == null) {
			player.sendMessage(ChatColor.RED
					+ "Destination could not be found!");
			return;
		}

		int delay = plugin.getMainConfig().getDelay(scroll);

		// Inform player that he is going to be teleported.
		player.sendMessage(plugin.getMainConfig().getCastMessage()
				.replace("%time%", delay + ""));

		// Set player ready to be teleported
		plugin.getTeleportHandler().setReady(player.getName(), true);
		
		if (plugin.getMainConfig().doCancelOnMove(scroll)) {
			// Send warning
			player.sendMessage(plugin.getMainConfig().getMoveWarningMessage());	
		}

		if (!player.hasPermission("scrollteleportation.delaybypass")) {
			// Teleport after delay
			plugin.getServer()
					.getScheduler()
					.runTaskLater(
							plugin,
							new TeleportRunnable(plugin, destination, item, player),
							delay * 20);
		} else {
			// Teleport instantly
			plugin.getTeleportHandler().teleport(player, destination, item);
		}
		

	}
}