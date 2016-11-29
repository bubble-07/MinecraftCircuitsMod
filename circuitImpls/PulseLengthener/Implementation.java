
public class Implementation {
	long input;
	long output;
	long multiplier;
	
	public Implementation() {
		input = 0;
		output = 0;
		multiplier = 2;
	}
	
	public void tick(long i1) {
		input = i1;
		output = value0();
	}
	
	public long value0() {
		return input*multiplier;
	}
	
	public void setMultiplier(long multiplier) {
		this.multiplier = multiplier;
	}
}
