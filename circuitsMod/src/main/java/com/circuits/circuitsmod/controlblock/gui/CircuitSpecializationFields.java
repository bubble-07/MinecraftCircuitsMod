package com.circuits.circuitsmod.controlblock.gui;

import java.util.List;
import java.util.Optional;

import com.circuits.circuitsmod.CircuitsMod;
import com.circuits.circuitsmod.circuit.CircuitConfigOptions;
import com.circuits.circuitsmod.circuit.SpecializedCircuitUID;
import com.circuits.circuitsmod.common.Log;
import com.circuits.circuitsmod.controlblock.gui.TextEntryBox.IntEntryBox;
import com.circuits.circuitsmod.controlblock.gui.model.CircuitCell;
import com.circuits.circuitsmod.controlblock.gui.net.ServerGuiMessage;
import com.circuits.circuitsmod.controlblock.gui.net.ServerGuiMessage.GuiMessageKind;
import com.circuits.circuitsmod.controlblock.gui.net.ServerGuiMessage.SpecializationInfo;
import com.circuits.circuitsmod.controlblock.gui.net.SpecializationValidationRequest;
import com.google.common.collect.Lists;

/**
 * Represents a gui widget to configure a circuit specialization (list of integer fields)
 * and obtain information back from the server about the circuit
 * @author bubble-07
 *
 */
public class CircuitSpecializationFields extends UIElement implements UIFocusable {
	
	List<IntEntryBox> entryBoxes = Lists.newArrayList();
	CircuitCell cell;
	int numBoxes;
	private static final int BOX_WIDTH = 20;
	private static final int BOX_SPACING = 2;
	String configName = null;
	SpecializationInfo infos = null;
	
	Optional<int[]> oldOptions = Optional.empty();

	public CircuitSpecializationFields(ControlGui parent, int x, int y,
			int width, int height, CircuitCell cell) {
		super(parent, x, y, width, height);
		
		this.configName = null;
		
		this.cell = cell;
		this.numBoxes = cell.getInfo().getNumSpecializationSlots();
		for (int i = 0; i < this.numBoxes; i++) {
			int xOffset = x + (i * (BOX_WIDTH + BOX_SPACING));
			IntEntryBox entryBox = new IntEntryBox(parent, xOffset, y + ControlGuiPage.shortLabelHeight, BOX_WIDTH, 
					                               ControlGuiPage.shortLabelHeight, 1);
			entryBoxes.add(entryBox);
		}
		serverValidateInfo();
	}
	
	public Optional<SpecializationInfo> getSpecializationInfo() {
		return Optional.ofNullable(infos);
	}
	
	private void serverValidateInfo() {
		Optional<int[]> opts = this.getOptions();
		if (!opts.isPresent()) {
			this.configName = null;
			return;
		}
		SpecializedCircuitUID uid = new SpecializedCircuitUID(this.cell.getUid(), new CircuitConfigOptions(opts.get()));
		CircuitsMod.network.sendToServer(
				new SpecializationValidationRequest.Message(parent.user.getUniqueID(), parent.tileEntity.getPos(), uid));
	}
	
	public Optional<String> getConfigName() {
		return Optional.ofNullable(configName);
	}
	
	public void setOptions(CircuitConfigOptions opts) {
		if (opts.asInts().length != this.numBoxes) {
			Log.internalError("Expected to have " + this.numBoxes + " config options, but got " + opts.asInts().length);
			return;
		}
		for (int i = 0; i < this.numBoxes; i++) {
			entryBoxes.get(i).setValue(opts.asInts()[i]);
		}
	}
	
	public Optional<SpecializedCircuitUID> getUID() {
		return getOptions().map((arr) -> new SpecializedCircuitUID(this.cell.getUid(), new CircuitConfigOptions(arr)));
	}
	
	private Optional<int[]> getOptions() {
		int[] result = new int[numBoxes];
		for (int i = 0; i < result.length; i++) {
			IntEntryBox entryBox = entryBoxes.get(i);
			if (!entryBox.getValue().isPresent()) {
				return Optional.empty();
			}
			result[i] = entryBox.getValue().get();
		}
		return Optional.of(result);
	}
	
	private void handleLocalUpdates() {
		Optional<int[]> newOptions = getOptions();
		if (!newOptions.equals(oldOptions)) {
			serverValidateInfo();
			this.oldOptions = newOptions;
		}
	}
	
	private void handleServerUpdates() {
		Optional<ServerGuiMessage> msg = parent.tileEntity.getGuiMessage(this.parent.user.getUniqueID());
		if (msg.isPresent()) {
			if (msg.get().getMessageKind().equals(GuiMessageKind.GUI_SPECIALIZATON_INFO)) {
				SpecializationInfo info = (ServerGuiMessage.SpecializationInfo) msg.get().getData();
				String name = info.getName();
				this.configName = name;
				this.infos = info;
			}
		}
	}

	@Override
	public void draw() {
		handleLocalUpdates();
		handleServerUpdates();
		
		//Draw the label at the top if we have a valid configuration, otherwise draw "Invalid config"
		String label = this.configName == null ? "Invalid Config" : this.configName;
		
		if (parent.getFontRenderer().getStringWidth(label) > ControlGuiPage.screenWidth) {
			//Truncate the label to omit the stuff before the opening paren
			int ind = label.indexOf("(");
			if (ind != -1) {
				label = label.substring(ind, label.length());
			}
		}
		
		parent.getFontRenderer().drawString(label, this.x, this.y, ControlGuiPage.elementColor);
		
		for (IntEntryBox entryBox : entryBoxes) {
			entryBox.draw();
		}
	}
	
	@Override
	public boolean handleClick(int mouseX, int mouseY) {
		return entryBoxes.stream().map((box) -> box.handleClick(mouseX, mouseY))
				         .reduce(false, Boolean::logicalOr);
	}
	@Override
	public void handleKey(char typed, int keyCode) {
		if (keyCode == 15) {
			for (int i = 0; i < entryBoxes.size(); i++) {
				if (entryBoxes.get(i).hasFocus()) {
					entryBoxes.get(i).unFocus();
					entryBoxes.get((i + 1) % entryBoxes.size()).requestFocus();
					break;
				}
			}
		}
		else {
			for (IntEntryBox box : entryBoxes) {
				box.handleKey(typed, keyCode);
			}
		}
	}

	@Override
	public void requestFocus() {
		//Do nothing, we handle this with other boxes
	}

	@Override
	public void unFocus() {
		for (int i = 0; i < entryBoxes.size(); i++) {
			entryBoxes.get(i).unFocus();
		}
	}

	@Override
	public boolean hasFocus() {
		return entryBoxes.stream().anyMatch(IntEntryBox::hasFocus);
	}
}
