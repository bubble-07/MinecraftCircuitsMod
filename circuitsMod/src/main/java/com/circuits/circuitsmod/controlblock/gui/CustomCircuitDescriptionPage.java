package com.circuits.circuitsmod.controlblock.gui;

import com.circuits.circuitsmod.controlblock.gui.model.CustomCircuitInfo;
import com.circuits.circuitsmod.controlblock.gui.widgets.TextButton;
import com.circuits.circuitsmod.controlblock.gui.widgets.TextEntryBox;

public class CustomCircuitDescriptionPage extends ControlGuiPage {
	
	private final CustomCircuitRecordingReceiptPage receiptPage;
	private TextEntryBox descripBox;
	

	public CustomCircuitDescriptionPage(CustomCircuitRecordingReceiptPage receiptPage) {
		super(receiptPage.parent);
		
		this.receiptPage = receiptPage;
		
		addElement(new TextButton(parent, "Back", screenX + screenWidth - shortLabelWidth, screenY, () -> {
			saveToInfo();
			parent.setDisplayPage(receiptPage);
		}));
		
		this.descripBox = new TextEntryBox(parent, screenX, screenY + shortLabelHeight, screenWidth, screenHeight - 3 * shortLabelHeight);
		this.descripBox.setText(getInfo().getDescription());
		this.addElement(descripBox);
		
		this.addElement(new TextButton(parent, "Next", screenX + screenWidth - shortLabelWidth, screenY + screenHeight - shortLabelHeight, () -> {
				saveToInfo();
				parent.setDisplayPage(new CustomCircuitIconPage(CustomCircuitDescriptionPage.this));
		}));
	}
	
	public CustomCircuitInfo getInfo() {
		return receiptPage.getInfo();
	}
	
	private void saveToInfo() {
		getInfo().setDescription(this.descripBox.getText());
	}


	@Override
	protected void draw() {
		parent.getFontRenderer().drawString("Description", screenX, screenY, elementColor);
	}

}


