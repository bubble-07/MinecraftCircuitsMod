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
}
