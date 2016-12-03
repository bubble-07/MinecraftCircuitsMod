package com.circuits.circuitsmod.controlblock.gui;

import com.circuits.circuitsmod.circuit.CircuitInfo;

public class CellDisplayPage extends ControlGuiPage {
	private final CircuitInfo cell;
	public CellDisplayPage(final ControlGui parent, final CircuitInfo cell) {
		super(parent);
		this.cell = cell;
		this.addElement(new TextButton(parent, "Back", screenX + screenWidth - shortLabelWidth, screenY, () -> {
				parent.setDisplayPage(new ControlGuiMainPage(parent));
		}));
		
		this.addElement(new TextButton(parent, "Test", screenX + screenWidth - shortLabelWidth, screenY + screenHeight - shortLabelHeight, () -> {
				parent.setDisplayPage(new TestSettingsPage(parent, cell));
		}));
		if (cell.isUnlocked()) {
			this.addElement(new TextButton(parent, "Craft", screenX, screenY + screenHeight - shortLabelHeight, () -> {
				parent.setDisplayPage(new CraftingPage(parent, cell));
			}));
		}
	}
	
	public void draw() {
		//Render the header
		parent.getFontRenderer().drawString(cell.getName(), screenX, screenY, elementColor);
		parent.drawHorizontalLine(screenX, screenX + screenWidth, screenY + 10, elementColor);
		
		cell.getCost().ifPresent((cost) -> {
			for (int i = 0; i < cost.size(); i++) {
				parent.renderItemStack(cost.get(i), screenX + screenWidth - 2*shortLabelWidth - 10*i, screenY);
			}
		});

		parent.getFontRenderer().drawSplitString(cell.getDescription(), 
				screenX, screenY + 16, screenWidth, elementColor);
	}
	
}
