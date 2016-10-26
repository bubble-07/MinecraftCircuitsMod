package com.circuits.circuitsmod.world;

import com.circuits.circuitsmod.CircuitsMod;
import com.circuits.circuitsmod.Config;

import net.minecraft.world.DimensionType;
import net.minecraftforge.common.DimensionManager;

public class ModDimensions {

    public static DimensionType testDimensionType;

    public static void init() {
        registerDimensionTypes();
        registerDimensions();
    }

    private static void registerDimensionTypes() {
        testDimensionType = DimensionType.register(CircuitsMod.MODID, "_test", Config.dimensionId, PuzzleWorldProvider.class, false);
    }

    private static void registerDimensions() {
        DimensionManager.registerDimension(Config.dimensionId, testDimensionType);
    }
}