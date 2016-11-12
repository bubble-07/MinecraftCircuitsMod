package com.circuits.circuitsmod.common;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Optional;

import net.minecraftforge.common.DimensionManager;

import org.apache.commons.lang3.StringUtils;

public class FileUtils {
	static final String worldSaveDirName = "microchips";

	
	public static String stringFromFile(File in) {
		try {
			return StringUtils.join(Files.readAllLines(in.toPath()), '\n');
		}
		catch (IOException e) {
			System.err.println(e);
			return "";
		}
	}
	
	public static File getCircuitDefinitionsDir() {
		return null;
	}
	
	public static File getCircuitLibDir() {
		return null;
	}
	
	public static File getWorldSaveDir() {
		File rootSaveDir = DimensionManager.getCurrentSaveRootDirectory();
		File saveDir = new File(rootSaveDir.getPath() + "/" + worldSaveDirName);	
		if (!saveDir.exists()) {
			saveDir.mkdir();
		}
		return saveDir;
	}
}
