package com.circuits.circuitsmod.controlblock.gui;

import java.util.Optional;

import com.circuits.circuitsmod.circuit.SpecializedCircuitUID;
import com.circuits.circuitsmod.controlblock.gui.model.CircuitCell;
import com.circuits.circuitsmod.controlblock.gui.net.CircuitCosts;


public class CraftingPage extends ControlGuiPage {
	CircuitCell cell;
	CircuitSpecializationFields specialFields;
	CircuitCosts costs;
	
	public CraftingPage(final ControlGui parent, final CircuitCell cell, final CircuitCosts costs) {
		super(parent);
		this.cell = cell;
		this.costs = costs;
		this.specialFields = new CircuitSpecializationFields(parent, screenX + (screenWidth / 2), screenY + 15, 
				                                             screenWidth, (screenHeight / 2),
				                                             cell);
		
		
		parent.tileEntity.unsetCraftingCell();
		this.addElement(new TextButton(parent, "Back", screenX + screenWidth - shortLabelWidth, screenY, new Runnable() {
			@Override public void run() {
				parent.setDisplayPage(new ControlGuiMainPage(parent)); 
				parent.tileEntity.unsetCraftingCell();
			}
		}));
		this.addElement(specialFields);
	}
	@Override
	public void draw() {
		Optional<String> configName = specialFields.getConfigName();
		if (configName.isPresent()) {
			Optional<SpecializedCircuitUID> uid = specialFields.getUID();
			if (uid.isPresent()) {
				parent.tileEntity.setCraftingCell(parent.user.getUniqueID(), uid.get());
				parent.tileEntity.updateCraftingGrid();
			}
		}
		
		parent.getFontRenderer().drawString(cell.getName(), screenX, screenY, elementColor);
		parent.drawHorizontalLine(screenX, screenX + screenWidth, screenY + 10, elementColor);
		
		costs.getCost().ifPresent((cost) -> {
			for (int i = 0; i < cost.size(); i++) {
				int x = screenX + 20*i;
				parent.renderItemStack(cost.get(i), x, screenY + screenHeight / 2 + shortLabelHeight);
				parent.getFontRenderer().drawString("" + cost.get(i).stackSize, x, screenY + screenHeight / 2, elementColor);
			}
		});
	}
}
