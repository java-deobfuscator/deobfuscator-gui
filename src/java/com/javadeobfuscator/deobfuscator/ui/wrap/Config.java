package com.javadeobfuscator.deobfuscator.ui.wrap;

import java.util.List;

import com.javadeobfuscator.deobfuscator.ui.util.Reflect;

/**
 * Config wrapper that allows for easy reflection manipulation of fields.
 */
public class Config
{

	private final Object instance;

	public Config(Object instance)
	{
		this.instance = instance;
	}

	public Object get()
	{
		return instance;
	}

	/**
	 * Set transformers list.
	 *
	 * @param trans
	 * @param transformerConfigs
	 */
	public void setTransformers(Transformers trans, List<Object> transformerConfigs) throws Exception
	{
		Reflect.setFieldO(instance, "transformers", transformerConfigs);
	}
}
