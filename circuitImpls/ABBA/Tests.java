import java.io.Serializable;
public class Tests implements Serializable {
        int counter = -1;
        int wholeInterval = 0;
	
	public Tests() {
	}
	
        public boolean tick() {
            counter++;
            return counter < wholeInterval;
        }
        public boolean input0() {
            return counter == 1;
        }
        
        public boolean slowable() {
            return false;
        }

        public int numTests() {
            return this.wholeInterval;
        }

        public String config(int aAlone, int bInterval) {
            this.wholeInterval = 2 * aAlone + bInterval + 3;
            return aAlone + "," + bInterval;
        }
}
