package com.circuits.circuitsmod.network;

import java.util.List;

import com.circuits.circuitsmod.circuit.CircuitInfoProvider;
import com.circuits.circuitsmod.controlblock.gui.net.CircuitCostRequest;
import com.circuits.circuitsmod.controlblock.gui.net.CraftingRequest;
import com.circuits.circuitsmod.controlblock.gui.net.SetCraftingCellRequest;
import com.circuits.circuitsmod.controlblock.gui.net.SpecializationValidationRequest;
import com.circuits.circuitsmod.controlblock.tester.net.CompileRecordingRequest;
import com.circuits.circuitsmod.controlblock.tester.net.RecordingRequest;
import com.circuits.circuitsmod.controlblock.tester.net.SendRecordingRequest;
import com.circuits.circuitsmod.controlblock.tester.net.TestRequest;
import com.circuits.circuitsmod.controlblock.tester.net.TestStopRequest;
import com.google.common.collect.Lists;

public class ServerHandlers extends Handlers {
	@Override
	public String getHandlerName() {
		return "Server";
	}

	@Override
	public List<Class<?>> getRequestKinds() {
		return Lists.newArrayList(CircuitInfoProvider.ModelRequestFromClient.class, CircuitInfoProvider.SpecializedInfoRequestFromClient.class
				                  , TestRequest.class, RecordingRequest.class, SetCraftingCellRequest.class, CircuitCostRequest.class,
				                    TestStopRequest.class, CraftingRequest.class, SpecializationValidationRequest.class,
				                    SendRecordingRequest.class, CompileRecordingRequest.class);
	}
}
