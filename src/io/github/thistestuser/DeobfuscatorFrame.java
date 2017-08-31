package io.github.thistestuser;

import java.awt.EventQueue;

import javax.swing.JFrame;

public class DeobfuscatorFrame
{
	private JFrame frame;
	private static final String VERSION = "1.0-alpha";
	
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
		frame.setBounds(100, 100, 580, 560);
		frame.setTitle("Deobfuscator-GUI " + VERSION);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
}
