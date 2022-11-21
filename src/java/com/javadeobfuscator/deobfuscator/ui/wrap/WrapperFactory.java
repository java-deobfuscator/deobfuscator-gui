package com.javadeobfuscator.deobfuscator.ui.wrap;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.javadeobfuscator.deobfuscator.ui.util.ByteLoader;
import com.javadeobfuscator.deobfuscator.ui.util.FallbackException;
import com.javadeobfuscator.deobfuscator.ui.util.InvalidJarException;
import com.javadeobfuscator.deobfuscator.ui.util.MiniClassReader;

public class WrapperFactory
{
	/**
	 * Buffer size to use for reading classes from deobfuscator jar.
	 */
	private final static int BUFF_SIZE = (int) Math.pow(2, 13);
	/**
	 * Loader to use.
	 */
	private static ByteLoader loader;

	/**
	 * @return Deobfuscator wrapper.
	 */
	public static Deobfuscator getDeobfuscator() throws FallbackException
	{
		return new Deobfuscator(loader);
	}

	/**
	 * @return Transformers wrapper.
	 */
	public static Transformers getTransformers() throws FallbackException
	{
		return new Transformers(loader);
	}

	/**
	 * Set load strategy to loading from specified jar.
	 *
	 * @param file Deobfuscator program jar.
	 * @throws IOException         Jar could not be read.
	 * @throws InvalidJarException Thrown if jar loaded was not an instance of JavaDeobfuscator
	 */
	public static void setupJarLoader(File file) throws IOException, InvalidJarException
	{
		loader = fromJar(file);
	}

	/**
	 * Set load strategy to loading from adjacent jar files.
	 *
	 * @param recursive Check sub-directories of adjacent folders.
	 */
	public static void setupJarLoader(boolean recursive)
	{
		loader = auto(recursive);
	}

	/**
	 * Create a loader for the deobfuscator jar.
	 *
	 * @param jar Deobfuscator program jar.
	 * @return JavaDeobfuscator loader.
	 * @throws IOException         Jar could not be read.
	 * @throws InvalidJarException Thrown if jar loaded was not an instance of JavaDeobfuscator
	 */
	private static ByteLoader fromJar(File jar) throws IOException, InvalidJarException
	{
		System.out.println("Loading deobfuscator from jar: " + jar.getAbsolutePath());
		return new ByteLoader(readClasses(jar));
	}

	/**
	 * Create a wrapper from the deobfuscator by searching for it in adjacent files / sub-directories.
	 *
	 * @param recurse Check sub-directories of adjacent folders.
	 * @return JavaDeobfuscator loader. {@code null} if no JavaDeobfuscator jar could be found.
	 */
	private static ByteLoader auto(boolean recurse)
	{
		return iter(new File(System.getProperty("user.dir")), recurse);
	}

	/**
	 * Iterate files to detect the Deobfuscator jar.
	 *
	 * @param dir     directory to look in
	 * @param recurse whether to recurse into subdirectories
	 * @return JavaDeobfuscator loader.
	 */
	private static ByteLoader iter(File dir, boolean recurse)
	{
		System.out.println("Searching for deobfuscator in " + dir.getAbsolutePath());
		File[] files = dir.listFiles();
		// return if no files exist in the directory
		if (files == null)
		{
			return null;
		}
		// check for common names
		File deobfuscator = new File(dir, "deobfuscator.jar");
		File deobfuscator100 = new File(dir, "deobfuscator-1.0.0.jar");
		if (deobfuscator.exists())
			try
			{
				return fromJar(deobfuscator);
			} catch (IOException | InvalidJarException e)
			{
				System.err.println("Failed to load deobfuscator from " + deobfuscator.getAbsolutePath());
				e.printStackTrace();
			}
		if (deobfuscator100.exists())
			try
			{
				return fromJar(deobfuscator100);
			} catch (IOException | InvalidJarException e)
			{
				System.err.println("Failed to load deobfuscator from " + deobfuscator100.getAbsolutePath());
				e.printStackTrace();
			}
		for (File file : files)
		{
			// check sub-dirs
			if (recurse && file.isDirectory())
			{
				ByteLoader v = iter(file, true);
				if (v != null)
				{
					return v;
				}
			}
			// check files in the directory
			else if (file.getName().endsWith(".jar"))
			{
				try
				{
					return fromJar(file);
				} catch (IOException | InvalidJarException e)
				{
					System.err.println("Failed to load deobfuscator from " + file.getAbsolutePath());
					e.printStackTrace();
				}
			}
		}
		return null;
	}

	/**
	 * Read a map from the given data file.
	 *
	 * @param jar File to read from.
	 * @return Map of class names to their bytecode.
	 * @throws IOException         Jar could not be read.
	 * @throws InvalidJarException Thrown if jar loaded was not an instance of JavaDeobfuscator
	 */
	private static Map<String, byte[]> readClasses(File jar) throws IOException, InvalidJarException
	{
		Map<String, byte[]> contents = new HashMap<>();
		boolean found = false;
		try (ZipFile file = new ZipFile(jar))
		{
			// iterate zip entries
			Enumeration<? extends ZipEntry> entries = file.entries();
			while (entries.hasMoreElements())
			{
				ZipEntry entry = entries.nextElement();
				// skip directories
				if (entry.isDirectory())
					continue;
				// skip non-classes (Deobfuscator doesn't have any resources aside for META)
				String name = entry.getName();
				if (!name.endsWith(".class"))
				{
					continue;
				}
				try (InputStream is = file.getInputStream(entry))
				{
					// skip non-classes (Deobfuscator doesn't have any resources aside for
					// META, which we will bundle to appease SLF4J's ServiceLoader screwery.)
					byte[] value = from(is);
					String className;
					try
					{
						className = new MiniClassReader(value).getClassName();
					} catch (Exception e)
					{
						continue;
					}
					// We know this class is in the deobfuscator jar, so if the jar does 
					// not contain it, it is not the correct file.
					if (!found && className.startsWith("com/javadeobfuscator/deobfuscator/Deobfuscator"))
					{
						found = true;
					}
					contents.put(className.replace("/", "."), value);
				}
			}
		}
		// check to ensure expected content of jar file
		if (!found)
		{
			throw new InvalidJarException();
		}
		return contents;
	}

	/**
	 * Reads the bytes from the InputStream into a byte array.
	 *
	 * @param is InputStream to read from.
	 * @return byte array representation of the input stream.
	 * @throws IOException Thrown if the given input stream cannot be read from.
	 */
	private static byte[] from(InputStream is) throws IOException
	{
		try (ByteArrayOutputStream buffer = new ByteArrayOutputStream())
		{
			int r;
			byte[] data = new byte[BUFF_SIZE];
			while ((r = is.read(data, 0, data.length)) != -1)
			{
				buffer.write(data, 0, r);
			}
			buffer.flush();
			return buffer.toByteArray();
		}
	}
}
