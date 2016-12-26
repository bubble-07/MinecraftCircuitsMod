package com.circuits.circuitsmod.reflective;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.ArrayUtils;

import com.circuits.circuitsmod.common.BusData;
import com.circuits.circuitsmod.common.StreamUtils;
import com.google.common.collect.Lists;

/**
 * Provides methods to serialize/deserialize the unboxed Java primitive fields of an arbitrary Object.
 * This is used for the default serialization of sequential circuits
 * 
 * As this is only the default serializer, it can afford to be pretty dumb. Consequently, the
 * byte-array serialization format here just concatenates BusData serializations of all Java primitive types in the class
 * 
 * @author bubble-07
 *
 */
public class BasicSerializer {
	public static Optional<byte[]> serialize(Object o) {
		List<Object> vals = Lists.newArrayList();
		
		List<Field> fields = getSortedFieldsOf(o);
		
		for (Field f : fields) {
			f.setAccessible(true);
			try {
				Object val = f.get(o);
				vals.add(val);
				
			} catch (IllegalAccessException | IllegalArgumentException e) {
				return Optional.empty();
			}
		}
		Byte[] result = StreamUtils.optionalMap(vals.stream(), val -> Optional.ofNullable(Invoker.bus(val)))
		           .flatMap(b -> Stream.of(ArrayUtils.toObject(b.toBytes()))).toArray(Byte[]::new);
		return Optional.of(ArrayUtils.toPrimitive(result));
		
	}
	
	private static List<Field> getSortedFieldsOf(Object o) {
		Class<?> clazz = o.getClass();
		//First, sort the fields by name
		List<Field> fields = Stream.of(clazz.getDeclaredFields()).sorted((f1, f2) -> f1.getName().compareTo(f2.getName())).collect(Collectors.toList());
		return fields;
	}
	
	
	public static <T> Optional<T> deserialize(T instance, byte[] fields) {
		if (fields.length % 12 != 0) {
			return Optional.empty();
		}
		List<byte[]> chunked = Lists.newArrayList();
		ByteBuffer buf = ByteBuffer.wrap(fields);
		while (buf.hasRemaining()) {
			byte[] toAdd = new byte[12];
			buf = buf.get(toAdd);
			chunked.add(toAdd);
		}
		List<Optional<Object>> objs = chunked.stream().map(BusData::fromBytes)
				                              .map((busData) -> busData.flatMap((data) -> Optional.ofNullable(Invoker.unBus(data)))).collect(Collectors.toList());
		
		ByteBuffer.wrap(fields).get(new byte[12]);
	}
		
}
