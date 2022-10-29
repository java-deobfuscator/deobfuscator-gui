package com.javadeobfuscator.deobfuscator.ui.util;

import java.util.Map;
import java.util.Set;

/**
 * ClassLoader that can find classes via their bytecode. Allows loading of external jar files to be instrumented before loading.
 */
public final class ByteLoader extends ClassLoader
{
	/**
	 * Map of class names to their bytecode.
	 */
	private final Map<String, byte[]> classes;

	/**
	 * Create the loader with the map of classes to load from.
	 *
	 * @param classes {@link #classes}.
	 */
	public ByteLoader(Map<String, byte[]> classes)
	{
		super(getSystemClassLoader());
		Thread.currentThread().setContextClassLoader(this);
		this.classes = classes;
	}

	@Override
	protected final Class<?> findClass(String name) throws ClassNotFoundException
	{
		Class<?> loadedClass = findLoadedClass(name);
		if (loadedClass != null)
		{
			return loadedClass;
		}
		
		byte[] bytes = classes.get(name);
		if (bytes == null)
		{
			throw new ClassNotFoundException(name);
		}
		return defineClass(name, bytes, 0, bytes.length);
	}

	public final Set<String> getClassNames()
	{
		return classes.keySet();
	}
}
