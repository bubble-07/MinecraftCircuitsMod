public class Tests {
    int delayLen;
    int bitWidth;
    int tick = -1;
    long maxVal;

    public Tests() {
    }

    long testCase(int tickVal) {
        if (tickVal == 0) {
            return 31 % maxVal;
        }
        else if (tickVal == delayLen + 1) {
            return 62 % maxVal;
        }
        else if (tickVal == 2 * (delayLen + 1)) {
            return 71 % maxVal;
        }
        return 0;
    }
    
    public boolean test(long out) {
        tick++;
        return (testCase(tick - delayLen - 1) % maxVal) == (out % maxVal);
    }
    
    public String config(int delayLen, int bitWidth) {
        this.delayLen = delayLen;
        this.bitWidth = bitWidth;
        this.maxVal = ((long) 1) << ((long) bitWidth);
        return "";
    }

    public long input0() {
        return testCase(tick); 
    }
   
    public int numTests() {
        return (delayLen + 1) * 3;
    }
    public boolean slowable() {
        return false;
    }
}
