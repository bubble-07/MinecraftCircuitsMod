public class Tests {

    //For the test of the clock, we really, really don't care about
    //the timing on the "reset" input relative to the rest of the circuit,
    //so long as we start with a "low" signal after reset
    
    int risingEdgeHitTime = -1;
    int tick = 0;

	public Tests() {
	}

        int clockLen;
	
        public boolean test(long out) {
            if (tick < 2) {
                tick++;
                return true;
            }
            if (tick == 2 && out != 0) {
                //Already got the "reset" signal, but not reset!
                return false;
            }
            if (risingEdgeHitTime == -1 && out != 0) {
                risingEdgeHitTime = tick;
                tick++;
                return true;
            }
            if (risingEdgeHitTime == -1 && tick > 2 * clockLen) {
                //Too long for the first rising edge
                return false; 
            }

            tick++;
            return ((((tick - 1) - risingEdgeHitTime) / clockLen) % 2) != (out % 2);
        }
        
        public String config(int clockLen) {
            this.clockLen = clockLen;
            return "" + clockLen;
        }

        public long input0() {
            return tick < clockLen ? 1 : 0;
        }
       
        public int numTests() {
            return clockLen * 5;
        }
        public boolean slowable() {
            return false;
        }
}
