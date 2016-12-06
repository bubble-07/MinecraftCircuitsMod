package com.circuits.circuitsmod.controlblock.gui;

import java.util.Optional;

import com.circuits.circuitsmod.circuit.CircuitInfoProvider;
import com.circuits.circuitsmod.circuit.SpecializedCircuitUID;
import com.circuits.circuitsmod.common.Log;
import com.circuits.circuitsmod.controlblock.gui.model.CircuitCell;

//Shown while waiting for the server to send the model
public class ServerWaitPage extends ControlGuiPage {
	public ServerWaitPage(ControlGui parent) {
		super(parent);
	}
	
	public void loadPage() {
		if (parent.tileEntity.getState() != null) {
			//Must've been testing! Display the test progress page instead!
			//TODO: Make this thing keep the previous config, too!
			SpecializedCircuitUID uid = parent.tileEntity.getState().circuitUID;
			
			Optional<CircuitCell> circuit = CircuitInfoProvider.getCellFor(uid.getUID());
			if (!circuit.isPresent()) {
				Log.internalError("The client doesn't know about the circuit with uid " + uid + " on the server wait page!");
				return;
			}
			TestSettingsPage settingsPage = new TestSettingsPage(parent, circuit.get());
			settingsPage.setCircuitOptions(uid.getOptions());
			parent.setDisplayPage(new TestProgressPage(settingsPage));
		}
		else {
			parent.setDisplayPage(new ControlGuiMainPage(parent));
		}
	}
	
	public void draw() {
		parent.getFontRenderer().drawString("Waiting For Server", screenX, screenY + (screenHeight / 2), ControlGuiPage.elementColor);
		if (parent.model == null) {
			if (CircuitInfoProvider.getCircuitListModel() != null) {
				parent.model = CircuitInfoProvider.getCircuitListModel();
				loadPage();
			}
			return;
		}
		else {
			loadPage();
		}
	}
}
