package com.circuits.circuitsmod.common;

public class Pair<X, Y> {
	private final X x;
	private final Y y;
	private Pair(X x, Y y) {
		this.x = x;
		this.y = y;
	}
	public static <X, Y> Pair<X, Y> of(X x, Y y) {
		return new Pair<X, Y>(x, y);
	}
	public X first() {
		return x;
	}
	public Y second() {
		return y;
	}
}
