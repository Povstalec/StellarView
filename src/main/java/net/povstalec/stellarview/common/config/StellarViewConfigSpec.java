package net.povstalec.stellarview.common.config;

import net.fabricmc.loader.api.FabricLoader;
import net.povstalec.stellarview.StellarView;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.function.Supplier;

public class StellarViewConfigSpec
{
	private String fileName;
	private HashMap<String, String> configMap;
	private List<ConfigValue<?>> values;
	private Section section;
	
	private File file;
	
	private StellarViewConfigSpec(List<ConfigValue<?>> values, Section section)
	{
		this.configMap = new HashMap<>();
		this.values = values;
		this.section = section;
	}
	
	public void register(String fileName)
	{
		this.fileName = fileName;
		this.file = getFile();
		parseConfig(file);
	}
	
	@Nullable
	private String getValue(String name)
	{
		return configMap.get(name);
	}
	
	@Nullable
	public Boolean getBoolean(String name)
	{
		String value = getValue(name);
		
		if(value == null)
			return null;
		
		return Boolean.parseBoolean(value);
	}
	
	@Nullable
	public Integer getInt(String name)
	{
		String value = getValue(name);
		
		if(value == null)
			return null;
		
		try { return Integer.parseInt(value); }
		catch(NumberFormatException e) { return null; }
	}
	
	private void parseLine(int line, String lineContent)
	{
		lineContent = lineContent.trim(); // Trim string to ignore leading whitespaces
		if(lineContent.isEmpty() || lineContent.startsWith("#") || lineContent.startsWith("[")) // Ignore line
			return;
		
		String[] keyValue = lineContent.split("=");
		
		if(keyValue.length == 2)
			configMap.put(keyValue[0].trim(), keyValue[1].trim());
		else
			StellarView.LOGGER.error("Syntax error on line " + line + " in file " + this.fileName + ", skipping.");
	}
	
	private void parseConfig(File file)
	{
		if(file == null)
			return;
		
		StellarView.LOGGER.info("Parsing config file " + file.getName());
		
		try
		{
			Scanner scanner = new Scanner(file);
			for(int line = 1; scanner.hasNext(); line++)
			{
				parseLine(line, scanner.nextLine());
			}
		}
		catch (FileNotFoundException e) { StellarView.LOGGER.error("Failed to locate file: " + e.toString()); }
	}
	
	private String configContents()
	{
		return section.write();
	}
	
	private void writeFile(File file)
	{
		if(file == null)
			return;
		
		try
		{
			PrintWriter writer = new PrintWriter(file, "UTF-8");
			
			writer.write(configContents());
			writer.close();
		}
		catch(Exception e) { StellarView.LOGGER.error("Failed to write to file: " + e.toString()); }
	}
	
	private void createFile(File file)
	{
		try
		{
			file.getParentFile().mkdirs();
			Files.createFile(file.toPath());
			
			writeFile(file);
		}
		catch(Exception e) { StellarView.LOGGER.error("Failed to create file: " + e.toString()); }
	}
	
	@Nullable
	public File getFile()
	{
		try
		{
			Path path = FabricLoader.getInstance().getConfigDir();
			File file = path.resolve(fileName).toFile();
			
			if(!file.exists())
			{
				StellarView.LOGGER.info("Config file " + fileName + " not found, attempting to create.");
				createFile(file);
			}
			
			return file;
		}
		catch(Exception e)
		{
			StellarView.LOGGER.error("Failed to access file: " + e.toString());
			return null;
		}
	}
	
	
	
	public static class Builder
	{
		private Section section = new Section();
		private List<ConfigValue<?>> values = new ArrayList<>();
		private int level = 0;
		
		public StellarViewConfigSpec build()
		{
			StellarViewConfigSpec spec = new StellarViewConfigSpec(this.values, this.section);
			
			for(ConfigValue<?> value : this.values)
			{
				value.setSpec(spec);
			}
			
			return spec;
		}
		
		public Builder push(String name)
		{
			this.section.add(new ConfigWritable(this.level, '[' + name + ']', true));
			this.level++;
			
			return this;
		}
		
		public Builder pop()
		{
			if(this.level == 0)
				throw new IllegalArgumentException("Attempted to pop more elements than possible");
			else
				this.level--;
			
			return this;
		}
		
		public Builder comment(String comment)
		{
			this.section.add(new ConfigWritable(level, "# " + comment, false));
			return this;
		}
		
		public BooleanValue define(String path, boolean defaultValue)
		{
			BooleanValue value = new BooleanValue(path, () -> defaultValue);
			this.values.add(value);
			this.section.add(new ConfigWritable(level, value, false));
			return value;
		}
		
		public IntValue defineInRange(String path, int defaultValue, int min, int max)
		{
			comment("#Range: " + min + " ~ " + max);
			
			if(defaultValue < min || defaultValue > max)
				throw new IllegalArgumentException("Default value of " + path + " must be between " + min + " and " + max);
			
			IntValue value = new IntValue(path, () -> defaultValue, min, max);
			this.values.add(value);
			this.section.add(new ConfigWritable(level, value, false));
			return value;
		}
	}
	
	
	
	protected static class ConfigWritable
	{
		private int depth;
		private Object held;
		private boolean spaceOut;
		
		public ConfigWritable(int depth, Object held, boolean spaceOut)
		{
			this.depth = depth;
			this.held = held;
			this.spaceOut = spaceOut;
		}
		
		public String offset()
		{
			StringBuilder builder = new StringBuilder();
			
			for(int i = 0; i < depth; i++)
			{
				builder.append('\t');
			}
			
			return builder.toString();
		}
		
		public String write()
		{
			return spaceOut ? '\n' + offset() + held.toString() + '\n' : offset() + held.toString() + '\n';
		}
	}
	
	private static class Section
	{
		private List<ConfigWritable> writables;
		
		private Section()
		{
			this.writables = new ArrayList<>();
		}
		
		public void add(ConfigWritable object)
		{
			writables.add(object);
		}
		
		protected String write()
		{
			StringBuilder builder = new StringBuilder();
			
			for(ConfigWritable writable : writables)
			{
				builder.append(writable.write());
			}
			
			builder.append('\n');
			return builder.toString();
		}
	}
	
	protected abstract static class ConfigValue<T> implements Supplier<T>
	{
		protected String name;
		protected Supplier<T> defaultSupplier;
		protected T value = null;
		@Nullable
		protected StellarViewConfigSpec spec;
		
		protected ConfigValue(String name, Supplier<T> defaultSupplier)
		{
			this.name = name;
			this.defaultSupplier = defaultSupplier;
		}
		
		public String name()
		{
			return this.name;
		}
		
		public void reset()
		{
			this.value = null;
		}
		
		public void set(T value)
		{
			this.value = value;
		}
		
		public void save()
		{
			System.out.println("Saving " + name + " " + value);
			spec.writeFile(spec.getFile());
		}
		
		@Override
		public T get()
		{
			if(value == null && spec != null)
				value = tryGetRaw();
			
			return value == null ? getDefault() : value;
		}
		
		public T getDefault()
		{
			return defaultSupplier.get();
		}
		
		@Nullable
		protected abstract T tryGetRaw();
		
		private void setSpec(StellarViewConfigSpec spec)
		{
			this.spec = spec;
		}
		
		@Override
		public String toString()
		{
			return name + " = " + get().toString();
		}
	}
	
	
	
	public static class BooleanValue extends ConfigValue<Boolean>
	{
		private BooleanValue(String name, Supplier<Boolean> defaultSupplier)
		{
			super(name, defaultSupplier);
		}
		
		@Override
		@Nullable
		protected Boolean tryGetRaw()
		{
			return spec.getBoolean(name);
		}
	}
	
	
	
	public static class IntValue extends ConfigValue<Integer>
	{
		int min, max;
		
		private IntValue(String name, Supplier<Integer> defaultSupplier, int min, int max)
		{
			super(name, defaultSupplier);
			
			this.min = min;
			this.max = max;
		}
		
		@Override
		public void set(Integer value)
		{
			if(value < min || value > max)
				throw new IllegalArgumentException("Value of " + name + " must be between " + min + " and " + max);
			
			super.set(value);
		}
		
		@Override
		@Nullable
		protected Integer tryGetRaw()
		{
			return spec.getInt(name);
		}
	}
}
