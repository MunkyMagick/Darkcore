package io.darkcraft.darkcore.mod.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashSet;

public class ConfigFile
{
	private volatile HashSet<ConfigItem> configItems = new HashSet<ConfigItem>();
	private final File configFile;
	
	protected ConfigFile(File f)
	{
		configFile = f;
		if(!f.exists())
		{
			try
			{
				f.createNewFile();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		else
		{
			readFromFile();
		}
	}
	
	public synchronized ConfigItem getConfigItem(ConfigItem ci)
	{
		for(ConfigItem c : configItems)
		{
			if(c.equals(ci))
				return c;
		}
		configItems.add(ci);
		writeToFile();
		return ci;
	}
	
	private void processInput(String totalInput)
	{
		String[] items = totalInput.split("\n\n");
		for(String item : items)
		{
			String[] lines = item.split("\n");
			String lastLine = lines[lines.length-1];
			String[] data = lastLine.split(":",3);
			if(data.length != 3)
			{
				System.err.println("Invalid data " + item);
				continue;
			}
			CType c = CType.fromPrintable(data[1]);
			Object d = c.toData(data[2]);
			String[] comments = null;
			if(lines.length > 1)
			{
				comments = new String[lines.length-1];
				for(int i = 0; i < lines.length -1; i++)
					comments[i] = lines[i];
			}
			configItems.add(new ConfigItem(data[0],c,d,comments));
		}
	}
	
	private void readFromFile()
	{
		BufferedReader br = null;
		try
		{
			br = new BufferedReader(new FileReader(configFile));
			StringBuilder complete = new StringBuilder();
			String line;
			while((line = br.readLine()) != null)
			{
				complete.append(line).append("\n");
			}
			processInput(complete.toString());
		}
		catch(IOException ioE)
		{
			ioE.printStackTrace();
		}
		finally
		{
			if(br != null)
			{
				try
				{
					br.close();
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		}
	}
	
	private void writeToFile()
	{
		PrintStream ps = null;
		try
		{
			ps = new PrintStream(new FileOutputStream(configFile));
			for(ConfigItem ci: configItems)
			{
				ps.print(ci.printable());
			}
			ps.close();
		}
		catch(IOException ioE)
		{
			ioE.printStackTrace();
		}
		finally
		{
			if(ps != null)
				ps.close();
		}
	}
}
