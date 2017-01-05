package com.circuits.circuitsmod.network;

import java.util.List;
import com.circuits.circuitsmod.circuit.CircuitInfoProvider;
import com.google.common.collect.Lists;

public class ClientHandlers extends Handlers {

	@Override
	public String getHandlerName() {
		return "Client";
	}

	@Override
	public List<Class<?>> getRequestKinds() {
		return Lists.newArrayList(CircuitInfoProvider.SpecializedInfoResponseFromServer.class);
	}
}
