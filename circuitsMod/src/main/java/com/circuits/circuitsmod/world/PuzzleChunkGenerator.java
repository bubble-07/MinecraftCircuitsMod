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
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldEntitySpawner;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.chunk.IChunkGenerator;
import net.minecraft.world.chunk.storage.AnvilChunkLoader;
import net.minecraft.world.gen.MapGenBase;
import net.minecraft.world.gen.MapGenCaves;
import net.minecraftforge.event.terraingen.InitMapGenEvent.EventType;
import net.minecraftforge.event.terraingen.TerrainGen;
import net.minecraft.world.*;

public class PuzzleChunkGenerator implements IChunkGenerator {

    private final World worldObj;
    private Random random;
    private Biome[] biomesForGeneration;

    private List<Biome.SpawnListEntry> mobs = Lists.newArrayList();
    private MapGenBase caveGenerator = new MapGenCaves();
    private PuzzleTerrainGenerator terraingen = new PuzzleTerrainGenerator();
    //private File chunkLocation;
    
    public PuzzleChunkGenerator(World worldObj) {
        this.worldObj = worldObj;
        long seed = worldObj.getSeed();
        this.random = new Random((seed + 516) * 314);
        terraingen.setup(worldObj, random);
        caveGenerator = TerrainGen.getModdedMapGen(caveGenerator, EventType.CAVE);
        //chunkLocation = new File(PuzzleChunkGenerator.getMcDir(worldObj).toString() + "\\config\\world");
    }
    
    public static File getMcDir(World worldObj) {
    	if (!worldObj.isRemote) {
    		//return new File(".");
    	}
    	return Minecraft.getMinecraft().mcDataDir;
    }
    
    /*@Override
    public Chunk provideChunk(int x, int z) {
    	try {
    		DataFixer dataFixIn = new DataFixer(0);
    		AnvilChunkLoader loader0 = new AnvilChunkLoader(chunkFile0, dataFixIn);
    		AnvilChunkLoader loaderM1 = new AnvilChunkLoader(chunkFileMinus1, dataFixIn);
    		
    		Chunk chunk0 = loader0.loadChunk(worldObj, x, z);
    		Chunk chunkM1 = loaderM1.loadChunk(worldObj, x, z);
    		if (chunk0 != null) {
    			return chunk0;
    		}
    		else if(chunkM1 != null) {
    			return chunkM1;
    		} else
    			throw new NullPointerException();
    	} catch(IOException e) {
    		e.printStackTrace();
    		ChunkPrimer chunkprimer = new ChunkPrimer();
    		Chunk chunk = new Chunk(this.worldObj, chunkprimer, x, z);
            byte[] biomeArray = chunk.getBiomeArray();
            for (int i = 0; i < biomeArray.length; ++i) {
                biomeArray[i] = (byte)Biome.getIdForBiome(this.biomesForGeneration[i]);
            }

            chunk.generateSkylightMap();
            return chunk;
    	}
    }*/

    @Override
    public Chunk provideChunk(int x, int z) {
        ChunkPrimer chunkprimer = new ChunkPrimer(); //instead of the ChunkPrimer, use an existing map.  
        //I would load the world file from the mod directory, not the world directory.
        //If it works, it would save into the saves.  
        //The version identifier could probably just be 0
        File chunkLocation = new File(PuzzleChunkGenerator.getMcDir(worldObj).toString() + "/config/world/");
        //System.out.println(chunkLocation);
        if (!chunkLocation.exists()) {
        	System.out.println("I DONT EXIST" );
        }
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
		
		
        // Setup biomes for terraingen
        this.biomesForGeneration = this.worldObj.getBiomeProvider().getBiomesForGeneration(this.biomesForGeneration, x * 4 - 2, z * 4 - 2, 10, 10);
        terraingen.setBiomesForGeneration(biomesForGeneration);
        terraingen.generate(x, z, chunkprimer);

        // Setup biomes again for actual biome decoration
        this.biomesForGeneration = this.worldObj.getBiomeProvider().getBiomesForGeneration(this.biomesForGeneration, x * 16, z * 16, 16, 16);
        // This will replace stone with the biome specific stones
        terraingen.replaceBiomeBlocks(x, z, chunkprimer, this, biomesForGeneration);
        
        // Generate caves
        this.caveGenerator.generate(this.worldObj, x, z, chunkprimer);

        Chunk chunk = new Chunk(this.worldObj, chunkprimer, x, z);

        byte[] biomeArray = chunk.getBiomeArray();
        for (int i = 0; i < biomeArray.length; ++i) {
            biomeArray[i] = (byte)Biome.getIdForBiome(this.biomesForGeneration[i]);
        }

        chunk.generateSkylightMap();
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
