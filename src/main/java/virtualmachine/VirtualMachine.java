package virtualmachine;

public interface VirtualMachine {
	//run program
	public void run();
	//returns the program
	public String program();
	//returns true iff program has no more instructions left
	public boolean terminated();
}
