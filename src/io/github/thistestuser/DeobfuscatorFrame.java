package io.github.thistestuser;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

public class DeobfuscatorFrame
{
	private static final String VERSION = "1.0";
	private JFrame frame;
	private JTextField deobfuscatorField;
	private File deobfuscatorPath;
	private JTextField inputField;
	private File inputOutputPath;
	private JTextField outputField;
	private DefaultListModel<String> transformerList;
	private DefaultListModel<String> selectedTransformers;
	private JList<String> transformerJList;
	private JList<String> selectedTransformersJList;
	private DefaultListModel<String> librariesList;
	private File libraryPath;
	
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
		
		// Sets up the "Select Deobfuscator" box
		JLabel selectDeobfuscator = new JLabel("");
		selectDeobfuscator.setFont(new Font("Tahoma", Font.PLAIN, 11));
		selectDeobfuscator.setBounds(10, 11, 544, 48);
		TitledBorder selectTitle = new TitledBorder("Select Deobfuscator");
		selectDeobfuscator.setBorder(selectTitle);
		frame.getContentPane().add(selectDeobfuscator);

		// Sets up the "Deobfuscator Arguments" box
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

		// 2 tabs: Transformers and libraries
		JTabbedPane tabbedPane = new JTabbedPane(SwingConstants.TOP);
		tabbedPane.setBounds(22, 213, 517, 227);
		frame.getContentPane().add(tabbedPane);
		
		JPanel transformers = new JPanel();
		transformers.setLayout(null);
		transformerList = new DefaultListModel<>();
		JScrollPane transformerListScroll = new JScrollPane();
		transformerJList = new JList<>(transformerList);
		transformerJList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		transformerJList.setToolTipText(
			"<html>Here you will select the transformers to run.<br>\r\nIf you see no transformers, you have not loaded deobfuscator.jar<br>\r\nor your jar file is corrupt.</html>");
		transformerJList.setBounds(10, 23, 179, 165);
		transformerJList.setModel(transformerList);
		transformerListScroll.setViewportView(transformerJList);
		transformerListScroll.setBounds(transformerJList.getBounds());
		transformers.add(transformerListScroll);
		
		JPanel libraries = new JPanel();
		libraries.setLayout(null);
		tabbedPane.addTab("Transformers", transformers);
		
		selectedTransformers = new DefaultListModel<>();
		JScrollPane transformerSelectScroll = new JScrollPane();
		selectedTransformersJList = new JList<>(selectedTransformers);
		selectedTransformersJList
			.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		selectedTransformersJList.setToolTipText(
			"<html>Here you will find the list of selected transformers. <br>\r\nThey will be applied in this order. </html>");
		selectedTransformersJList.setBounds(323, 23, 179, 165);
		transformerSelectScroll.setViewportView(selectedTransformersJList);
		transformerSelectScroll
			.setBounds(selectedTransformersJList.getBounds());
		transformers.add(transformerSelectScroll);
		
		JButton addTransformer = new JButton(">");
		addTransformer.setToolTipText("Select the transformer.");
		addTransformer.setBounds(213, 66, 89, 23);
		addTransformer.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				if(!transformerJList.isSelectionEmpty())
					selectedTransformers.add(selectedTransformers.size(),
						transformerJList.getSelectedValue());
			}
		});
		transformers.add(addTransformer);
		
		JButton removeTransformer = new JButton("<");
		removeTransformer.setToolTipText("Unselect the transformer.");
		removeTransformer.setBounds(213, 114, 89, 23);
		removeTransformer.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				if(!selectedTransformersJList.isSelectionEmpty())
					selectedTransformers
						.remove(selectedTransformersJList.getSelectedIndex());
			}
		});
		transformers.add(removeTransformer);
		
		JLabel lblTransformersAvailable = new JLabel("Transformers Available");
		lblTransformersAvailable.setBounds(10, 6, 133, 14);
		transformers.add(lblTransformersAvailable);
		
		JLabel lblTransformersSelected = new JLabel("Transformers Selected");
		lblTransformersSelected.setBounds(323, 6, 133, 14);
		transformers.add(lblTransformersSelected);
		tabbedPane.addTab("Libraries", libraries);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(10, 11, 386, 177);
		libraries.add(scrollPane);
		
		librariesList = new DefaultListModel<>();
		JList<String> libraryList = new JList<>(librariesList);
		libraryList.setToolTipText(
			"<html>The library list is here.<br>\r\nYou can choose folders or files to add. If you choose a folder,<br>\r\nall the non-folder contents will be added as a library.<br>\r\nKeep in mind that folders in the folder you selected will be ignored,<br>\r\nand all the content inside them will be ignored too.</html>");
		scrollPane.setViewportView(libraryList);
		
		JButton btnAdd = new JButton("Add");
		btnAdd.setToolTipText("Adds a library.");
		btnAdd.setBounds(406, 38, 89, 23);
		libraries.add(btnAdd);
		btnAdd.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				JFileChooser library = new JFileChooser();
				library
					.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
				if(libraryPath != null)
					library.setSelectedFile(libraryPath);
				int action = library.showOpenDialog(null);
				if(action == JFileChooser.APPROVE_OPTION)
				{
					libraryPath = library.getSelectedFile();
					String path = library.getSelectedFile().toString();
					librariesList.addElement(path);
				}
			}
		});
		
		JButton btnDelete = new JButton("Delete");
		btnDelete.setToolTipText("Deletes a library.");
		btnDelete.setBounds(406, 107, 89, 23);
		libraries.add(btnDelete);
		btnDelete.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				if(!libraryList.isSelectionEmpty())
					librariesList.remove(libraryList.getSelectedIndex());
			}
		});
		
		JLabel lblInput = new JLabel("Input:");
		lblInput.setToolTipText(
			"<html>Select your file to deobfuscate here.</html>");
		lblInput.setBounds(26, 126, 46, 14);
		frame.getContentPane().add(lblInput);
		
		JLabel lblOutput = new JLabel("Output:");
		lblOutput.setToolTipText(
			"<html>Select what the deobfuscated file should be named here.</html>");
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
				if(inputOutputPath != null)
					inputFile.setSelectedFile(inputOutputPath);
				int action = inputFile.showOpenDialog(null);
				if(action == JFileChooser.APPROVE_OPTION)
				{
					inputOutputPath = inputFile.getSelectedFile();
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
				if(inputOutputPath != null)
					outputFile.setSelectedFile(inputOutputPath);
				int action = outputFile.showOpenDialog(null);
				if(action == JFileChooser.APPROVE_OPTION)
				{
					inputOutputPath = outputFile.getSelectedFile();
					String path = outputFile.getSelectedFile().toString();
					outputField.setText(path);
				}
			}
		});
		
		JButton btnLoadConfig = new JButton("Load Config");
		btnLoadConfig.setBounds(10, 474, 89, 26);
		frame.getContentPane().add(btnLoadConfig);
		
		JButton btnSaveConfig = new JButton("Save Config");
		btnSaveConfig.setBounds(117, 474, 99, 26);
		frame.getContentPane().add(btnSaveConfig);
		
		JButton btnRun = new JButton("Run");
		btnRun.setBounds(465, 474, 89, 23);
		frame.getContentPane().add(btnRun);
		btnRun.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				// Converts the above into args
				List<String> command = new ArrayList<>();
				command.add("java");
				command.add("-jar");
				command.add(deobfuscatorField.getText());
				command.add("-input");
				command.add(inputField.getText());
				command.add("-output");
				command.add(outputField.getText());
				for(int i = 0; i < selectedTransformers.getSize(); i++)
				{
					command.add("-transformer");
					command.add(selectedTransformers.get(i));
				}
				for(int i = 0; i < librariesList.getSize(); i++)
				{
					command.add("-path");
					command.add(librariesList.get(i));
				}
				// Start
				ProcessBuilder builder = new ProcessBuilder(command);
				JFrame newFrame = new JFrame();
				newFrame.setTitle("Console");
				JTextArea area = new JTextArea();
				newFrame.add(new JScrollPane(area));
				newFrame.pack();
				newFrame.setSize(800, 600);
				newFrame.setVisible(true);
				new SwingWorker<Void, String>()
				{
					@Override
					protected Void doInBackground() throws Exception
					{
						builder.redirectErrorStream(true);
						Process process = builder.start();
						BufferedReader reader = new BufferedReader(
							new InputStreamReader(process.getInputStream()));
						String line;
						while((line = reader.readLine()) != null)
							publish(line);
						return null;
					}
					
					@Override
					protected void process(List<String> chunks)
					{
						for(String line : chunks)
						{
							area.append(line);
							area.append("\n");
						}
					}
				}.execute();
			}
		});
	}
	
	private void loadTransformers(String path, JLabel displayLabel)
	{
		try
		{
			ZipInputStream zip = new ZipInputStream(new FileInputStream(path));
			for(ZipEntry entry = zip.getNextEntry(); entry != null; entry =
				zip.getNextEntry())
				if(!entry.isDirectory() && entry.getName().endsWith(".class"))
				{
					String className = entry.getName().replace('/', '.');
					if(className.startsWith(
						"com.javadeobfuscator.deobfuscator.transformers.")
						&& className.length()
							- className.replace(".", "").length() > 5
						&& !className.contains("$"))
					{
						String name = className.substring(0,
							className.length() - ".class".length());
						String toPut = name.substring(
							"com.javadeobfuscator.deobfuscator.transformers."
								.length());
						if(!transformerList.contains(toPut))
							transformerList.addElement(toPut);
					}
				}
			zip.close();
			displayLabel.setText("Successfully loaded transformers!");
			displayLabel.setForeground(Color.GREEN);
		}catch(Exception e)
		{
			e.printStackTrace();
			displayLabel
				.setText("Failed to load transformers (corrupted jar?)");
			displayLabel.setForeground(Color.red);
		}
	}
}
