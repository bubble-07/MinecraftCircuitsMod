import java.io.Serializable;
public class Implementation implements Serializable {
    
        long storedVal;
        int bitWidth;
	
	public Implementation() {
	}
	
	public void tick(long data, boolean hold) {
            if (!hold) {
                storedVal = data;
            }
	}
	 
	public long value0() {
	    return this.storedVal; 
	}
        public String config(int bitWidth) {
            this.bitWidth = bitWidth;
            return "" + bitWidth;
        }
        public int[] inputWidths() {
            return new int[]{bitWidth, 1};
        }
        public int[] outputWidths() {
            return new int[]{bitWidth};
        }
}
