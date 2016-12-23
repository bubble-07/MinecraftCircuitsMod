import java.io.Serializable;
public class Implementation implements Serializable {
        int bitWidth;
        int bitSelect;
        long outputVal;
	
	public Implementation() {
	}

        public String config(int bitSelect, int bitWidth) {
            this.bitWidth = bitWidth;
            this.bitSelect = bitSelect;
            return "select=" + bitSelect + "width=" + bitWidth;
        }
	
	public void tick(boolean data) {
            outputVal = data ? 1 : 0;
            for (int i = 0; i < bitSelect; i++) {
                outputVal = outputVal << 1;
            }
	}
	 
	public long value0() {
            return outputVal;
        }

        public int[] outputWidths() {
            return new int[]{this.bitWidth};
        }
}
