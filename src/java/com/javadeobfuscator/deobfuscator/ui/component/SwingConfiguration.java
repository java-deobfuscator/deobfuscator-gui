package com.javadeobfuscator.deobfuscator.ui.component;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.JTextField;

import com.javadeobfuscator.deobfuscator.ui.util.Reflect;

public class SwingConfiguration
{
	/**
	 * Ignored field names.
	 */
	private final static Set<String> IGNORED_VALUES = new HashSet<>();

	static
	{
		IGNORED_VALUES.add("transformers");
	}

	public List<ConfigItem> fieldsList;

	public SwingConfiguration(Object config)
	{
		//Fill fieldsList
		fieldsList = new ArrayList<>();
		for (Field field : Reflect.fields(config.getClass()))
		{
			if (IGNORED_VALUES.contains(field.getName()))
				continue;
			fieldsList.add(new ConfigItem(config, field));
		}
	}

	public static class ConfigItem
	{
		private final Field field;
		public final ItemType type;
		private Object instance;

		/**
		 * The swing component that we'll use to call the set value
		 */
		public Object component;

		public ConfigItem(Object instance, Field field)
		{
			this.instance = instance;
			this.field = field;
			if (field.getType().equals(File.class))
				type = ItemType.FILE;
			else if (field.getType().equals(boolean.class))
				type = ItemType.BOOLEAN;
			else if (field.getType().equals(List.class))
			{
				Type[] args = ((ParameterizedType) field.getGenericType()).getActualTypeArguments();
				if (args.length > 0 && args[0].getTypeName().contains("File"))
					type = ItemType.FILELIST;
				else
					type = ItemType.STRINGLIST;
			} else
				type = null;
		}

		public String getFieldName()
		{
			return field.getName();
		}

		public String getDisplayName()
		{
			String name = field.getName();
			return name.substring(0, 1).toUpperCase() + name.substring(1);
		}

		/**
		 * Gets the component value (used in save config) Returns string if file and list if defaultlistmodel.
		 */
		public Object getValue()
		{
			if (type == ItemType.FILE)
				return ((JTextField) component).getText();
			if (type == ItemType.BOOLEAN)
				return ((JCheckBox) component).isSelected();
			return Arrays.asList(((DefaultListModel<?>) component).toArray());
		}

		/**
		 * Sets the component value. Must be either a string, boolean, or DefaultListModel of String.
		 */
		public void setValue(Object o)
		{
			if (type == ItemType.FILE)
				((JTextField) component).setText((String) o);
			else if (type == ItemType.BOOLEAN)
				((JCheckBox) component).setSelected((Boolean) o);
			else
				component = o;
		}

		/**
		 * Clears the component value.
		 */
		public void clearValue()
		{
			if (type == ItemType.FILE)
				((JTextField) component).setText("");
			else if (type == ItemType.BOOLEAN)
				((JCheckBox) component).setSelected(false);
			else
			{
				DefaultListModel<Object> listModel = (DefaultListModel<Object>) component;
				listModel.clear();
			}
		}

		/**
		 * Sets the field value with the component value. Used when run deobfuscator is clicked.
		 */
		public void setFieldValue()
		{
			Object o = getValue();
			if (type == ItemType.FILE)
				o = new File((String) o);
			else if (type == ItemType.FILELIST)
			{
				List<File> files = new ArrayList<>();
				for (Object obj : (List<?>) o)
				{
					files.add(new File((String) obj));
				}
				o = files;
			}
			try
			{
				Reflect.setFieldO(instance, field.getName(), o);
			} catch (Exception e)
			{
			}
		}

		/**
		 * Clears the field value.
		 */
		public void clearFieldValue()
		{
			try
			{
				if (type == ItemType.FILE)
					Reflect.setFieldO(instance, field.getName(), null);
				else if (type == ItemType.BOOLEAN)
					Reflect.setFieldO(instance, field.getName(), false);
				else
					Reflect.setFieldO(instance, field.getName(), new ArrayList<>());
			} catch (Exception e)
			{
			}
		}
	}

	public enum ItemType
	{
		FILE,
		BOOLEAN,
		FILELIST,
		STRINGLIST;
	}
}
