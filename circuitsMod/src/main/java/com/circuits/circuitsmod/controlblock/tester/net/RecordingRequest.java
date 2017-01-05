package com.circuits.circuitsmod.controlblock.tester.net;

import java.util.UUID;

import com.circuits.circuitsmod.tester.TestConfig;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class RecordingRequest extends ControlTileEntityClientRequest {
	private static final long serialVersionUID = 1L;
	private String circuitName;
	private TestConfig config;

	public RecordingRequest(String circuitName, UUID playerId, BlockPos pos, TestConfig config) {
		super(playerId, pos);
		this.circuitName = circuitName;
		this.config = config;
	}
	
	public static void handle(RecordingRequest in, World worldIn) {
		worldIn.getMinecraftServer().addScheduledTask(() -> {
			in.performOnControlTE(worldIn, (te) -> te.startCircuitRecording(in.circuitName, in.config));
		});
	}

}
