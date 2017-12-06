package com.keremc.lobby.event;

import com.keremc.lobby.service.PlayerManager;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class MinecraftListener implements Listener {

	private final PlayerManager playerManager = new PlayerManager();

	@EventHandler
	public void onPlayerHealthChange(final EntityDamageEvent event) {
		event.setCancelled(true);
	}

	@EventHandler
	public void onPlayerBlock(final BlockBreakEvent event) {
		final Player player = event.getPlayer();

		if (player.getGameMode() != GameMode.CREATIVE) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onPlayerPlace(final BlockPlaceEvent event) {
		final Player player = event.getPlayer();

		if (player.getGameMode() != GameMode.CREATIVE) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onPlayerJoin(final PlayerJoinEvent event) {
		playerManager.initializePlayer(event.getPlayer());
	}

	@EventHandler
	public void onPlayerLeave(final PlayerQuitEvent event) {
		playerManager.abandonTeam(event.getPlayer(), true);
	}

	@EventHandler
	public void onPlayerDropItem(final PlayerDropItemEvent event) {
		event.setCancelled(true);
	}

	@EventHandler
	public void onPlayerInventory(final InventoryClickEvent event) {
		event.setCancelled(true);
	}
}
