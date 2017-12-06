package com.keremc.lobby.user;

import lombok.Data;
import org.apache.commons.lang3.Validate;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Data
public class LobbyTeam {
	private final UUID teamId = UUID.randomUUID();

	private UUID leader;
	private final Set<UUID> teamMembers;

	public int size() {
		return teamMembers.size();
	}

	public void removeMember(final UUID uuid) {
		Validate.notNull(uuid);
		teamMembers.remove(uuid);
	}

	public void addMember(final UUID uuid) {
		Validate.notNull(uuid);
		teamMembers.add(uuid);
	}

	public boolean isMember(final UUID uuid) {
		Validate.notNull(uuid);
		return teamMembers.contains(uuid);
	}

	public List<Player> getPlayers() {
		return teamMembers.stream().map(Bukkit::getPlayer).collect(Collectors.toList());
	}

	public void sendMessage(final String message) {
		Validate.notNull(message);
		getPlayers().forEach(player -> sendMessage(message));
	}
}
