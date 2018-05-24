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
	@SuppressWarnings("serial")
	private final static Set<String> IGNORED_VALUES = new HashSet<String>() {
		{
			add("transformers");
		}
	};
	public List<ConfigItem> fieldsList;
	
	public SwingConfiguration(Object config)
	{
		//Fill fieldsList
		fieldsList = new ArrayList<>();
		for(Field field : Reflect.fields(config.getClass()))
		{
			if(IGNORED_VALUES.contains(field.getName()))
				continue;
			fieldsList.add(new ConfigItem(config, field));
		}
	}
	
	public static class ConfigItem
	{
		private final Field field;
		public final ItemType type;
		private Object instance;
		
		/** The swing component that we'll use to call the set value */
		public Object component;
		
		public ConfigItem(Object instance, Field field)
		{
			this.instance = instance;
			this.field = field;
			if(field.getType().equals(File.class))
				type = ItemType.FILE;
			else if(field.getType().equals(boolean.class))
				type = ItemType.BOOLEAN;
			else if(field.getType().equals(List.class))
			{
				Type[] args =  ((ParameterizedType)field.getGenericType()).getActualTypeArguments();
				if (args.length > 0 && args[0].getTypeName().contains("File"))
					type = ItemType.FILELIST;
				else
					type = ItemType.STRINGLIST;
			}else
				type = null;
		}
		
		public String getDisplayName()
		{
			String name =  field.getName();
			return name.substring(0, 1).toUpperCase() + name.substring(1);
		}
		
		/**
		 * Gets the component value (used in save config)
		 */
		public Object getValue()
		{
			if(type == ItemType.FILE)
				return ((JTextField)component).getText();
			if(type == ItemType.BOOLEAN)
				return ((JCheckBox)component).isSelected();
			return Arrays.asList(((DefaultListModel<?>)component).toArray());
		}
		
		/**
		 *  Sets the component value with the field value. Used on first load and load config.
		 */
		public void setValue()
		{
			if(getFieldValue() == null)
				return;
			if(type == ItemType.FILE)
				((JTextField)component).setText((String)getFieldValue());
			else if(type == ItemType.BOOLEAN)
				((JCheckBox)component).setSelected((Boolean)getFieldValue());
			else
			{
				DefaultListModel<Object> listModel = (DefaultListModel<Object>)component;
				listModel.clear();
				List<?> fieldValue = (List<?>)getFieldValue();
				for(Object o : fieldValue)
					listModel.addElement(o);
			}
		}
		
		/**
		 * Gets the field value (used on first load)
		 */
		public Object getFieldValue()
		{
			try
			{
				return Reflect.getFieldO(instance, field.getName());
			}catch(Exception e)
			{
				return null;
			}
		}
		
		/**
		 * Sets the field value with the component value. Used when run deobfuscator is clicked.
		 */
		public void setFieldValue()
		{
			try
			{
				Reflect.setFieldO(instance, field.getName(), getValue());
			}catch(Exception e)
			{}
		}
		
	}
	
	public static enum ItemType
	{
		FILE,
		BOOLEAN,
		FILELIST,
		STRINGLIST;
	}
}
