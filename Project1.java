//Salwa Maheen Haider
//Project1

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Scanner;

public class Project1
{
	public static void main(String[] args)
	{
		if(args.length < 2)
		{
			System.err.println("Not enough arguments");
			System.exit(1);
		}
		
		String programInput = args[0];
		int timeout = Integer.parseInt(args[1]);
		Runtime runtime = Runtime.getRuntime();

		// Call the Memory process with the input program argument
		try 
		{
			Process memory = runtime.exec("java Memory "+programInput);
			final InputStream error = memory.getErrorStream();
			new Thread(new Runnable()
			{
			    public void run(){
			        byte[] buffer = new byte[8192];
			        int length = -1;
			        try 
					{
						while((length = error.read(buffer)) > 0)
						{
						    System.err.write(buffer, 0, length);
						}
					} catch (IOException exp) 
					{
						exp.printStackTrace();
					}
			    }
			}).start();
			
			Scanner memoryIn = new Scanner(memory.getInputStream());
			PrintWriter memoryOut = new PrintWriter(memory.getOutputStream());
			CPU cpu = new CPU(memoryIn, memoryOut, timeout);
			cpu.run();
		} 
		catch (IOException exp) 
		{
			exp.printStackTrace();
			System.err.println("Unable to create new process.");
			System.exit(1);
		}
	}
	
	private static void message(Object... str)
	{

	}
	
	private static class CPU
	{
		private int PC, SP, IR, AC, X, Y;
		private int timer, timeout;
		private boolean kernelMode;
		private Scanner memoryIn;
		private PrintWriter memoryOut;
		
		public CPU(Scanner memoryIn, PrintWriter memoryOut, int timeout) 
		{
			this.memoryIn = memoryIn;
			this.memoryOut = memoryOut;
			this.timeout = timeout;
			kernelMode = false;
			PC = IR = AC = X = Y = timer = 0;
			SP = 1000;
		}
		
		private void fetch()
		{
			IR = readMemory(PC++);
		}
		
		private void push(int data)
		{
			writeMemory(--SP, data);
		}
		
		private int pop()
		{
			return readMemory(SP++);
		}

		public void run()
		{
			boolean running = true;
			while(running)
			{
				fetch();
				running = instructionExecute();
				timer++;
				
				if(timer >= timeout)
				{
					if(!kernelMode) 
					{
						timer = 0;
						kernelMode();
						PC = 1000;
					}
					
				}
			}
		}
		
		private void kernelMode()
		{
			message("Entering kernel mode.");
			kernelMode = true;
			int tempSP = SP; 
			SP = 2000;
			push(tempSP);
			push(PC);
			push(IR);
			push(AC);
			push(X);
			push(Y);
		}

		private int readMemory(int address)
		{
			if(address >= 1000 && !kernelMode)
			{
				System.err.println("System memory is in user mode!!");
				System.exit(-1);
			}
			memoryOut.println("r"+address);
			memoryOut.flush();
			return Integer.parseInt(memoryIn.nextLine());
		}
		
		private void writeMemory(int address, int data)
		{
			memoryOut.printf("w%d,%d\n", address, data);
			memoryOut.flush();
		}
		
		private void endMemoryProcess()
		{
			memoryOut.println("e");
			memoryOut.flush();
		}

		private boolean instructionExecute()
		{
			switch(IR)
			{
				case 1: // Load value: Load value into AC
					fetch();
					message("Loading "+IR+" into AC");
					AC = IR;
					break;
				case 2: // Load addr: Load value at address into AC
					fetch();
					AC = readMemory(IR);
					message("Loading from address "+IR+" into AC: "+AC);
					break;
				case 3: // LoadInd addr: Load value from address at given address into AC
					fetch();
					AC = readMemory(readMemory(IR));
					message("Loading indirectly from address"+IR+" into AC: "+AC);
					break;
				case 4: // LoadInxX addr: Load value at (given address + X) into AC
					fetch();
					AC = readMemory(IR + X);
					message("LoadInxX", IR, X, "->", AC);
					break;
				case 5: // LoadInxY addr: Load value at (given address + Y) into AC
					fetch();
					AC = readMemory(IR + Y);
					message("LoadInxY", IR, Y, "->", AC);
					break;
				case 6: // LoadSpX: Load from (SP+X) into AC
					AC = readMemory(SP+X);
					message("LoadSpX", SP, X, "->", AC);
					break;
				case 7: // Store addr: Store AC to address
					fetch();
					writeMemory(IR, AC);
					message("Store",IR,AC);
					break;
				case 8: // Get: Get random int 1-100 into AC
					AC = (int) (Math.random()*100+1);
					message("Get",AC);
					break;
				case 9: // Put port: If port=1, write AC to screen as int, if port=2, write AC to screen as char
					fetch();
					if(IR == 1)
					{
						System.out.print(AC);
						message("Put","int",AC);
					}
						
					else if(IR == 2)
					{
						System.out.print((char)AC);
						message("Put","char",(char)AC);
					}
						
					break;
				case 10: // AddX: Add X to AC
					AC += X; 
					break;
				case 11: // AddY: Add Y to AC
					AC += Y; 
					break;
				case 12: // SubX: Sub X to AC
					AC -= X; 
					break;
				case 13: // SubY: Sub Y to AC
					AC -= Y; 
					break;
				case 14: // CopyToX: Copy value in AC to X
					X = AC; 
					break;
				case 15: // CopyFromX: Copy value in X to AC
					AC = X; 
					break;
				case 16: // CopyToY: Copy AC to Y
					Y = AC; 
					break;
				case 17: // CopyFromY: Copy Y to AC
					AC = Y; 
					break;
				case 18: // CopyToSp: Copy AC to SP
					SP = AC; 
					break;
				case 19: // CopyFromSp: Copy SP to AC
					AC = SP; 
					break;
				case 20: // Jump addr: Jump to address
					fetch();
					PC = IR;
					break;
				case 21: // JumpIfEqual addr: Jump only if AC is zero
					fetch();
					if(AC == 0)
						PC = IR;
					break;
				case 22: // JumpIfNotEqual addr: Jump only if AC is not zero
					fetch();
					if(AC != 0)
						PC = IR;
					break;
				case 23: // Call addr: Push return addr to stack, jump
					fetch();
					push(PC);
					PC = IR;
					break;
				case 24: // Ret: Pop return addr, jump back
					PC = pop();
					break;
				case 25: // IncX: Increment X
					X++; 
					break;
				case 26: // DecX: Decrement X
					X--; 
					break;
				case 27: // Push: Push AC onto stack
					push(AC);
					message("Pushing AC",AC);
					break;
				case 28: // Pop: Pop from stack onto AC
					AC = pop();
					message("Popping AC",AC);
					break;
				case 29: 
					// Disable interrupts during interrupt processing
					if(!kernelMode)
					{
						kernelMode();
						PC = 1500;
					}
					break;
				case 30: 
					message("Exiting kernel mode.");
					Y = pop();
					X = pop();
					AC = pop();
					IR = pop();
					PC = pop();
					SP = pop();
					kernelMode = false;
					break;
				case 50: 
					endMemoryProcess();
					return false;
				default: 
					System.err.println("Invalid instruction.");
					endMemoryProcess();
					return false;
			}
			return true;
		}
		
	}
}