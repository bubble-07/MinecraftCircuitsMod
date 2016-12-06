package com.circuits.circuitsmod.controlblock.gui;

import java.util.List;

import com.circuits.circuitsmod.controlblock.gui.model.CircuitCell;

import net.minecraft.item.ItemStack;

public class ControlGuiMainPage extends ControlGuiPage {
	
	private int firstItem = 0;
	private final int maxToDisplay = 3;
	
	
	public ControlGuiMainPage(ControlGui parent) {
		super(parent);
	}
	public void draw() {
		int y_incr = screenHeight / maxToDisplay;
		int current_y = screenY;
		for (int i = firstItem; i < Math.min(firstItem + maxToDisplay, parent.model.numEntries()); i++) {
			renderCell(parent.model.getCell(i), current_y, y_incr);
			current_y += y_incr;
		}	
	}
	
	private void renderCell(CircuitCell cell, int cell_y, int cell_height) {
		int nameWidth = (screenWidth * 3) / 4;
		String displayName = parent.getFontRenderer().trimStringToWidth(cell.getName(), nameWidth);
		parent.getFontRenderer().drawString(displayName, screenX, cell_y, elementColor);
		parent.drawHorizontalLine(screenX, screenX + screenWidth, cell_y - 2, elementColor);
		
		//TODO: Make it __visually apparent__ whether something has been unlocked,
		//but don't display the cost -- too cramped!
		/*
		if (cell.isUnlocked()) {
			//Special stuff to draw (materials cost) if the cell has been unlocked
			List<ItemStack> materials = cell.getCost().get();
			int currentX = screenX + nameWidth;
			for (ItemStack material : materials) {
				currentX += 10;
				parent.renderItemStack(material, currentX, cell_y);
			}
		}*/
	}
	
	@Override
	protected void handleScrollUp() {
		if (firstItem > 0) {
			firstItem--;
		}
	}
	
	@Override
	protected void handleScrollDown() {
		if (firstItem < (parent.model.numEntries() - maxToDisplay)) {
			firstItem++;
		}
	}
	
	private CircuitCell yToCell(int ypos) {
		int y_incr = screenHeight / maxToDisplay;
		int index =  ((ypos - screenY) / y_incr) + firstItem;
		if (index > parent.model.numEntries()) {
			return null;
		}
		return parent.model.getCell(index);
	}
	
	@Override
	public void handleClick(int mouseX, int mouseY) {
		if (mouseX > screenX && mouseX < screenX + screenWidth && 
				mouseY > 0 && mouseY < screenHeight) {
			//Must be trying to click on a cell
			CircuitCell cell = yToCell(mouseY);
			if (cell != null) {
				parent.setDisplayPage(new CellDisplayPage(parent, cell));
			}
		}
	}
}
