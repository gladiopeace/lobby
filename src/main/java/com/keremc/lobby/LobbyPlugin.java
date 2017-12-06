package com.keremc.lobby;

import com.keremc.lobby.world.NullChunkGenerator;
import lombok.Getter;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public class LobbyPlugin extends JavaPlugin {

	@Getter
	private static Logger pluginLogger;

	@Override
	public void onEnable() {
		LobbyPlugin.pluginLogger = getLogger();
	}

	@Override
	public ChunkGenerator getDefaultWorldGenerator(String worldName, String id) {
		return new NullChunkGenerator();
	}
}
