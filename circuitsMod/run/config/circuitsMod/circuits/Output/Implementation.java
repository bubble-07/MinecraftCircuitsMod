import java.io.Serializable;
public class Implementation implements Serializable {
        int bitWidth;
        int index;
        long output;
	
	public Implementation() {
	}
	
	public void tick(long i1) {
            this.output = i1;
	}
	 
	public long value0() {
            return output;
	}
        public String config(int index, int bitWidth) {
            this.bitWidth = bitWidth;
            this.index = index;
            return "num=" + index + ",width=" + bitWidth;
        }
        public int[] inputWidths() {
            return new int[]{bitWidth};
        }
}
