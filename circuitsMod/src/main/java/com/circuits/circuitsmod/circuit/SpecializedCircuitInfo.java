package com.circuits.circuitsmod.circuit;

import java.io.Serializable;

import com.circuits.circuitsmod.reflective.SpecializedChipImpl;

/**
 * Information about a circuit after specialization by a list of CircuitConfigOptions.
 * This class is designed to be communicated between the client and the server
 * @author bubble-07
 *
 */
public class SpecializedCircuitInfo implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private CircuitInfo info;
	private CircuitConfigOptions origOpts;
	private int[] inputWidths;
	private int[] outputWidths;
	private boolean[] analogInputs;
	private boolean[] analogOutputs;
	private String configName;
	private SpecializedCircuitUID uid;
	
	public CircuitInfo getInfo() {
		return this.info;
	}
	public int[] getInputWidths() {
		return this.inputWidths;
	}
	public int[] getOutputWidths() {
		return this.outputWidths;
	}
	public boolean[] getAnalogInputs() {
		return this.analogInputs;
	}
	public boolean[] getAnalogOutputs() {
		return this.analogOutputs;
	}
	
	public CircuitConfigOptions getConfigOptions() {
		return this.origOpts;
	}
	public String getConfigName() {
		return this.configName;
	}
	
	public SpecializedCircuitUID getUID() {
		return this.uid;
	}
	
	public String getFullDisplayName() {
		String result = this.getInfo().getName();
		result += "(" + this.getConfigName() + ")";
		return result;
	}
	
	/**
	 * Note: only to be called from the server!
	 * @param info
	 * @param implementation
	 */
	public SpecializedCircuitInfo(SpecializedCircuitUID uid, CircuitInfo info, SpecializedChipImpl impl) {
		this.info = info;
		this.uid = uid;
		this.configName = impl.getInvoker().getConfigName();
		this.origOpts = impl.getInvoker().getConfigOptions();
		this.inputWidths = impl.getInvoker().inputWidths();
		this.outputWidths = impl.getInvoker().outputWidths();	
		this.analogInputs = impl.getInvoker().analogInputs();
		this.analogOutputs = impl.getInvoker().analogOutputs();
	}
}
