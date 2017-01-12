package com.circuits.circuitsmod.controlblock.gui;

import net.minecraft.client.gui.Gui;

import com.circuits.circuitsmod.CircuitsMod;
import com.circuits.circuitsmod.common.Pair;
import com.circuits.circuitsmod.controlblock.gui.model.CustomCircuitImage;
import com.circuits.circuitsmod.controlblock.gui.model.CustomCircuitInfo;
import com.circuits.circuitsmod.controlblock.gui.widgets.TextButton;
import com.circuits.circuitsmod.controlblock.gui.widgets.UIElement;
import com.circuits.circuitsmod.network.TypedMessage;
import com.circuits.circuitsmod.controlblock.tester.net.CompileRecordingRequest;

public class CustomCircuitIconPage extends ControlGuiPage {
	
	private static int viewX = screenX + shortLabelWidth;
	private static int viewY = screenY + shortLabelHeight;
	private static int viewWidth = screenWidth - 2 * shortLabelWidth;
	private static int viewHeight = viewWidth;
	
	private final CustomCircuitDescriptionPage prev;
	
	private CustomCircuitImage.Color paintColor = CustomCircuitImage.Color.BLACK;
	protected Mode mode = Mode.PIXEL;
	private int hoverX;
	private int hoverY;
	
	private int prevX = -1;
	private int prevY = -1;
	
	
	public class ColorButton extends UIElement {
		
		private final int colorToDraw;
		private CustomCircuitImage.Color myColor;

		public ColorButton(int x, int y, CustomCircuitImage.Color color) {
			super(CustomCircuitIconPage.this.parent, x, y, shortLabelHeight, shortLabelHeight);
			this.myColor = color;
			colorToDraw = CustomCircuitImage.toGUIColor(color);
		}

		@Override
		public void draw() {
			if (this.myColor.equals(paintColor)) {
				parent.drawBox(x - 1, y - 1, width + 2, height + 2);
			}
			Gui.drawRect(x - 2, y - 2, 
			        x + width, y + height, colorToDraw);
			parent.drawBox(x, y, width, height);
		}
		
		@Override
		public boolean handleClick() {
			setPaintColor(myColor);
			return true;
		}

	}
	
	
	protected abstract class ModeButton extends UIElement {
		Mode parentMode;
		public ModeButton(int x, int y, Mode parent) {
			super(CustomCircuitIconPage.this.parent, x, y, shortLabelHeight, shortLabelHeight);
			this.parentMode = parent;
		}
		@Override
		public boolean handleClick() {
			CustomCircuitIconPage.this.setMode(parentMode);
			return true;
		}
		@Override 
		public void draw() {
			if (CustomCircuitIconPage.this.mode.equals(parentMode)) {
				parent.drawBox(x - 1, y - 1, width + 2, height + 2);
			}
			parent.drawBox(x, y, width, height);
			this.drawIcon();
		}
		public abstract void drawIcon();
	}
	
	private static enum Mode {
		RECTANGLE, LINE, PIXEL;
	}
	
	public ModeButton getButtonForMode(Mode mode) {
		switch (mode) {
			case PIXEL:
				return new ModeButton(screenX + screenWidth - shortLabelHeight - 2, screenY + 1 * shortLabelHeight, Mode.PIXEL) {
					@Override
					public void drawIcon() {
						parent.drawBox(x + (width / 2), y + (height / 2), 0, -1);
					}
				};
			case LINE:
				return new ModeButton(screenX + screenWidth - shortLabelHeight - 2, screenY + 2 * shortLabelHeight + 5, Mode.LINE) {
					@Override
					public void drawIcon() {
						parent.drawHorizontalLine(x, x + width - 2, y + (height / 2) - 1, elementColor);
					}
				};

			case RECTANGLE:
				return new ModeButton(screenX + screenWidth - shortLabelHeight - 2, screenY + 3 * shortLabelHeight + 10, Mode.RECTANGLE) {
					@Override
					public void drawIcon() {
						parent.drawBox(x + 3, y + 6, width - 6, height - 12);
					}
				};

		}
		return null;
	}
	
	public CustomCircuitInfo getInfo() {
		return prev.getInfo();
	}
	
	protected void setPaintColor(CustomCircuitImage.Color paintColor) {
		this.paintColor = paintColor;
	}
	
	protected void setMode(Mode mode) {
		this.mode = mode;
	}

	public CustomCircuitIconPage(CustomCircuitDescriptionPage prev) {
		super(prev.parent);
		this.prev = prev;
		
		for (Mode mode : Mode.values()) {
			this.addElement(getButtonForMode(mode));
		}
		
		addElement(new ColorButton(screenX, screenY + shortLabelHeight, CustomCircuitImage.Color.BLACK));
		addElement(new ColorButton(screenX, screenY + 3 * shortLabelHeight, CustomCircuitImage.Color.WHITE));
		
		addElement(new TextButton(parent, "Back", screenX + screenWidth - shortLabelWidth, screenY, () -> {
			parent.setDisplayPage(prev);
		}));
		
		this.addElement(new TextButton(parent, "Done", screenX + screenWidth - shortLabelWidth, screenY + screenHeight - shortLabelHeight, () -> {
			parent.setDisplayPage(new CustomCircuitCompilationPage(CustomCircuitIconPage.this));
			CircuitsMod.network.sendToServer(new TypedMessage(new CompileRecordingRequest(parent.user.getUniqueID(), parent.tileEntity.getPos(), getInfo())));
		}));
	}
	
	protected boolean hasPreviousLocation() {
		return !(this.prevX == -1 && this.prevY == -1);
	}
	protected void setPreviousLocation(int mouseX, int mouseY) {
		Pair<Integer, Integer> coords = getInfo().getImage().fromGUICoords(viewX, viewY, viewWidth, viewHeight, mouseX, mouseY);
		if (CustomCircuitImage.boundsCheck(coords.first(), coords.second())) {
			prevX = coords.first();
			prevY = coords.second();
		}
	}
	protected void resetPreviousLocation() {
		this.prevX = -1;
		this.prevY = -1;
	}
	
	protected void handleTwoClickOp(int x1, int y1, int x2, int y2) {
		if (this.mode.equals(Mode.RECTANGLE)) {
			getInfo().getImage().fillRect(x1, y1, x2, y2, this.paintColor);
		}
		else if (this.mode.equals(Mode.LINE)) {
			getInfo().getImage().drawLine(x1, y1, x2, y2, this.paintColor);
		}
	}
	
	@Override
	protected void handleClick(int mouseX, int mouseY) {
		if (this.mode.equals(Mode.RECTANGLE) || this.mode.equals(Mode.LINE)) {
			if (!hasPreviousLocation()) {
				setPreviousLocation(mouseX, mouseY);
				if (hasPreviousLocation()) {
					//Draw a "marker pixel"
					getInfo().getImage().setPixel(prevX, prevY, paintColor);
				}
			}
			else {
				Pair<Integer, Integer> coords = getInfo().getImage().fromGUICoords(viewX, viewY, viewWidth, viewHeight, mouseX, mouseY);
				if (CustomCircuitImage.boundsCheck(coords.first(), coords.second())) {
					handleTwoClickOp(coords.first(), coords.second(), prevX, prevY);
					resetPreviousLocation();
				}
			}
		}
		else {
			handleMouseMove(mouseX, mouseY);
		}
	}
	
	@Override
	protected void handleMouseMove(int mouseX, int mouseY) {
		Pair<Integer, Integer> coords = getInfo().getImage().fromGUICoords(viewX, viewY, viewWidth, viewHeight, mouseX, mouseY);
		int x = coords.first();
		int y = coords.second();
		if (CustomCircuitImage.boundsCheck(x, y) && this.mode.equals(Mode.PIXEL)) {
			getInfo().getImage().setPixel(x, y, paintColor);
		}
	}
	
	@Override
	protected void handleMouseHover(int mouseX, int mouseY) {
		Pair<Integer, Integer> coords = getInfo().getImage().fromGUICoords(viewX, viewY, viewWidth, viewHeight, mouseX, mouseY);
		this.hoverX = coords.first();
		this.hoverY = coords.second();
	}

	@Override
	protected void draw() {
		parent.getFontRenderer().drawString("Icon", screenX, screenY, elementColor);
		getInfo().getImage().drawInGUI(viewX, viewY, viewWidth, viewHeight, hoverX, hoverY);
		parent.drawBox(viewX, viewY, viewWidth - 4, viewHeight - 4);

	}

}
