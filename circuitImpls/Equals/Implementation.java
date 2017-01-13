import java.io.Serializable;
public class Implementation implements Serializable {

        long input1;
        long input2;
        int bitWidth;
	
	public Implementation() {
	}
	
	public void tick(long i1, long i2) {
		 input1 = i1;
		 input2 = i2;
	 }
	 
	public boolean value0() {
            return input1 == input2;
	}
        public String config(int bitWidth) {
            if (!Utils.isValidBusWidth(bitWidth)) {
                return null;
            }
            this.bitWidth = bitWidth;
            return "" + bitWidth;
        }
        public int[] inputWidths() {
            return new int[]{bitWidth, bitWidth};
        }
}
