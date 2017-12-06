package com.keremc.lobby.user;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public final class UserProfile {
	private final UUID uuid;
	private final String playerName;
}
