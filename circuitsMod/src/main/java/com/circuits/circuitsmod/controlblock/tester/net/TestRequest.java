package com.circuits.circuitsmod.controlblock.tester.net;

import java.util.UUID;

import com.circuits.circuitsmod.circuit.SpecializedCircuitUID;
import com.circuits.circuitsmod.tester.TestConfig;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class TestRequest extends ControlTileEntityClientRequest {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private SpecializedCircuitUID uid;
	private TestConfig config;
	public TestRequest(UUID playerId, SpecializedCircuitUID uid, BlockPos pos, TestConfig config) {
		super(playerId, pos);
		this.uid = uid;
		this.config = config;
	}
	
	public static void handle(TestRequest in, World worldIn) {
		worldIn.getMinecraftServer().addScheduledTask(() -> {
			in.performOnControlTE(worldIn, (te) -> te.startCircuitTest(in.getPlayerID(), in.uid, in.config));
		});
	}
}
