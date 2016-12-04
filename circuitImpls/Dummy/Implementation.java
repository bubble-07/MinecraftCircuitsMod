
public class Implementation {

        long input1;
        int bitWidth;
	
	public Implementation() {
		input1 = 0L;
	}
	
	public void tick(long i1) {
		 input1 = i1;
	 }
	 
	public long value0() {
		 return 0L;
	}
        public String config(int bitWidth) {
            this.bitWidth = bitWidth;
            return "" + bitWidth;
        }
        public int[] inputWidths() {
            return new int[]{bitWidth};
        }

}
