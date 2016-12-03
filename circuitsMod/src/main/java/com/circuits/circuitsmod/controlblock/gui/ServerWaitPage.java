package com.circuits.circuitsmod.controlblock.gui;

import java.util.Optional;

import com.circuits.circuitsmod.circuit.CircuitInfo;
import com.circuits.circuitsmod.controlblock.frompoc.Microchips;

//Shown while waiting for the server to send the model
public class ServerWaitPage extends ControlGuiPage {
	public ServerWaitPage(ControlGui parent) {
		super(parent);
	}
	
	public void loadPage() {
		if (parent.tileEntity.getState() != null) {
			//Must've been testing! Display the test progress page instead!
			//TODO: Make this thing keep the previous config, too!
			Optional<CircuitInfo> circuit = Common.getCellFromName(parent.tileEntity.getState().circuitName);
			if (!circuit.isPresent()) {
				System.out.println("WEIRD");
				return;
			}
			parent.setDisplayPage(new TestProgressPage(new TestSettingsPage(parent, circuit.get())));
		}
		else {
			parent.setDisplayPage(new ControlGuiMainPage(parent));
		}
	}
	
	public void draw() {
		parent.getFontRenderer().drawString("Waiting For Server", screenX, screenY + (screenHeight / 2), ControlGuiPage.elementColor);
		if (parent.model == null) {
			if (Microchips.mainModel != null) {
				parent.model = Microchips.mainModel;
				loadPage();
			}
			return;
		}
		else {
			loadPage();
		}
	}
}
