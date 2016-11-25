package com.circuits.circuitsmod.common;

import java.util.Map;
import java.util.Optional;

public class MapUtils {
	public static <K, V> Optional<V> lookup(Map<K, V> m, K key) {
		if (m.containsKey(key)) {
			return Optional.of(m.get(key));
		}
		return Optional.empty();
	}
}
