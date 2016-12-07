package com.circuits.circuitsmod.tester;

import java.io.Serializable;

public class TestConfig implements Serializable {
	private static final long serialVersionUID = 1L;
	public int tickDelay;
	public TestConfig(int tickDelay) {
		this.tickDelay = tickDelay;
	}
	
}
