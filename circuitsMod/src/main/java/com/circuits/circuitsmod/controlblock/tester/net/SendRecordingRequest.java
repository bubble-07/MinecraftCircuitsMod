package com.circuits.circuitsmod.controlblock.tester.net;

import java.util.Optional;
import java.util.UUID;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import com.circuits.circuitsmod.controlblock.gui.net.ServerGuiMessage;
import com.circuits.circuitsmod.recorder.CircuitRecording;

/**
 * Request from the client to the server to send a circuit recording back
 * as a GUI message after stopping the sequence.
 * @author bubble-07
 *
 */
public class SendRecordingRequest extends ControlTileEntityClientRequest {
	private static final long serialVersionUID = 1L;

	public SendRecordingRequest(UUID playerId, BlockPos pos) {
		super(playerId, pos);
	}
	
	public static void handle(SendRecordingRequest req, World worldIn) {
		req.performOnControlTE(worldIn, (entity) -> {
			Optional<CircuitRecording> recording = entity.getRecording();
			if (recording.isPresent()) {
				entity.postGuiMessage(req.getPlayerID(), 
						new ServerGuiMessage(ServerGuiMessage.GuiMessageKind.GUI_RECORDING_DATA, new ServerGuiMessage.RecordingData(recording.get())));
			}
			entity.stopSequence();
		});
	}

}
