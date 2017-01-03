package com.circuits.circuitsmod.controlblock.gui.model;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public interface CircuitTreeNode extends Serializable {
	public String getName();
	public List<CircuitTreeNode> getChildren();
	public default boolean hasChildren() {
		return !this.getChildren().isEmpty();
	}
	public Optional<CircuitDirectory> getParent();
	
	public default CircuitDirectory getRoot() {
		if (this.getParent().isPresent()) {
			return this.getParent().get().getRoot();
		}
		else {
			return ((CircuitDirectory) this);
		}
	}
	
	public default void applyToCellsRecursively(Consumer<CircuitCell> action) {
		applyRecursive((node) -> {
			if (node instanceof CircuitCell) {
				action.accept((CircuitCell) node);
			}
		});
	}
	
	public default void applyRecursive(Consumer<CircuitTreeNode> action) {
		for (CircuitTreeNode child : getChildren()) {
			if (child instanceof CircuitDirectory) {
				((CircuitDirectory) child).applyRecursive(action);
			}
			action.accept(child);
		}
	}
}
