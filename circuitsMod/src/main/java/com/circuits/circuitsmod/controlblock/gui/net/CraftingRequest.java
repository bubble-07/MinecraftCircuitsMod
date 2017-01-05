package com.circuits.circuitsmod.controlblock.gui.net;

import java.util.UUID;

import com.circuits.circuitsmod.circuit.SpecializedCircuitUID;
import com.circuits.circuitsmod.controlblock.tester.net.ControlTileEntityClientRequest;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

//Request to be used when client requests a circuit be crafted
public class CraftingRequest extends ControlTileEntityClientRequest {
	private static final long serialVersionUID = 1L;
	private int numCrafted;
	private SpecializedCircuitUID circuitUID;
	
	public CraftingRequest(UUID player, BlockPos pos, int numCrafted, SpecializedCircuitUID uid) {
		super(player, pos);
		this.numCrafted = numCrafted;
		this.circuitUID = uid;
	}
	
	public static void handle(CraftingRequest in, World worldIn) {
		in.performOnControlTE(worldIn, (entity) -> {
			entity.setCraftingCell(in.getPlayerID(), in.circuitUID);
			entity.craftingSlotPickedUp(in.numCrafted);
		});
	}
}
