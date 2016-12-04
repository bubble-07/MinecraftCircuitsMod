
public class Implementation {
	
	boolean input1;
	boolean input2;
	
	public Implementation() {
		input1 = false;
		input2 = false;
	}
	
	public void tick(boolean i1, boolean i2) {
		 input1 = i1;
		 input2 = i2;
	 }
	 
	public boolean value0() {
		 return input1 && input2;
	 }
}
