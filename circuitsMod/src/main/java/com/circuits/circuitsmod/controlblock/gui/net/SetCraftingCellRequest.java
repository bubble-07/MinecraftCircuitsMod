package com.circuits.circuitsmod.controlblock.gui.net;

import java.util.UUID;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import com.circuits.circuitsmod.circuit.SpecializedCircuitUID;
import com.circuits.circuitsmod.controlblock.tester.net.ControlTileEntityClientRequest;


public class SetCraftingCellRequest extends ControlTileEntityClientRequest {
	private static final long serialVersionUID = 1L;
	private SpecializedCircuitUID uid;
	public SetCraftingCellRequest(UUID playerId, SpecializedCircuitUID uid, BlockPos pos) {
		super(playerId, pos);
		this.uid = uid;
	}
	public SpecializedCircuitUID getUID() {
		return this.uid;
	}
	
	public static void handle(SetCraftingCellRequest in, World worldIn) {
		in.performOnControlTE(worldIn, (entity) -> {
			entity.setCraftingCell(in.getPlayerID(), in.uid);
			entity.updateCraftingGrid();
		});
	}
}
