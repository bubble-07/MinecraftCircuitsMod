package com.circuits.circuitsmod.controlblock.tester.net;

import java.io.Serializable;
import java.util.Optional;

import com.circuits.circuitsmod.common.Log;
import com.circuits.circuitsmod.common.SerialUtils;
import com.circuits.circuitsmod.controlblock.ControlBlock;
import com.circuits.circuitsmod.controlblock.ControlTileEntity;
import com.circuits.circuitsmod.controlblock.tester.TestState;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class TestStateUpdate implements Serializable {
	private static final long serialVersionUID = 1L;
	private TestState state;
	private Long pos;
	public TestStateUpdate(TestState state, BlockPos pos) {
		this.state = state;
		this.pos = pos.toLong();
	}
	
	public BlockPos getPos() {
		return BlockPos.fromLong(pos);
	}
	public TestState getState() {
		return state;
	}
	
	public static void handleUpdateRequest(TestStateUpdate in, World worldIn) {
		Optional<ControlTileEntity> entity = ControlBlock.getControlTileEntityAt(worldIn, in.getPos());
		if (!entity.isPresent()) {
			Log.internalError("Attempting to update test state at " + in.getPos() + " but no control TE present!");
		}
		entity.get().updateState(in.state);
	}
	
	public static class Message implements IMessage {
		public TestStateUpdate message = null;
		public Message() { }
		public Message(TestState update, BlockPos pos) {
			message = new TestStateUpdate(update, pos);
		}
		@Override
		public void fromBytes(ByteBuf in) {
			message = (TestStateUpdate) SerialUtils.fromBytes(in);
		}
		public void toBytes(ByteBuf in) {
			SerialUtils.toBytes(in, message);
		}
		public static class Handler implements IMessageHandler<TestStateUpdate.Message, IMessage> {
			@Override
			public IMessage onMessage(TestStateUpdate.Message msg, MessageContext ctxt) {
				World world = Minecraft.getMinecraft().theWorld;
				
				TestStateUpdate.handleUpdateRequest(msg.message, world);
				return null;
			}
		}
	}
}
