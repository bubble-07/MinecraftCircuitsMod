package com.circuits.circuitsmod.common;

import java.util.function.Function;
import java.util.stream.Stream;

public class StreamUtils {
	public static <T> Stream<Pair<Integer, T>> indexStream(Stream<T> in) {
		
		Function<T, Pair<Integer, T>> indexer = new Function<T, Pair<Integer, T>>() {
			int index = -1;
			@Override
			public Pair<Integer, T> apply(T t) {
				index++;
				return Pair.of(index, t);
			}
		};
		return in.sequential().map(indexer);
	}
	public static Stream<Boolean> fromArray(boolean[] array) {
		Boolean[] temp = new Boolean[array.length];
		for (int i = 0; i < array.length; i++) {
			temp[i] = array[i];
		}
		
		return Stream.of(temp);
	}
}
