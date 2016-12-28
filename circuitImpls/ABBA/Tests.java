import java.io.Serializable;
public class Tests implements Serializable {
        int abs_counter = -1;
        int seq_counter = -1;
        int wholeInterval = 0;
        //This test for an ABBA circuit fundamentally doesn't care
        //about the timings, so long as we get the right activation sequence
        //This is done because tick-accuracy is really annoying to get right
        //and I don't want to subject players to that
	
	public Tests() {
	}
	
        public boolean test(long aIn, long bIn) {
            boolean a = aIn > 0;
            boolean b = bIn > 0;

            abs_counter++;
            if (seq_counter != -1) {
                switch (seq_counter) {
                    case 0:
                        if (a && !b) {
                            return true;
                        }
                        if (a && b) {
                            seq_counter++;
                            return true;
                        }
                        return false;
                    case 1:
                        if (a && b) {
                            return true;
                        }
                        if (a && !b) {
                            seq_counter++;
                            return true;
                        }
                        return false;
                    case 2:
                        if (a && !b) {
                            return true;
                        }
                        if (!a && !b) {
                            seq_counter++;
                            return true;
                        }
                        return false;
                    case 3:
                        return !a && !b;
                }
            }
            else if (a && !b) {
                seq_counter = 0;
            }
            else if (abs_counter >= 4) {
                //Fail the test, waited too long to start the sequence
                return false;
            }
            if (abs_counter == this.wholeInterval - 1) {
                return seq_counter == 3;
            }

            return true;
        }
        public boolean input0() {
            return abs_counter == 0;
        }
        
        public boolean slowable() {
            return false;
        }

        public int numTests() {
            return this.wholeInterval;
        }

        public String config(int aAlone, int bInterval) {
            this.wholeInterval = 2 * aAlone + bInterval + 6;
            return aAlone + "," + bInterval;
        }
}
