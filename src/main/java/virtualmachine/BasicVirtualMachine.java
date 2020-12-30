package virtualmachine;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * A virtual machine that runs BrainF programs symbol by symbol, as opposed to applying optimizations.
 */
public class BasicVirtualMachine implements VirtualMachine {
	
	private static final byte DEFAULT_MEMORY_VALUE = 0;
	private static final byte DEFAULT_INPUT_VALUE = 0;
	
	private String program;
	private int instructionPointer;
	
	private Map<Integer, Byte> memoryTape;
	private int memoryPointer;
	
	Optional<OutputStream> outputStream;
	Optional<InputStream> inputStream;
	
	private BasicVirtualMachine(Builder builder) {
		this.program = builder.program;
		this.inputStream = builder.inputStream;
		this.outputStream = builder.outputStream;
		
		this.memoryTape = new HashMap<>();
		this.memoryPointer = 0;
		this.instructionPointer = 0;
	}

	/**
	 * Builder class for {@link BasicVirtualMachine}
	 */
	public static class Builder {
		private String program;
		
		private Optional<InputStream> inputStream = Optional.empty();
		private Optional<OutputStream> outputStream = Optional.empty();

		/**
		 * Creates a builder object for {@link BasicVirtualMachine}
		 * @param program The BrainF Program
		 */
		public Builder(String program) {
			this.program = program;
		}

		/**
		 * Sets the input stream for the virtual machine
		 * @param inputStream The {@link InputStream}
		 * @return The {@link Builder} Object
		 */
		public Builder inputStream(InputStream inputStream) {
			this.inputStream = Optional.of(inputStream);
			return this;
		}

		/**
		 * Sets the output stream for the virtual machine
		 * @param outputStream The {@link OutputStream}
		 * @return	The {@link Builder} Object
		 */
		public Builder outputStream(OutputStream outputStream) {
			this.outputStream = Optional.of(outputStream);
			return this;
		}

		/**
		 * Builds the virtual machine
		 * @return The {@link BasicVirtualMachine}
		 */
		public BasicVirtualMachine build() {
			return new BasicVirtualMachine(this);
		}
	}


	@Override
	public void run() {
		while(instructionPointer < program.length()) {
			step();
		}
	}

	/**
	 * Runs the virtual machine with a specified delay in milliseconds between each instruction
	 * @param delay the delay in milliseconds
	 * @throws InterruptedException
	 */
	public void run(int delay) throws InterruptedException {
		while(instructionPointer < program.length()) {
			step();
			Thread.sleep(delay);
		}
	}

	/**
	 * Execute a single instruction on the virtual machine.
	 */
	public void step() {
		if(terminated())
			throw new IllegalStateException("The program has terminated and cannot be stepped");
		//get the current instruction
		char currentInstruction = program.charAt(instructionPointer);
		
		//increment the instruction pointer
		instructionPointer++;
		
		//switch depending on the instruction
		switch(currentInstruction) {
		
			case '>':
				memoryPointer ++;
			break;
			
			case '<':
				memoryPointer --;
			break;
			
			case '+':
				memoryTape.put(memoryPointer, (byte) (memoryTape.getOrDefault(memoryPointer, DEFAULT_MEMORY_VALUE) + 1));
			break;
			
			case '-':
				memoryTape.put(memoryPointer, (byte) (memoryTape.getOrDefault(memoryPointer, DEFAULT_MEMORY_VALUE) - 1));
			break;
			
			case '.':
				outputStream.ifPresent(stream -> {
					try {
						stream.write(memoryTape.getOrDefault(memoryPointer, DEFAULT_MEMORY_VALUE).intValue());
						stream.flush();
					} catch (IOException e) {
						e.printStackTrace();
					}
				});
			break;
			
			case ',':
				inputStream.ifPresent(stream -> {
					try {
						if(stream.available() > 0) {
							memoryTape.put(memoryPointer, (byte) stream.read());
						} else {
							memoryTape.put(memoryPointer, DEFAULT_INPUT_VALUE);
						}
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				});
			break;
			
			case '[':
				if(memoryTape.getOrDefault(memoryPointer, (byte) 0) == 0) {
					setPointer(']', n -> n + 1);
				}
			break;
			case ']':
				if(memoryTape.getOrDefault(memoryPointer, (byte) 0) != 0) {
					setPointer('[', n -> n - 1);
				}
		}

	}
	
	@Override
	public boolean terminated() {
		return program.length() == instructionPointer? true : false;
	}
	
	//sets the instruction pointer to the first index after the index that contains char and follows the pattern supplied by nextIndex 
	private void setPointer(char target, Function<Integer, Integer> nextIndex) {
		int testPointer = nextIndex.apply(instructionPointer);
		
		while(program.charAt(testPointer) != target) {
			testPointer = nextIndex.apply(testPointer);
		}
		
		instructionPointer = testPointer + 1;
	}
	
	public static void main(String[] args) throws InterruptedException {
		String program = ">+++++++++[<++++++++>-]<.>++++++[<+++++>-]<-.+++++++..+++.>>\n" +
				"+++++++[<++++++>-]<++.------------.<++++++++.--------.+++.------.--------.\n" +
				">+.>++++++++++.";
		BasicVirtualMachine vm = new Builder(program).outputStream(System.out).build();
		while(!vm.terminated()) {
			vm.step();
			Thread.sleep(10);
			System.out.println(vm.toString());
		}
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder("Basic Virtual Machine: |");
		//TODO: This is not efficient or pleasant to read
		int lowestValue = memoryTape.entrySet().stream().collect(Collectors.minBy((firstPair, secondPair) -> firstPair.getKey() - secondPair.getKey())).map(Map.Entry::getKey).orElseGet(()->0);
		int highestValue = memoryTape.entrySet().stream().collect(Collectors.maxBy((firstPair, secondPair) -> firstPair.getKey() - secondPair.getKey())).map(Map.Entry::getKey).orElseGet(()->0);

		for(int i = lowestValue; i <= highestValue; i++){

			if(i == memoryPointer)
				builder.append(">");

			builder.append(memoryTape.getOrDefault(i, (byte)0));

			if(i == memoryPointer)
				builder.append("<");

			builder.append('|');
		}

		return builder.toString();
	}
}
