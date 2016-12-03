package com.circuits.circuitsmod.controlblock.gui;

import com.circuits.circuitsmod.circuit.CircuitInfo;


public class TestExistsPage extends ControlGuiPage {

	int countdown = 60;
	
	public TestExistsPage(ControlGui parent, CircuitInfo cell) {
		super(parent);
	}

	@Override
	protected void draw() {
		countdown--;
		parent.getFontRenderer().drawString("A Test Already Exists! \n Displaying Current Test...", screenX, screenY + (screenHeight / 2), ControlGuiPage.elementColor);
		if (countdown <= 0) {
			parent.setDisplayPage(new ServerWaitPage(parent));
		}
	}
}
