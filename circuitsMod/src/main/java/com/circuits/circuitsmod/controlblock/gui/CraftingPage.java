package com.circuits.circuitsmod.controlblock.gui;

import java.util.Optional;

import com.circuits.circuitsmod.circuit.SpecializedCircuitUID;
import com.circuits.circuitsmod.controlblock.gui.model.CircuitCell;


public class CraftingPage extends ControlGuiPage {
	CircuitCell cell;
	CircuitSpecializationFields specialFields;
	
	public CraftingPage(final ControlGui parent, final CircuitCell cell) {
		super(parent);
		this.cell = cell;
		this.specialFields = new CircuitSpecializationFields(parent, screenX, screenY + (screenHeight / 2), 
				                                             screenWidth, (screenHeight / 2),
				                                             cell);
		
		
		parent.tileEntity.unsetCraftingCell();
		this.addElement(new TextButton(parent, "Back", screenX + screenWidth - shortLabelWidth, screenY, new Runnable() {
			@Override public void run() {
				parent.setDisplayPage(new ControlGuiMainPage(parent)); 
				parent.tileEntity.unsetCraftingCell();
			}
		}));
	}
	@Override
	public void draw() {
		Optional<String> configName = specialFields.getConfigName();
		if (configName.isPresent()) {
			Optional<SpecializedCircuitUID> uid = specialFields.getUID();
			parent.tileEntity.setCraftingCell(parent.user.getUniqueID(), uid.get());
		}
		
		parent.getFontRenderer().drawString(cell.getName(), screenX, screenY, elementColor);
		parent.drawHorizontalLine(screenX, screenX + screenWidth, screenY + 10, elementColor);
		/*
		cell.getInfo().getCost().ifPresent((cost) -> {
			for (int i = 0; i < cost.size(); i++) {
				int x = screenX + 20*i;
				parent.renderItemStack(cost.get(i), x, screenY + screenHeight / 2 + shortLabelHeight);
				parent.getFontRenderer().drawString("" + cost.get(i).stackSize, x, screenY + screenHeight / 2, elementColor);
			}
		});*/
	}
}
