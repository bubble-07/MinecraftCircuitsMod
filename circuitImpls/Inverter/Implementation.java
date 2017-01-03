import java.io.Serializable;
public class Implementation implements Serializable {

        long input1;
        int bitWidth;
	
	public Implementation() {
		input1 = 0L;
	}
	
	public void tick(long i1) {
		 input1 = i1;
	 }
	 
	public long value0() {
		 return ~input1;
	}
        public String config(int bitWidth) {
            if (!Utils.isValidBusWidth(bitWidth)) {
                return null;
            }
            this.bitWidth = bitWidth;
            return "" + bitWidth;
        }
        public int[] inputWidths() {
            return new int[]{bitWidth};
        }
        public int[] outputWidths() {
            return new int[]{bitWidth};
        }
}
