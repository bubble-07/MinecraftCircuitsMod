import java.io.Serializable;
public class Implementation implements Serializable {
        int inputNum;
        int bitWidth;

        long output;
	
	public Implementation() {
	}
	
	public void tick(long input1) {
            this.output = input1;
	}
	 
	public long value0() {
            return output;
	}
        public String config(int inputNum, int bitWidth) {
            this.inputNum = inputNum;
            this.bitWidth = bitWidth;
            return "num=" + inputNum + ",width=" + bitWidth;
        }
        public int[] outputWidths() {
            return new int[]{bitWidth};
        }
}
