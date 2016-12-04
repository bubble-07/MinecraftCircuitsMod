import java.io.Serializable;
public class Implementation implements Serializable {
	
	byte input1;
	boolean output1;
	boolean output2;
	boolean output3;
	
	public Implementation() {
		input1 = 0;
		output1 = false;
		output2 = false;
		output3 = false;
	}
	
	public void tick(byte i1) {
		 input1 = i1;
		 output1 = value0();
		 output2 = value1();
		 output3 = value2();
	 }
	 
	public boolean value0() {
		 int isOn = input1 & 0100;
		 return (isOn > 0);
	 }
	 
	 public boolean value1() {
		 int isOn = input1 & 0010;
		 return (isOn > 0);
	 }
	 
	 public boolean value2() {
		 int isOn = input1 & 0001;
		 return (isOn > 0);
	 }
	 
	 public int[] inputWidths() {
		 int[] widths = {4};
		 return widths;
	 }
	 
	 public int[] outputWidths() {
		 int[] widths = {1, 1, 1};
		 return widths;
	 }

}
