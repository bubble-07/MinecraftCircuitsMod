
public class Implementation {

        byte output;
	
	public Implementation() {
	}
	
	public void tick(boolean high, boolean low) {
            output = 0;
            output += high ? 2 : 0;
            output += low ? 1 : 0;
	 }
	 
	public byte value0() {
	    return output;	 
        }
        public int[] outputWidths() {
            return new int[]{2};
        }
}
