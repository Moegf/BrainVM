package virtualmachine;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public class BasicVirtualMachine implements VirtualMachine {
	
	private static final byte DEFAULT_MEMORY_VALUE = 0;
	private static final byte DEFAULT_INPUT_VALUE = 0;
	
	String program;
	int instructionPointer;
	
	Map<Integer, Byte> memoryTape;
	int memoryPointer;
	
	Optional<OutputStream> outputStream;
	Optional<InputStream> inputStream;
	
	Optional<Integer> clockSpeed;
	
	private BasicVirtualMachine(Builder builder) {
		this.program = builder.program;
		this.inputStream = builder.inputStream;
		this.outputStream = builder.outputStream;
		
		this.memoryTape = new HashMap<>();
		this.memoryPointer = 0;
		this.instructionPointer = 0;
	}
	
	public static class Builder {
		String program;
		
		Optional<InputStream> inputStream = Optional.empty();
		Optional<OutputStream> outputStream = Optional.empty();
		Optional<Integer> clockSpeed = Optional.empty();
		
		Builder(String program) {
			this.program = program;
		}
		
		public Builder inputStream(InputStream inputStream) {
			this.inputStream = Optional.of(inputStream);
			return this;
		}
		
		public Builder outputStream(OutputStream outputStream) {
			this.outputStream = Optional.of(outputStream);
			return this;
		}
		
		public BasicVirtualMachine build() {
			return new BasicVirtualMachine(this);
		}
	}
	
	@Override
	public String program() {
		return program;
	}


	@Override
	public void run() {
		while(instructionPointer < program.length()) {
			step();
		}
	}
	
	//run with a specified pause between each step in milliseconds
	public void run(int delay) throws InterruptedException {
		while(instructionPointer < program.length()) {
			step();
			Thread.sleep(delay);
		}
	}

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
	
	public static void main(String[] args) {
		String program = "";
		VirtualMachine vm = new Builder(program).outputStream(System.out).build();
		vm.run();
	}

}
