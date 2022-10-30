package com.javadeobfuscator.deobfuscator.ui.wrap;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.javadeobfuscator.deobfuscator.ui.util.ByteLoader;
import com.javadeobfuscator.deobfuscator.ui.util.FallbackException;

public class Transformers
{
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

	public Transformers(ByteLoader loader)
	{
		this.loader = loader;
	}

	/**
	 * @return List of all transformer classes.
	 * @throws FallbackException when an error occurs while reading transformer classes
	 */
	public List<Class<?>> getTransformers() throws FallbackException
	{
		if (transformers.isEmpty())
		{
			try
			{
				Class<?> transformer = loader.loadClass("com.javadeobfuscator.deobfuscator.transformers.Transformer");
				Class<?> transformerD = loader.loadClass("com.javadeobfuscator.deobfuscator.transformers.DelegatingTransformer");
				List<String> filtered = new ArrayList<>();
				for (String name : loader.getClassNames())
				{
					if (name.startsWith("com.javadeobfuscator.deobfuscator.transformers.") && !name.endsWith("package-info") && !name.endsWith("Config")
						&& !name.contains("$"))
					{
						filtered.add(name);
					}
				}
				Collections.sort(filtered);
				for (String name : filtered)
				{
					Class<?> clazz = loader.loadClass(name);
					if (!clazz.equals(transformer) && !clazz.equals(transformerD) && transformer.isAssignableFrom(clazz)
						&& !Modifier.isAbstract(clazz.getModifiers()))
					{
						transformers.add(clazz);
					}
				}
			} catch (Exception e)
			{
				transformers.clear();
				throw new FallbackException("Loading Problem", "Failed to parse transformer list.", e);
			}
		}
		return transformers;
	}

	/**
	 * @param transClass Transformer class
	 * @return Config instance for transformer class.
	 */
	public Object getConfigFor(Class<?> transClass) throws Exception
	{
		Class<?> confLoader = loader.loadClass("com.javadeobfuscator.deobfuscator.config.TransformerConfig");
		Method configFor = confLoader.getDeclaredMethod("configFor", Class.class);
		return configFor.invoke(null, transClass);
	}
}
