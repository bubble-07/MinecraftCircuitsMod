import java.io.Serializable;
public class Implementation implements Serializable {
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
	}
	
	public boolean value0() {
		if (!prevInput && input1) {
			return true;
		} else
			return false;
	}
        public boolean isSequential() {
            return true;
        }
}
