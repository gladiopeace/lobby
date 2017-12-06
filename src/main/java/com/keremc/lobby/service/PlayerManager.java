package com.keremc.lobby.service;

import com.keremc.lobby.LobbyPlugin;
import com.keremc.lobby.user.LobbyTeam;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.apache.commons.lang3.Validate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.logging.Logger;

public class PlayerManager {
	private static final Logger logger = LobbyPlugin.getPluginLogger();

	private static final int MAX_TEAM_SIZE = 4;

	private final Set<LobbyTeam> teams = new TreeSet<>();
	private final Map<QueueMode, Set<LobbyTeam>> queues = new HashMap<>();
	private final Map<UUID, TreeSet<UUID>> invitations = new HashMap<>();

	/* SETUP */

	public void initializePlayer(final Player player) {
		Validate.notNull(player);

		final UUID playerId = player.getUniqueId();

		teams.forEach(team -> team.removeMember(playerId));
		teams.removeIf(team -> team.getTeamMembers().size() == 0);

		final LobbyTeam playerTeam = new LobbyTeam(Collections.singleton(playerId));
		playerTeam.setLeader(playerId);

		teams.add(playerTeam);
		logger.info("Initialized team '" + playerTeam.getTeamId() + "' for player '" + player.getName() + "' (" + playerId + ")");

		setupInventory(player);
	}

	public void enqueueTeam(final QueueMode queueMode, final LobbyTeam lobbyTeam) {
		Validate.notNull(queueMode);
		Validate.notNull(lobbyTeam);

	}

	public LobbyTeam getTeam(final UUID playerId) {
		Validate.notNull(playerId);
		return teams.stream().filter(team -> team.isMember(playerId)).findAny().orElseThrow(IllegalStateException::new);
	}

	public void abandonTeam(final Player player, final boolean logout) {
		Validate.notNull(player);

		final UUID playerId = player.getUniqueId();
		final LobbyTeam currentTeam = getTeam(playerId);

		invitations.remove(playerId);

		if (currentTeam.size() == 1) {
			if (!logout) {
				player.sendMessage(ChatColor.RED + "You are not part of a team!");
			} else {
				teams.remove(currentTeam);
			}
		} else {
			final Collection<Player> teamMembers = currentTeam.getPlayers();
			currentTeam.sendMessage(ChatColor.WHITE + player.getName() + ChatColor.GOLD + " has left the team");
			currentTeam.removeMember(playerId);

			if (currentTeam.getLeader().equals(playerId)) {
				currentTeam.setLeader(currentTeam.getTeamMembers().iterator().next());

				final Player newLeader = Bukkit.getPlayer(currentTeam.getLeader());

				if (currentTeam.size() > 1) {
					currentTeam.sendMessage(ChatColor.GOLD + "Team leadership has been passed to " + ChatColor.WHITE + newLeader.getName());
				} else {
					newLeader.sendMessage(ChatColor.GOLD + "Team has been disbanded");
				}
			}
			teamMembers.forEach(this::setupInventory);
		}

	}

	public void setupInventory(final Player player) {
		Validate.notNull(player);

		final UUID playerId = player.getUniqueId();
		final LobbyTeam playerTeam = teams.stream().filter(team -> team.isMember(playerId)).findAny().orElseThrow(IllegalStateException::new);

		final ItemStack queueItem = new ItemStack(Material.INK_SACK);
		final ItemMeta queueMeta = queueItem.getItemMeta();
		queueMeta.setDisplayName(ChatColor.RED + "Join a match");
		queueItem.setItemMeta(queueMeta);

		final ItemStack teamItem = new ItemStack(Material.SKULL_ITEM);
		final SkullMeta skullMeta = (SkullMeta) teamItem.getItemMeta();
		skullMeta.setOwner(player.getName());
		teamItem.setItemMeta(skullMeta);
		teamItem.setDurability((short) 3);
		teamItem.setAmount(playerTeam.size());

		final ItemStack inviteItem = new ItemStack(Material.BLAZE_ROD);
		final ItemMeta inviteMeta = inviteItem.getItemMeta();
		inviteMeta.setDisplayName(ChatColor.YELLOW + "Right click a player to send team invite");
		inviteItem.setItemMeta(inviteMeta);

		player.getInventory().setItem(0, queueItem);
		player.getInventory().setItem(1, inviteItem);
		player.getInventory().setItem(8, teamItem);

		final LobbyTeam lobbyTeam = getTeam(playerId);

		if (lobbyTeam.size() > 1) {
			final ItemStack leaveTeamItem = new ItemStack(Material.ANVIL);
			final ItemMeta leaveMeta = leaveTeamItem.getItemMeta();
			leaveMeta.setDisplayName(ChatColor.RED + "Leave team");
			leaveTeamItem.setItemMeta(leaveMeta);

			player.getInventory().setItem(7, leaveTeamItem);
		}

		final long inviteCount = invitations.values().stream().filter(set -> set.contains(playerId)).count();

		if (inviteCount > 0) {
			final ItemStack pendingInvites = new ItemStack(Material.LEATHER);
			final ItemMeta pendingInvitesItemMeta = pendingInvites.getItemMeta();

			if (inviteCount == 1) {
				pendingInvitesItemMeta.setDisplayName(ChatColor.YELLOW + "One invite pending - click to accept");
			} else {
				pendingInvitesItemMeta.setDisplayName(ChatColor.GREEN + String.valueOf(inviteCount) + ChatColor.YELLOW + " invites pending - click to maange");
			}

			pendingInvites.setItemMeta(pendingInvitesItemMeta);
			player.getInventory().setItem(4, pendingInvites);
		}

		player.updateInventory();

	}

	public void invitePlayer(final Player invitorPlayer, final Player recipientPlayer) {
		Validate.notNull(invitorPlayer);
		Validate.notNull(recipientPlayer);

		final UUID invitor = invitorPlayer.getUniqueId();
		final UUID recipient = recipientPlayer.getUniqueId();

		final Set<UUID> pendingOutboundInvites = invitations.computeIfAbsent(invitor, k -> new TreeSet<>());

		if (pendingOutboundInvites.contains(recipient)) {
			invitorPlayer.sendMessage(ChatColor.RED + "You have already invited '" + recipientPlayer.getName() + "'");
		} else {
			pendingOutboundInvites.add(recipient);

			invitorPlayer.sendMessage(ChatColor.GREEN + "Successfully invited '" + recipientPlayer.getName() + "'");
			recipientPlayer.sendMessage("'" + ChatColor.GREEN + invitorPlayer.getName() + ChatColor.YELLOW + "' has invited you to their team");

			final TextComponent message = new TextComponent();

			final TextComponent clickPart1 = new TextComponent("CLick [");
			clickPart1.setColor(net.md_5.bungee.api.ChatColor.YELLOW);

			message.addExtra(clickPart1);

			final TextComponent clickPart2 = new TextComponent("here");
			clickPart2.setColor(net.md_5.bungee.api.ChatColor.WHITE);
			clickPart2.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "accept " + recipient));

			message.addExtra(clickPart2);

			final TextComponent clickPart3 = new TextComponent("] to join the team");
			clickPart1.setColor(net.md_5.bungee.api.ChatColor.YELLOW);

			message.addExtra(clickPart3);

			recipientPlayer.spigot().sendMessage(message);

			setupInventory(recipientPlayer);

		}
	}

	public void acceptInvitation(final Player recipientPlayer, final UUID invitor) {
		Validate.notNull(recipientPlayer);
		Validate.notNull(invitor);

		final UUID recipient = recipientPlayer.getUniqueId();

		// verify an invite exists
		// verify the invite is valid

		// if its valid, leave the current team and join that team

		final Set<UUID> otherInvites = invitations.get(invitor);

		if (!otherInvites.contains(recipient)) {
			recipientPlayer.sendMessage(ChatColor.RED + "Invitation does not exist!");
		} else {
			final LobbyTeam invitedTeam = getTeam(invitor);

			if (invitedTeam.size() >= MAX_TEAM_SIZE - 1) {
				recipientPlayer.sendMessage(ChatColor.RED + "That team is full!");
			} else {
				invitedTeam.addMember(recipient);
				invitedTeam.sendMessage(ChatColor.WHITE + recipientPlayer.getName() + ChatColor.GOLD + " has joined the team");

				invitedTeam.getPlayers().forEach(this::setupInventory);
			}
		}

	}

}
