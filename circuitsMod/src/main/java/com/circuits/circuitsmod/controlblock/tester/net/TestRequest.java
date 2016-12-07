package com.circuits.circuitsmod.controlblock.tester.net;

import java.io.Serializable;
import java.util.Optional;
import java.util.UUID;

import com.circuits.circuitsmod.circuit.SpecializedCircuitUID;
import com.circuits.circuitsmod.common.Log;
import com.circuits.circuitsmod.common.SerialUtils;
import com.circuits.circuitsmod.controlblock.ControlBlock;
import com.circuits.circuitsmod.controlblock.ControlTileEntity;
import com.circuits.circuitsmod.tester.TestConfig;

import io.netty.buffer.ByteBuf;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class TestRequest implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private UUID playerId;
	private SpecializedCircuitUID uid;
	private Long pos;
	private TestConfig config;
	public TestRequest(UUID playerId, SpecializedCircuitUID uid, BlockPos pos, TestConfig config) {
		this.playerId = playerId;
		this.uid = uid;
		this.pos = pos.toLong();
		this.config = config;
	}
	
	public BlockPos getPos() {
		return BlockPos.fromLong(pos);
	}
	public SpecializedCircuitUID getUID() {
		return this.uid;
	}
	
	public static void handleTestRequest(TestRequest in, World worldIn) {
		Optional<ControlTileEntity> entity = ControlBlock.getControlTileEntityAt(worldIn, in.getPos());
		if (!entity.isPresent()) {
			Log.internalError("Attempting to handle test request at " + in.getPos() + " but no control TE present!");
			return;
		}
		entity.get().startTest(in.playerId, in.uid, in.config);
		
	}
	
	public static class Message implements IMessage {
		public TestRequest message = null;
		public Message() { }
		public Message(UUID playerId, SpecializedCircuitUID uid, BlockPos pos, TestConfig config) {
			message = new TestRequest(playerId, uid, pos, config);
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
