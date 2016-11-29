
public class Implementation {
	int output;
	int input1;
	int reset;
	double time;
	
	public Implementation() {
		output = 0;
		time = 0;
		input1 = 0;
		reset = 0;
	}
	
	public void tick(int i1, int reset) {
		time = time + 0.10;
		input1 = i1;
		this.reset = reset;
		output = value0();
	}
	
	public int value0() {
		if (time % 2 > 0 || reset > 0)
			return 15;
		 else
			return 0;
	}
	
	public boolean isSequential() {
		return true;
	}
}
