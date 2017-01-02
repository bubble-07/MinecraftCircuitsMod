package com.circuits.circuitsmod.controlblock.gui.net;

import io.netty.buffer.ByteBuf;

import java.io.Serializable;
import java.util.Optional;
import java.util.UUID;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

import com.circuits.circuitsmod.circuit.SpecializedCircuitUID;
import com.circuits.circuitsmod.common.Log;
import com.circuits.circuitsmod.common.SerialUtils;
import com.circuits.circuitsmod.controlblock.ControlBlock;
import com.circuits.circuitsmod.controlblock.ControlTileEntity;


public class SetCraftingCellRequest implements Serializable {
	private static final long serialVersionUID = 1L;
	private UUID playerId;
	private SpecializedCircuitUID uid;
	private Long pos;
	public SetCraftingCellRequest(UUID playerId, SpecializedCircuitUID uid, BlockPos pos) {
		this.playerId = playerId;
		this.uid = uid;
		this.pos = pos.toLong();
	}
	
	public BlockPos getPos() {
		return BlockPos.fromLong(pos);
	}
	public SpecializedCircuitUID getUID() {
		return this.uid;
	}
	
	public static void handleSetCraftingCellRequest(SetCraftingCellRequest in, World worldIn) {
		worldIn.getMinecraftServer().addScheduledTask(() -> {
			Optional<ControlTileEntity> entity = ControlBlock.getControlTileEntityAt(worldIn, in.getPos());
			if (!entity.isPresent()) {
				Log.internalError("Attempting to handle crafting cell set request at " + in.getPos() + " but no control TE present!");
				return;
			}
			entity.get().setCraftingCell(in.playerId, in.uid);
			entity.get().updateCraftingGrid();
		});
	}
	
	public static class Message implements IMessage {
		public SetCraftingCellRequest message = null;
		public Message() { }
		public Message(UUID playerId, SpecializedCircuitUID uid, BlockPos pos) {
			message = new SetCraftingCellRequest(playerId, uid, pos);
		}
		@Override
		public void fromBytes(ByteBuf in) {
			message = (SetCraftingCellRequest) SerialUtils.fromBytes(in);
		}
		public void toBytes(ByteBuf in) {
			SerialUtils.toBytes(in, message);
		}
	}
}
