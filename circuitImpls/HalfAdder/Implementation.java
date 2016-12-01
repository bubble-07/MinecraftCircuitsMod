

public class Implementation {
	boolean input;
	boolean cin;
	
	public Implementation() {
	}
	
	public void tick(boolean input, boolean cin) {
		this.input = input;
                this.cin = cin;
	}
	
        //Carry out
	public boolean value0() {
            return input && cin;
	}
        
        //Value out
	public boolean value1() {
            return input ^ cin;
	}
}
