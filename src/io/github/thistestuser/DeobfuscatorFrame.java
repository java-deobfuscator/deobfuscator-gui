package io.github.thistestuser;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.border.TitledBorder;

import java.awt.Font;
import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class DeobfuscatorFrame
{
	private JFrame frame;
	private static final String VERSION = "1.0-alpha";
	private JTextField textField;
	
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
		frame.getContentPane().add(selectDeob);
		
		JLabel lblDeobfuscatorJar = new JLabel("Deobfuscator Jar:");
		lblDeobfuscatorJar.setBounds(22, 30, 110, 14);
		frame.getContentPane().add(lblDeobfuscatorJar);
		
		textField = new JTextField();
		textField.setBounds(117, 28, 315, 20);
		frame.getContentPane().add(textField);
		textField.setColumns(10);
	}
}
