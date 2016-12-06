import java.io.Serializable;
public class Implementation implements Serializable {
        int aAlone;
        int bInterval;
        int tick;
	
	public Implementation() {
	}
	
	public void tick(boolean i1) {
            tick++;
            if (i1) {
                tick = 0;
            }
	}
	
        //A output
	public boolean value0() {
            return tick <= 2 * aAlone + bInterval;
	}

        //B output
        public boolean value1() {
            return tick >= aAlone && tick <= aAlone + bInterval;
        }

        public String config(int aAlone, int bInterval) {
            this.aAlone = aAlone;
            this.bInterval = bInterval;
            this.tick = 2 * aAlone + bInterval + 1;
            return aAlone + "," + bInterval;
        }

        public boolean isSequential() {
            return true;
        }
}
