package com.javadeobfuscator.deobfuscator.ui.util;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * ClassLoader that can find classes via their bytecode. Allows loading of
 * external jar files to be instrumented before loading.
 */
public final class ByteLoader extends ClassLoader {
	/**
	 * Map of class names to their bytecode.
	 */
	private final Map<String, byte[]> classes;
	/**
	 * Set of loaded names.
	 */
	private final Set<String> loaded = new HashSet<>();

	/**
	 * Create the loader with the map of classes to load from.
	 * 
	 * @param classes
	 *            {@link #classes}.
	 */
	public ByteLoader(Map<String, byte[]> classes) {
		super(getSystemClassLoader());
		Thread.currentThread().setContextClassLoader(this);
		this.classes = classes;
	}

	@Override
	public final Class<?> findClass(String name) throws ClassNotFoundException {
		// Load from map of classes
		if (!loaded.contains(name) && classes.containsKey(name)) {
			byte[] bytes = classes.get(name);
			loaded.add(name);
			return defineClass(name, bytes, 0, bytes.length, null);
		}
		// Unknown class, defer to system ClassLoader
		return loadClass(name, false);

	}
}