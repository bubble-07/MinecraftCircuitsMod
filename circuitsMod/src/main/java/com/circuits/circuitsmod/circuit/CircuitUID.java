package com.circuits.circuitsmod.circuit;

import java.io.Serializable;
import java.util.Optional;

public class CircuitUID implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private static int lastUIDNum = -1;
	
	private final int idNum;
	
	private CircuitUID(int id) {
		this.idNum = id;
	}
	
	public static void bumpLastUID(int id) {
		lastUIDNum = Math.max(lastUIDNum, id);
	}
	
	public static CircuitUID getNextUID() {
		lastUIDNum++;
		return new CircuitUID(lastUIDNum);
	}
	
	
	/**
	 * Given the integer representation of a CircuitUID, return the corresponding UID
	 * @param idNum
	 * @return
	 */
	public static CircuitUID fromInteger(int idNum) {
		return new CircuitUID(idNum);
	}
	
	public int toInteger() {
		return idNum;
	}
	
	public static Optional<CircuitUID> fromString(String s) {
		try {
			return Optional.of(fromInteger(Integer.parseInt(s)));
		}
		catch (NumberFormatException e) {
			return Optional.empty();
		}
	}
	
	public String toString() {
		return "" + idNum;
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + idNum;
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
		CircuitUID other = (CircuitUID) obj;
		if (idNum != other.idNum)
			return false;
		return true;
	}
	
	
}
