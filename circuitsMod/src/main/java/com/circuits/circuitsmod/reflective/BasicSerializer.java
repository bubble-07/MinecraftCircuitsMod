package com.circuits.circuitsmod.reflective;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
			try {
				Object val = f.get(o);
				vals.add(val);
				
			} catch (IllegalAccessException | IllegalArgumentException e) {
				return Optional.empty();
			}
		}
		List<BusData> busDatas = StreamUtils.optionalMap(vals.stream(), val -> Optional.ofNullable(Invoker.bus(val))).collect(Collectors.toList());
		return Optional.of(BusData.listToBytes(busDatas));
	}
	
	private static List<Field> getSortedFieldsOf(Object o) {
		Class<?> clazz = o.getClass();
		//First, sort the fields by name
		List<Field> fields = Stream.of(clazz.getDeclaredFields()).sorted((f1, f2) -> f1.getName().compareTo(f2.getName())).collect(Collectors.toList());
		fields.forEach((f) -> f.setAccessible(true));
		return fields;
	}
	
	
	public static <T> Optional<T> deserialize(T instance, byte[] bytes) {
		Optional<List<BusData>> busdatas = BusData.listFromBytes(bytes);
		if (!busdatas.isPresent()) {
			return Optional.empty();
		}
		
		List<Object> objs = StreamUtils.optionalMap(busdatas.get().stream(), 
				                                    (data) -> Optional.ofNullable(Invoker.unBus(data))).collect(Collectors.toList());
		
		List<Field> fields = getSortedFieldsOf(instance);
		int i = 0;
		for (Field f : fields) {
			if (Invoker.getTypeWidth(f.getType()) != 0) {
				try {
					f.set(instance, objs.get(i));
				} catch (IllegalArgumentException | IllegalAccessException | IndexOutOfBoundsException e) {
					return Optional.empty();
				}
				i++;
			}
		}
		return Optional.of(instance);
	}
		
}
