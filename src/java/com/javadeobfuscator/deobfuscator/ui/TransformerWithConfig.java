package com.javadeobfuscator.deobfuscator.ui;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Set;

import com.javadeobfuscator.deobfuscator.ui.util.TransformerConfigUtil;

public class TransformerWithConfig
{
	private final String shortName;
	private Object config;

	public TransformerWithConfig(String shortName) {
		this.shortName = shortName;
	}

	public TransformerWithConfig(String shortName, Object config) {
		this.shortName = shortName;
		this.config = config;
	}

	public String getShortName()
	{
		return shortName;
	}

	public Object getConfig()
	{
		return config;
	}

	public void setConfig(Object config)
	{
		this.config = config;
	}
	
	public String toExportString() {
		Set<Field> fields = TransformerConfigUtil.getTransformerConfigFieldsWithSuperclass(config.getClass());
		StringBuilder sb = new StringBuilder(this.shortName);
		for (Field field : fields)
		{
			field.setAccessible(true);
			try
			{
				Object val = field.get(config);
				if (val == null) {
					continue;
				}
				sb.append(':').append(field.getName()).append('=');
				if (val instanceof File) {
					sb.append('"').append(((File) val).getAbsolutePath()).append('"');
				} else {
				sb.append(val);
				}
			} catch (IllegalAccessException e)
			{
				e.printStackTrace();
			}
		}
		return sb.toString();
	}

	@Override
	public String toString()
	{
		return this.shortName;
	}
}
