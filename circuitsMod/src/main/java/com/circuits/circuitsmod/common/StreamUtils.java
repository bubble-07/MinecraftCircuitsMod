package com.circuits.circuitsmod.common;

import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

public class StreamUtils {
	
	public static <T, S> Stream<Pair<T, S>> focusStream(Stream<T> in, Function<T, S> derived) {
		return in.map((t) -> Pair.of(t, derived.apply(t)));
	}
	
	public static <T, S> Stream<S> optionalMap(Stream<T> in, Function<T, Optional<S>> f) {
		return in.flatMap((T t) -> {
			Optional<S> result = f.apply(t);
			if (result.isPresent()) {
				return Stream.of(result.get());
			}
			return Stream.empty();
		});
	}
	
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
