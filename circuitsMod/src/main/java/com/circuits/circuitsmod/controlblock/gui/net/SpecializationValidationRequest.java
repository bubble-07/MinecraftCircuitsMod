package com.circuits.circuitsmod.controlblock.gui.net;

import io.netty.buffer.ByteBuf;

import java.io.Serializable;
import java.util.Optional;
import java.util.UUID;

import com.circuits.circuitsmod.circuit.CircuitInfoProvider;
import com.circuits.circuitsmod.circuit.SpecializedCircuitInfo;
import com.circuits.circuitsmod.circuit.SpecializedCircuitUID;
import com.circuits.circuitsmod.common.Log;
import com.circuits.circuitsmod.common.SerialUtils;
import com.circuits.circuitsmod.controlblock.ControlBlock;
import com.circuits.circuitsmod.controlblock.frompoc.ControlTileEntity;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class SpecializationValidationRequest implements Serializable {
	private static final long serialVersionUID = 1L;
	UUID player;
	SpecializedCircuitUID uid;
	private Long pos;
	public SpecializationValidationRequest(UUID player, BlockPos pos, SpecializedCircuitUID uid) {
		this.pos = pos.toLong();
		this.player = player;
		this.uid = uid;
	}
	public BlockPos getPos() {
		return BlockPos.fromLong(pos);
	}
	public SpecializedCircuitUID getUID() {
		return uid;
	}
	public static void handleSpecializationValidationRequest(SpecializationValidationRequest in, World worldIn) {
		Optional<ControlTileEntity> entity = ControlBlock.getControlTileEntityAt(worldIn, in.getPos());
		if (!entity.isPresent()) {
			Log.internalError("Attempting to validate circuit for " + in.getPos() +  " but no control TE present!");
			return;
		}
		Optional<SpecializedCircuitInfo> info = CircuitInfoProvider.getSpecializedInfoFor(in.uid);
		String specialName = info.flatMap((i) -> Optional.of(i.getFullDisplayName())).orElse(null);
		entity.get().postGuiMessage(in.player, 
				new ServerGuiMessage(ServerGuiMessage.GuiMessageKind.GUI_SPECIALIZATON_INFO, specialName));

	}
	public static class Message implements IMessage {
		public SpecializationValidationRequest message = null;
		public Message() { }
		public Message(UUID player, BlockPos pos, SpecializedCircuitUID uid) {
			message = new SpecializationValidationRequest(player, pos, uid);
		}
		@Override
		public void fromBytes(ByteBuf in) {
			message = (SpecializationValidationRequest) SerialUtils.fromBytes(in);
		}
		public void toBytes(ByteBuf in) {
			SerialUtils.toBytes(in, message);
		}
	}

}
