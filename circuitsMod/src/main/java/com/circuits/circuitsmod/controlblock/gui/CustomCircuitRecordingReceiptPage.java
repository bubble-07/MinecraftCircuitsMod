package com.circuits.circuitsmod.controlblock.gui;

import java.util.Optional;

import com.circuits.circuitsmod.controlblock.gui.model.CustomCircuitInfo;
import com.circuits.circuitsmod.controlblock.gui.net.ServerGuiMessage;
import com.circuits.circuitsmod.controlblock.gui.net.ServerGuiMessage.GuiMessageKind;
import com.circuits.circuitsmod.controlblock.gui.net.ServerGuiMessage.SpecializationInfo;
import com.circuits.circuitsmod.controlblock.gui.widgets.ScrollableTextDisplay;
import com.circuits.circuitsmod.controlblock.gui.widgets.TextButton;

public class CustomCircuitRecordingReceiptPage extends ControlGuiPage {

	private final ControlCustomCircuitMainPage prev;
	private ScrollableTextDisplay textDisplay = null;
	
	public CustomCircuitRecordingReceiptPage(ControlCustomCircuitMainPage prev) {
		super(prev.parent);
		this.prev = prev;
	}
	
	public CustomCircuitInfo getInfo() {
		return prev.getInfo();
	}

	@Override
	protected void draw() {
		
		if (textDisplay == null) {
			parent.getFontRenderer().drawString("Waiting for Server Data", 
				                            screenX, screenY + (screenHeight / 2), elementColor);
		}
		else {
			parent.getFontRenderer().drawString("Datasheet", 
                    screenX, screenY, elementColor);
		}
		handleServerUpdates();
	}
	public void handleServerUpdates() {
		Optional<ServerGuiMessage> msg = parent.tileEntity.getGuiMessage(this.parent.user.getUniqueID());
		if (msg.isPresent()) {
			if (msg.get().getMessageKind().equals(GuiMessageKind.GUI_RECORDING_DATA)) {
				ServerGuiMessage.RecordingData infos = (ServerGuiMessage.RecordingData) msg.get().getData();
				prev.getInfo().setRecording(infos.getRecording());
				
				if (textDisplay != null) {
					this.removeElement(textDisplay);
				}
				textDisplay = new ScrollableTextDisplay(parent, infos.getRecording().toTableDisplayString());
				this.addElement(textDisplay);
				
				addElement(new TextButton(parent, "Back", screenX + screenWidth - shortLabelWidth, screenY, () -> {
					parent.setDisplayPage(prev);
				}));
				addElement(new TextButton(parent, "Next", screenX + screenWidth - shortLabelWidth, screenY + screenHeight - shortLabelHeight, () -> {
					parent.setDisplayPage(new CustomCircuitDescriptionPage(this));
				}));
			}
		}
	}

}
