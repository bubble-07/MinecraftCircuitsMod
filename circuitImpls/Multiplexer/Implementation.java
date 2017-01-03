import java.io.Serializable;
//Convention: a high signal on the select line of a mux indicates the 0th (leftmost) input
public class Implementation implements Serializable {

        long input1;
        long input2;
        boolean select;

        int bitWidth;
	
	public Implementation() {}
	
	public void tick(long i1, long i2, boolean select) {
		 input1 = i1;
		 input2 = i2;
                 this.select = select;
	 }
	 
	public long value0() {
            return select ? input1 : input2;
	}
        public String config(int bitWidth) {
            if (!Utils.isValidBusWidth(bitWidth)) {
                return null;
            }
            this.bitWidth = bitWidth;
            return "" + bitWidth;
        }
        public int[] inputWidths() {
            return new int[]{bitWidth, bitWidth, 1};
        }
        public int[] outputWidths() {
            return new int[]{bitWidth};
        }
}
