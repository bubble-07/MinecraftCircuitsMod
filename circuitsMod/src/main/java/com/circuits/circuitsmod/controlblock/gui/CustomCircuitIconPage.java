package com.circuits.circuitsmod.controlblock.gui;

import com.circuits.circuitsmod.CircuitsMod;
import com.circuits.circuitsmod.common.Pair;
import com.circuits.circuitsmod.controlblock.gui.model.CustomCircuitImage;
import com.circuits.circuitsmod.controlblock.gui.model.CustomCircuitInfo;
import com.circuits.circuitsmod.controlblock.gui.widgets.ColorButton;
import com.circuits.circuitsmod.controlblock.gui.widgets.TextButton;
import com.circuits.circuitsmod.network.TypedMessage;
import com.circuits.circuitsmod.controlblock.tester.net.CompileRecordingRequest;

public class CustomCircuitIconPage extends ControlGuiPage {
	
	private static int viewX = screenX + shortLabelWidth;
	private static int viewY = screenY + shortLabelHeight;
	private static int viewWidth = screenWidth - 2 * shortLabelWidth;
	private static int viewHeight = viewWidth;
	
	private final CustomCircuitDescriptionPage prev;
	
	private CustomCircuitImage.Color paintColor = CustomCircuitImage.Color.BLACK;
	
	public CustomCircuitInfo getInfo() {
		return prev.getInfo();
	}
	
	protected void setPaintColor(CustomCircuitImage.Color paintColor) {
		this.paintColor = paintColor;
	}

	public CustomCircuitIconPage(CustomCircuitDescriptionPage prev) {
		super(prev.parent);
		this.prev = prev;
		
		addElement(new ColorButton(parent, screenX, screenY + shortLabelHeight, shortLabelHeight, shortLabelHeight, 
				CustomCircuitImage.Color.BLACK, () -> {
					setPaintColor(CustomCircuitImage.Color.BLACK);
				}));
		addElement(new ColorButton(parent, screenX, screenY + 3 * shortLabelHeight, shortLabelHeight, shortLabelHeight, 
                CustomCircuitImage.Color.WHITE, () -> {
             	   setPaintColor(CustomCircuitImage.Color.WHITE);
                }));
		
		addElement(new TextButton(parent, "Back", screenX + screenWidth - shortLabelWidth, screenY, () -> {
			parent.setDisplayPage(prev);
		}));
		
		this.addElement(new TextButton(parent, "Done", screenX + screenWidth - shortLabelWidth, screenY + screenHeight - shortLabelHeight, () -> {
			parent.setDisplayPage(new CustomCircuitCompilationPage(CustomCircuitIconPage.this));
			CircuitsMod.network.sendToServer(new TypedMessage(new CompileRecordingRequest(parent.user.getUniqueID(), parent.tileEntity.getPos(), getInfo())));
		}));
	}
	
	@Override
	protected void handleClick(int mouseX, int mouseY) {
		handleMouseMove(mouseX, mouseY);
	}
	
	@Override
	protected void handleMouseMove(int mouseX, int mouseY) {
		Pair<Integer, Integer> coords = getInfo().getImage().fromGUICoords(viewX, viewY, viewWidth, viewHeight, mouseX, mouseY);
		int x = coords.first();
		int y = coords.second();
		if (CustomCircuitImage.boundsCheck(x, y)) {
			getInfo().getImage().setPixel(x, y, paintColor);
		}
	}

	@Override
	protected void draw() {
		parent.getFontRenderer().drawString("Icon", screenX, screenY, elementColor);
		getInfo().getImage().drawInGUI(viewX, viewY, viewWidth, viewHeight);
		parent.drawBox(viewX, viewY, viewWidth - 4, viewHeight - 4);

	}

}
