public class Tests {

        int abs_counter = 0;
        boolean startedPulse = false;
        boolean endedPulse = false;

	public Tests() {
	}
	
        public boolean test(long out) {
            if (abs_counter >= 7) {
                return endedPulse;
            }
            if (abs_counter < 1) {
                return (out % 2) == 0;
            }
            if (!startedPulse) {
                if ((out % 2) != 0 && abs_counter >= 1) {
                    startedPulse = true;
                }
            }
            else {
                if ((out % 2) == 0) {
                    endedPulse = true;
                }
            }

            if (endedPulse && (out % 2) != 0) {
                return false;
            }

            abs_counter++;
            return true;
        }
        public long input0() {
            return abs_counter < 2 ? 0 : 1;
        }
       
        public int numTests() {
            return 8;
        }
        public boolean slowable() {
            return false;
        }
}
