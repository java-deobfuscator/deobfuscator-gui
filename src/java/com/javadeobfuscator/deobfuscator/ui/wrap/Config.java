package com.javadeobfuscator.deobfuscator.ui.wrap;

import java.util.ArrayList;
import java.util.List;

import com.javadeobfuscator.deobfuscator.ui.FxWindow;
import com.javadeobfuscator.deobfuscator.ui.util.Reflect;

/**
 * Conflg wrapper that allows for easy reflection maniuplation of fields.
 */
public class Config {

	private final Object instance;

	public Config(Object instance) {
		this.instance = instance;
	}

	public Object get() {
		return instance;
	}

	/**
	 * Set transformers list.
	 * 
	 * @param trans
	 * @param classes
	 */
	public void setTransformers(Transformers trans, List<Class<?>> classes) {
		List<Object> transformerConfigs = new ArrayList<>();
		for (Class<?> clazz : classes) {
			transformerConfigs.add(trans.getConfigFor(clazz));
		}
		try {
			Reflect.setFieldO(instance, "transformers", transformerConfigs);
		} catch (Exception e) {
			//TODO FxWindow.fatalSwing("Config error", "Failed to set transformers list from selected values.");
		}
	}
}
