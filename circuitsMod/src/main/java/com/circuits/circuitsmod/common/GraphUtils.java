package com.circuits.circuitsmod.common;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.Stack;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class GraphUtils {

	public static <T> Optional<T> generalSearch(T init, Function<T, Stream<T>> neighbors, Predicate<T> success) {
		//TODO: Make this a stream-generating function!
		Set<T> searched = new HashSet<T>();
		Stack<T> front = new Stack<T>();
		front.add(init);
		while (!front.isEmpty()) {
			T current = front.pop();
			if (success.test(current)) {
				return Optional.of(current);
			}
			searched.add(current);
			neighbors.apply(current).filter((p) -> !searched.contains(p))
				.forEach((p) -> front.push(p));
		}
		return Optional.empty();
	}

}
