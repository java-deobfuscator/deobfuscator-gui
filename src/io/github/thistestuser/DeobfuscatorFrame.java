package io.github.thistestuser;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

public class DeobfuscatorFrame
{
	private static final String VERSION = "2.0";
	
	/**
	 * New - Latest API
	 * Legacy - Old API
	 */
	private static DeobfuscatorVersion DEOBFUSCATOR_VERSION = DeobfuscatorVersion.UNKNOWN;
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
	private Thread thread;
	private Process process;
	
	/**
	 * The singleton instance of URLClassLoader
	 */
	private URLClassLoader loader;
	
	/**
	 * A list of transformers in this deobfuscator
	 */
	private List<Class<?>> transformerClasses =  new ArrayList<>();
	
	/**
	 * If its new mode, has Deobfuscator.class, Configuration.class, TransformerConfigDeserializer.class, and Transformer.class
	 */
	private Class<?>[] loadClasses;
	
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
		frame.setResizable(false);
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
		successOrFail.setBounds(203, 70, 181, 14);
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
		transformerJList.addMouseListener(new MouseAdapter() 
		{
			@Override
		    public void mouseClicked(MouseEvent e) 
		    {
				JList<String> list = (JList<String>)e.getSource();
		        if(e.getClickCount() == 2) 
		        {
		            int index = list.locationToIndex(e.getPoint());
		            selectedTransformers.add(selectedTransformers.size(),
						transformerList.getElementAt(index));
		        }
		    }
		});
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
		
		JButton btnDeselectAll = new JButton("Deselect All");
		btnDeselectAll.setBounds(213, 153, 89, 23);
		btnDeselectAll.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				selectedTransformers.clear();
			}
		});
		transformers.add(btnDeselectAll);
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
		btnLoadConfig.setBounds(26, 474, 89, 26);
		frame.getContentPane().add(btnLoadConfig);
		btnLoadConfig.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				JFrame newFrame = new JFrame();
				newFrame.setTitle("Load Config");
				newFrame.setBounds(100, 100, 450, 150);
				newFrame.setResizable(false);
				newFrame.getContentPane().setLayout(null);
				
				JLabel lblPasteYourCommand = new JLabel("<html>Paste your command that you use to run java-deobfuscator here.<br>\r\nThis should be the command you paste via the command line.</html>");
				lblPasteYourCommand.setBounds(10, 11, 379, 34);
				newFrame.getContentPane().add(lblPasteYourCommand);
				
				JTextField command = new JTextField();
				command.setBounds(20, 56, 369, 20);
				command.setColumns(10);
				newFrame.getContentPane().add(command);
				
				JButton btnSubmit = new JButton("Submit");
				btnSubmit.setBounds(157, 77, 89, 23);
				newFrame.getContentPane().add(btnSubmit);
				newFrame.setVisible(true);
				btnSubmit.addActionListener(new ActionListener()
				{
					@Override
					public void actionPerformed(ActionEvent e)
					{
						String args = command.getText();
						Matcher matcher = Pattern.compile("([^\"]\\S*|\".+?\")\\s*").matcher(args);
						List<String> split = new ArrayList<>();
						while(matcher.find())
							split.add(matcher.group(1).replace("\"", ""));
						deobfuscatorField.setText("");
						inputField.setText("");
						outputField.setText("");
						selectedTransformers.clear();
						librariesList.clear();
						for(int i = 0; i < split.size(); i++)
						{
							String arg = split.get(i);
							if(arg.equals("-jar") && split.size() > i + 1)
							{
								deobfuscatorField.setText(split.get(i + 1));
								loadTransformers(split.get(i + 1), successOrFail);
							}else if(arg.equals("-input") && split.size() > i + 1)
								inputField.setText(split.get(i + 1));
							else if(arg.equals("-output") && split.size() > i + 1)
								outputField.setText(split.get(i + 1));
							else if(arg.equals("-transformer") && split.size() > i + 1)
								selectedTransformers.addElement(split.get(i + 1));
							else if(arg.equals("-path") && split.size() > i + 1)
								librariesList.addElement(split.get(i + 1));
						}
						newFrame.dispose();
					}
				});
			}
		});
		
		JButton btnCopyConfig = new JButton("Copy Config");
		btnCopyConfig.setBounds(130, 474, 99, 26);
		frame.getContentPane().add(btnCopyConfig);
		btnCopyConfig.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				JFrame newFrame = new JFrame();
				newFrame.setTitle("Copy Config");
				newFrame.setBounds(100, 100, 450, 200);
				newFrame.setResizable(false);
				newFrame.getContentPane().setLayout(null);
				
				JLabel lblCopyYourCommand = new JLabel("<html>Copy the command below and execute it via\r\nyour command executor to run it.</html>");
				lblCopyYourCommand.setBounds(10, 11, 379, 34);
				newFrame.getContentPane().add(lblCopyYourCommand);
				
				JScrollPane scrollPane = new JScrollPane();
				JTextPane textPane = new JTextPane();
				textPane.setBounds(20, 42, 369, 70);
				textPane.setEditable(false);
				scrollPane.setViewportView(textPane);
				scrollPane.setBounds(textPane.getBounds());
				newFrame.getContentPane().add(scrollPane);
				
				//Write args
				StringBuilder builder = new StringBuilder();
				builder.append("java -jar");
				if(deobfuscatorField.getText().split(" ").length > 1)
					builder.append(" \"" + deobfuscatorField.getText() + "\"");
				else
					builder.append(" " + deobfuscatorField.getText());
				if(inputField.getText().split(" ").length > 1)
					builder.append(" -input " + "\"" + inputField.getText() + "\"");
				else
					builder.append(" -input " + inputField.getText());
				if(outputField.getText().split(" ").length > 1)
					builder.append(" -output " + "\"" + outputField.getText() + "\"");
				else
					builder.append(" -output " + outputField.getText());
				for(Object o : selectedTransformers.toArray())
				{
					String transformer = (String)o;
					builder.append(" -transformer " + transformer);
				}
				for(Object o : librariesList.toArray())
				{
					String library = (String)o;
					if(library.split(" ").length > 1)	
						builder.append(" -path " + "\""  + library + "\"");
					else
						builder.append(" -path " + library);
				}
				textPane.setText(builder.toString());
				
				JButton btnCopy = new JButton("Copy");
				btnCopy.setBounds(170, 127, 89, 23);
				newFrame.getContentPane().add(btnCopy);
				btnCopy.addActionListener(new ActionListener()
				{
					@Override
					public void actionPerformed(ActionEvent e)
					{
						Toolkit.getDefaultToolkit().
							getSystemClipboard().setContents(new StringSelection(textPane.getText()), null);
					}
				});
				newFrame.setVisible(true);
			}
		});
		
		JButton btnRun = new JButton("Run");
		btnRun.setBounds(465, 474, 89, 23);
		frame.getContentPane().add(btnRun);
		btnRun.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				btnRun.setEnabled(false);
				// Start
				JFrame newFrame = new JFrame();
				newFrame.setTitle("Console");
				JTextArea area = new JTextArea();
				area.setEditable(false);
				newFrame.getContentPane().add(new JScrollPane(area));
				newFrame.pack();
				newFrame.setSize(800, 600);
				newFrame.setVisible(true);
				PrintStream print = new PrintStream(new DeobfuscatorOutputStream(area));
				System.setErr(print);
				System.setOut(print);
				// Runs it using reflection
				loadTransformers(deobfuscatorField.getText(), successOrFail);
				if(DEOBFUSCATOR_VERSION == DeobfuscatorVersion.NEW)
				{
					thread = new Thread(new Runnable() 
					{
						@Override
						public void run()
						{
							try
							{
								Object configuration = loadClasses[1].newInstance();
								loadClasses[1].getDeclaredMethod("setInput", File.class).
								invoke(configuration, new File(inputField.getText()));
								loadClasses[1].getDeclaredMethod("setOutput", File.class).
								invoke(configuration, new File(outputField.getText()));
								List<Object> transformers = new ArrayList<>();
								for(Object transformer : selectedTransformers.toArray())
									try
									{
										Class<?> transformerClass = null;
										for(Class<?> clazz : transformerClasses)
											if(clazz.getName().equals("com.javadeobfuscator.deobfuscator.transformers." + transformer))
												transformerClass = clazz;
										if(transformerClass == null)
											throw new ClassNotFoundException();
										Object transformerConfig =
											loadClasses[2].getDeclaredMethod("configFor", Class.class).invoke(
												null, transformerClass.asSubclass(loadClasses[3]));
										transformers.add(transformerConfig);
									}catch(ClassNotFoundException e)
									{
										System.out.println("Could not find transformer " + transformer);
										continue;
									}
								loadClasses[1].getDeclaredMethod("setTransformers", List.class).
								invoke(configuration, transformers);
								List<File> libraries = new ArrayList<>();
								for(Object library : librariesList.toArray())
									libraries.add(new File((String)library));
								loadClasses[1].getDeclaredMethod("setPath", List.class).
								invoke(configuration, libraries);
								Object deobfuscator = 
									loadClasses[0].getDeclaredConstructor(loadClasses[1]).newInstance(configuration);
								try
								{
									loadClasses[0].getDeclaredMethod("start").invoke(deobfuscator);
								}catch(InvocationTargetException e)
								{
									if(e.getTargetException().getClass().getName().
										equals("com.javadeobfuscator.deobfuscator.exceptions.NoClassInPathException"))
									{
										for(int i = 0; i < 5; i++)
							                System.out.println();
							            System.out.println("** DO NOT OPEN AN ISSUE ON GITHUB **");
							            System.out.println("Could not locate a class file.");
							            System.out.println("Have you added the necessary files to the -path argument?");
							            System.out.println("The error was:");
									}else if(e.getTargetException().getClass().getName().
										equals("com.javadeobfuscator.deobfuscator.exceptions.PreventableStackOverflowError"))
									{
										for(int i = 0; i < 5; i++)
							                System.out.println();
							            System.out.println("** DO NOT OPEN AN ISSUE ON GITHUB **");
							            System.out.println("A StackOverflowError occurred during deobfuscation, but it is preventable");
							            System.out.println("Try increasing your stack size using the -Xss flag");
							            System.out.println("The error was:");
									}else
									{
										for(int i = 0; i < 5; i++)
											System.out.println();
										System.out.println("Deobfuscation failed. Please open a ticket on GitHub and provide the following error:");
									}
									e.getTargetException().printStackTrace();
								}
							}catch(Exception e)
							{
								e.printStackTrace();
							}
						}
					});
					thread.setContextClassLoader(loader);
					thread.start();
					newFrame.addWindowListener(new WindowAdapter()
			        {
			            @Override
			            public void windowClosing(WindowEvent e)
			            {
			            	btnRun.setEnabled(true);
			            	if(thread != null)
			            	{
			            		thread.stop();
			            		thread = null;
			            		System.gc();
			            	}
			                e.getWindow().dispose();
			            }
			        });
				}else
				{
					btnRun.setEnabled(false);
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
					SwingWorker<Void, String> worker = new SwingWorker<Void, String>()
					{
						@Override
						protected Void doInBackground() throws Exception
						{
							builder.redirectErrorStream(true);
							Process process = builder.start();
							DeobfuscatorFrame.this.process = process;
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
					};
					worker.execute();
					newFrame.addWindowListener(new WindowAdapter()
			        {
			            @Override
			            public void windowClosing(WindowEvent e)
			            {
			            	btnRun.setEnabled(true);
			            	worker.cancel(true);
			            	if(process != null)
			            	{
			            		process.destroyForcibly();
			            		process = null;
			            	}
			                e.getWindow().dispose();
			            }
			        });
				}
			}
		});
	}
	
	private void loadTransformers(String path, JLabel displayLabel)
	{
		try
		{
			if(loader != null)
				loader.close();
			loader = URLClassLoader.newInstance(new URL[]{new File(path).toURI().toURL()}, DeobfuscatorFrame.class.getClassLoader());
			transformerClasses.clear();
			transformerList.clear();
			DEOBFUSCATOR_VERSION = DeobfuscatorVersion.LEGACY;
			Class<?> transformerClass = loader.loadClass("com.javadeobfuscator.deobfuscator.transformers.Transformer");
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
						Class<?> clazz = loader.loadClass(name);
						if(transformerClass.isAssignableFrom(clazz))
						{
							transformerClasses.add(loader.loadClass(name));
							String toPut = name.substring(
								"com.javadeobfuscator.deobfuscator.transformers."
									.length());
							if(!transformerList.contains(toPut))
								transformerList.addElement(toPut);
						}
					}else if(className.equals("com.javadeobfuscator.deobfuscator.config.Configuration.class"))
					{
						loadClasses = new Class<?>[4];
						loadClasses[0] = loader.loadClass("com.javadeobfuscator.deobfuscator.Deobfuscator");
						loadClasses[1] = loader.loadClass("com.javadeobfuscator.deobfuscator.config.Configuration");
						loadClasses[2] = loader.loadClass("com.javadeobfuscator.deobfuscator.config.TransformerConfig");
						loadClasses[3] = loader.loadClass("com.javadeobfuscator.deobfuscator.transformers.Transformer");
						DEOBFUSCATOR_VERSION = DeobfuscatorVersion.NEW;
					}
				}
			zip.close();
			displayLabel.setText("Successfully loaded transformers!");
			displayLabel.setForeground(Color.GREEN);
		}catch(Exception e)
		{
			e.printStackTrace();
			displayLabel
				.setText("Failed to load transformers!");
			displayLabel.setForeground(Color.red);
		}
	}
	
	private static enum DeobfuscatorVersion
	{
		NEW,
		LEGACY,
		UNKNOWN;
	}
	
	private class DeobfuscatorOutputStream extends OutputStream 
	{
	    private JTextArea console;
	     
	    public DeobfuscatorOutputStream(JTextArea console) 
	    {
	        this.console = console;
	    }
	     
	    @Override
	    public void write(int b) throws IOException 
	    {
	    	console.append(String.valueOf((char)b));
	    	console.setCaretPosition(console.getDocument().getLength());
	    }
	}
}
