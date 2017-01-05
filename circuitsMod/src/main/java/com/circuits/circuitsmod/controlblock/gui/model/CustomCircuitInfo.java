package com.circuits.circuitsmod.controlblock.gui.model;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Optional;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;

import com.circuits.circuitsmod.common.Log;
import com.circuits.circuitsmod.common.StringUtils;
import com.circuits.circuitsmod.recorder.CircuitRecording;

public class CustomCircuitInfo implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private String name;
	private final CustomCircuitImage image;
	private String description;
	private CircuitRecording recording;
	
	public CustomCircuitInfo() {
		this.recording = null;
		this.name = "";
		this.image = new CustomCircuitImage();
		this.description = "";
	}
	
	public String getName() {
		return this.name;
	}
	public CircuitRecording getRecording() {
		return this.recording;
	}
	public String getDescription() {
		return this.description;
	}
	public CustomCircuitImage getImage() {
		return this.image;
	}
	public void setName(String name) {
		this.name = name;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	
	public void setRecording(CircuitRecording recording) {
		this.recording = recording;
	}
	
	/**
	 * Only ever call this server-side. Writes a custom circuit into the given parent directory,
	 * but with an uncompiled Java file.
	 * @param parentDirectory
	 * @return the directory of the created circuit, or Optional.empty() if the operation failed
	 */
	public Optional<File> writeIntoDirectory(File parentDirectory) {
		String sanitizedName = StringUtils.sanitizeAlphaNumeric(getName());
		File circuitDir = new File(parentDirectory, sanitizedName);
		circuitDir.mkdirs();
		try {
			FileUtils.writeStringToFile(new File(circuitDir, "Implementation.java"), getRecording().toJavaSource());
		}
		catch (IOException e) {
			Log.internalError("Failed to write implementation source for custom circuit " + circuitDir);
			return Optional.empty();
		}
		
		try {
			FileUtils.writeStringToFile(new File(circuitDir, "description.txt"), getDescription());
		}
		catch (IOException e) {
			Log.internalError("Failed to write description for custom circuit " + circuitDir);
			return Optional.empty();
		}
		try {
			ImageIO.write(getImage().toBufferedImage(), "png", new File(circuitDir, "Icon.png"));
		}
		catch (IOException e) {
			Log.internalError("Failed to write image for custom circuit " + circuitDir);
			return Optional.empty();
		}
		return Optional.of(circuitDir);
	}
	
	
}
