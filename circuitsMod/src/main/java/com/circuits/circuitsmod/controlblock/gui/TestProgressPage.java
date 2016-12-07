package com.circuits.circuitsmod.controlblock.gui;

import com.circuits.circuitsmod.CircuitsMod;
import com.circuits.circuitsmod.controlblock.gui.model.CircuitCell;
import com.circuits.circuitsmod.controlblock.tester.net.TestStopRequest;
import com.circuits.circuitsmod.tester.TestState;

public class TestProgressPage extends ControlGuiPage {
	private final CircuitCell cell;
	private final TestSettingsPage prev; 
	int successBootTimer = 40;
	
	public TestProgressPage(final TestSettingsPage prev) {
		super(prev.parent);
		this.cell = prev.cell;
		this.prev = prev;
		addElement(new TextButton(parent, "Back", screenX + screenWidth - shortLabelWidth, screenY, () -> {
				parent.setDisplayPage(prev);
		}));
		addElement(new TextButton(parent, "Stop", screenX + screenWidth - shortLabelWidth, screenY + screenHeight - shortLabelHeight, () -> {
				parent.tileEntity.stopTest();
				CircuitsMod.network.sendToServer(new TestStopRequest.Message(parent.tileEntity.getPos()));
				parent.setDisplayPage(prev);
		}));
	}
	public void draw() {
		float progress = parent.tileEntity.getTestProgress();
		parent.drawBox(screenX + 6, screenY + (screenHeight / 2) - 2, screenWidth - 12, shortLabelHeight + 4);
		parent.drawBox(screenX + 10, screenY + (screenHeight / 2), (int) (progress * (screenWidth - 20)), shortLabelHeight);
		
		TestState state = parent.tileEntity.getState();
		if (state != null && state.finished) {
			String toDisplay = "";
			if (state.success) {
				toDisplay = "Success!";
				successBootTimer--;
				if (successBootTimer < 0) {
					//Send a message to the server to stop the test
					parent.tileEntity.stopTest();
					CircuitsMod.network.sendToServer(new TestStopRequest.Message(parent.tileEntity.getPos()));
					
					parent.setDisplayPage(new CellDisplayPage(parent, cell));
					return;
				}
			}
			else {
				toDisplay = "Failure";
			}
			parent.getFontRenderer().drawString(toDisplay, screenX + 12, screenY + (screenHeight / 2) + 2, ControlGuiPage.elementColor);

		}
		
	}
}
