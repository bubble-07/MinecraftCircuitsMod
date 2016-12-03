package com.circuits.circuitsmod.controlblock.gui;

import com.circuits.circuitsmod.circuit.SpecializedCircuitInfo;


public class CraftingPage extends ControlGuiPage {
	SpecializedCircuitInfo cell;
	public CraftingPage(final ControlGui parent, final SpecializedCircuitInfo cell) {
		super(parent);
		this.cell = cell;
		parent.tileEntity.setCraftingCell(cell);
		this.addElement(new TextButton(parent, "Back", screenX + screenWidth - shortLabelWidth, screenY, new Runnable() {
			@Override public void run() {
				parent.setDisplayPage(new ControlGuiMainPage(parent)); 
				parent.tileEntity.setCraftingCell(null);
			}
		}));
	}
	@Override
	public void draw() {
		parent.getFontRenderer().drawString(cell.getFullDisplayName(), screenX, screenY, elementColor);
		parent.drawHorizontalLine(screenX, screenX + screenWidth, screenY + 10, elementColor);
		
		cell.getInfo().getCost().ifPresent((cost) -> {
			for (int i = 0; i < cost.size(); i++) {
				int x = screenX + 20*i;
				parent.renderItemStack(cost.get(i), x, screenY + screenHeight / 2 + shortLabelHeight);
				parent.getFontRenderer().drawString("" + cost.get(i).stackSize, x, screenY + screenHeight / 2, elementColor);
			}
		});
	}
}
