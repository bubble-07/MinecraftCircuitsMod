package com.circuits.circuitsmod;

import java.util.Arrays;
import java.util.logging.Level;

import com.circuits.circuitsmod.recipes.RecipeDeterminer.CostCurve;

import net.minecraftforge.common.config.Configuration;

public class Config {

    private static final String CATEGORY_GENERAL = "general";
    private static final String CATEGORY_DIMENSIONS = "dimensions";

	public static int dimensionId = 5;
	public static boolean isCircuitsProgressWorldGlobal = false;
	public static double circuitCostMultiplier = 1.0;
	public static CostCurve circuitCostCurve = CostCurve.LOG;
	public static boolean shouldRenderTextOnCircuit = true;

    public static void readConfig() {
        Configuration cfg = CommonProxy.config;
        try {
            cfg.load();
            initGeneralConfig(cfg);
            initDimensionConfig(cfg);
        } catch (Exception e1) {
            CircuitsMod.logger.log(Level.SEVERE, "Problem loading config file!", e1);
        } finally {
            if (cfg.hasChanged()) {
                cfg.save();
            }
        }
    }

    private static void initGeneralConfig(Configuration cfg) {
        cfg.addCustomCategoryComment(CATEGORY_GENERAL, "General configuration");
        
        isCircuitsProgressWorldGlobal = cfg.getBoolean("CircuitCraftingCollaborative", CATEGORY_GENERAL, isCircuitsProgressWorldGlobal, 
        		       "Set this to true if you want the circuit unlock progress to be "
        		       + "shared among all players on the server");
        
        circuitCostMultiplier = (double) cfg.getFloat("CircuitCostMultiplier", CATEGORY_GENERAL, (float) circuitCostMultiplier, 
        		                                      0.0f, 1000.0f, "Change this if you want to change the multiplier"
        		                                      + "on the item quantities required to craft circuits."
        		                                      + "If the cost curve is constant, this sets a cap on the number of items instead");
        String costCurve = cfg.getString("CircuitCostComputation", CATEGORY_GENERAL, circuitCostCurve.getName(), 
        		                         "One of linear, log, sqrt, constant. Determines the method used to compute circuit costs");
        circuitCostCurve = Arrays.stream(CostCurve.values())
        		                 .filter((c) -> c.getName().equalsIgnoreCase(costCurve))
        		                 .findFirst().orElse(circuitCostCurve);
        shouldRenderTextOnCircuit = cfg.getBoolean("RenderTextOnCircuit", CATEGORY_GENERAL, shouldRenderTextOnCircuit,
        		        "Set this to false if you do not want to render the circuit config text on circuits placed in the world");
    }

    private static void initDimensionConfig(Configuration cfg) {
        cfg.addCustomCategoryComment(CATEGORY_DIMENSIONS, "Dimension configuration");

    }
}