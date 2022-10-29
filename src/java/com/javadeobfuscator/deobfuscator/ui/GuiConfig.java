package com.javadeobfuscator.deobfuscator.ui;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Base64;
import java.util.Properties;

import javax.swing.JOptionPane;

import com.github.weisj.darklaf.settings.SettingsConfiguration;
import com.github.weisj.darklaf.settings.ThemeSettings;

public class GuiConfig
{
	private static final File PROPERTY_FILE = new File("deobfuscator-gui.properties");
	private static final Properties PROPERTIES = new Properties();

	private static final String LIMIT_CONSOLE_LINES = "limit_console_lines";
	private static final String STORE_CONFIG_ON_CLOSE = "store_config_on_close";
	private static final String CONFIG = "config";
	private static final String DARKLAF_ENABLED = "darklaf_enabled";
	private static final String DARKLAF_SETTINGS = "darklaf_settings";

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
		try (InputStreamReader reader = new InputStreamReader(Files.newInputStream(PROPERTY_FILE.toPath()), StandardCharsets.UTF_8))
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
		try (OutputStreamWriter writer = new OutputStreamWriter(Files.newOutputStream(PROPERTY_FILE.toPath()), StandardCharsets.UTF_8))
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

	public static boolean isDarkLaf()
	{
		return Boolean.parseBoolean(PROPERTIES.getProperty(DARKLAF_ENABLED));
	}

	public static void setDarkLaf(boolean state)
	{
		PROPERTIES.setProperty(DARKLAF_ENABLED, Boolean.toString(state));
	}

	public static SettingsConfiguration getDarklafSettings()
	{
		String property = PROPERTIES.getProperty(DARKLAF_SETTINGS);
		if (property == null)
		{
			return ThemeSettings.getInstance().exportConfiguration();
		}
		try
		{
			byte[] decode = Base64.getDecoder().decode(property);
			ByteArrayInputStream bain = new ByteArrayInputStream(decode);
			try (ObjectInputStream objectInputStream = new ObjectInputStream(bain))
			{
				Object obj = objectInputStream.readObject();
				if (obj instanceof SettingsConfiguration)
				{
					return (SettingsConfiguration) obj;
				}
			} catch (IOException | ClassNotFoundException e)
			{
				e.printStackTrace();
				PROPERTIES.remove(DARKLAF_SETTINGS);
			}
		} catch (Throwable t)
		{
			t.printStackTrace();
		}
		return ThemeSettings.getInstance().exportConfiguration();
	}

	public static void setDarklafSettings(SettingsConfiguration state)
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(baos))
		{
			objectOutputStream.writeObject(state);
			objectOutputStream.flush();
			PROPERTIES.setProperty(DARKLAF_SETTINGS, Base64.getEncoder().encodeToString(baos.toByteArray()));
		} catch (IOException e)
		{
			e.printStackTrace();
		}
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
