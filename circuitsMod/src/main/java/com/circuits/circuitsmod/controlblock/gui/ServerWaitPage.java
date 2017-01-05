package com.circuits.circuitsmod.controlblock.gui;

import java.util.Optional;

import com.circuits.circuitsmod.circuit.CircuitInfoProvider;
import com.circuits.circuitsmod.circuit.SpecializedCircuitUID;
import com.circuits.circuitsmod.common.Log;
import com.circuits.circuitsmod.controlblock.gui.model.CircuitCell;
import com.circuits.circuitsmod.recorder.RecordingState;
import com.circuits.circuitsmod.tester.TestState;

//Shown while waiting for the server to send the model
public class ServerWaitPage extends ControlGuiPage {
	public ServerWaitPage(ControlGui parent) {
		super(parent);
	}
	
	public void loadPage() {
		if (parent.tileEntity.getState() != null) {
			
			if (parent.tileEntity.getState() instanceof TestState) {
				TestState testState = ((TestState) parent.tileEntity.getState());
				
				//Must've been testing! Display the test progress page instead!
				SpecializedCircuitUID uid = testState.getUID();
				
				Optional<CircuitCell> circuit = CircuitInfoProvider.getCellFor(uid.getUID());
				if (!circuit.isPresent()) {
					Log.internalError("The client doesn't know about the circuit with uid " + uid + " on the server wait page!");
					return;
				}
				TestSettingsPage settingsPage = new TestSettingsPage(parent, circuit.get());
				settingsPage.setCircuitOptions(uid.getOptions());
				settingsPage.setTestConfigs(testState.config);
				parent.setDisplayPage(new TestProgressPage(settingsPage));
			}
			else if (parent.tileEntity.getState() instanceof RecordingState) {
				RecordingState recState = (RecordingState) parent.tileEntity.getState();
				ControlCustomCircuitMainPage mainPage = new ControlCustomCircuitMainPage(parent);
				mainPage.setName(recState.getCircuitName());
				mainPage.setTestConfigs(recState.config);
				parent.setDisplayPage(new RecordingProgressPage(mainPage));
			}

		}
		else {
			parent.setDisplayPage(new ControlGuiDirectoryPage(parent, parent.model.getRootDirectory()));
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
