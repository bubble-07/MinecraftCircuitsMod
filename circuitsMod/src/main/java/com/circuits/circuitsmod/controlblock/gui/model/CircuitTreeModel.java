package com.circuits.circuitsmod.controlblock.gui.model;

import io.netty.buffer.ByteBuf;

import java.io.Serializable;
import java.util.Comparator;
import com.circuits.circuitsmod.common.SerialUtils;

import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

/**
 * Internal model for the circuits directory tree
 * @author bubble-07
 *
 */
public class CircuitTreeModel implements IMessage, Serializable {
	private static final long serialVersionUID = 1L;
	
	private CircuitDirectory circuitRoot;
	
	public CircuitTreeModel(CircuitDirectory circuitRoot) {
		this.circuitRoot = circuitRoot;
		this.sortAlphabetic();
	}
	
	//TODO: Should we also support sorting by frequency of use or something similar?
	public void sortBy(Comparator<CircuitTreeNode> comp) {
		circuitRoot.sortBy(comp);
	}
	
	public void sortAlphabetic() {
		circuitRoot.sortBy((n1, n2) -> n1.getName().compareTo(n2.getName()));
	}
	
	public CircuitDirectory getRootDirectory() {
		return this.circuitRoot;
	}
	
	public CircuitDirectory getCustomDirectory() {
		return (CircuitDirectory) getRootDirectory().getChildren().stream().filter((c) -> c.getName().equalsIgnoreCase("Custom")).findFirst().get();
	}
	
	@Override
	public void fromBytes(ByteBuf buf) {
		circuitRoot = (CircuitDirectory) SerialUtils.fromBytes(buf);
	}
	@Override
	public void toBytes(ByteBuf buf) {
		SerialUtils.toBytes(buf, circuitRoot);
	}

}
