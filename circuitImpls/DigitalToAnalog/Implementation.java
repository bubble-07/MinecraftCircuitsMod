

public class Implementation {
    byte input;
	
	public Implementation() {
	}
	
	public void tick(byte i1) {
            this.input = i1;
	}
	
	public byte value0() {
            return input;
	}
        
        public int[] inputWidths() {
            return new int[]{4};
        }
        public int[] outputWidths() {
            return new int[]{4};
        }
        public boolean[] analogOutputs() {
            return new boolean[]{true};
        }
}
