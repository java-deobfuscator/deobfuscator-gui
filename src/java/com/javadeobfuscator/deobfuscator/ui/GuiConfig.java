package com.javadeobfuscator.deobfuscator.ui;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

import javax.swing.JOptionPane;

public class GuiConfig
{
	private static final File PROPERTY_FILE = new File("deobfuscator-gui.properties");
	private static final Properties PROPERTIES = new Properties();

	private static final String LIMIT_CONSOLE_LINES = "limit_console_lines";
	private static final String STORE_CONFIG_ON_CLOSE = "store_config_on_close";
	private static final String CONFIG = "config";

	static
	{
		PROPERTIES.setProperty(LIMIT_CONSOLE_LINES, "false");
		PROPERTIES.setProperty(STORE_CONFIG_ON_CLOSE, "true");
	}

	public static void read()
	{
		if (!PROPERTY_FILE.exists())
		{
			save();
			return;
		}
		try (InputStreamReader reader = new InputStreamReader(new FileInputStream(PROPERTY_FILE), StandardCharsets.UTF_8))
		{
			PROPERTIES.load(reader);
		} catch (IOException e)
		{
			e.printStackTrace();
			PROPERTY_FILE.delete();
			JOptionPane.showMessageDialog(null, "Gui config file " + PROPERTY_FILE.getName() + " could not be read and was replaced with " +
												"the default config.\n\n" + e.getClass().getName() + ": " + e.getMessage(), "Deobfuscator GUI",
					JOptionPane.WARNING_MESSAGE);
			save();
		}
	}

	public static void save()
	{
		try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(PROPERTY_FILE), StandardCharsets.UTF_8))
		{
			PROPERTIES.store(writer, null);
		} catch (IOException e)
		{
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "Gui config file " + PROPERTY_FILE.getName() + " could not be saved.\n\n" +
												e.getClass().getName() + ": " + e.getMessage(), "Deobfuscator GUI", JOptionPane.WARNING_MESSAGE);
		}
	}

	public static boolean isLimitConsoleLines()
	{
		return Boolean.parseBoolean(PROPERTIES.getProperty(LIMIT_CONSOLE_LINES));
	}

	public static void setLimitConsoleLines(boolean state)
	{
		PROPERTIES.setProperty(LIMIT_CONSOLE_LINES, Boolean.toString(state));
	}

	public static boolean getStoreConfigOnClose()
	{
		return Boolean.parseBoolean(PROPERTIES.getProperty(STORE_CONFIG_ON_CLOSE));
	}

	public static void setStoreConfigOnClose(boolean state)
	{
		PROPERTIES.setProperty(STORE_CONFIG_ON_CLOSE, Boolean.toString(state));
	}

	public static String getConfig()
	{
		return PROPERTIES.getProperty(CONFIG);
	}

	public static void setConfig(String config)
	{
		if (getStoreConfigOnClose())
		{
			PROPERTIES.setProperty(CONFIG, config);
		} else
		{
			PROPERTIES.remove(CONFIG);
		}
	}
}
