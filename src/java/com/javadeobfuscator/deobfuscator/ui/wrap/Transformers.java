package com.javadeobfuscator.deobfuscator.ui.wrap;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.javadeobfuscator.deobfuscator.ui.FxWindow;
import com.javadeobfuscator.deobfuscator.ui.util.ByteLoader;
import com.javadeobfuscator.deobfuscator.ui.util.FallbackException;

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
	 * @throws FallbackException 
	 */
	public List<Class<?>> getTransformers() throws FallbackException {
		if (transformers.size() == 0) {
			try {
				Class<?> transformer = loader.findClass("com.javadeobfuscator.deobfuscator.transformers.Transformer");
				Class<?> transformerD = loader.findClass("com.javadeobfuscator.deobfuscator.transformers.DelegatingTransformer");
				List<String> names = new ArrayList<>(loader.getClassNames());
				Collections.sort(names);
				for (String name : names) {
					if (name.startsWith("com.javadeobfuscator.deobfuscator.transformers.")) {
						Class<?> clazz = loader.findClass(name);
						if (!clazz.equals(transformer) && !clazz.equals(transformerD) && transformer.isAssignableFrom(clazz)
							&& !Modifier.isAbstract(clazz.getModifiers())) {
							transformers.add(clazz);
						}
					}
					
				}
				
			} catch (Exception e) {
				transformers.clear();
				throw new FallbackException("Loading Problem", "Failed to parse transformer list.");
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
			//TODO FxWindow.fatalSwing("Loading problem", "Failed to load TransformerConfig from Transformer class"); Only stop deobfuscator run
		}
		return null;
	}

}
