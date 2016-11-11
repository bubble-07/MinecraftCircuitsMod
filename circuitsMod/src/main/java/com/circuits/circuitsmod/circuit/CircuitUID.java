package com.circuits.circuitsmod.circuit;

import java.util.Optional;

public class CircuitUID {
	
	private final int idNum;
	
	private CircuitUID(int id) {
		this.idNum = id;
	}
	
	
	/**
	 * Given the integer representation of a CircuitUID, return the corresponding UID
	 * @param idNum
	 * @return
	 */
	public static Optional<CircuitUID> fromInteger(int idNum) {
		//TODO: Check the existing ID database in the save folder to see if this is valid!
		return Optional.of(new CircuitUID(idNum));
	}
	
	public int toInteger() {
		return idNum;
	}
}
