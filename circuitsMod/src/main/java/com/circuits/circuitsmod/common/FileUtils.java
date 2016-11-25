package com.circuits.circuitsmod.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.util.Optional;

import net.minecraft.client.Minecraft;
import net.minecraftforge.common.DimensionManager;

import org.apache.commons.lang3.StringUtils;

public class FileUtils {
	static final String worldSaveDirName = "microchips";

	public static Optional<Object> objectFromFile(File in) {
		try {
			FileInputStream inStream = new FileInputStream(in);
			ObjectInputStream objIn = new ObjectInputStream(inStream);
			Object result = objIn.readObject();
			objIn.close();
			inStream.close();
			return Optional.of(result);
		}
		catch (IOException | ClassNotFoundException e) {
			Log.internalError("Could not load object from " + in);
		}
		return Optional.empty();
	}
	public static void objectToFile(File in, Object obj) {
		try {
			FileOutputStream outStream = new FileOutputStream(in);
			ObjectOutputStream objOut = new ObjectOutputStream(outStream);
			objOut.writeObject(obj);
			objOut.close();
			outStream.close();
		}
		catch (IOException e) {
			Log.internalError("Could not access file " + in + " for object writing");
		}
		
	}
	
	public static String stringFromFile(File in) {
		try {
			return StringUtils.join(Files.readAllLines(in.toPath()), '\n');
		}
		catch (IOException e) {
			System.err.println(e);
			return "";
		}
	}
	
	public static File getConfigRootDir() {
		return new File(Minecraft.getMinecraft().mcDataDir.toString() + "/config/circuitsMod");
	}
	

	
	public static File getCircuitDefinitionsDir() {
		return new File(getConfigRootDir().toString() + "/circuits");
	}
	
	public static File getCircuitLibDir() {
		return new File(getConfigRootDir().toString() + "/circuitsCommon");
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
