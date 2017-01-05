package com.circuits.circuitsmod.controlblock.gui;

import java.util.Optional;

import net.minecraft.util.StringUtils;

import com.circuits.circuitsmod.CircuitsMod;
import com.circuits.circuitsmod.circuit.CircuitInfoProvider;
import com.circuits.circuitsmod.controlblock.gui.model.CircuitDirectory;
import com.circuits.circuitsmod.controlblock.gui.model.CustomCircuitInfo;
import com.circuits.circuitsmod.controlblock.gui.widgets.TextButton;
import com.circuits.circuitsmod.controlblock.gui.widgets.TextEntryBox;
import com.circuits.circuitsmod.controlblock.tester.net.RecordingRequest;
import com.circuits.circuitsmod.network.TypedMessage;
import com.circuits.circuitsmod.tester.TestConfig;

/**
 * First page for creating a custom circuit. Contains name entry
 * and delay config
 * @author bubble-07
 *
 */
//TODO: Can this be merged elegantly with the TestSettingsPage? Or no?
public class ControlCustomCircuitMainPage extends ControlGuiPage {
	CircuitDirectory customDir;
	CustomCircuitInfo info = new CustomCircuitInfo();
	
	TextEntryBox nameBox;
	TextEntryBox.IntEntryBox delayBox;
	
	public CustomCircuitInfo getInfo() {
		return this.info;
	}
	
	public void setName(String name) {
		this.nameBox.setText(name);
	}
	
	public String getName() {
		return this.nameBox.getText();
	}
	
	public void setTestConfigs(TestConfig config) {
		this.delayBox.setValue(config.tickDelay);
	}
	
	public ControlCustomCircuitMainPage(ControlGui parent) {
		this(parent, CircuitInfoProvider.getCircuitListModel().getCustomDirectory());
	}
	
	private Optional<TestConfig> getEnteredConfig() {
		Optional<Integer> tickDelay = delayBox.getValue();
		if (!tickDelay.isPresent()) {
			delayBox.requestFocus();
		}
		return tickDelay.map((delay) -> new TestConfig(delay));
	}

	public ControlCustomCircuitMainPage(ControlGui parent, CircuitDirectory customDir) {
		super(parent);
		this.customDir = customDir;
		addElement(new TextButton(parent, "Back", screenX + screenWidth - shortLabelWidth, screenY, new Runnable() {
			@Override public void run() {
				parent.setDisplayPage(new ControlGuiDirectoryPage(parent, customDir)); 
			}
		}));
		delayBox = new TextEntryBox.IntEntryBox(parent, screenX, screenY + (screenHeight / 2) + shortLabelHeight, 
				shortLabelWidth, shortLabelHeight, 20);
		nameBox = new TextEntryBox(parent, screenX, screenY + shortLabelHeight, (3 * screenWidth) / 4, shortLabelHeight);
		this.addElement(delayBox);
		this.addElement(nameBox);
		
		ControlCustomCircuitMainPage thiz = this;
		
		this.addElement(new TextButton(parent, "Rec", screenX + screenWidth - shortLabelWidth, screenY + screenHeight - shortLabelHeight, () -> {
			if (parent.tileEntity.sequenceInProgress()) {
				parent.setDisplayPage(new TestExistsPage(parent));
			}
			else {
				Optional<TestConfig> config = getEnteredConfig();
				if (config.isPresent() && !StringUtils.isNullOrEmpty(getName())) {
					
					saveInfo();
					
					parent.setDisplayPage(new RecordingProgressPage(thiz));
					//Recording, ENGAGE
					CircuitsMod.network.sendToServer(new TypedMessage(new RecordingRequest(thiz.getName(), parent.user.getUniqueID(), 
							parent.tileEntity.getPos(), config.get())));
				}
			}
		}));
		
	}
	
	public void saveInfo() {
		this.getInfo().setName(getName());
	}

	@Override
	protected void draw() {
		parent.getFontRenderer().drawString("Name", screenX, screenY, elementColor);
		parent.getFontRenderer().drawString("Delay", screenX, screenY + (screenHeight / 2), elementColor);
	}

}
