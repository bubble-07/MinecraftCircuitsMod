import java.io.Serializable;
public class Implementation implements Serializable {

    int ticksSinceLast;
    int length;
	
	public Implementation() {
	}
	
	public void tick(boolean input) {
            ticksSinceLast++;
            if (input) {
                ticksSinceLast = 0;
            }
	 }
	 
	public boolean value0() {
            return ticksSinceLast <= length;
	}
        public String config(int length) {
            this.length = length;
            this.ticksSinceLast = length + 1;
            return "" + length;
        }
        public boolean isSequential() {
            return true;
        }
}
