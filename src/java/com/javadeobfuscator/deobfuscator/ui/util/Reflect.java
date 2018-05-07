package com.javadeobfuscator.deobfuscator.ui.util;

import java.lang.reflect.*;

/**
 * Reflection wrapper
 */
@SuppressWarnings("unchecked")
public class Reflect {

	/**
	 * Get all fields belonging to the given class.
	 * 
	 * @param clazz
	 *            Class containing fields.
	 * @return Array of class's fields.
	 */
	public static Field[] fields(Class<?> clazz) {
		return clazz.getDeclaredFields();
	}

	/**
	 * Get the value of the field by its name in the given object instance.
	 * 
	 * @param instance
	 *            Object instance.
	 * @param fieldName
	 *            Field name.
	 * @return Field value. {@code null} if could not be reached.
	 */
	public static <T> T get(Object instance, String fieldName) {
		try {
			Field field = instance.getClass().getDeclaredField(fieldName);
			field.setAccessible(true);
			return get(instance, field);
		} catch (NoSuchFieldException | SecurityException e) {
			return null;
		}
	}

	/**
	 * Get the value of the field in the given object instance.
	 * 
	 * @param owner
	 *            Object instance.
	 * @param field
	 *            Field, assumed to be accessible.
	 * @return Field value. {@code null} if could not be reached.
	 */
	public static <T> T get(Object instance, Field field) {
		try {
			return (T) field.get(instance);
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Sets the value of the field in the given object instance.
	 * 
	 * @param owner
	 *            Object instance.
	 * @param field
	 *            Field, assumed to be accessible.
	 * @param value
	 *            Value to set.
	 */
	public static void set(Object instance, Field field, Object value) {
		try {
			field.set(instance, value);
		} catch (Exception e) {}
	}

	/**
	 * Get instance field.
	 * 
	 * @param instance
	 * @param name
	 * @return
	 * @throws Exception
	 */
	public static <T> T getFieldO(Object instance, String name) throws Exception {
		Field f = instance.getClass().getDeclaredField(name);
		f.setAccessible(true);
		// hack access
		Field acc = Field.class.getDeclaredField("modifiers");
		acc.setAccessible(true);
		acc.setInt(f, f.getModifiers() & ~Modifier.FINAL);
		// get
		return (T) f.get(instance);
	}

	/**
	 * Get static field.
	 * 
	 * @param clazz
	 * @param name
	 * @return
	 * @throws Exception
	 */
	public static <T> T getFieldS(Class<?> clazz, String name) throws Exception {
		Field f = clazz.getDeclaredField(name);
		f.setAccessible(true);
		// hack access
		Field acc = Field.class.getDeclaredField("modifiers");
		acc.setAccessible(true);
		acc.setInt(f, f.getModifiers() & ~Modifier.FINAL);
		// get
		return (T) f.get(null);
	}

	/**
	 * Set instance field.
	 * 
	 * @param instance
	 * @param name
	 * @param value
	 * @throws Exception
	 */
	public static void setFieldO(Object instance, String name, Object value) throws Exception {
		Field f = instance.getClass().getDeclaredField(name);
		f.setAccessible(true);
		// hack access
		Field acc = Field.class.getDeclaredField("modifiers");
		acc.setAccessible(true);
		acc.setInt(f, f.getModifiers() & ~Modifier.FINAL);
		// set
		f.set(instance, value);
	}

	/**
	 * Set static field.
	 * 
	 * @param instance
	 * @param name
	 * @param value
	 * @throws Exception
	 */
	public static void setFieldS(Class<?> clazz, String name, Object value) throws Exception {
		Field f = clazz.getDeclaredField(name);
		f.setAccessible(true);
		// hack access
		Field acc = Field.class.getDeclaredField("modifiers");
		acc.setAccessible(true);
		acc.setInt(f, f.getModifiers() & ~Modifier.FINAL);
		// set
		f.set(null, value);
	}

}
