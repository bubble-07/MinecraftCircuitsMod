package com.circuits.circuitsmod.circuit;

import java.io.Serializable;

/**
 * A Circuit UID together with the list of options used to arrive at a circuit specialization
 * @author bubble-07
 *
 */
public class SpecializedCircuitUID implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private CircuitUID uid;
	private CircuitConfigOptions configs;
	
	public SpecializedCircuitUID(CircuitUID uid, CircuitConfigOptions configs) {
		this.uid = uid;
		this.configs = configs;
	}
	public CircuitUID getUID() {
		return this.uid;
	}
	public CircuitConfigOptions getOptions() {
		return this.configs;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((configs == null) ? 0 : configs.hashCode());
		result = prime * result + ((uid == null) ? 0 : uid.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SpecializedCircuitUID other = (SpecializedCircuitUID) obj;
		if (configs == null) {
			if (other.configs != null)
				return false;
		} else if (!configs.equals(other.configs))
			return false;
		if (uid == null) {
			if (other.uid != null)
				return false;
		} else if (!uid.equals(other.uid))
			return false;
		return true;
	}
	@Override
	public String toString() {
		return "SpecializedCircuitUID [uid=" + uid + ", configs=" + configs
				+ "]";
	}
	
	
}
