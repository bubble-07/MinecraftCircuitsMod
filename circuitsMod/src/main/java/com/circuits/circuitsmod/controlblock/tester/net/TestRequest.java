package com.circuits.circuitsmod.controlblock.tester.net;

import java.io.Serializable;
import java.util.Optional;

import com.circuits.circuitsmod.common.Log;
import com.circuits.circuitsmod.common.SerialUtils;
import com.circuits.circuitsmod.controlblock.ControlBlock;
import com.circuits.circuitsmod.controlblock.frompoc.ControlTileEntity;
import com.circuits.circuitsmod.controlblock.tester.TestConfig;

import io.netty.buffer.ByteBuf;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class TestRequest implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String circuitName;
	private Long pos;
	private TestConfig config;
	public TestRequest(String name, BlockPos pos, TestConfig config) {
		this.circuitName = name;
		this.pos = pos.toLong();
		this.config = config;
	}
	
	public BlockPos getPos() {
		return BlockPos.fromLong(pos);
	}
	public String getName() {
		return circuitName;
	}
	
	public static void handleTestRequest(TestRequest in, World worldIn) {
		Optional<ControlTileEntity> entity = ControlBlock.getControlTileEntityAt(worldIn, in.getPos());
		if (!entity.isPresent()) {
			Log.internalError("Attempting to handle test request at " + in.getPos() + " but no control TE present!");
			return;
		}
		entity.get().startTest(in.circuitName, in.config);
		
	}
	
	public static class Message implements IMessage {
		public TestRequest message = null;
		public Message() { }
		public Message(String circuitName, BlockPos pos, TestConfig config) {
			message = new TestRequest(circuitName, pos, config);
		}
		@Override
		public void fromBytes(ByteBuf in) {
			message = (TestRequest) SerialUtils.fromBytes(in);
		}
		public void toBytes(ByteBuf in) {
			SerialUtils.toBytes(in, message);
		}
	}
	
}
