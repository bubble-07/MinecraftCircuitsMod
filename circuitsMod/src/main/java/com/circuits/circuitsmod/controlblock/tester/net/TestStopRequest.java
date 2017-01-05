package com.circuits.circuitsmod.controlblock.tester.net;

import java.util.UUID;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class TestStopRequest extends ControlTileEntityClientRequest {
	private static final long serialVersionUID = 1L;
	public TestStopRequest(UUID playerId, BlockPos pos) {
		super(playerId, pos);
	}
	public static void handle(TestStopRequest in, World worldIn) {
		in.performOnControlTE(worldIn, (entity) -> {
			entity.stopSequence();
		});
	}
}
