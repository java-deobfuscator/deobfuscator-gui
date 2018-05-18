package com.javadeobfuscator.deobfuscator.ui.wrap;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.javadeobfuscator.deobfuscator.ui.FxWindow;
import com.javadeobfuscator.deobfuscator.ui.util.ByteLoader;

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
				Class<?> transformer = loader.findClass("com.javadeobfuscator.deobfuscator.transformers.Transformer");
				Class<?> transformerD = loader.findClass("com.javadeobfuscator.deobfuscator.transformers.DelegatingTransformer");
				List<String> names = new ArrayList<>(loader.getClassNames());
				Collections.sort(names);
				for (String name : names) {
					if (name.startsWith("com.javadeobfuscator.deobfuscator.transformers.") && name.endsWith("er")) {
						Class<?> clazz = loader.findClass(name);
						if (!clazz.equals(transformer) && !clazz.equals(transformerD) && transformer.isAssignableFrom(clazz)) {
							transformers.add(clazz);
						}
					}
					
				}
				
			} catch (Exception e) {
				FxWindow.fatalSwing("Loading problem", "Failed to parse transformer list");
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
			FxWindow.fatalSwing("Loading problem", "Failed to load TransformerConfig from Transformer class");
		}
		return null;
	}

}
