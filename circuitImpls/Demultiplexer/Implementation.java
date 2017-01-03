import java.io.Serializable;
//Convention: a high signal on the select line of a demux indicates the 0th (leftmost) output
public class Implementation implements Serializable {

        long input1;
        boolean select;

        int bitWidth;
	
	public Implementation() {}
	
	public void tick(long i1, boolean select) {
		 input1 = i1;
                 this.select = select;
	 }
	 
	public long value0() {
            return select ? input1 : 0;
	}

        public long value1() {
            return select ? 0 : input1; 
        }

        public String config(int bitWidth) {
            if (!Utils.isValidBusWidth(bitWidth)) {
                return null;
            }
            this.bitWidth = bitWidth;
            return "" + bitWidth;
        }
        public int[] inputWidths() {
            return new int[]{bitWidth, 1};
        }
        public int[] outputWidths() {
            return new int[]{bitWidth, bitWidth};
        }
}
