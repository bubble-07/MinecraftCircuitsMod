package com.circuits.circuitsmod.circuit;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.List;
import java.util.Optional;

import javax.imageio.ImageIO;

import net.minecraft.item.ItemStack;

import com.circuits.circuitsmod.circuitblock.WireDirectionMapper;
import com.circuits.circuitsmod.circuitblock.WireDirectionMapper.WireDirectionGenerator;
import com.circuits.circuitsmod.common.FileUtils;
import com.circuits.circuitsmod.common.Log;
import com.circuits.circuitsmod.common.SerializableItemStack;

/**
 * Information about a circuit that may be communicated over the network.
 * As a rule, this is pretty much everything but the implementation of
 * tests or of the circuit itself. This class is not responsible for
 * handling/keeping track of circuit UIDs -- see CircuitInfoProvider for that.
 * @author bubble-07
 */
public class CircuitInfo implements Serializable {
	private static final long serialVersionUID = 1L;
	
	List<SerializableItemStack> materialsCost;
	String name;
	String description;
	WireDirectionGenerator wireDirGen;
	
	transient BufferedImage image = null;
	
	private CircuitInfo() { }
	
	public static Optional<CircuitInfo> fromFolder(File containingFolder) {
		CircuitInfo result = new CircuitInfo();
		File descripFile = new File(containingFolder.toString() + "/description.txt");
		File imageFile = new File(containingFolder.toString() + "/Icon.png");
		File matlsFile = new File(containingFolder.toString() + "/materials.nbt");
		File nameFile = new File(containingFolder.toString() + "/name.txt");
		
		try {
			result.name = FileUtils.stringFromFile(nameFile);
		}
		catch (Exception e) {
			result.name = containingFolder.getName();		
		}
			
		if (result.name.equals("")) {
			return Optional.empty();
		}
		
		try {
			result.description = FileUtils.stringFromFile(descripFile);
		}
		catch (Exception e) {
			Log.userError("Missing Description for " + result.name);
			result.description = null;
		}
		
		try {
			result.image = ImageIO.read(imageFile);
		}
		catch (IOException e) {
			Log.userError("Missing Image for " + result.name);
			//TODO: Maybe add a "missing image" texture instead of making this null
			result.image = null;
		}
		
		try {
			result.materialsCost = SerializableItemStack.serializeItemStacks(SerializableItemStack.itemStacksFromFile(matlsFile));
		}
		catch (Exception e) {
			result.materialsCost = null;
		}
		
		//TODO: allow for the ability to override the default wire generator stuff in a file!
		result.wireDirGen = new WireDirectionMapper.DefaultGenerator();
		
		return Optional.of(result);
	}
	
	
	private void writeObject(ObjectOutputStream out) throws IOException {
		out.defaultWriteObject();
		if (image != null) {
			ImageIO.write(image, "png", out);		
		}
	}
	
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();
		try {
			image = ImageIO.read(in);
		}
		catch (IOException e) {
			image = null;
		}
	}
	
	
	public boolean isUnlocked() {
		return (materialsCost != null);
	}
	
	/**
	 * @return The cost of this Circuit entry, if unlocked.
	 */
	public Optional<List<ItemStack>> getCost() {
		return Optional.ofNullable(materialsCost).map(SerializableItemStack::deserializeItemStacks);
	}
	public BufferedImage getImage() {
		return image;
	}
	public String getName() {
		return name;
	}
	public String getDescription() {
		return description;
	}
	public WireDirectionGenerator getWireDirectionGenerator() {
		return this.wireDirGen;
	}

}
