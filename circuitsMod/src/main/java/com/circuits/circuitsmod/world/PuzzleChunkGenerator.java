package com.circuits.circuitsmod.world;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Random;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldEntitySpawner;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.chunk.IChunkGenerator;
import net.minecraft.world.chunk.storage.AnvilChunkLoader;

public class PuzzleChunkGenerator implements IChunkGenerator {

    private final World worldObj;
    private Random random;

    private List<Biome.SpawnListEntry> mobs = Lists.newArrayList();
    //private File chunkLocation;
    
    public PuzzleChunkGenerator(World worldObj) {
        this.worldObj = worldObj;
        long seed = worldObj.getSeed();
        this.random = new Random((seed + 516) * 314);
    }
    
    public static File getMcDir(World worldObj) {
    	if (!worldObj.isRemote) {
    		//return new File(".");
    	}
    	return Minecraft.getMinecraft().mcDataDir;
    }

    @Override
    public Chunk provideChunk(int x, int z) {
        //I would load the world file from the mod directory, not the world directory.
        //If it works, it would save into the saves.  
        //The version identifier could probably just be 0
        File chunkLocation = new File(PuzzleChunkGenerator.getMcDir(worldObj).toString() + "/config/world/");
        //System.out.println(chunkLocation);
        /*if (!chunkLocation.exists()) {
        	System.out.println("I DONT EXIST" );
        }*/
        DataFixer dataFixIn = new DataFixer(0);
		AnvilChunkLoader loader = new AnvilChunkLoader(chunkLocation, dataFixIn);
		
		try {
			Chunk chunk = loader.loadChunk(worldObj, x, z);
			if (chunk != null) {
				return chunk;
			}
		} catch (IOException e) {
			System.out.println("The premade chunks were not loaded properly.");
		}
		Chunk chunk = new Chunk(this.worldObj, new ChunkPrimer(), x, z);
		return chunk;
		
        
    }

    @Override
    public void populate(int x, int z) {
        int i = x * 16;
        int j = z * 16;
        BlockPos blockpos = new BlockPos(i, 0, j);
        Biome biome = this.worldObj.getBiomeGenForCoords(blockpos.add(16, 0, 16));
      
        // Add biome decorations (like flowers, grass, trees, ...)
        biome.decorate(this.worldObj, this.random, blockpos);

        // Make sure animals appropriate to the biome spawn here when the chunk is generated
        WorldEntitySpawner.performWorldGenSpawning(this.worldObj, biome, i + 8, j + 8, 16, 16, this.random);
    }

    @Override
    public boolean generateStructures(Chunk chunkIn, int x, int z) {
        return false;
    }

    @Override
    public List<Biome.SpawnListEntry> getPossibleCreatures(EnumCreatureType creatureType, BlockPos pos) {
        // If you want normal creatures appropriate for this biome then uncomment the
        // following two lines:
//        Biome biome = this.worldObj.getBiome(pos);
//        return biome.getSpawnableList(creatureType);

        if (creatureType == EnumCreatureType.MONSTER){
            return mobs;
        }
        return ImmutableList.of();

    }

    @Nullable
    @Override
    public BlockPos getStrongholdGen(World worldIn, String structureName, BlockPos position) {
        return null;
    }

    @Override
    public void recreateStructures(Chunk chunkIn, int x, int z) {

    }

	}
