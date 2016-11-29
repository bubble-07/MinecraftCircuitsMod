

public class Implementation {
	
	long input;
	long output;
	
	public Implementation() {
		input = 0;
		output = 0;
	}
	
	public void tick(long i1) {
		input = i1;
		output = value0();
	}
	
	long value0() {
		return ~input;
	}
	
	public int[] inputWidths() {
		int[] returnArray = {64};
		return returnArray;
	}
	
	public int[] outputWidths() {
		int[] returnArray = {64};
		return returnArray;
	}
	
}
