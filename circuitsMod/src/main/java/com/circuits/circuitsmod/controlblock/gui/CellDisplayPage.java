package com.circuits.circuitsmod.controlblock.gui;

import java.util.List;
import java.util.Optional;

import net.minecraft.item.ItemStack;

import com.circuits.circuitsmod.CircuitsMod;
import com.circuits.circuitsmod.common.ItemUtils;
import com.circuits.circuitsmod.controlblock.gui.model.CircuitCell;
import com.circuits.circuitsmod.controlblock.gui.net.CircuitCostRequest;
import com.circuits.circuitsmod.controlblock.gui.net.CircuitCosts;
import com.circuits.circuitsmod.controlblock.gui.net.ServerGuiMessage;
import com.circuits.circuitsmod.controlblock.gui.net.ServerGuiMessage.GuiMessageKind;
import com.circuits.circuitsmod.controlblock.gui.widgets.ScrollableTextDisplay;
import com.circuits.circuitsmod.controlblock.gui.widgets.TextButton;
import com.circuits.circuitsmod.network.TypedMessage;

public class CellDisplayPage extends ControlGuiPage {
	private final CircuitCell cell;
	private final TextButton craftButton;
	private CircuitCosts costs;
	private ScrollableTextDisplay descripDisplay;
	
	public CellDisplayPage(final ControlGui parent, final CircuitCell cell) {
		super(parent);
		this.cell = cell;
		this.addElement(new TextButton(parent, "Back", screenX + screenWidth - shortLabelWidth, screenY, () -> {
				parent.setDisplayPage(new ControlGuiDirectoryPage(parent, cell.getParent().get()));
		}));
		
		this.addElement(new TextButton(parent, "Test", screenX + screenWidth - shortLabelWidth, screenY + screenHeight - shortLabelHeight, () -> {
				parent.setDisplayPage(new TestSettingsPage(parent, cell));
		}));
		
		this.craftButton = new TextButton(parent, "Craft", screenX, screenY + screenHeight - shortLabelHeight, () -> {
			CircuitCosts costs = getCosts();
			if (costs != null) {
				parent.setDisplayPage(new CraftingPage(parent, cell, getCosts()));
			}
		});
		
		this.descripDisplay = new ScrollableTextDisplay(parent, cell.getDescription());
		this.addElement(descripDisplay);
		
		CircuitsMod.network.sendToServer(new TypedMessage(new CircuitCostRequest(parent.user.getUniqueID(), parent.tileEntity.getPos(), cell.getUid())));
	}
	
	private CircuitCosts getCosts() {
		return this.costs;
	}
	
	@Override
	protected void handleScrollUp() {
		descripDisplay.handleScrollUp();
	}
	
	@Override
	protected void handleScrollDown() {
		descripDisplay.handleScrollDown();
	}
	
	
	
	public void draw() {
		//Render the header
		parent.getFontRenderer().drawString(cell.getName(), screenX, screenY, elementColor);
		parent.drawHorizontalLine(screenX, screenX + screenWidth, screenY + 10, elementColor);
		
		this.removeElement(craftButton);
		if (costs != null && costs.isUnlocked()) {
			this.addElement(craftButton);
			costs.getCost().ifPresent((cost) -> {
				for (int i = 0; i < cost.size(); i++) {
					ItemStack toRender = ItemUtils.getRenderableItemStack(cost.get(i));
					parent.renderItemStack(toRender, screenX + screenWidth - 2*shortLabelWidth - 10*i, screenY);
				}
			});
		}
		
		Optional<ServerGuiMessage> msg = parent.tileEntity.getGuiMessage(this.parent.user.getUniqueID());
		if (msg.isPresent()) {
			if (msg.get().getMessageKind().equals(GuiMessageKind.GUI_CIRCUIT_COSTS)) {
				this.costs = (CircuitCosts) msg.get().getData();
			}
		}
	}
	
}
