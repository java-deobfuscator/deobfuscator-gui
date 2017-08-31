package io.github.thistestuser;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

public class DeobfuscatorFrame
{
	private static final String VERSION = "1.0-alpha";
	private JFrame frame;
	private JTextField deobfuscatorField;
	private File deobfuscatorPath;
	private JTextField inputField;
	private File inputPath;
	private JTextField outputField;
	private File outputPath;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args)
	{
		EventQueue.invokeLater(new Runnable()
		{
			@Override
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

		//Sets up the "Select Deobfuscator" box
		JLabel selectDeobfuscator = new JLabel("");
		selectDeobfuscator.setFont(new Font("Tahoma", Font.PLAIN, 11));
		selectDeobfuscator.setBounds(10, 11, 544, 48);
		TitledBorder selectTitle = new TitledBorder("Select Deobfuscator");
		selectDeobfuscator.setBorder(selectTitle);
		frame.getContentPane().add(selectDeobfuscator);
		
		//Sets up the "Deobfuscator Arguments" box
		JLabel deobfuscatorArguments = new JLabel("");
		deobfuscatorArguments.setFont(new Font("Tahoma", Font.PLAIN, 11));
		deobfuscatorArguments.setBounds(10, 95, 544, 368);
		TitledBorder deobArgs = new TitledBorder("Deobfuscator Arguments");
		deobfuscatorArguments.setBorder(deobArgs);
		frame.getContentPane().add(deobfuscatorArguments);

		JLabel successOrFail = new JLabel("Select deobfuscator.jar to begin!");
		successOrFail.setBounds(166, 70, 181, 14);
		frame.getContentPane().add(successOrFail);

		JButton selectDeob = new JButton("Select");
		selectDeob.setToolTipText(
			"<html>Here you will select the deobfuscator.jar you downloaded. <br>\r\nIf you don't have it, build the jar from <br>\r\nhttps://github.com/java-deobfuscator/deobfuscator</html>");
		selectDeob.setBounds(451, 25, 89, 23);
		selectDeob.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				JFileChooser deobFile = new JFileChooser();
				deobFile.setFileSelectionMode(JFileChooser.FILES_ONLY);
				deobFile.setAcceptAllFileFilterUsed(false);
				deobFile.addChoosableFileFilter(
					new FileNameExtensionFilter("Executable JAR files", "jar"));
				if(deobfuscatorPath != null)
					deobFile.setSelectedFile(deobfuscatorPath);
				int action = deobFile.showOpenDialog(null);
				if(action == JFileChooser.APPROVE_OPTION)
				{
					deobfuscatorPath = deobFile.getSelectedFile();
					String path = deobFile.getSelectedFile().toString();
					deobfuscatorField.setText(path);
					loadTransformers(path, successOrFail);
				}
			}
		});
		frame.getContentPane().add(selectDeob);

		deobfuscatorField = new JTextField();
		deobfuscatorField.setBounds(117, 28, 315, 20);
		frame.getContentPane().add(deobfuscatorField);
		deobfuscatorField.setColumns(10);

		JLabel lblDeobfuscatorJar = new JLabel("Deobfuscator Jar:");
		lblDeobfuscatorJar.setBounds(22, 30, 110, 14);
		frame.getContentPane().add(lblDeobfuscatorJar);
		
		
		
		//2 tabs: Transformers and libraries
		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		tabbedPane.setBounds(22, 213, 517, 227);
		frame.getContentPane().add(tabbedPane);
		
        JPanel transformers = new JPanel();
        JPanel libraries = new JPanel();
        JLabel label1 = new JLabel();
        label1.setText("You are in area of Tab1");
        JLabel label2 = new JLabel();
        label2.setText("You are in area of Tab2");
        transformers.add(label1);
        libraries.add(label2);
        tabbedPane.addTab("Transformers", transformers);
        tabbedPane.addTab("Libraries", libraries);
        
        JLabel lblInput = new JLabel("Input:");
        lblInput.setToolTipText("<html>Select your file to deobfuscate here.</html>");
        lblInput.setBounds(26, 126, 46, 14);
        frame.getContentPane().add(lblInput);
        
        JLabel lblOutput = new JLabel("Output:");
        lblOutput.setToolTipText("<html>Select what the deobfuscated file should be named here.</html>");
        lblOutput.setBounds(22, 165, 46, 14);
        frame.getContentPane().add(lblOutput);
        
        inputField = new JTextField();
        inputField.setBounds(71, 123, 361, 20);
        frame.getContentPane().add(inputField);
        inputField.setColumns(10);
        
        outputField = new JTextField();
        outputField.setBounds(71, 162, 361, 20);
        frame.getContentPane().add(outputField);
        outputField.setColumns(10);
        
        JButton selectInput = new JButton("Select");
        selectInput.setBounds(451, 122, 89, 23);
        frame.getContentPane().add(selectInput);
        selectInput.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				JFileChooser inputFile = new JFileChooser();
				if(inputPath != null)
					inputFile.setSelectedFile(inputPath);
				int action = inputFile.showOpenDialog(null);
				if(action == JFileChooser.APPROVE_OPTION)
				{
					inputPath = inputFile.getSelectedFile();
					String path = inputFile.getSelectedFile().toString();
					inputField.setText(path);
				}
			}
		});
        
        JButton selectOutput = new JButton("Select");
        selectOutput.setBounds(451, 161, 89, 23);
        frame.getContentPane().add(selectOutput);
        selectOutput.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				JFileChooser outputFile = new JFileChooser();
				if(outputPath != null)
					outputFile.setSelectedFile(outputPath);
				int action = outputFile.showOpenDialog(null);
				if(action == JFileChooser.APPROVE_OPTION)
				{
					outputPath = outputFile.getSelectedFile();
					String path = outputFile.getSelectedFile().toString();
					outputField.setText(path);
				}
			}
		});
	}

	private void loadTransformers(String path, JLabel displayLabel)
	{
		try
		{
			ZipInputStream zip = new ZipInputStream(
				new FileInputStream(path));
			for(ZipEntry entry = zip.getNextEntry(); entry != null; entry =
				zip.getNextEntry())
				if(!entry.isDirectory() && entry.getName().endsWith(".class"))
				{
					String className = entry.getName().replace('/', '.');
					if(className.startsWith("com.javadeobfuscator.deobfuscator.transformers"))
					{
						System.out.println(className);
					}
				}
			zip.close();
			displayLabel.setText("Successfully loaded transformers!");
			displayLabel.setForeground(Color.GREEN);
		}catch(Exception e)
		{
			e.printStackTrace();
			displayLabel.setText("Failed to load transformers (corrupted jar?)");
			displayLabel.setForeground(Color.red);
		}
	}
}
