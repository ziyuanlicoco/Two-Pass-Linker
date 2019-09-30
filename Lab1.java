import java.util.*;

public class Lab1 {

	public static void main(String[] args) {
		ArrayList<Module> Linker = new ArrayList<>();
		ArrayList<String> defined_symbols = new ArrayList<>();
		ArrayList<String> used_symbols = new ArrayList<>();
		ArrayList<Integer> address = new ArrayList<>();
		ArrayList<Boolean> defined_or_not = new ArrayList<>();
		Scanner input = new Scanner(System.in);
		int num_module = input.nextInt();
		int base_address = 0;
		int memory_address = 200;
		int arbitrary_limit = 8;
		
		//read and store information module by module
		for (int i = 0; i<num_module; i++) {
			Module current = new Module();
			ArrayList<Integer> temp = new ArrayList<>(); //store absolute address, we need to check if the address exceeds length of the module, afer we read number of instructions later
			int num_def = input.nextInt();
			for (int j = 0; j<num_def; j++) {
				String symbol = input.next();
				if (symbol.length() > arbitrary_limit) { //we cannot store symbol longer than arbitrary limit
					System.out.println("Error : number of characters in a symbol exceeds limits. This symbol cannot be stored");
				}
				else { //if this symbol is used before, we ignore this address and use the first one given
					if (defined_symbols.contains(symbol)) {
						System.out.println("Error: " + symbol + " is multuply defined. It will use the value given in the first definition");
						input.nextInt();
					}
					else { //if this is a new symbol, calculate absolute address based on relative ones and store all information
						defined_symbols.add(symbol);
						defined_or_not.add(true);
						int relative_add = input.nextInt();
						int absolute_add = relative_add + base_address;
						temp.add(absolute_add);
					}
				}
				

			}
			
			int num_use = input.nextInt(); //store information about symbol usage
			ArrayList<Integer> temp_add = new ArrayList<>();
			ArrayList<String> temp_use = new ArrayList<>();
			for (int k = 0; k<num_use; k++) {
				String use_symbol = input.next();
				int use_relative_add = input.nextInt();
				if (temp_add.contains(use_relative_add)) { //if multiple symbols are used in the same instruction, we replace previous symbol with new one in this instruction
					// if multiple usage happens in the second pass instead of a same pointer in the useage line at first, the previous data will be overwritten during the second pass
					System.out.println("Error: multiple usage of same address");
					int index = temp_add.indexOf(use_relative_add);
					temp_use.set(index, use_symbol);
				}
				else { //otherwise, store all information respectively
					temp_add.add(use_relative_add);
					temp_use.add(use_symbol);
				}
			}
			
			for (int k = 0; k<temp_add.size(); k++) {
				used_symbols.add(temp_use.get(k));
				current.usage.add(temp_use.get(k));
				current.usage_add.add(temp_add.get(k));
			}
			
			
			int num_instructions = input.nextInt(); //in first pass, we just need this to ensure length of module from this line
			current.length = num_instructions;
			for (int j = 0; j<temp.size(); j++) { 
				if (temp.get(j) > base_address+num_instructions-1) {
					System.out.println("Error: address of " +defined_symbols.get(j) + " exceeds module length. It is reset to relative address 0");
					temp.set(j, base_address); 
					address.add(base_address);
				}
				else 
					address.add(temp.get(j));
			}
			
		
			// for all detail instructions, just read and store instrutions in the module
			for (int x = 0; x < num_instructions; x++) {
				int instruction = input.nextInt();
				current.instruction.add(instruction);
			}

			base_address += num_instructions;
			Linker.add(current);
		}
		
		//check for warning and error
		for (int i = 0; i<defined_symbols.size(); i++) {
			if (used_symbols.contains(defined_symbols.get(i))== false)
				System.out.println("Warning: " + defined_symbols.get(i) + " is defined but not used.");
		}
		for (int i = 0; i<used_symbols.size(); i++) {
			if (defined_symbols.contains(used_symbols.get(i))== false) {
				System.out.println("Error: " + used_symbols.get(i) + " is used but not defined. It is assigned a value 0");
				defined_symbols.add(used_symbols.get(i));
				address.add(0);
				defined_or_not.add(false);
				
			}
		}
		
		//what we need from first pass: print symbol table and absolute address
		System.out.println("");
		System.out.println("Symbol Table");
		for(int i= 0; i< defined_symbols.size(); i++) {
			if (defined_or_not.get(i) == true)
				System.out.println(defined_symbols.get(i)+ "=" + address.get(i));
		}
		//end of first pass
		
		
		System.out.println("");
		System.out.println("Memory Map");
		int plus = 0;
		
		for (int i = 0; i <num_module;i++) { //we process and print out memory map module by module
			ArrayList<String> error_message = new ArrayList<>();
			ArrayList<Boolean> external_use = new ArrayList<>();
			int module_length = Linker.get(i).length;
			for (int m = 0; m<module_length; m++) {
				String error = " ";
				Boolean b = false;
				error_message.add(error);
				external_use.add(b);
			} //these are for storing error messages we need to print out later
			
			for (int j = 0; j < Linker.get(i).usage.size(); j++) {
				//we start passing from first usage of each symbol
				String using = Linker.get(i).usage.get(j);
				int use_ind  = defined_symbols.indexOf(using);
				int external_add = address.get(use_ind);
				int start_point = Linker.get(i).usage_add.get(j);
				
				while (true) {
					int start = Linker.get(i).instruction.get(start_point);
					int opcode = start/10000; //first digit out of 5 is opcode
					int addre = (start - opcode*10000)/10; //next three digits are address pointer
					int add_type = start %10; //last digit refer to address type
					
					if (add_type == 1) { //if we are directed to an immidiate address
						String currenterror = "Error: an immediate address is used here.It has been treated as an external address";
						error_message.set(start_point, currenterror);
						Linker.get(i).instruction.set(start_point,start+3);
						external_use.set(start_point, true);
					}
					else if(addre == 777) { //if the pointer is 777, we reached the end of this module
						start = opcode * 10000+ external_add * 10 + add_type;
						Linker.get(i).instruction.set(start_point,start);
						external_use.set(start_point, true);
						break;
					}
					else { //otherwise, we process to next address based on pointer in the instruction
						start = opcode * 10000+ external_add * 10 + add_type;
						Linker.get(i).instruction.set(start_point,start);
						external_use.set(start_point, true);
						start_point = addre;
					}
				}
				
			}
			
			//before printing out memory map in current module, we process other address types and check for other protential error
			for(int k = 0; k < module_length; k++) {
				int inst = Linker.get(i).instruction.get(k);
				int k_op = inst /10000;
				int k_add = (inst - k_op*10000 )/10;
				int k_type = inst % 10;
				
				if (k_type == 3) { //if relative address,relocate to absolute address
					//k_add = k_add + module_length;
					inst += plus*10;
					Linker.get(i).instruction.set(k,inst);
				}
				else if (k_type == 2) { //if absolute address, check if exceed machine size
					if (k_add >memory_address) {
						String a_error = "Error: absolute address exceed size of machine. it is relocated to largest legal value " + memory_address + ".";
						error_message.set(k, a_error);
						inst = k_op*10000+memory_address*10+k_type;
						Linker.get(i).instruction.set(k,inst);
					}
				}
				else if (k_type == 4) { //if external address, check if it is used.
					if (external_use.get(k) == false){
					String e_error = "Error: external address never used. It has been treated as an immidiate address";
					error_message.set(k, e_error);
					}
				}
				
			}
			
			for (int y = 0; y< module_length; y++) { //print out memory map and error message, if it has any
				int map = Linker.get(i).instruction.get(y)/10;
				int ind = y+plus;
				if (error_message.get(y) == " ")
					System.out.println(ind + ": " + map);
				else
					System.out.println(ind + ": " + map + "     " + error_message.get(y));
			}
			plus += module_length;
		}
		
		
		
		
		input.close();
	}

}
