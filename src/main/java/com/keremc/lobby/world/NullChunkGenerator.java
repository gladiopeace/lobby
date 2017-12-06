package com.keremc.lobby.world;

import org.bukkit.World;
import org.bukkit.craftbukkit.v1_8_R3.generator.CraftChunkData;
import org.bukkit.generator.ChunkGenerator;

import java.util.Random;

public class NullChunkGenerator extends ChunkGenerator {

	@Override
	public ChunkData generateChunkData(World world, Random random, int x, int z, BiomeGrid biome) {
		return new CraftChunkData(world);
	}

}
