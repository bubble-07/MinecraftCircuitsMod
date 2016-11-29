

public class Implementation {

	boolean input1;
	boolean input2;
	int output;
	int carryOut;
	boolean carryIn;
	
	public Implementation() {
		input1 = false;
		input2 = false;
		output = 0;
		carryOut = 0;
		carryIn = false;
	}
	
	public void tick(boolean i1, boolean i2, boolean cin) {
		input1 = i1;
		input2 = i2;
		carryIn = cin;
		output = toInt(value0());
		carryOut = toInt(value1());
	}
	
	public boolean value0() {
		return (input1 ^ input2) ^ carryIn;
	}
	
	public boolean value1() {
		return (input1 && input2) || (input2 && carryIn) || (carryIn && input1);
	}
	
	public int toInt(boolean input) {
		return ((input == true) ? 1 : 0);
	}
	
}
