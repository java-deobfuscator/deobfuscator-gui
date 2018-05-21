package com.javadeobfuscator.deobfuscator.ui.wrap;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import com.javadeobfuscator.deobfuscator.ui.FxWindow;
import com.javadeobfuscator.deobfuscator.ui.util.*;

public class Deobfuscator {
	/**
	 * ClassLoader to load classes from deobfuscator jar.
	 */
	private ByteLoader loader;
	/**
	 * Config wrapper to use in deobfuscator.
	 */
	private Config config;

	Deobfuscator(ByteLoader loader) {
		this.loader = loader;
	}

	/**
	 * Allow easy interception of logging calls.
	 * 
	 * @param hook
	 *            PrintStream to redirect log calls to.
	 */
	public void hookLogging(PrintStream hook) {
		try {
			hookLogger("com.javadeobfuscator.deobfuscator.Deobfuscator", hook);
			hookLogger("com.javadeobfuscator.deobfuscator.DeobfuscatorMain", hook);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * @return Config wrapper.
	 * @throws FallbackException 
	 */
	public Config getConfig() throws FallbackException {
		if (config == null) {
			try {
				Class<?> conf = loader.findClass("com.javadeobfuscator.deobfuscator.config.Configuration");
				config = new Config(conf.newInstance());
			} catch (Exception e) {
				throw new FallbackException("Loading Problem", "Could not create Config instance.");
			}
		}
		return config;
	}

	/**
	 * Runs the deobfuscator process.
	 * 
	 * @throws Exception
	 *             Thrown for any failure in the deobfuscator.
	 */
	public void run() throws Exception {
		Class<?> main = loader.findClass("com.javadeobfuscator.deobfuscator.Deobfuscator");
		Config conf = getConfig();
		Constructor<?> con = main.getDeclaredConstructor(conf.get().getClass());
		Object deob = con.newInstance(conf.get());
		Method start = main.getMethod("start");
		start.invoke(deob);
	}

	/**
	 * Intercept logging calls.
	 * 
	 * @param ownerName
	 * @param hook
	 * @throws Exception
	 */
	private void hookLogger(String ownerName, PrintStream hook) throws Exception {
		// unused, but required to load class, which sets up some important static fields
		getLogger(loader.findClass(ownerName));
		Class<?> simpleLogger = loader.findClass("org.slf4j.simple.SimpleLogger");

		Object config = Reflect.getFieldS(simpleLogger, "CONFIG_PARAMS");
		Object outChoice = Reflect.getFieldO(config, "outputChoice");

		Class<?> typeEnum = loader.findClass("org.slf4j.simple.OutputChoice$OutputChoiceType");
		Object enumChoice = Reflect.getFieldS(typeEnum, "FILE");

		// hook
		Reflect.setFieldO(outChoice, "outputChoiceType", enumChoice);
		Reflect.setFieldO(outChoice, "targetPrintStream", hook);
	}

	private Object getLogger(Class<?> loggerOwner) throws Exception {
		// LoggerFactory.getLogger(getClass())
		Class<?> factory = loader.findClass("org.slf4j.LoggerFactory");
		Method m = factory.getDeclaredMethod("getLogger", Class.class);
		return m.invoke(null, loggerOwner);
	}
}
