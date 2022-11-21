package com.javadeobfuscator.deobfuscator.ui.util;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import com.javadeobfuscator.deobfuscator.ui.SwingWindow;
import com.javadeobfuscator.deobfuscator.ui.component.SynchronousJFXFileChooser;
import javafx.stage.FileChooser;

public class FallbackException extends Exception
{
	public String path;

	public FallbackException(String title, String msg, Throwable cause)
	{
		super(msg, cause);
		this.printStackTrace();
		SwingWindow.ensureSwingLafLoaded();
		SwingWindow.initJFX();
		JPanel fallback = new JPanel();
		fallback.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		gbc.insets = new Insets(0, 0, 4, 0);
		fallback.add(new JLabel(msg), gbc);
		if (cause != null) {
			gbc.gridy++;
			JTextArea area = new JTextArea(ExceptionUtil.getStackTrace(cause));
			area.setEditable(false);
			fallback.add(area, gbc);
		}
		gbc.gridy++;
		fallback.add(new JLabel("Select deobfuscator.jar to try again:"), gbc);
		gbc.gridy++;
		JTextField textField = new JTextField(35);
		fallback.add(textField);
		gbc.gridx++;
		JButton button = new JButton("Select");
		button.addActionListener(e ->
		{
			SynchronousJFXFileChooser chooser = new SynchronousJFXFileChooser(() -> {
				FileChooser ch = new FileChooser();
				ch.setTitle("Select deobfuscator jar");
				ch.setInitialDirectory(new File("abc").getAbsoluteFile().getParentFile());
				ch.getExtensionFilters().addAll(
						new FileChooser.ExtensionFilter("Jar files", "*.jar"),
						new FileChooser.ExtensionFilter("Jar and Zip files", "*.jar", "*.zip"),
						new FileChooser.ExtensionFilter("Zip files", "*.zip"),
						new FileChooser.ExtensionFilter("All Files", "*.*"));
				return ch;
			});
			File file = chooser.showOpenDialog();
			if (file != null)
			{
				textField.setText(file.getAbsolutePath());
			}
		});
		GridBagConstraints gbc_Button = new GridBagConstraints();
		gbc_Button.insets = new Insets(0, 2, 0, 0);
		fallback.add(button, gbc_Button);
		Object[] options = {"Ok", "Cancel"};
		int result = JOptionPane.showOptionDialog(null, fallback, title,
				JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE,
				null, options, null);
		if (result == JOptionPane.CLOSED_OPTION || result == JOptionPane.NO_OPTION)
			System.exit(0);
		path = textField.getText();
	}
}
