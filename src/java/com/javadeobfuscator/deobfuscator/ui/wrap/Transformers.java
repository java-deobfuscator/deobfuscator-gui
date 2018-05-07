package com.javadeobfuscator.deobfuscator.ui.wrap;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import com.javadeobfuscator.deobfuscator.ui.FxWindow;
import com.javadeobfuscator.deobfuscator.ui.util.ByteLoader;
import com.javadeobfuscator.deobfuscator.ui.util.Reflect;

public class Transformers {
	// load with:
	// com/javadeobfuscator/deobfuscator/config/TransformerConfig.configFor(class)

	/**
	 * List of all transformers.
	 */
	private static final List<Class<?>> transformers = new ArrayList<>();
	/**
	 * ClassLoader to load classes from deobfuscator jar.
	 */
	private ByteLoader loader;

	public Transformers(ByteLoader loader) {
		this.loader = loader;
	}

	/**
	 * @return List of all transformer classes.
	 */
	public List<Class<?>> getTransformers() {
		if (transformers.size() == 0) {
			try {
				Class<?> transList = loader.findClass("com.javadeobfuscator.deobfuscator.transformers.Transformers");
				recurse(transList);
			} catch (Exception e) {
				FxWindow.fatal("Loading problem", "Failed to parse transformer list");
			}
		}
		return transformers;
	}

	/**
	 * @param transClass
	 *            Transformer class
	 * @return Config instance for transformer class.
	 */
	public Object getConfigFor(Class<?> transClass) {
		try {
			Class<?> confLoader = loader.findClass("com.javadeobfuscator.deobfuscator.config.TransformerConfig");
			Method configFor = confLoader.getDeclaredMethod("configFor", Class.class);
			return configFor.invoke(null, transClass);
		} catch (Exception e) {
			FxWindow.fatal("Loading problem", "Failed to load TransformerConfig from Transformer class");
		}
		return null;
	}

	/**
	 * Since the Transformers class is organized by inner-classes wrapping transformer
	 * classes, allow recursive calls to inners to find every possible transformer.
	 * 
	 * @param outer
	 *            Outer class.
	 * @throws Exception
	 *             Fields could not be iterated.
	 */
	private void recurse(Class<?> outer) throws Exception {
		// search inners for values
		for (Class<?> inner : outer.getDeclaredClasses()) {
			for (Field field : Reflect.fields(inner)) {
				transformers.add((Class<?>) field.get(null));
			}
			// search inner's inners
			recurse(inner);
		}
	}
}
