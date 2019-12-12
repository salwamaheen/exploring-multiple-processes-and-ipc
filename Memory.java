//Salwa Maheen Haider

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

//memory class
public class Memory 
{
	static int[] memory; //creates static variable

	//this is the main function
	public static void main(String[] args)
	{
		//if user does not enter enough arguments, exits program with error message
		if(args.length < 1)
		{
			System.err.println("Not enough arguments");
			System.exit(1);
		}

		String path = args[0]; //path as argument

		try 
		{
			memoryInitialize(path); //initializes path
		} 
		//exception if not found
		catch (FileNotFoundException exp) 
		{
			System.err.println("Input file not found.");
			System.exit(1);
		}
		
		Scanner input = new Scanner(System.in); //scanner takes input
		//if input has next line
		while(input.hasNextLine())
		{
			String line = input.nextLine();
			char command = line.charAt(0);
			int address, data;
			switch(command)
			{
				case 'r': //this case reads the command
					address = Integer.parseInt(line.substring(1));
					System.out.println(read(address));
					break;
				case 'w': //this case writes command
					String[] params = line.substring(1).split(",");
					address = Integer.parseInt(params[0]);
					data = Integer.parseInt(params[1]);
					write(address, data);
					break;
				case 'e': //this case exits command
					System.exit(0);
			}
			
		}
		input.close();
	}

	//method for outputting message
	private static void message(String str)
	{

	}
	
	//reading method using address as parameter
	private static int read(int address)
	{
		message("Reading "+address+" = "+memory[address]);
		return memory[address];
	}

	//write method
	private static void write(int address, int data)
	{
		message("Writing "+data+" to "+address);
		memory[address] = data;
	}

	//method to initialize memory 
	private static void memoryInitialize(String inputFilePath) throws FileNotFoundException
	{
		memory = new int[2000];
		Scanner s = new Scanner(new File(inputFilePath));
		int index = 0;
		while(s.hasNextLine())
		{
			String line = s.nextLine().trim();
			if(line.length() < 1) 
				continue;
			
			if(line.charAt(0) == '.')
			{
				index = Integer.parseInt(line.substring(1).split("\\s+")[0]);
				continue;
			}
			
			if(line.charAt(0) < '0' || line.charAt(0) > '9')
				continue;
			
			String[] split = line.split("\\s+");
			if(split.length < 1) 
				continue;
			else 
				memory[index++] = Integer.parseInt(split[0]);
		}
		s.close();
	}
}
