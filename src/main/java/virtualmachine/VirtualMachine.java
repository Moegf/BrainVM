package virtualmachine;

/**
 * An interface that represents virtual machines that run BrainF
 */
public interface VirtualMachine extends Runnable {

	/**
	runs the virtual machine
	 */
	void run();

	/**
	 * Returns whether the machine has stopped running
	 * @return {@code true} if and only if the machine has halted
	 */
	boolean terminated();
}
