package io.github.thistestuser;

import java.awt.EventQueue;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

public class DeobfuscatorFrame
{
	private JFrame frame;
	private static final String VERSION = "1.0-alpha";
	private JTextField textField;
	private File deobfuscatorPath;
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args)
	{
		EventQueue.invokeLater(new Runnable()
		{
			public void run()
			{
				try
				{
					DeobfuscatorFrame window = new DeobfuscatorFrame();
					window.frame.setVisible(true);
				}catch(Exception e)
				{
					e.printStackTrace();
				}
			}
		});
	}
	
	/**
	 * Create the application.
	 */
	public DeobfuscatorFrame()
	{
		initialize();
	}
	
	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize()
	{
		frame = new JFrame();
		frame.getContentPane().setFont(new Font("Tahoma", Font.PLAIN, 11));
		frame.setBounds(100, 100, 580, 560);
		frame.setTitle("Deobfuscator-GUI " + VERSION + " By ThisTestUser");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		try
		{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}catch(ClassNotFoundException | InstantiationException
			| IllegalAccessException | UnsupportedLookAndFeelException e)
		{
			e.printStackTrace();
		}
		
		JLabel selectDeobfuscator = new JLabel("");
		selectDeobfuscator.setFont(new Font("Tahoma", Font.PLAIN, 11));
		selectDeobfuscator.setBounds(10, 11, 544, 48);
		TitledBorder selectTitle = new TitledBorder("Select Deobfuscator");
		selectDeobfuscator.setBorder(selectTitle);
		frame.getContentPane().add(selectDeobfuscator);
		
		JButton selectDeob = new JButton("Select");
		selectDeob.setToolTipText("<html>Here you will select the deobfuscator.jar you downloaded. <br>\r\nIf you don't have it, build the jar from <br>\r\nhttps://github.com/java-deobfuscator/deobfuscator</html>");
		selectDeob.setBounds(451, 25, 89, 23);
		selectDeob.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				JFileChooser deobFile = new JFileChooser();
				deobFile.setFileSelectionMode(JFileChooser.FILES_ONLY);
				deobFile.setAcceptAllFileFilterUsed(false);
				deobFile.addChoosableFileFilter(new FileNameExtensionFilter(
					"Executable JAR files", "jar"));
				if(deobfuscatorPath != null)
					deobFile.setSelectedFile(deobfuscatorPath);
				int action = deobFile.showOpenDialog(null);
				if(action == JFileChooser.APPROVE_OPTION)
				{
					deobfuscatorPath = deobFile.getSelectedFile();
					String path = deobFile.getSelectedFile().toString();
					textField.setText(path);
					loadTransformers();
				}
			}
		});
		frame.getContentPane().add(selectDeob);
		
		textField = new JTextField();
		textField.setBounds(117, 28, 315, 20);
		frame.getContentPane().add(textField);
		textField.setColumns(10);
		
		JLabel lblDeobfuscatorJar = new JLabel("Deobfuscator Jar:");
		lblDeobfuscatorJar.setBounds(22, 30, 110, 14);
		frame.getContentPane().add(lblDeobfuscatorJar);
	}
	
	private void loadTransformers()
	{
		
	}
}
