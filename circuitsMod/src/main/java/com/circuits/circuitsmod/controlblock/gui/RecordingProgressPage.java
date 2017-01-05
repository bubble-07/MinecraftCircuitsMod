package com.circuits.circuitsmod.controlblock.gui;

import java.io.Serializable;
import java.util.UUID;

import net.minecraft.util.math.BlockPos;

import com.circuits.circuitsmod.controlblock.tester.net.SendRecordingRequest;
import com.circuits.circuitsmod.controlblock.tester.net.TestStopRequest;
import com.circuits.circuitsmod.recorder.RecordingState;

public class RecordingProgressPage extends SequenceProgressPage<ControlCustomCircuitMainPage> {

	public RecordingProgressPage(ControlCustomCircuitMainPage prev) {
		super(prev);
	}

	@Override
	public Serializable getSuccessStopRequest(UUID playerId, BlockPos pos) {
		return new SendRecordingRequest(playerId, pos);
	}
	
	@Override
	public ControlGuiPage getSuccessPage() {
		return new CustomCircuitRecordingReceiptPage(prev);
	}

	@Override
	public boolean isRightSequenceStateType(Class<?> clazz) {
		return RecordingState.class.isAssignableFrom(clazz);
	}

}
