package circuitsMod;

import static org.junit.Assert.*;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;




import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;







import com.circuits.circuitsmod.reflective.ChipInvoker;
import com.circuits.circuitsmod.reflective.Invoker;
import com.circuits.circuitsmod.circuit.CircuitConfigOptions;
import com.circuits.circuitsmod.common.BusData;
import com.circuits.circuitsmod.common.OptionalUtils;
import com.google.common.collect.Lists;


public class ChipInvokerTest {
	

	@Before
	public void setUp() throws Exception {
		
	}
	
	/**
	 * Given a class to load and something that takes a ChipInvoker
	 * and returns an optional string, representing an error message (if any),
	 * perform tester, and fail if an error message is present. If the class fails to load, fail. 
	 * @param toLoad
	 * @param tester
	 */
	public void presenceTest(Class<?> toLoad, Function<ChipInvoker, Optional<String>> tester) {
		Optional<ChipInvoker> testInvoker = ChipInvoker.getInvoker(toLoad, new CircuitConfigOptions());
		String name = toLoad.getName();
		if (!testInvoker.isPresent()) {
			fail("Failed to load " + name);
		}
		ChipInvoker invoker = testInvoker.get();
		Optional<String> errorMsg = tester.apply(invoker);
		if (errorMsg.isPresent()) {
			fail(toLoad.getName() + " " + errorMsg.get());
		}
	}
	
	public void absenceTest(Class<?> toLoad) {
		if(ChipInvoker.getInvoker(toLoad, new CircuitConfigOptions()).isPresent()) {
			fail("Should not have loaded " + toLoad.getName());
		}
	}
	
	/**
	 * A simple test class which conforms to the circuits API -- will provide
	 * a way to internally (without loading .class files) test the functionality
	 * of ChipInvoker. This one is just the identity circuit on a 32-bit input.
	 * @author bubble-07
	 *
	 */
	public static class SimpleTest { 
		private int result;
		public void tick(int input) {
			result = input;
		}
		public int value0() {
			return result;
		}
	}
	
	private Optional<String> checkArray(int[] real, int[] expected, String kind) {
		if (real.length != expected.length) {
			return Optional.of("We expect " + expected.length + " " + kind + " but we have" + real.length);
		}
		for (int i = 0; i < real.length; i++) {
			if (real[i] != expected[i]) {
				return Optional.of(kind + " don't match! Expected " + expected + 
						           "but got" + real);
			}
		}
		return Optional.empty();
	}
	
	public Optional<String> checkInputWidths(ChipInvoker in, int... widths) {
		return checkArray(in.inputWidths(), widths, "input widths");
	}
	
	public Optional<String> checkOutputWidths(ChipInvoker in, int... widths) {
		return checkArray(in.outputWidths(), widths, "output widths");
	}
	
	public Optional<String> checkIO(ChipInvoker in, Invoker.State state, long[] inputs, long[] outputs) {
		List<BusData> busInputs = Lists.newArrayList();
		for (int i = 0; i < inputs.length; i++) {
			busInputs.add(new BusData(in.inputWidths()[i], inputs[i]));
		}
		List<BusData> busOutputs = in.invoke(state, busInputs);
		for (int i = 0; i < outputs.length; i++) {
			if (busOutputs.get(i).getData() != outputs[i]) {
				return Optional.of("Expected " + outputs + " but got " + busOutputs + " on inputs "
						           + inputs);
			}
			if (busOutputs.get(i).getWidth() != in.outputWidths()[i]) {
				return Optional.of("Expected bus widths: " + in.outputWidths() + " but got " +
			                        busOutputs.stream().map(BusData::getWidth).collect(Collectors.toList()));
			}
		}
		return Optional.empty();
	}
	

	@Test
	public void simpleTest() {
		presenceTest(SimpleTest.class, (invoker) -> {
			return OptionalUtils.firstOf(checkInputWidths(invoker, 32),
					                     checkOutputWidths(invoker, 32),
					                     checkIO(invoker, invoker.initState(), 
					                    		 new long[]{123}, new long[]{123}));
		});
	}
	
	/**
	 * Simple test class for sequential circuits (accumulator)
	 * @author bubble-07
	 *
	 */
	public static class SimpleSequentialTest { 
		private int result = 0;
		public void tick(short input) {
			result += input;
		}
		public int value0() {
			return result;
		}
		public boolean isSequential() {
			return true;
		}
	}
	
	@Test
	public void simpleSequentialTest() {
		presenceTest(SimpleSequentialTest.class, (invoker) -> {
			Invoker.State state = invoker.initState();
			return OptionalUtils.firstOf(checkInputWidths(invoker, 16),
					                     checkOutputWidths(invoker, 32),
					                     checkIO(invoker, state,
					                    		 new long[]{1}, new long[]{1}),
					                     checkIO(invoker, state,
					                    		 new long[]{1}, new long[]{2}));
		});
	}
	
	/**
	 * Simple test class for a 4-bit input, 4-bit output
	 * circuit defined using the outputWidths(), inputWidths() methods
	 * @author bubble-07
	 *
	 */
	public static class LimitingWidthTest {
		private byte result = 0;
		public void tick(byte input) {
			result = input;
		}
		public byte value0() {
			return result;
		}
		public int[] outputWidths() {
			return new int[]{4};
		}
		public int[] inputWidths() {
			return new int[]{4};
		}
	}
	
	@Test
	public void limitingWidthTest() {
		presenceTest(LimitingWidthTest.class, (invoker) -> {
			return OptionalUtils.firstOf(checkInputWidths(invoker, 4),
					                     checkOutputWidths(invoker, 4),
					                     checkIO(invoker, invoker.initState(),
					                    		 new long[]{1}, new long[]{1}));
		});
	}
	
	/**
	 * A test to ensure that all default type-widths are determined correctly
	 * @author bubble-07
	 *
	 */
	public static class LowArgTypesTest {
		public void tick(boolean booley, byte bitey, short shortie) {
		}
		public byte value0() {
			return 0;
		}
	}
	@Test
	public void lowArgTypesTest() {
		presenceTest(LowArgTypesTest.class, (invoker) -> {
			return checkInputWidths(invoker, 1, 8, 16);
		});
	}
	
	public static class HighArgTypesTest {
		public void tick(int x, long y) {
		}
		public byte value0() {
			return 0;
		}
	}
	
	@Test
	public void highArgTypesTest() {
		presenceTest(HighArgTypesTest.class, (invoker) -> {
			return checkInputWidths(invoker, 32, 64);
		});
	}
	
	/**
	 * A test to ensure that circuits with more than three inputs are rejected.
	 */
	public static class TooManyArgs {
		public void tick(int x, int y, int z, int w) { }
		public int value0() { return 1; }
	}
	
	@Test
	public void tooManyArgsTest() {
		absenceTest(TooManyArgs.class);
	}
	
	/**
	 * A test to ensure that circuits with circuits with (#inputs + #outputs) > 4 are rejected
	 */
	public static class TooManyWires {
		public void tick(int x, int y, int z, int w) { }
		public int value0() { return 1;}
		public int value1() { return 1;}
	}
	
	@Test
	public void tooManyWiresTest() {
		absenceTest(TooManyWires.class);
	}
	
	/**
	 * A test to ensure that circuits with non-sequential output numbers are rejected
	 * @author bubble-07
	 *
	 */
	public static class NonSequentialOutputs {
		public void tick(int x) { }
		public int value0() { return 1;}
		public int value2() { return 1;}
	}
	
	@Test
	public void nonSequentialWiresTest() {
		absenceTest(NonSequentialOutputs.class);
	}

}
