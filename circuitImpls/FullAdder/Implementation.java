import java.io.Serializable;
public class Implementation implements Serializable {
        int bitWidth;
        long sum;
        boolean carryOut;
	
	public Implementation() {
	}

        public int[] inputWidths() {
            return new int[]{bitWidth * 2, 1};
        }
        public int[] outputWidths() {
            return new int[]{1, bitWidth};
        }

        public String config(int bitWidth) {
            if (bitWidth == 64 || !Utils.isValidBusWidth(bitWidth)) {
                return null;
            }
            this.bitWidth = bitWidth;
            return "" + bitWidth;
        }
	
	public void tick(long combinedInputs, boolean carryIn) {
            long input1 = combinedInputs >> bitWidth;
            long input2 = (combinedInputs - (input1 << bitWidth));
            sum = input1 + input2 + (carryIn ? 1 : 0);
            carryOut = (sum >> bitWidth) > 0;
	}
	
	public boolean value0() {
		return carryOut;
	}
	
	public long value1() {
            return sum;
	}
}
