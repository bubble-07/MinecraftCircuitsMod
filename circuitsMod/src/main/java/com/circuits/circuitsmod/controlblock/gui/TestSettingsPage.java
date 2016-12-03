package com.circuits.circuitsmod.controlblock.gui;

import java.util.Optional;

import com.circuits.circuitsmod.circuit.CircuitInfo;
import com.circuits.circuitsmod.controlblock.frompoc.Microchips;
import com.circuits.circuitsmod.controlblock.tester.TestConfig;
import com.circuits.circuitsmod.controlblock.tester.net.TestRequest;

public class TestSettingsPage extends ControlGuiPage {
	public final CircuitInfo cell;
	private TextEntryBox.IntEntryBox delayBox;
	public TestSettingsPage(final ControlGui parent, final CircuitInfo cell) {
		super(parent);
		this.cell = cell;
		addElement(new TextButton(parent, "Back", screenX + screenWidth - shortLabelWidth, screenY, new Runnable() {
			@Override public void run() {
				parent.setDisplayPage(new CellDisplayPage(parent, cell)); }
		}));
		
		final TestSettingsPage thiz = this;
		
		delayBox = new TextEntryBox.IntEntryBox(parent, screenX, screenY + (screenHeight / 2), 
				shortLabelWidth, shortLabelHeight, 20);
		addElement(delayBox);
		
		this.addElement(new TextButton(parent, "Test", screenX + screenWidth - shortLabelWidth, screenY + screenHeight - shortLabelHeight, new Runnable() {
			@Override public void run() {
				if (parent.tileEntity.testInProgress()) {
					parent.setDisplayPage(new TestExistsPage(parent, cell));
				}
				else {
					TestConfig config = getEnteredConfig();
					if (config != null) {
						parent.setDisplayPage(new TestProgressPage(thiz.getThis()));
						//Testing, ENGAGE
						Microchips.network.sendToServer(new TestRequest.Message(cell.getName(), parent.tileEntity.getPos(), config));
					}
				}
			}
		}));
		
	}
	
	/**
	 * Returns the user-entered testing config (if valid), otherwise, return null and focus on the first errant field
	 * @return
	 */
	private TestConfig getEnteredConfig() {
		Optional<Integer> tickDelay = delayBox.getValue();
		if (!tickDelay.isPresent()) {
			delayBox.requestFocus();
		}
		return new TestConfig(tickDelay.get());
		
	}
	
	private TestSettingsPage getThis() {
		return this;
	}
	
	public void draw() {
		//Render the header
		parent.getFontRenderer().drawString(cell.getName(), screenX, screenY, elementColor);
		parent.drawHorizontalLine(screenX, screenX + screenWidth, screenY + 10, elementColor);
		parent.getFontRenderer().drawString("Delay (Ticks)", screenX, 
											screenY + (screenHeight / 2) - shortLabelHeight, ControlGuiPage.elementColor);
	}
}