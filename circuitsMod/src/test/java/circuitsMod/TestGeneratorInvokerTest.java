package circuitsMod;

import static org.junit.Assert.*;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

import com.circuits.circuitsmod.reflective.Invoker;
import com.circuits.circuitsmod.reflective.TestGeneratorInvoker;

/**
 * We fundamentally care less about testing the Test Generator Invoker,
 * because much of its functionality overlaps with ChipInvoker. 
 * Nevertheless, we should make sure there's no insanity in the
 * functionality specific to it.
 * @author bubble-07
 *
 */
public class TestGeneratorInvokerTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testFailsOnNoTickMethod() {
		if (TestGeneratorInvoker.getInvoker(TestGeneratorInvokerTest.class).isPresent()) {
			fail("Test Generator Invokers load with no tick() method? Huh?");
		}
	}
	public static class RealSimpleCase {
		int value = -1;
		public boolean tick() {
			return ++value < 3;
		}
		public int input0() {
			return value;
		}
		
	}
	@Test
	public void testRealSimpleCase() {
		Optional<TestGeneratorInvoker> invoker = TestGeneratorInvoker.getInvoker(RealSimpleCase.class);
		if (!invoker.isPresent()) {
			fail("Test Generator Invoker isn't present!");
		}
		Invoker.State state = invoker.get().initState();
		for (int i = 0; i < 3; i++) {
			if (invoker.get().invoke(state).get().get(0).getData() != i) {
				fail("Unexpected testGenerator case");
			}
		}
	}

}
