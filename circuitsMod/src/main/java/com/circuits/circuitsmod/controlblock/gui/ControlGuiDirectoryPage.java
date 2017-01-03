package com.circuits.circuitsmod.controlblock.gui;

import org.apache.commons.lang3.StringUtils;

import com.circuits.circuitsmod.circuit.CircuitConfigOptions;
import com.circuits.circuitsmod.circuit.SpecializedCircuitUID;
import com.circuits.circuitsmod.circuitblock.CircuitItem;
import com.circuits.circuitsmod.controlblock.gui.model.CircuitCell;
import com.circuits.circuitsmod.controlblock.gui.model.CircuitDirectory;
import com.circuits.circuitsmod.controlblock.gui.model.CircuitTreeNode;

public class ControlGuiDirectoryPage extends ControlGuiPage {
	
	private int firstItem = 0;
	private final int maxToDisplay = 5;
	
	private final CircuitDirectory directory;
	private TextButton backButton;
	
	public ControlGuiDirectoryPage(ControlGui parent, CircuitDirectory directory) {
		super(parent);
		this.directory = directory;
		
		if (!this.directory.isRoot()) {
			this.backButton = new TextButton(parent, "Back", screenX + screenWidth - shortLabelWidth, screenY, () -> {
				parent.setDisplayPage(new ControlGuiDirectoryPage(parent, directory.getParent().get()));
			});
			this.addElement(backButton);
		}
	}
	public void draw() {
		int y_incr = screenHeight / maxToDisplay;
		int current_y = screenY;
		for (int i = firstItem; i < Math.min(firstItem + maxToDisplay, directory.numEntries()); i++) {
			renderEntry(directory.getChildAt(i), current_y, y_incr);
			current_y += y_incr;
		}	
	}
	
	private void renderEntry(CircuitTreeNode node, int cell_y, int cell_height) {
		parent.drawHorizontalLine(screenX, screenX + screenWidth, cell_y - 2, elementColor);
		if (node instanceof CircuitCell) {
			renderCell((CircuitCell) node, cell_y, cell_height);
		}
		else {
			String name = parent.getFontRenderer().trimStringToWidth(node.getName(), screenWidth - 10) + "/";
			parent.getFontRenderer().drawString(name, screenX, cell_y, elementColor);
		}
	}
	
	private void renderCell(CircuitCell cell, int cell_y, int cell_height) {
		int nameWidth = screenWidth - 20;
		String displayName = parent.getFontRenderer().trimStringToWidth(cell.getName(), nameWidth);
		parent.getFontRenderer().drawString(displayName, screenX + 20, cell_y, elementColor);
		parent.renderItemStack(CircuitItem.getStackFromUID(new SpecializedCircuitUID(cell.getUid(), new CircuitConfigOptions())), screenX, cell_y - 2);
	}
	
	private void scrollToChar(char character) {
		String asString = (character + "").toUpperCase();
		if (!StringUtils.isAlpha(asString)) {
			return;
		}
		for (int i = 0; i < directory.numEntries(); i++) {
			if (asString.compareTo(directory.getChildAt(i).getName().toUpperCase()) <= 0) {
				//We've found the cell to skip to
				firstItem = i;
				return;
			}
		}
		return;
	}
	
	@Override
	protected void handleKeyboardInput(char charTyped, int keyCode) {
		if (keyCode == 203 && this.backButton != null) {
			backButton.handleClick();
		}
		scrollToChar(charTyped);
	}
	
	@Override
	protected void handleScrollUp() {
		if (firstItem > 0) {
			firstItem--;
		}
	}
	
	@Override
	protected void handleScrollDown() {
		if (firstItem < (directory.numEntries() - maxToDisplay)) {
			firstItem++;
		}
	}
	
	private CircuitTreeNode yToNode(int ypos) {
		int y_incr = screenHeight / maxToDisplay;
		int index =  ((ypos - screenY) / y_incr) + firstItem;
		if (index > directory.numEntries()) {
			return null;
		}
		return directory.getChildAt(index);
	}
	
	@Override
	public void handleClick(int mouseX, int mouseY) {
		if (mouseX > screenX && mouseX < screenX + screenWidth && 
				mouseY > 0 && mouseY < screenHeight) {
			
			//Must be trying to click on somethin'
			CircuitTreeNode node = yToNode(mouseY);
			if (node instanceof CircuitCell) {
				parent.setDisplayPage(new CellDisplayPage(parent, (CircuitCell) node));
			}
			else if (node instanceof CircuitDirectory) {
				parent.setDisplayPage(new ControlGuiDirectoryPage(parent, (CircuitDirectory) node));
			}
		}
	}
}
