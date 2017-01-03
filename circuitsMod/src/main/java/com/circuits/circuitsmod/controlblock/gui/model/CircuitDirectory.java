package com.circuits.circuitsmod.controlblock.gui.model;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.circuits.circuitsmod.circuit.CircuitUID;
import com.google.common.collect.Lists;

public class CircuitDirectory implements CircuitTreeNode {
	private static final long serialVersionUID = 1L;
	
	private final String name;
	private List<CircuitTreeNode> children = Lists.newArrayList();
	private final CircuitDirectory parent;
	private Map<CircuitUID, CircuitCell> cellMap;
	
	//Constructor for the root
	public CircuitDirectory(String name) {
		this(null, name);
		this.cellMap = new HashMap<>();
	}
	//Constructor for children
	public CircuitDirectory(CircuitDirectory parent, String name) {
		this.parent = parent;
		this.name = name;
	}
	
	public Optional<CircuitCell> locateCellForUID(CircuitUID uid) {
		return Optional.ofNullable(this.getRoot().cellMap.get(uid));
	}
	
	public boolean isRoot() {
		return this.parent == null;
	}
	
	public void addChild(CircuitTreeNode node) {
		this.children.add(node);
		if (this.isRoot()) {
			node.applyToCellsRecursively((cell) -> {
				this.cellMap.put(cell.getUid(), cell);
			});
		}
	}
	
	public void sortBy(Comparator<CircuitTreeNode> sorter) {
		this.children.sort(sorter);
		for (CircuitTreeNode child : getChildren()) {
			if (child instanceof CircuitDirectory) {
				((CircuitDirectory) child).sortBy(sorter);
			}
		}
	}
	
	public int numEntries() {
		return children.size();
	}
	
	public CircuitTreeNode getChildAt(int ind) {
		return children.get(ind);
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public List<CircuitTreeNode> getChildren() {
		return this.children;
	}

	@Override
	public Optional<CircuitDirectory> getParent() {
		return Optional.ofNullable(parent);
	}

}
