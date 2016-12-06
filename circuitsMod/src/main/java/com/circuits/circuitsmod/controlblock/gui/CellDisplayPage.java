package com.circuits.circuitsmod.controlblock.gui;

import com.circuits.circuitsmod.controlblock.gui.model.CircuitCell;

public class CellDisplayPage extends ControlGuiPage {
	private final CircuitCell cell;
	private final TextButton craftButton;
	public CellDisplayPage(final ControlGui parent, final CircuitCell cell) {
		super(parent);
		this.cell = cell;
		this.addElement(new TextButton(parent, "Back", screenX + screenWidth - shortLabelWidth, screenY, () -> {
				parent.setDisplayPage(new ControlGuiMainPage(parent));
		}));
		
		this.addElement(new TextButton(parent, "Test", screenX + screenWidth - shortLabelWidth, screenY + screenHeight - shortLabelHeight, () -> {
				parent.setDisplayPage(new TestSettingsPage(parent, cell));
		}));
		
		this.craftButton = new TextButton(parent, "Craft", screenX, screenY + screenHeight - shortLabelHeight, () -> {
			parent.setDisplayPage(new CraftingPage(parent, cell));
		});
		/*
		if (cell.isUnlocked()) {
			this.addElement(craftButton);
		}*/
	}
	
	public void draw() {
		//Render the header
		parent.getFontRenderer().drawString(cell.getName(), screenX, screenY, elementColor);
		parent.drawHorizontalLine(screenX, screenX + screenWidth, screenY + 10, elementColor);
		/*
		cell.getCost().ifPresent((cost) -> {
			for (int i = 0; i < cost.size(); i++) {
				parent.renderItemStack(cost.get(i), screenX + screenWidth - 2*shortLabelWidth - 10*i, screenY);
			}
		});*/

		parent.getFontRenderer().drawSplitString(cell.getDescription(), 
				screenX, screenY + 16, screenWidth, elementColor);
	}
	
}
