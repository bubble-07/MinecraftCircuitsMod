
public class Implementation {
	boolean input1;
	boolean prevInput;
	boolean output;
	
	public Implementation() {
		input1 = false;
		output = false;
		prevInput = false;
	}
	
	public void tick(boolean i1) {
		prevInput = input1;
		input1 = i1;
		output = value0();
	}
	
	public boolean value0() {
		if (!prevInput && input1) {
			return true;
		} else
			return false;
	}
}
