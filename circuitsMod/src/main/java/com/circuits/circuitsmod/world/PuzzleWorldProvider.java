package com.circuits.circuitsmod.world;

import net.minecraft.world.DimensionType;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.chunk.IChunkGenerator;

public class PuzzleWorldProvider extends WorldProvider {

    @Override
    public DimensionType getDimensionType() {
        return ModDimensions.testDimensionType;
    }

    @Override
    public String getSaveFolder() {
        return "TEST";
    }

    @Override
    public IChunkGenerator createChunkGenerator() {
        return new PuzzleChunkGenerator(worldObj);
    }
}