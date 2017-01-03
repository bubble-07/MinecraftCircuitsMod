package com.circuits.circuitsmod.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.Enumeration;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.DimensionManager;

import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.lang3.StringUtils;

public class FileUtils {
	static final String worldSaveDirName = "circuitsMod";

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
	
	public static void copyResourceToFile(ResourceLocation rsc, File dest) {
		try {
			FileOutputStream outStream = new FileOutputStream(dest);
			InputStream is = Minecraft.getMinecraft().getResourceManager().getResource(rsc).getInputStream();
			IOUtils.copy(is, outStream);
			outStream.close();
		}
		catch (IOException e) {
			Log.internalError("Could not copy resource file from " + rsc + " to " + dest);
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
	
	public static File getCircuitLibDir() {
		return new File(getConfigRootDir().toString() + "/circuitsCommon");
	}

	
	/**
	 * @return The mod-global (not world-specific) circuit definitions directory.
	 * The contents of this folder are copied to every world's circuit definition directory
	 * to make it so players can both maintain a preferred configuration and easily share
	 * save files
	 */
	public static File getGlobalCircuitDefinitionsDir() {
		return new File(getConfigRootDir().toString() + "/circuits");
	}
	
	/**
	 * @return The world-specific circuit definitions directory
	 */
	public static File getWorldCircuitDefinitionsDir() {
		return new File(getWorldSaveDir().getPath() + "/circuits");
	}
	
	public static File getWorldRecipesDir() {
		return new File(getWorldSaveDir().getPath() + "/recipes");
	}
	
	public static File getWorldSaveDir() {
		File rootSaveDir = DimensionManager.getCurrentSaveRootDirectory();
		File saveDir = new File(rootSaveDir.getPath() + "/" + worldSaveDirName);	
		if (!saveDir.exists()) {
			saveDir.mkdir();
		}
		return saveDir;
	}
	
	//Kind of a pain that Java's standard library doesn't have a built-in method
	//for unzipping a directory. Props to Geoffrey de Smet
	//http://stackoverflow.com/questions/9324933/what-is-a-good-java-library-to-zip-unzip-files
	//for putting together something decently clean, adapted below
	public static void unzip(File inputFile, File outputDirectory) {
		java.util.zip.ZipFile zipFile = null;
		try {
			zipFile = new ZipFile(inputFile);
			Enumeration<? extends ZipEntry> entries = zipFile.entries();
			while (entries.hasMoreElements()) {
				ZipEntry entry = entries.nextElement();
				if (entry.getName().contains("MACOSX")) {
					continue;
				}
				File entryDestination = new File(outputDirectory.getAbsolutePath(), entry.getName());
				if (entry.isDirectory()) {
					entryDestination.mkdirs();
				} else {
					entryDestination.getParentFile().mkdirs();
					InputStream in = zipFile.getInputStream(entry);
					OutputStream out = new FileOutputStream(entryDestination);
					IOUtils.copy(in, out);
					IOUtils.closeQuietly(in);
					out.close();
				}
			}
			zipFile.close();
		}
		catch (IOException e) {
			Log.internalError("Failed to unzip " + inputFile + " into " + outputDirectory);
		}
	}
	
	
}
