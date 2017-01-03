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
            if (!Utils.isValidBusWidth(bitWidth)) {
                return null;
            }
            this.delayLen = delayLen;
            this.bitWidth = bitWidth;
            this.vals = new long[delayLen];
            //The delay is capped at 32, because otherwise, 
            //you would need to download more RAM or somethin'
            if (delayLen < 1 || delayLen > 32) {
                return null;
            }
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
