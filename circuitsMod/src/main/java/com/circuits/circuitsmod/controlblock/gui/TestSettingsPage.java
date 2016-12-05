package com.circuits.circuitsmod.controlblock.gui;

import java.util.Optional;

import com.circuits.circuitsmod.CircuitsMod;
import com.circuits.circuitsmod.circuit.CircuitConfigOptions;
import com.circuits.circuitsmod.circuit.CircuitInfo;
import com.circuits.circuitsmod.circuit.SpecializedCircuitUID;
import com.circuits.circuitsmod.common.Log;
import com.circuits.circuitsmod.controlblock.gui.model.CircuitCell;
import com.circuits.circuitsmod.controlblock.tester.TestConfig;
import com.circuits.circuitsmod.controlblock.tester.net.TestRequest;

public class TestSettingsPage extends ControlGuiPage {
	public final CircuitCell cell;
	private TextEntryBox.IntEntryBox delayBox;
	private CircuitSpecializationFields circuitFields;
	
	public void setCircuitOptions(CircuitConfigOptions configs) {
		circuitFields.setOptions(configs);
	}
	
	public TestSettingsPage(final ControlGui parent, final CircuitCell cell) {
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
		
		circuitFields = new CircuitSpecializationFields(parent, screenX, screenY + (screenHeight / 2) + shortLabelHeight,
				                                        screenWidth, (screenHeight / 2), cell);
		addElement(circuitFields);
		
		
		this.addElement(new TextButton(parent, "Test", screenX + screenWidth - shortLabelWidth, screenY + screenHeight - shortLabelHeight, new Runnable() {
			@Override public void run() {
				if (parent.tileEntity.testInProgress()) {
					parent.setDisplayPage(new TestExistsPage(parent, cell));
				}
				else {
					TestConfig config = getEnteredConfig();
					if (config != null && circuitFields.getConfigName().isPresent()) {
						Optional<SpecializedCircuitUID> uid = circuitFields.getUID();
						if (!uid.isPresent()) {
							Log.internalError("Circuit specialization has a name, but no valid UID");
							return;
						}
						parent.setDisplayPage(new TestProgressPage(thiz.getThis()));
						//Testing, ENGAGE
						CircuitsMod.network.sendToServer(
								new TestRequest.Message(parent.user.getUniqueID(),
										                uid.get(), parent.tileEntity.getPos(), config));
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