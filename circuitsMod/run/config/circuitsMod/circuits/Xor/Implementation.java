
public class Implementation {

        long input1;
        long input2;
        int bitWidth;
	
	public Implementation() {
		input1 = 0L;
		input2 = 0L;
	}
	
	public void tick(long i1, long i2) {
		 input1 = i1;
		 input2 = i2;
	 }
	 
	public long value0() {
		 return input1 ^ input2;
	}
        public String config(int bitWidth) {
            this.bitWidth = bitWidth;
            return "" + bitWidth;
        }
        public int[] inputWidths() {
            return new int[]{bitWidth, bitWidth};
        }
        public int[] outputWidths() {
            return new int[]{bitWidth};
        }
}
