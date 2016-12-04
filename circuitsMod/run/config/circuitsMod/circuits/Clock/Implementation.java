
public class Implementation {
        int clockLen;
        int tick;
	
	public Implementation() {
	}
	
	public void tick(boolean reset) {
            this.tick++;
            if (this.tick >= clockLen * 2) {
                this.tick = 0;
            }
            if (reset) {
                this.tick = 0;
            }
	}
	
	public boolean value0() {
           return (this.tick >= clockLen);
	}
        public String config(int clockLen) {
            this.clockLen = clockLen;
            return "" + clockLen;
        }
	
	public boolean isSequential() {
		return true;
	}
}
