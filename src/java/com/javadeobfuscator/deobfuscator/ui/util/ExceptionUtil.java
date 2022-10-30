package com.javadeobfuscator.deobfuscator.ui.util;

import java.io.PrintWriter;
import java.io.StringWriter;

import javax.swing.JOptionPane;

import com.javadeobfuscator.deobfuscator.ui.SwingWindow;

public class ExceptionUtil
{
	public static void showFatalError(String message) {
		showFatalError(message, null);
	}

	public static void showFatalError(String message, Throwable t)
	{
		try
		{
			if (t != null)
			{
				t.printStackTrace();
				message = message + "\n" + getStackTrace(t);
			}
			SwingWindow.ensureSwingLafLoaded();
			JOptionPane.showMessageDialog(null, message, "Deobfuscator GUI - Error", JOptionPane.ERROR_MESSAGE);
		} catch (Throwable t2)
		{
			t2.printStackTrace();
		}
		System.exit(1);
		throw new Error("exit", t);
	}

	public static String getStackTrace(Throwable t)
	{
		StringWriter sw = new StringWriter();
		t.printStackTrace(new PrintWriter(sw));
		return sw.toString();
	}
}
