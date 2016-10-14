package com.circuits.circuitsmod.common;

import java.util.Optional;

public class OptionalUtils {
	@SafeVarargs
	public static <T> Optional<T> firstOf(Optional<T>... opts) {
		for (Optional<T> opt : opts) {
			if (opt.isPresent()) {
				return opt;
			}
		}
		return Optional.empty();
	}
	/**
	 * Converts a Java 8 optional to a google optional. This is why aggressive library
	 * standardization should be the norm.
	 * @param in
	 * @return
	 */
	public static <T> com.google.common.base.Optional<T> toGoogle(Optional<T> in) {
		if (in.isPresent()) {
			return com.google.common.base.Optional.of(in.get());
		}
		return com.google.common.base.Optional.absent();
	}
}
