package com.circuits.circuitsmod.controlblock.tester.net;

import io.netty.buffer.ByteBuf;

import java.io.Serializable;
import java.util.Optional;

import com.circuits.circuitsmod.common.Log;
import com.circuits.circuitsmod.common.SerialUtils;
import com.circuits.circuitsmod.controlblock.ControlBlock;
import com.circuits.circuitsmod.controlblock.TileEntityControl;
import com.circuits.circuitsmod.controlblock.frompoc.ControlTileEntity;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class TestStopRequest implements Serializable {
	private static final long serialVersionUID = 1L;
	private String circuitName;
	private Long pos;
	public TestStopRequest(String name, BlockPos pos) {
		this.circuitName = name;
		this.pos = pos.toLong();
	}
	public BlockPos getPos() {
		return BlockPos.fromLong(pos);
	}
	public String getName() {
		return circuitName;
	}
	public static void handleTestStopRequest(TestStopRequest in, World worldIn) {
		Optional<ControlTileEntity> entity = ControlBlock.getControlTileEntityAt(worldIn, in.getPos());
		if (!entity.isPresent()) {
			Log.internalError("Attempting to stop circuit test at position " + in.getPos() +  " but no control TE present!");
			return;
		}
		entity.get().stopTest();
	}
	public static class Message implements IMessage {
		public TestStopRequest message = null;
		public Message() { }
		public Message(String circuitName, BlockPos pos) {
			message = new TestStopRequest(circuitName, pos);
		}
		@Override
		public void fromBytes(ByteBuf in) {
			message = (TestStopRequest) SerialUtils.fromBytes(in);
		}
		public void toBytes(ByteBuf in) {
			SerialUtils.toBytes(in, message);
		}
	}
}
