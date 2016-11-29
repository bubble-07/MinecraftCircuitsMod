

public class Implementation {
	int input1;
	byte output;
	
	public Implementation() {
		input1 = 0;
		output = 0;
	}
	
	public void tick(byte i1) {
		input1 = i1;
		output = value0();
	}
	
	public byte value0() {
		return (byte) Math.abs(input1);
	}
}
