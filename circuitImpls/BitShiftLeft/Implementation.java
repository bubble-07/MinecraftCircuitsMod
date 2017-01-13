import java.io.Serializable;
public class Implementation implements Serializable {
        long input;
        int bitWidth;
        int shiftAmt;
	
	public Implementation() {
	}
	
	public void tick(long i1) {
            this.input = i1; 
	}
	 
	public long value0() {
            return input << shiftAmt;
	}
        public String config(int shiftAmt, int bitWidth) {
            if (shiftAmt < 1 || shiftAmt >= bitWidth) {
                return null;
            }
            if (!Utils.isValidBusWidth(bitWidth) || bitWidth < 2) {
                return null;
            }
            this.shiftAmt = shiftAmt;
            this.bitWidth = bitWidth;
            return "shift=" + shiftAmt + ", width=" + bitWidth;
        }
        public int[] inputWidths() {
            return new int[]{bitWidth};
        }
        public int[] outputWidths() {
            return new int[]{bitWidth};
        }
}
