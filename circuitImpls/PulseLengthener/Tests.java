public class Tests {
    int length;
    int tick = -1;
    int startTick = -1;
    int endTick = -1;

    public Tests() {
    }
    
    public boolean test(long out) {
        tick++;
        if (out != 0 && startTick == -1) {
            startTick = tick;
        }
        if (out == 0 && startTick != -1 && endTick == -1) {
            endTick = tick;
        }
        if (tick == length + 4) {
            return (endTick - startTick) == length + 1;
        }
        return true;
    }
    
    public String config(int length) {
        this.length = length;
        return "";
    }

    public long input0() {
        return tick == 0 ? 1 : 0;
    }
   
    public int numTests() {
        return length + 5;
    }
    public boolean slowable() {
        return false;
    }
}
