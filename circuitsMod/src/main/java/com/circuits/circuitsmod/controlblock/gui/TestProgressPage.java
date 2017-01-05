package com.circuits.circuitsmod.controlblock.gui;

import java.io.Serializable;
import java.util.UUID;

import net.minecraft.util.math.BlockPos;

import com.circuits.circuitsmod.controlblock.gui.model.CircuitCell;
import com.circuits.circuitsmod.controlblock.tester.net.TestStopRequest;
import com.circuits.circuitsmod.tester.TestState;

public class TestProgressPage extends SequenceProgressPage<TestSettingsPage> {
	private final CircuitCell cell;
	
	public TestProgressPage(final TestSettingsPage prev) {
		super(prev);
		this.cell = prev.cell;
	}
	
	@Override
	public Serializable getSuccessStopRequest(UUID playerId, BlockPos pos) {
		return new TestStopRequest(playerId, pos);
	}

	@Override
	public ControlGuiPage getSuccessPage() {
		return new CellDisplayPage(parent, cell);
	}

	@Override
	public boolean isRightSequenceStateType(Class<?> clazz) {
		return TestState.class.isAssignableFrom(clazz);
	}
}
