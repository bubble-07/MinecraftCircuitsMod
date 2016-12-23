import java.io.Serializable;
public class Implementation implements Serializable {
        int bitWidth;
        int bitSelect;
        boolean outputVal;
	
	public Implementation() {
	}

        public String config(int bitSelect, int bitWidth) {
            this.bitWidth = bitWidth;
            this.bitSelect = bitSelect;
            return "select=" + bitSelect + "width=" + bitWidth;
        }
	
	public void tick(long data) {
            for (int i = 0; i < bitSelect; i++) {
                data = data >> 1;
            }
            outputVal = (data & 1) > 0;
	}
	 
	public boolean value0() {
            return outputVal;
        }

        public int[] inputWidths() {
            return new int[]{this.bitWidth};
        }
}
