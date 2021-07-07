package com.javadeobfuscator.deobfuscator.ui.util;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

import com.javadeobfuscator.deobfuscator.ui.SwingWindow;

public class TransformerConfigUtil
{
	private TransformerConfigUtil()
	{
		throw new UnsupportedOperationException();
	}

	public static Field getTransformerConfigFieldWithSuperclass(Class<?> cfgClazz, String fieldName)
	{
		if (cfgClazz == null || fieldName == null || cfgClazz.getName().equals("com.javadeobfuscator.deobfuscator.config.TransformerConfig"))
		{
			return null;
		}
		Field field = null;
		try
		{
			field = cfgClazz.getDeclaredField(fieldName);
		} catch (NoSuchFieldException ex)
		{
			Class<?> superclass = cfgClazz.getSuperclass();
			return getTransformerConfigFieldWithSuperclass(superclass, fieldName);
		}
		return field;
	}

	public static Set<Field> getTransformerConfigFieldsWithSuperclass(Class<?> cfgClazz)
	{
		return getTransformerConfigFielddWithSuperclass0(cfgClazz, new LinkedHashSet<>());
	}

	private static Set<Field> getTransformerConfigFielddWithSuperclass0(Class<?> cfgClazz, Set<Field> result)
	{
		if (cfgClazz == null || cfgClazz.getName().equals("com.javadeobfuscator.deobfuscator.config.TransformerConfig"))
		{
			return result;
		}
		Collections.addAll(result, cfgClazz.getDeclaredFields());
		return getTransformerConfigFielddWithSuperclass0(cfgClazz.getSuperclass(), result);
	}

	public static Object convertToObj(Class<?> fType, String strVal)
	{
		Object oval = null;
		if (fType == String.class || fType == CharSequence.class)
		{
			oval = strVal;
		} else if (fType == File.class)
		{
			oval = new File(strVal);
		} else if (fType == boolean.class || fType == Boolean.class)
		{
			oval = Boolean.parseBoolean(strVal);
		} else if (fType == byte.class || fType == Byte.class)
		{
			oval = Byte.parseByte(strVal);
		} else if (fType == short.class || fType == Short.class)
		{
			oval = Short.parseShort(strVal);
		} else if (fType == int.class || fType == Integer.class)
		{
			oval = Integer.parseInt(strVal);
		} else if (fType == long.class || fType == Long.class)
		{
			oval = Long.parseLong(strVal);
		} else if (fType == float.class || fType == Float.class)
		{
			oval = Float.parseFloat(strVal);
		} else if (fType == double.class || fType == Double.class)
		{
			oval = Double.parseDouble(strVal);
		} else if (fType.isEnum())
		{
			for (Object eObj : fType.getEnumConstants())
			{
				Enum<?> e = (Enum<?>) eObj;
				if (e.name().toLowerCase(Locale.ROOT).equals(strVal.toLowerCase(Locale.ROOT)))
				{
					return e;
				}
			}
		}
		return oval;
	}

	public static Object getConfig(Class<?> transformerClass)
	{
		try
		{
			Object cfg = SwingWindow.trans.getConfigFor(transformerClass);
			if (cfg != null && cfg.getClass().getName().equals("com.javadeobfuscator.deobfuscator.config.TransformerConfig"))
			{
				return null;
			}
			return cfg;
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
		return null;
	}
}
