import java.io.Serializable;
public class Implementation implements Serializable {
        int delayLen;
        int bitWidth;
        long[] vals;
	
	public Implementation() {
	}

        private void shift() {
            for (int i = 1; i < vals.length; i++) {
                vals[i - 1] = vals[i];
            }
        }
	
	public void tick(long input) {
            shift();
            vals[vals.length - 1] = input;
	}
	
	public long value0() {
            return vals[0];
	}

        public String config(int delayLen, int bitWidth) {
            this.delayLen = delayLen;
            this.bitWidth = bitWidth;
            this.vals = new long[delayLen + 1];
            return "delay=" + delayLen + ",width=" + bitWidth;
        }

        public int[] inputWidths() {
            return new int[]{this.bitWidth};
        }
        public int[] outputWidths() {
            return new int[]{this.bitWidth};
        }
	
	public boolean isSequential() {
		return true;
	}
}
