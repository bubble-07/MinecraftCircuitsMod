import java.io.Serializable;
public class Tests implements Serializable {

        int seq_counter = 0;
        long maxVal;
        int MAX_TESTS = 5;

	public Tests() {
	}
	
        public boolean test(long out) {
            long val = pseudoRand(seq_counter / 4);
            seq_counter++;
            return (val % maxVal) == (out % maxVal);
        }
        public long input0() {
            if ((seq_counter % 4) != 2) {
                return pseudoRand(seq_counter / 4);
            }
            return 0;
        }
        public long input1() {
            return ((seq_counter % 4) == 1) || ((seq_counter % 4) == 2) ? 1 : 0;
        }
        
        //Maybe this should just return 4 or something instead
        public long pseudoRand(int seed) {
            return (seed * 31) % maxVal;
        }

        public int numTests() {
            return this.MAX_TESTS * 4;
        }

        public String config(int bitWidth) {
            this.maxVal = (((long) 1) << ((long) bitWidth));
            return "" + bitWidth;
        }
}
