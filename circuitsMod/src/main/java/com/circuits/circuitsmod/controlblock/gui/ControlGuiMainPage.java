package com.circuits.circuitsmod.controlblock.gui;

import org.apache.commons.lang3.StringUtils;

import com.circuits.circuitsmod.controlblock.gui.model.CircuitCell;

public class ControlGuiMainPage extends ControlGuiPage {
	
	private int firstItem = 0;
	private final int maxToDisplay = 5;
	
	
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
		int nameWidth = screenWidth;
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
	
	private void scrollToChar(char character) {
		String asString = (character + "").toUpperCase();
		if (!StringUtils.isAlpha(asString)) {
			return;
		}
		for (int i = 0; i < parent.model.numEntries(); i++) {
			if (asString.compareTo(parent.model.getCell(i).getName().toUpperCase()) <= 0) {
				//We've found the cell to skip to
				firstItem = i;
				return;
			}
		}
		return;
	}
	
	@Override
	protected void handleKeyboardInput(char charTyped, int keyCode) {
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
