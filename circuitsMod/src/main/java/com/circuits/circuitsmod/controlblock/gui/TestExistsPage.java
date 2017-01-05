package com.circuits.circuitsmod.controlblock.gui;


public class TestExistsPage extends ControlGuiPage {

	int countdown = 60;
	
	public TestExistsPage(ControlGui parent) {
		super(parent);
	}

	@Override
	protected void draw() {
		countdown--;
		parent.getFontRenderer().drawString("A Test or Recording Already Exists! \n Displaying...", screenX, screenY + (screenHeight / 2), ControlGuiPage.elementColor);
		if (countdown <= 0) {
			parent.setDisplayPage(new ServerWaitPage(parent));
		}
	}
}
