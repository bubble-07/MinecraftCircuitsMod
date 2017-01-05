package com.circuits.circuitsmod.controlblock.tester.net;

import java.io.Serializable;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

import com.circuits.circuitsmod.common.Log;
import com.circuits.circuitsmod.controlblock.ControlBlock;
import com.circuits.circuitsmod.controlblock.ControlTileEntity;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Common base class for client requests originating from a control tile entity
 * @author bubble-07
 *
 */
public abstract class ControlTileEntityClientRequest implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private UUID playerId;
	private Long pos;
	public ControlTileEntityClientRequest(UUID playerId, BlockPos pos) {
		this.playerId = playerId;
		this.pos = pos.toLong();
	}
	public BlockPos getPos() {
		return BlockPos.fromLong(pos);
	}
	public UUID getPlayerID() {
		return this.playerId;
	}
	public void performOnControlTE(World worldIn, Consumer<ControlTileEntity> action) {
		Optional<ControlTileEntity> entity = ControlBlock.getControlTileEntityAt(worldIn, getPos());
		if (!entity.isPresent()) {
			Log.internalError("Attempting to handle control TE request" + this.getClass().getName() +" at " + getPos() + " but no control TE present!");
			return;
		}
		action.accept(entity.get());
	}
}
