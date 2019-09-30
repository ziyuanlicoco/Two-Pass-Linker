import java.util.*;

//this program uses OOP
//each module is stored in and treated as a Module object

public class Module {
	public ArrayList<String> usage;
	public ArrayList<Integer> usage_add;
	public int length;
	public ArrayList<Integer> instruction;

	public Module() {
		usage = new ArrayList<>();
		usage_add = new ArrayList<>();
		length = 0;
		instruction = new ArrayList<>();
	}
}
