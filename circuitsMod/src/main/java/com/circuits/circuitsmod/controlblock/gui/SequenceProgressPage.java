package com.circuits.circuitsmod.controlblock.gui;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

import net.minecraft.util.math.BlockPos;

import com.circuits.circuitsmod.CircuitsMod;
import com.circuits.circuitsmod.common.BusData;
import com.circuits.circuitsmod.controlblock.gui.widgets.TextButton;
import com.circuits.circuitsmod.controlblock.tester.net.TestStopRequest;
import com.circuits.circuitsmod.network.TypedMessage;
import com.circuits.circuitsmod.tester.SequenceReaderState;

public abstract class SequenceProgressPage<PrevPageType extends ControlGuiPage> extends ControlGuiPage {
	protected final PrevPageType prev; 
	int successBootTimer = 40;
	
	public SequenceProgressPage(final PrevPageType prev) {
		super(prev.parent);
		this.prev = prev;
		addElement(new TextButton(parent, "Back", screenX + screenWidth - shortLabelWidth, screenY, () -> {
				parent.setDisplayPage(prev);
		}));
		addElement(new TextButton(parent, "Stop", screenX + screenWidth - shortLabelWidth, screenY + screenHeight - shortLabelHeight, () -> {
			stopAndGoToPrevPage();
		}));
	}
	
	private void stopAndGoToPrevPage() {
		parent.tileEntity.stopSequence();
		CircuitsMod.network.sendToServer(new TypedMessage(new TestStopRequest(parent.user.getUniqueID(), parent.tileEntity.getPos())));
		parent.setDisplayPage(prev);
	}
	
	public abstract ControlGuiPage getSuccessPage();
	public abstract boolean isRightSequenceStateType(Class<?> clazz);
	public abstract Serializable getSuccessStopRequest(UUID playerId, BlockPos pos);
	
	public void draw() {
		float progress = parent.tileEntity.getProgress();
		parent.drawBox(screenX + 6, screenY + (screenHeight / 2) - 2, screenWidth - 12, shortLabelHeight + 4);
		parent.drawBox(screenX + 10, screenY + (screenHeight / 2), (int) (progress * (screenWidth - 20)), shortLabelHeight);
		
		if (parent.tileEntity.getState() != null && !isRightSequenceStateType(parent.tileEntity.getState().getClass())) {
			stopAndGoToPrevPage();
			return;
		}
		
		SequenceReaderState state = parent.tileEntity.getState();
		if (state != null && state.finished) {
			String toDisplay = "";
			if (state.success) {
				toDisplay = "Success!";
				successBootTimer--;
				if (successBootTimer < 0) {
					//Send a message to the server to stop the test
					parent.tileEntity.stopSequence();
					CircuitsMod.network.sendToServer(new TypedMessage(getSuccessStopRequest(parent.user.getUniqueID(), parent.tileEntity.getPos())));
					
					parent.setDisplayPage(getSuccessPage());
					return;
				}
			}
			else {
				toDisplay = "Failure";
				//Display information about the failed test case
				List<BusData> inputCase = state.getInputCase();
				String failedCaseDisplay = "Unknown";
				if (state.getFailureReason().isPresent()) {
					failedCaseDisplay = state.getFailureReason().get();
				}
				else {
					failedCaseDisplay = "Failed on Input: ";
					failedCaseDisplay += BusData.listToDispString(inputCase);
				}
				parent.getFontRenderer().drawString(failedCaseDisplay, screenX, (screenHeight / 5), ControlGuiPage.elementColor);
				parent.getFontRenderer().drawString("on test tick: " + state.getTick(), screenX, (screenHeight / 5) + ControlGuiPage.shortLabelHeight, 
						                            ControlGuiPage.elementColor);
			}
			parent.getFontRenderer().drawString(toDisplay, screenX + 12, screenY + (screenHeight / 2) + 2, ControlGuiPage.elementColor);

		}
		
	}
}
