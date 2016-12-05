package com.circuits.circuitsmod.controlblock.gui.model;

import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.util.List;
import java.util.Optional;

import net.minecraft.item.ItemStack;

import com.circuits.circuitsmod.circuit.CircuitInfo;
import com.circuits.circuitsmod.circuit.CircuitUID;
import com.circuits.circuitsmod.circuitblock.WireDirectionMapper.WireDirectionGenerator;

/**
 * Class which contains all information necessary for displaying a circuit (not specialized!)
 * in the GUI
 * @author bubble-07
 *
 */
public class CircuitCell implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private CircuitInfo info;
	private CircuitUID uid;
	public CircuitInfo getInfo() {
		return info;
	}
	public CircuitUID getUid() {
		return uid;
	}
	public CircuitCell(CircuitUID uid, CircuitInfo info) {
		this.info = info;
		this.uid = uid;
	}
	
	public boolean isUnlocked() {
		return info.isUnlocked();
	}
	
	/**
	 * @return The cost of this Circuit entry, if unlocked.
	 */
	public Optional<List<ItemStack>> getCost() {
		return info.getCost();
	}
	public BufferedImage getImage() {
		return info.getImage();
	}
	public String getName() {
		return info.getName();
	}
	public String getDescription() {
		return info.getDescription();
	}
	public WireDirectionGenerator getWireDirectionGenerator() {
		return info.getWireDirectionGenerator();
	}
}
