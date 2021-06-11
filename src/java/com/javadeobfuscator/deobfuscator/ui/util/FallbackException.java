package com.javadeobfuscator.deobfuscator.ui.util;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class FallbackException extends Exception
{
	public String path;

	public FallbackException(String title, String msg)
	{
		JPanel fallback = new JPanel();
		fallback.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		gbc.insets = new Insets(0, 0, 4, 0);
		fallback.add(new JLabel(msg), gbc);
		gbc.gridy++;
		fallback.add(new JLabel("Select deobfuscator.jar to try again:"), gbc);
		gbc.gridy++;
		JTextField textField = new JTextField(35);
		fallback.add(textField);
		gbc.gridx++;
		JButton button = new JButton("Select");
		button.addActionListener(e ->
		{
			JFileChooser inputFile = new JFileChooser();
			int action = inputFile.showOpenDialog(null);
			if (action == JFileChooser.APPROVE_OPTION)
			{
				String path = inputFile.getSelectedFile().toString();
				textField.setText(path);
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
