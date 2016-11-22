
public class Implementation {

        byte input;
	
	public Implementation() {
	}
	
	public void tick(byte input) {
            this.input = input;
	 }
	 
	public boolean value0() {
            return (input & 2) > 0;	 
        }
        public boolean value1() {
            return (input & 1) > 0;
        }
        public int[] inputWidths() {
            return new int[]{2};
        }
}
