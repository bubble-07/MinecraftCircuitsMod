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
import com.circuits.circuitsmod.controlblock.gui.net.SpecializationValidationRequest;
import com.google.common.collect.Lists;

/**
 * Represents a gui widget to configure a circuit specialization (list of integer fields)
 * @author bubble-07
 *
 */
public class CircuitSpecializationFields extends UIElement {
	
	List<IntEntryBox> entryBoxes = Lists.newArrayList();
	CircuitCell cell;
	int numBoxes;
	private static final int BOX_WIDTH = 20;
	private static final int BOX_SPACING = 2;
	String configName = null;
	
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
				String name = (String) msg.get().getData();
				this.configName = name;
			}
		}
	}

	@Override
	public void draw() {
		handleLocalUpdates();
		handleServerUpdates();
		
		//Draw the label at the top if we have a valid configuration, otherwise draw "Invalid config"
		String label = this.configName == null ? "Invalid Config" : this.configName;
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
		for (IntEntryBox box : entryBoxes) {
			box.handleKey(typed, keyCode);
		}
	}
}
