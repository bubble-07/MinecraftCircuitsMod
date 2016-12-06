package com.circuits.circuitsmod.controlblock.gui;

import java.util.List;
import java.util.Optional;

import com.circuits.circuitsmod.CircuitsMod;
import com.circuits.circuitsmod.controlblock.gui.model.CircuitCell;
import com.circuits.circuitsmod.controlblock.gui.net.CircuitCostRequest;
import com.circuits.circuitsmod.controlblock.gui.net.CircuitCosts;
import com.circuits.circuitsmod.controlblock.gui.net.ServerGuiMessage;
import com.circuits.circuitsmod.controlblock.gui.net.ServerGuiMessage.GuiMessageKind;

public class CellDisplayPage extends ControlGuiPage {
	private final CircuitCell cell;
	private final TextButton craftButton;
	private CircuitCosts costs;
	
	private static final int MAX_DESCRIP_LINES = 4;
	private static final int SCROLL_INCREMENT = 2;
	
	private int scrollY = 0;
	
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
			CircuitCosts costs = getCosts();
			if (costs != null) {
				parent.setDisplayPage(new CraftingPage(parent, cell, getCosts()));
			}
		});
		
		CircuitsMod.network.sendToServer(new CircuitCostRequest.Message(parent.user.getUniqueID(), parent.tileEntity.getPos(), cell.getUid()));
	}
	
	private CircuitCosts getCosts() {
		return this.costs;
	}
	
	private String truncateToLines(String str, int begin, int end) {
		List<String> lines = parent.getFontRenderer().listFormattedStringToWidth(str, screenWidth);
		String result = "";
		if (end >= lines.size()) {
			end = lines.size() - 1;
		}
		if (begin < 0) {
			begin = 0;
		}
		for (int i = begin; i <= end; i++) {
			result += lines.get(i) + "\n";
		}
		return result;
	}
	
	@Override
	protected void handleScrollUp() {
		this.scrollY = Math.max(0, this.scrollY - SCROLL_INCREMENT);
	}
	
	@Override
	protected void handleScrollDown() {
		this.scrollY += SCROLL_INCREMENT;
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
					parent.renderItemStack(cost.get(i), screenX + screenWidth - 2*shortLabelWidth - 10*i, screenY);
				}
			});
		}
		
		Optional<ServerGuiMessage> msg = parent.tileEntity.getGuiMessage(this.parent.user.getUniqueID());
		if (msg.isPresent()) {
			if (msg.get().getMessageKind().equals(GuiMessageKind.GUI_CIRCUIT_COSTS)) {
				this.costs = (CircuitCosts) msg.get().getData();
			}
		}
		
		String descrip = truncateToLines(cell.getDescription(), scrollY, scrollY + MAX_DESCRIP_LINES);

		parent.getFontRenderer().drawSplitString(descrip, 
				screenX, screenY + 16, screenWidth, elementColor);
	}
	
}
