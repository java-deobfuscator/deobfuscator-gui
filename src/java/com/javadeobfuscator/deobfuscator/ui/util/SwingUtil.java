package com.javadeobfuscator.deobfuscator.ui.util;

import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.util.function.Consumer;

public class SwingUtil
{
	private SwingUtil()
	{
		throw new UnsupportedOperationException();
	}

	public static void registerGBC(Container parent, Component component, int x, int y)
	{
		registerGBC(parent, component, x, y, null);
	}

	public static void registerGBC(Container parent, Component component, int x, int y, Consumer<GridBagConstraints> consumer)
	{
		registerGBC(parent, component, x, y, 1, 1, consumer);
	}

	public static void registerGBC(Container parent, Component component, int x, int y, int w, int h)
	{
		registerGBC(parent, component, x, y, w, h, null);
	}

	public static void registerGBC(Container parent, Component component, int x, int y, int w, int h, Consumer<GridBagConstraints> consumer)
	{
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(10, 10, 10, 10);
		gbc.gridx = x;
		gbc.gridy = y;
		gbc.gridwidth = w;
		gbc.gridheight = h;
		if (consumer != null)
		{
			consumer.accept(gbc);
		}
		parent.add(component, gbc);
	}
}
