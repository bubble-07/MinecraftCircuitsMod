import java.io.Serializable;
//Splitters will be named after their input widths,
//value0 will be the high bits, value1 will be the low bits (convention)
public class Implementation implements Serializable {
        int halfWidth;
        int bitWidth;
        long lowBits;
        long highBits;
	
	public Implementation() {
	}

        public String config(int bitWidth) {
            this.bitWidth = bitWidth;
            this.halfWidth = bitWidth >> 1;
            return "" + bitWidth;
        }
	
	public void tick(long data) {
            this.highBits = data >> halfWidth;
            this.lowBits = data - (highBits << halfWidth);
	 }
	 
	public long value0() {
            return highBits;
        }

        public long value1() {
            return lowBits;
        }

        public int[] inputWidths() {
            return new int[]{this.bitWidth};
        }
        public int[] outputWidths() {
            return new int[]{this.halfWidth, this.halfWidth};
        }
}
