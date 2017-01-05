package circuitsMod;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.circuits.circuitsmod.circuit.CircuitConfigOptions;
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
		if (TestGeneratorInvoker.getInvoker(TestGeneratorInvokerTest.class, new CircuitConfigOptions()).isPresent()) {
			fail("Test Generator Invokers load with no test() method? Huh?");
		}
	}
	//TODO: Write more extensive tests!
}
