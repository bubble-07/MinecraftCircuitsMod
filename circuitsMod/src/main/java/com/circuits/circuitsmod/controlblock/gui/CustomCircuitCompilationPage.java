package com.circuits.circuitsmod.controlblock.gui;

import java.util.Optional;

import net.minecraftforge.fml.client.config.GuiMessageDialog;

import com.circuits.circuitsmod.circuit.CircuitInfoProvider;
import com.circuits.circuitsmod.controlblock.gui.net.ServerGuiMessage;
import com.circuits.circuitsmod.controlblock.gui.net.ServerGuiMessage.GuiMessageKind;
import com.circuits.circuitsmod.controlblock.gui.widgets.ScrollableTextDisplay;
import com.circuits.circuitsmod.controlblock.gui.widgets.TextButton;

public class CustomCircuitCompilationPage extends ControlGuiPage {
	
	private boolean success = false;
	private int autoKick = -1;
	
	private final CustomCircuitIconPage prev;

	public CustomCircuitCompilationPage(CustomCircuitIconPage prev) {
		super(prev.parent);
		this.prev = prev;
	}
	
	private void exitPage() {
		if (!success) {
			parent.setDisplayPage(prev);
			return;
		}
		//Go to the newly-created circuit cell page, if possible
		ControlGuiDirectoryPage customRootPage = new ControlGuiDirectoryPage(parent, CircuitInfoProvider.getCircuitListModel().getCustomDirectory());
		ControlGuiPage childPage = customRootPage.getChildPage(parent.user.getName());
		if (childPage instanceof ControlGuiDirectoryPage) {
			childPage = ((ControlGuiDirectoryPage) childPage).getChildPage(prev.getInfo().getName());
		}
		parent.setDisplayPage(childPage);
	}

	@Override
	protected void draw() {
		if (autoKick == -1) {	
			parent.getFontRenderer().drawString("Compiling Implementation", 
	                screenX, screenY + (screenHeight / 2), elementColor);
			handleServerUpdates();
		}
		else {
			String resultMessage = success ? "Success" : "Failed - Check Log";
			parent.getFontRenderer().drawString(resultMessage, 
	                screenX, screenY + (screenHeight / 2), elementColor);
			autoKick--;
			if (autoKick == 0) {
				exitPage();
			}
		}
	}
	
	public void handleServerUpdates() {
		Optional<ServerGuiMessage> msg = parent.tileEntity.getGuiMessage(this.parent.user.getUniqueID());
		if (msg.isPresent()) {
			if (msg.get().getMessageKind().equals(GuiMessageKind.GUI_COMPILATION_RESULT)) {
				this.success = ((ServerGuiMessage.CompilationResult) msg.get().getData()).isSuccess();
				this.autoKick = 20;
			}
		}
	}

}
