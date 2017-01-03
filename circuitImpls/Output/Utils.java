public class Utils {
    public static boolean isValidBusWidth(int val) {
        if (val < 1 || val > 64) {
            return false;
        }
        return (val & (val - 1)) == 0;
    }
    public static boolean isValidBitPos(int bitSelect, int bitWidth) {
        if (!isValidBusWidth(bitWidth)) {
            return false;
        }
        if (bitSelect < 0 || bitSelect >= bitWidth) {
            return false;
        }
        return true;
    }
}
