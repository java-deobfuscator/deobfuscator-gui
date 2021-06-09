package com.javadeobfuscator.deobfuscator.ui;

import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

import javax.swing.*;
import javax.swing.border.TitledBorder;

import com.javadeobfuscator.deobfuscator.ui.component.SwingConfiguration;
import com.javadeobfuscator.deobfuscator.ui.component.SwingConfiguration.ConfigItem;
import com.javadeobfuscator.deobfuscator.ui.component.SwingConfiguration.ItemType;
import com.javadeobfuscator.deobfuscator.ui.component.SynchronousJFXCaller;
import com.javadeobfuscator.deobfuscator.ui.component.SynchronousJFXFileChooser;
import com.javadeobfuscator.deobfuscator.ui.component.WrapLayout;
import com.javadeobfuscator.deobfuscator.ui.util.FallbackException;
import com.javadeobfuscator.deobfuscator.ui.util.InvalidJarException;
import com.javadeobfuscator.deobfuscator.ui.util.TransformerConfigUtil;
import com.javadeobfuscator.deobfuscator.ui.wrap.Config;
import com.javadeobfuscator.deobfuscator.ui.wrap.Deobfuscator;
import com.javadeobfuscator.deobfuscator.ui.wrap.Transformers;
import com.javadeobfuscator.deobfuscator.ui.wrap.WrapperFactory;
import javafx.stage.FileChooser;

public class SwingWindow
{

	private static Deobfuscator deob;
	public static Transformers trans;
	private static Config config;
	private static List<Class<?>> transformers;
	private static JCheckBoxMenuItem shouldLimitLines;
	private static final Map<Class<?>, String> TRANSFORMER_TO_NAME = new HashMap<>();
	private static final Map<String, Class<?>> NAME_TO_TRANSFORMER = new HashMap<>();

	public static void main(String[] args)
	{
		SynchronousJFXCaller.init();
		try
		{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e)
		{
			e.printStackTrace();
		}
		loadWrappers();
		List<ConfigItem> fields = new SwingConfiguration(config.get()).fieldsList;
		//Initial frame
		JFrame frame = new JFrame();
		frame.setTitle("Deobfuscator GUI");
		frame.setBounds(100, 100, 580, 650);
		frame.getContentPane().setLayout(new GridBagLayout());
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

		//Menu
		JMenuBar menuBar = new JMenuBar();
		JMenu menu = new JMenu("Options");
		menuBar.add(menu);
		shouldLimitLines = new JCheckBoxMenuItem("Limit Console Lines");
		menu.add(shouldLimitLines);
		frame.setJMenuBar(menuBar);

		//Deobfuscator Input
		JPanel inputPnl = new JPanel();
		{
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbc.anchor = GridBagConstraints.WEST;
			gbc.insets = new Insets(15, 10, 0, 10);
			gbc.gridwidth = 2;
			gbc.weightx = 1;
			frame.getContentPane().add(inputPnl, gbc);
		}
		inputPnl.setBorder(new TitledBorder("Deobfuscator Input"));
		inputPnl.setLayout(new GridBagLayout());

		int gridy = 0;
		for (ConfigItem i : fields)
		{
			if (i.type != SwingConfiguration.ItemType.FILE)
				continue;
			JLabel label = new JLabel(i.getDisplayName() + ":");
			{
				GridBagConstraints gbc = new GridBagConstraints();
				gbc.anchor = GridBagConstraints.PAGE_START;
				gbc.insets = new Insets(5, 2, 2, 2);
				gbc.gridx = 0;
				gbc.gridy = gridy;
				inputPnl.add(label, gbc);
			}
			JTextField textField = new JTextField();
			i.component = textField;
			{
				GridBagConstraints gbc = new GridBagConstraints();
				gbc.insets = new Insets(5, 2, 2, 2);
				gbc.gridx = 1;
				gbc.gridy = gridy;
				gbc.weightx = 1;
				gbc.fill = GridBagConstraints.HORIZONTAL;
				inputPnl.add(textField, gbc);
			}
			JButton button = new JButton("Select");
			{
				GridBagConstraints gbc = new GridBagConstraints();
				gbc.insets = new Insets(5, 7, 2, 2);
				gbc.gridx = 2;
				gbc.gridy = gridy;
				gbc.ipadx = 15;
				inputPnl.add(button, gbc);
			}
			button.addActionListener(e ->
			{
				File selectedFile = new SynchronousJFXFileChooser(() ->
				{
					FileChooser ch = new FileChooser();
					ch.setTitle("Select " + i.getDisplayName());
					Object value = i.getValue();
					if (value instanceof String && !((String) value).trim().isEmpty())
					{
						File f = new File((String) value);
						if (f.exists())
							ch.setInitialFileName(f.getName());
						if (f.getParentFile().exists())
							ch.setInitialDirectory(f.getParentFile());
					}
					ch.getExtensionFilters().addAll(
							new FileChooser.ExtensionFilter("Jar and Zip files", "*.jar", "*.zip"),
							new FileChooser.ExtensionFilter("Jar files", "*.jar"),
							new FileChooser.ExtensionFilter("Zip files", "*.zip"),
							new FileChooser.ExtensionFilter("All Files", "*.*"));
					return ch;
				}).showOpenDialog();
				if (selectedFile != null)
				{
					String path = selectedFile.toString();
					textField.setText(path);
				}
			});
			gridy++;
		}

		// Boolean options
		JPanel boolWrapPanel = new JPanel(new WrapLayout(FlowLayout.LEFT, 5, 2));
		{
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.anchor = GridBagConstraints.WEST;
			gbc.insets = new Insets(5, 2, 2, 2);
			gbc.gridx = 0;
			gbc.gridy = gridy;
			gbc.gridwidth = GridBagConstraints.REMAINDER;
			inputPnl.add(boolWrapPanel, gbc);
		}

		for (ConfigItem i : fields)
		{
			if (i.type != ItemType.BOOLEAN)
				continue;
			JCheckBox checkBox = new JCheckBox(i.getDisplayName());
			i.component = checkBox;
			boolWrapPanel.add(checkBox);
		}

		//Other Options
		JPanel optionsPnl = new JPanel();
		optionsPnl.setBorder(new TitledBorder("Other Options"));
		optionsPnl.setLayout(new GridBagLayout());
		{
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.fill = GridBagConstraints.BOTH;
			gbc.anchor = GridBagConstraints.WEST;
			gbc.insets = new Insets(15, 10, 20, 10);
			gbc.gridwidth = 2;
			gbc.gridy = 1;
			gbc.weightx = 1;
			gbc.weighty = 1;
			frame.getContentPane().add(optionsPnl, gbc);
		}

		//The tabbed pane
		JTabbedPane tabbedPane = new JTabbedPane(SwingConstants.TOP);
		{
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.fill = GridBagConstraints.BOTH;
			gbc.weightx = 1;
			gbc.weighty = 1;
			optionsPnl.add(tabbedPane, gbc);
		}

		//Transformers
		JPanel transformersPanel = new JPanel();
		transformersPanel.setLayout(new GridBagLayout());
		//First list (available)
		JScrollPane transformerListScroll = new JScrollPane();
		DefaultListModel<String> transformerList = new DefaultListModel<>();
		for (Class<?> clazz : transformers)
		{
			transformerList.addElement(toShortName(clazz));
		}
		JList<String> transformerJList = new JList<>(transformerList);
		transformerJList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		transformerJList.setModel(transformerList);
		transformerListScroll.setViewportView(transformerJList);
		{
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.gridx = 0;
			gbc.gridy = 0;
			gbc.gridheight = 4;
			gbc.anchor = GridBagConstraints.WEST;
			gbc.fill = GridBagConstraints.BOTH;
			gbc.insets = new Insets(10, 10, 10, 0);
			gbc.weightx = 0.5;
			gbc.weighty = 1;
			transformersPanel.add(transformerListScroll, gbc);
		}
		//Second list (selected)
		JScrollPane transformerSelectedScroll = new JScrollPane();
		DefaultListModel<TransformerWithConfig> transformerSelected = new DefaultListModel<>();
		JList<TransformerWithConfig> selectedJList = new JList<>(transformerSelected);
		selectedJList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		selectedJList.setModel(transformerSelected);
		transformerSelectedScroll.setViewportView(selectedJList);
		{
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.gridy = 0;
			gbc.gridx = 2;
			gbc.gridheight = 4;
			gbc.anchor = GridBagConstraints.SOUTHEAST;
			gbc.fill = GridBagConstraints.BOTH;
			gbc.insets = new Insets(10, 0, 10, 10);
			gbc.weightx = 0.5;
			gbc.weighty = 1;
			transformersPanel.add(transformerSelectedScroll, gbc);
		}
		transformerJList.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent e)
			{
				if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2)
				{
					JList<String> list = (JList<String>) e.getSource();
					int index = list.locationToIndex(e.getPoint());
					transformerSelected.addElement(new TransformerWithConfig(transformerList.getElementAt(index)));
				}
			}
		});
		selectedJList.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent e)
			{
				if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 2)
				{
					JList<TransformerWithConfig> list = (JList<TransformerWithConfig>) e.getSource();
					int index = list.locationToIndex(e.getPoint());
					transformerSelected.remove(index);
				}
			}

			@Override
			public void mouseReleased(MouseEvent e)
			{
				if (SwingUtilities.isRightMouseButton(e) && e.getClickCount() == 1)
				{
					JList<TransformerWithConfig> list = (JList<TransformerWithConfig>) e.getSource();
					int index = list.locationToIndex(e.getPoint());
					TransformerWithConfig tConfig = transformerSelected.get(index);
					if (tConfig.getConfig() == null)
					{
						Class<?> tClass = NAME_TO_TRANSFORMER.get(tConfig.getShortName());
						Object config = TransformerConfigUtil.getConfig(tClass);
						if (config == null)
						{
							return;
						}
						tConfig.setConfig(config);
					}

					String title = "Options for transformer " + (index + 1) + ": " + tConfig.getShortName();
					JDialog jd = new JDialog(frame, title, Dialog.ModalityType.APPLICATION_MODAL);
					jd.setBounds(100, 200, 450, 200);
					jd.setLocationRelativeTo(frame);
					jd.getContentPane().setLayout(new GridBagLayout());
					jd.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "Close");
					jd.getRootPane().getActionMap().put("Close", new AbstractAction()
					{
						@Override
						public void actionPerformed(ActionEvent e)
						{
							if (jd.isFocused() && jd.isActive())
							{
								jd.dispose();
							}
						}
					});

					TransformerSpecificConfigDialog.fill(jd, tConfig);

					jd.setVisible(true);
				}
			}
		});
		//Buttons
		//4 panels to position buttons correctly
		JPanel panel1 = new JPanel();
		{
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.gridx = 1;
			gbc.weighty = 0.5;
			transformersPanel.add(panel1, gbc);
		}

		JPanel panel2 = new JPanel();
		panel2.setLayout(new GridBagLayout());
		{
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.gridx = 1;
			gbc.weighty = 0.5;
			transformersPanel.add(panel2, gbc);
		}

		JPanel panel3 = new JPanel();
		panel3.setLayout(new GridBagLayout());
		{
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.gridx = 1;
			gbc.weighty = 0.5;
			transformersPanel.add(panel3, gbc);
		}

		JPanel panel4 = new JPanel();
		{
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.gridx = 1;
			gbc.weighty = 0.5;
			transformersPanel.add(panel4, gbc);
		}

		JButton add = new JButton(">");
		Insets margin = add.getMargin();
		add.setMargin(new Insets(margin.top + 30, 2, margin.bottom + 30, 2));
		add.setPreferredSize(new Dimension(30, add.getPreferredSize().height));
		add.addActionListener(e ->
		{
			for (String str : transformerJList.getSelectedValuesList())
			{
				transformerSelected.addElement(new TransformerWithConfig(str));
			}
		});
		{
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.anchor = GridBagConstraints.CENTER;
			gbc.insets = new Insets(5, 0, 5, 0);
			gbc.ipadx = 10;
			panel2.add(add, gbc);
		}

		JButton remove = new JButton("<");
		margin = remove.getMargin();
		remove.setMargin(new Insets(margin.top + 30, 2, margin.bottom + 30, 2));
		remove.setPreferredSize(new Dimension(30, remove.getPreferredSize().height));
		remove.addActionListener(e ->
		{
			int[] indexes = selectedJList.getSelectedIndices();
			Arrays.sort(indexes);
			int[] reversed = IntStream.range(0, indexes.length).map(i -> indexes[indexes.length - i - 1])
					.toArray();
			for (int i : reversed)
			{
				transformerSelected.remove(i);
			}
		});
		{
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.anchor = GridBagConstraints.CENTER;
			gbc.insets = new Insets(5, 2, 5, 2);
			gbc.ipadx = 10;
			panel3.add(remove, gbc);
		}

		tabbedPane.addTab("Transformers", transformersPanel);

		for (ConfigItem i : fields)
		{
			if (i.type != ItemType.FILELIST)
				continue;
			JPanel libPanel = new JPanel();
			libPanel.setLayout(new GridBagLayout());
			JScrollPane libListScroll = new JScrollPane();
			DefaultListModel<String> librariesList = new DefaultListModel<>();
			i.component = librariesList;
			JList<String> libJList = new JList<>(librariesList);
			libJList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			libJList.setModel(librariesList);
			libListScroll.setViewportView(libJList);
			{
				GridBagConstraints gbc = new GridBagConstraints();
				gbc.gridx = 0;
				gbc.gridy = 0;
				gbc.gridheight = 4;
				gbc.anchor = GridBagConstraints.WEST;
				gbc.fill = GridBagConstraints.BOTH;
				gbc.insets = new Insets(10, 10, 10, 10);
				gbc.weightx = 1;
				gbc.weighty = 1;
				libPanel.add(libListScroll, gbc);
			}

			libJList.addKeyListener(new KeyAdapter()
			{
				@Override
				public void keyPressed(KeyEvent event)
				{
					if (event.getKeyCode() == KeyEvent.VK_DELETE)
					{
						int[] indexes = libJList.getSelectedIndices();
						Arrays.sort(indexes);
						int[] reversed = IntStream.range(0, indexes.length).map(i -> indexes[indexes.length - i - 1])
								.toArray();
						for (int i : reversed)
						{
							librariesList.remove(i);
						}
					}
				}
			});

			//Buttons
			//4 panels to position buttons correctly
			JPanel paddingPanel1 = new JPanel();
			{
				GridBagConstraints gbc = new GridBagConstraints();
				gbc.gridx = 1;
				gbc.weighty = 0.5;
				libPanel.add(paddingPanel1, gbc);
			}
			JPanel addLibPanel = new JPanel();
			addLibPanel.setLayout(new GridBagLayout());
			{
				GridBagConstraints gbc = new GridBagConstraints();
				gbc.gridx = 1;
				gbc.weighty = 0.5;
				libPanel.add(addLibPanel, gbc);
			}
			JPanel removeLibPanel = new JPanel();
			removeLibPanel.setLayout(new GridBagLayout());
			{
				GridBagConstraints gbc = new GridBagConstraints();
				gbc.gridx = 1;
				gbc.weighty = 0.5;
				libPanel.add(removeLibPanel, gbc);
			}
			JPanel paddingPanel2 = new JPanel();
			{
				GridBagConstraints gbc = new GridBagConstraints();
				gbc.gridx = 1;
				gbc.weighty = 0.5;
				libPanel.add(paddingPanel2, gbc);
			}
			JButton addLib = new JButton("   Add   ");

			addLib.addActionListener(e ->
			{
				List<File> selectedFiles = new SynchronousJFXFileChooser(() ->
				{
					FileChooser ch = new FileChooser();
					ch.setTitle("Select " + i.getDisplayName());
					Object value = i.getValue();
					if (value instanceof List && !((List<?>) value).isEmpty())
					{
						List<String> list = (List<String>) value;
						File f = new File(list.get(list.size() - 1));
						if (f.getParentFile().exists())
							ch.setInitialDirectory(f.getParentFile());
					}
					ch.getExtensionFilters().addAll(
							new FileChooser.ExtensionFilter("Jar and Zip files", "*.jar", "*.zip"),
							new FileChooser.ExtensionFilter("Jar files", "*.jar"),
							new FileChooser.ExtensionFilter("Zip files", "*.zip"),
							new FileChooser.ExtensionFilter("All Files", "*.*"));
					return ch;
				}).showOpenMultipleDialog();
				if (selectedFiles != null)
				{
					for (File selectedFile : selectedFiles)
					{
						String path = selectedFile.getPath();
						librariesList.addElement(path);
					}
				}
			});
			{
				GridBagConstraints gbc = new GridBagConstraints();
				gbc.anchor = GridBagConstraints.CENTER;
				gbc.insets = new Insets(5, 5, 5, 20);
				addLibPanel.add(addLib, gbc);
			}

			JButton removeLib = new JButton("Remove");
			removeLib.addActionListener(e ->
			{
				int[] indexes = libJList.getSelectedIndices();
				Arrays.sort(indexes);
				int[] reversed = IntStream.range(0, indexes.length).map(i1 -> indexes[indexes.length - i1 - 1])
						.toArray();
				for (int i1 : reversed)
				{
					librariesList.remove(i1);
				}
			});
			{
				GridBagConstraints gbc = new GridBagConstraints();
				gbc.anchor = GridBagConstraints.CENTER;
				gbc.insets = new Insets(5, 5, 5, 20);
				removeLibPanel.add(removeLib, gbc);
			}

			tabbedPane.addTab(i.getDisplayName(), libPanel);
		}

		for (ConfigItem i : fields)
		{
			if (i.type != ItemType.STRINGLIST)
				continue;
			JPanel stringPanel = new JPanel();
			stringPanel.setLayout(new GridBagLayout());

			JPanel stringLeftPanel = new JPanel(new GridBagLayout());
			{
				GridBagConstraints gbc = new GridBagConstraints();
				gbc.gridx = 0;
				gbc.gridy = 0;
				gbc.gridheight = GridBagConstraints.REMAINDER;
				gbc.fill = GridBagConstraints.BOTH;
				gbc.insets = new Insets(10, 0, 10, 0);
				gbc.weightx = 1;
				gbc.weighty = 1;
				stringPanel.add(stringLeftPanel, gbc);
			}

			JScrollPane stringListScroll = new JScrollPane();
			DefaultListModel<String> stringList = new DefaultListModel<>();
			i.component = stringList;
			JList<String> stringJList = new JList<>(stringList);
			stringJList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			stringJList.setModel(stringList);
			stringListScroll.setViewportView(stringJList);
			{
				GridBagConstraints gbc = new GridBagConstraints();
				gbc.gridx = 0;
				gbc.gridy = 0;
				gbc.gridheight = 4;
				gbc.anchor = GridBagConstraints.WEST;
				gbc.fill = GridBagConstraints.BOTH;
				gbc.insets = new Insets(0, 10, 0, 10);
				gbc.weightx = 1;
				gbc.weighty = 1;
				stringLeftPanel.add(stringListScroll, gbc);
			}

			//Text pane
			JTextField textPane = new JTextField();
			textPane.addKeyListener(new KeyAdapter()
			{

				@Override
				public void keyPressed(KeyEvent event)
				{
					if (event.getKeyCode() == KeyEvent.VK_ENTER && textPane.getText() != null && !textPane.getText().isEmpty())
					{
						stringList.addElement(textPane.getText());
						textPane.setText("");
					}
				}
			});
			{
				GridBagConstraints gbc = new GridBagConstraints();
				gbc.gridx = 0;
				gbc.gridy = 4;
				gbc.anchor = GridBagConstraints.WEST;
				gbc.fill = GridBagConstraints.HORIZONTAL;
				gbc.insets = new Insets(0, 10, 0, 10);
				gbc.weightx = 1;
				stringLeftPanel.add(textPane, gbc);
			}

			stringJList.addKeyListener(new KeyAdapter()
			{

				@Override
				public void keyPressed(KeyEvent event)
				{
					if (event.getKeyCode() == KeyEvent.VK_DELETE)
					{
						int[] indexes = stringJList.getSelectedIndices();
						Arrays.sort(indexes);
						int[] reversed = IntStream.range(0, indexes.length).map(i -> indexes[indexes.length - i - 1])
								.toArray();
						for (int i : reversed)
						{
							stringList.remove(i);
						}
					}
				}
			});

			//Buttons
			//4 panels to position buttons correctly
			JPanel paddingPanel1 = new JPanel();
			{
				GridBagConstraints gbc = new GridBagConstraints();
				gbc.gridx = 1;
				gbc.weighty = 0.5;
				stringPanel.add(paddingPanel1, gbc);
			}
			JPanel addStringPanel = new JPanel();
			addStringPanel.setLayout(new GridBagLayout());
			{
				GridBagConstraints gbc = new GridBagConstraints();
				gbc.gridx = 1;
				gbc.weighty = 0.5;
				stringPanel.add(addStringPanel, gbc);
			}
			JPanel removeStringPanel = new JPanel();
			removeStringPanel.setLayout(new GridBagLayout());
			{
				GridBagConstraints gbc = new GridBagConstraints();
				gbc.gridx = 1;
				gbc.weighty = 0.5;
				stringPanel.add(removeStringPanel, gbc);
			}
			JPanel paddingPanel2 = new JPanel();
			{
				GridBagConstraints gbc = new GridBagConstraints();
				gbc.gridx = 1;
				gbc.weighty = 0.5;
				stringPanel.add(paddingPanel2, gbc);
			}

			JButton addString = new JButton("   Add   ");
			addString.addActionListener(e ->
			{
				if (textPane.getText() != null && !textPane.getText().isEmpty())
				{
					stringList.addElement(textPane.getText());
					textPane.setText("");
				}
			});
			{
				GridBagConstraints gbc = new GridBagConstraints();
				gbc.anchor = GridBagConstraints.CENTER;
				gbc.insets = new Insets(5, 5, 5, 20);
				addStringPanel.add(addString, gbc);
			}

			JButton removeString = new JButton("Remove");
			removeString.addActionListener(e ->
			{
				int[] indexes = stringJList.getSelectedIndices();
				Arrays.sort(indexes);
				int[] reversed = IntStream.range(0, indexes.length).map(i12 -> indexes[indexes.length - i12 - 1])
						.toArray();
				for (int i12 : reversed)
				{
					stringList.remove(i12);
				}
			});
			{
				GridBagConstraints gbc = new GridBagConstraints();
				gbc.anchor = GridBagConstraints.CENTER;
				gbc.insets = new Insets(5, 5, 5, 20);
				removeStringPanel.add(removeString, gbc);
			}

			tabbedPane.addTab(i.getDisplayName(), stringPanel);
		}

		//Config and Run buttons
		JButton load = new JButton("Load Config");
		{
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.gridy = 2;
			gbc.anchor = GridBagConstraints.WEST;
			gbc.insets = new Insets(0, 20, 20, 10);
			frame.getContentPane().add(load, gbc);
		}

		load.addActionListener(e ->
		{
			JDialog newFrame = new JDialog(frame, Dialog.ModalityType.APPLICATION_MODAL);
			newFrame.setTitle("Load Config");
			newFrame.setBounds(100, 200, 450, 200);
			newFrame.setLocationRelativeTo(frame);
			newFrame.setResizable(true);
			newFrame.getContentPane().setLayout(new GridBagLayout());

			JLabel yourConfiguration = new JLabel("Input your configuration below:");
			{
				GridBagConstraints gbc = new GridBagConstraints();
				gbc.anchor = GridBagConstraints.PAGE_START;
				gbc.insets = new Insets(15, 5, 5, 5);
				gbc.gridx = 0;
				gbc.gridy = 0;
				newFrame.getContentPane().add(yourConfiguration, gbc);
			}

			JScrollPane scrollPane = new JScrollPane();
			JTextPane textPane = new JTextPane();
			scrollPane.setViewportView(textPane);
			{
				GridBagConstraints gbc = new GridBagConstraints();
				gbc.insets = new Insets(10, 10, 5, 10);
				gbc.gridx = 0;
				gbc.gridy = 1;
				gbc.weightx = 1;
				gbc.weighty = 1;
				gbc.fill = GridBagConstraints.BOTH;
				newFrame.getContentPane().add(scrollPane, gbc);
			}

			JButton copyButton = new JButton("Submit");
			{
				GridBagConstraints gbc = new GridBagConstraints();
				gbc.insets = new Insets(0, 0, 10, 5);
				gbc.gridx = 0;
				gbc.gridy = 2;
				newFrame.getContentPane().add(copyButton, gbc);
			}
			copyButton.addActionListener(e13 ->
			{
				String args1 = textPane.getText();
				Matcher matcher = Pattern.compile("([^\"]\\S*|\".*?\")\\s*").matcher(args1);
				List<String> split = new ArrayList<>();
				while (matcher.find())
				{
					split.add(matcher.group(1));
				}
				for (ConfigItem i : fields)
				{
					i.clearValue();
				}
				transformerSelected.clear();
				for (int i = 0; i < split.size(); i++)
				{
					String arg = split.get(i);
					for (ConfigItem item : fields)
					{
						if (arg.equals("-" + item.getFieldName()))
						{
							if (item.type == ItemType.BOOLEAN)
								item.setValue(true);
							else if (split.size() > i + 1)
							{
								String value = split.get(i + 1);
								if (value.charAt(0) == '"' && value.charAt(value.length() - 1) == '"')
								{
									value = value.substring(1, value.length() - 1);
								}
								if (item.type == ItemType.FILE)
									item.setValue(value);
								else
									((DefaultListModel<String>) item.component).addElement(value);
							}
							continue;
						}
					}
					if (arg.equals("-transformer") && split.size() > i + 1)
					{
						String value = split.get(i + 1);
						int pos = value.indexOf(":");
						if (pos != -1)
						{
							String transformerClass = value.substring(0, pos);
							if (NAME_TO_TRANSFORMER.containsKey(transformerClass))
							{
								Class<?> clazz = NAME_TO_TRANSFORMER.get(transformerClass);
								String shortenedName = TRANSFORMER_TO_NAME.get(clazz);
								Object cfg = TransformerConfigUtil.getConfig(clazz);
								if (cfg != null)
								{
									Class<?> cfgClazz = cfg.getClass();
									try
									{
										String cfgStr = value.substring(pos + 1);
										int start = 0;
										boolean inQuotes = false;
										List<String> opts = new ArrayList<>();
										for (int current = 0; current < cfgStr.length(); current++)
										{
											if (cfgStr.charAt(current) == '"')
											{
												inQuotes = !inQuotes;
											} else if (cfgStr.charAt(current) == ':' && !inQuotes)
											{
												String str = cfgStr.substring(start, current);
												opts.add(str);
												start = current + 1;
											}
										}
										opts.add(cfgStr.substring(start));
										for (String opt : opts)
										{
											String[] optSplit = opt.split("=", 2);
											if (optSplit.length != 2)
											{
												System.out.println("Transformer config option without value: " + opt);
												continue;
											}
											String key = optSplit[0];
											String sval = optSplit[1];
											if (sval.charAt(0) == '"' && sval.charAt(sval.length() - 1) == '"')
											{
												sval = sval.substring(1, sval.length() - 1);
											}
											Field field = TransformerConfigUtil.getTransformerConfigFieldWithSuperclass(cfgClazz, key);
											if (field == null)
											{
												System.out.println("Unknown transformer config option " + key);
												continue;
											}
											Class<?> fType = field.getType();
											field.setAccessible(true);
											try
											{
												Object oval = TransformerConfigUtil.convertToObj(fType, sval);
												if (oval == null)
												{
													System.out.println("GUI does not support config type " + fType + ", option name: " + key + " in " +
																	   shortenedName);
													continue;
												}
												field.set(cfg, oval);
											} catch (NumberFormatException ex)
											{
												System.out.println("Could not convert " + sval + " to " + fType + ", option name: " + key + " in " +
																   shortenedName);
												ex.printStackTrace();
											}
										}
									} catch (ReflectiveOperationException ex)
									{
										ex.printStackTrace();
									}
								}
								transformerSelected.addElement(new TransformerWithConfig(shortenedName, cfg));
							}
						} else
						{
							if (NAME_TO_TRANSFORMER.containsKey(value))
							{
								String shortenedName = TRANSFORMER_TO_NAME.get(NAME_TO_TRANSFORMER.get(value));
								transformerSelected.addElement(new TransformerWithConfig(shortenedName));
							}
						}
					}
				}
				newFrame.dispose();
			});
			newFrame.setVisible(true);
		});

		JButton save = new JButton("Save Config");
		{
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.gridx = 1;
			gbc.gridy = 2;
			gbc.anchor = GridBagConstraints.WEST;
			gbc.insets = new Insets(0, 10, 20, 20);
			frame.getContentPane().add(save, gbc);
		}

		save.addActionListener(e ->
		{
			JDialog newFrame = new JDialog(frame, Dialog.ModalityType.APPLICATION_MODAL);
			newFrame.setTitle("Save Config");
			newFrame.setBounds(100, 200, 450, 200);
			newFrame.setLocationRelativeTo(frame);
			newFrame.setResizable(true);
			newFrame.getContentPane().setLayout(new GridBagLayout());

			JLabel yourConfiguration = new JLabel("Your current configuration is below.");
			GridBagConstraints gbc_yourConfiguration = new GridBagConstraints();
			gbc_yourConfiguration.anchor = GridBagConstraints.PAGE_START;
			gbc_yourConfiguration.insets = new Insets(15, 5, 5, 5);
			gbc_yourConfiguration.gridx = 0;
			gbc_yourConfiguration.gridy = 0;
			newFrame.getContentPane().add(yourConfiguration, gbc_yourConfiguration);

			JScrollPane scrollPane = new JScrollPane();
			JTextPane textPane = new JTextPane();
			textPane.setEditable(false);
			textPane.setToolTipText(
					"Tip: If you copy this and paste it in the \"Load Config\" box, it will automatically input your configuration.");
			scrollPane.setViewportView(textPane);
			GridBagConstraints gbc_scrollPane = new GridBagConstraints();
			gbc_scrollPane.insets = new Insets(10, 10, 5, 10);
			gbc_scrollPane.gridx = 0;
			gbc_scrollPane.gridy = 1;
			gbc_scrollPane.weightx = 1;
			gbc_scrollPane.weighty = 1;
			gbc_scrollPane.fill = GridBagConstraints.BOTH;
			newFrame.getContentPane().add(scrollPane, gbc_scrollPane);

			//Write args
			StringBuilder builder = new StringBuilder();
			builder.append("java -jar deobfuscator.jar");
			for (ConfigItem i : fields)
			{
				if (i.type != ItemType.FILE)
					continue;
				if (((String) i.getValue()).split(" ").length > 1)
					builder.append(" -").append(i.getFieldName()).append(" ").append("\"").append(i.getValue()).append("\"");
				else if (!((String) i.getValue()).isEmpty())
					builder.append(" -").append(i.getFieldName()).append(" ").append(i.getValue());
				else
					builder.append(" -").append(i.getFieldName()).append(" \"\"");
			}
			for (Object o : transformerSelected.toArray())
			{
				TransformerWithConfig transformer = (TransformerWithConfig) o;
				builder.append(" -transformer ").append(transformer.toExportString());
			}
			for (ConfigItem i : fields)
			{
				if (i.type != ItemType.FILELIST && i.type != ItemType.STRINGLIST)
					continue;
				for (Object o : (List<?>) i.getValue())
				{
					if (((String) o).split(" ").length > 1)
						builder.append(" -").append(i.getFieldName()).append(" ").append("\"").append(o).append("\"");
					else if (!((String) o).isEmpty())
						builder.append(" -").append(i.getFieldName()).append(" ").append(o);
					else
						builder.append(" -").append(i.getFieldName()).append(" \"\"");
				}
			}
			for (ConfigItem i : fields)
			{
				if (i.type != ItemType.BOOLEAN)
					continue;
				if ((Boolean) i.getValue())
					builder.append(" -").append(i.getFieldName());
			}
			textPane.setText(builder.toString());

			JButton copyButton = new JButton("Copy");
			GridBagConstraints gbc_copyButton = new GridBagConstraints();
			gbc_copyButton.insets = new Insets(0, 0, 10, 5);
			gbc_copyButton.gridx = 0;
			gbc_copyButton.gridy = 2;
			newFrame.getContentPane().add(copyButton, gbc_copyButton);
			copyButton.addActionListener(e12 -> Toolkit.getDefaultToolkit().
					getSystemClipboard().setContents(new StringSelection(textPane.getText()), null));
			newFrame.setVisible(true);
		});

		GridBagConstraints gbl_run = new GridBagConstraints();
		gbl_run.anchor = GridBagConstraints.SOUTHEAST;
		gbl_run.gridx = 1;
		gbl_run.gridy = 2;
		gbl_run.ipadx = 15;
		gbl_run.insets = new Insets(0, 10, 20, 20);
		JButton run = new JButton("Run");

		JTextArea area = new JTextArea();
		PrintStream print = new PrintStream(new DeobfuscatorOutputStream(System.out, area));
		System.setErr(print);
		System.setOut(print);
		deob.hookLogging(print);

		run.addActionListener(e ->
		{
			run.setEnabled(false);
			// Start
			JFrame newFrame = new JFrame();
			newFrame.setTitle("Console");
			area.setEditable(false);
			newFrame.getContentPane().add(new JScrollPane(area));
			newFrame.pack();
			newFrame.setSize(800, 600);
			newFrame.setVisible(true);
			Thread thread = new Thread(() ->
			{
				try
				{
					//Set fields
					for (ConfigItem item : fields)
					{
						item.clearFieldValue();
						item.setFieldValue();
					}
					List<Object> transformerConfigs = new ArrayList<>();
					for (Object o : transformerSelected.toArray())
					{
						TransformerWithConfig transformerWithConfig = (TransformerWithConfig) o;
						if (transformerWithConfig.getConfig() == null)
						{
							transformerConfigs.add(trans.getConfigFor(NAME_TO_TRANSFORMER.get(transformerWithConfig.getShortName())));
						} else
						{
							transformerConfigs.add(transformerWithConfig.getConfig());
						}
					}
					deob.getConfig().setTransformers(trans, transformerConfigs);
					try
					{
						deob.run();
					} catch (InvocationTargetException e1)
					{
						if (e1.getTargetException().getClass().getName().
								equals("com.javadeobfuscator.deobfuscator.exceptions.NoClassInPathException"))
						{
							for (int i = 0; i < 5; i++)
							{
								System.out.println();
							}
							System.out.println("** DO NOT OPEN AN ISSUE ON GITHUB **");
							System.out.println("Could not locate a class file.");
							System.out.println("Have you added the necessary files to the -libraries argument?");
							System.out.println("The error was:");
						} else if (e1.getTargetException().getClass().getName().
								equals("com.javadeobfuscator.deobfuscator.exceptions.PreventableStackOverflowError"))
						{
							for (int i = 0; i < 5; i++)
							{
								System.out.println();
							}
							System.out.println("** DO NOT OPEN AN ISSUE ON GITHUB **");
							System.out.println("A StackOverflowError occurred during deobfuscation, but it is preventable");
							System.out.println("Try increasing your stack size using the -Xss flag");
							System.out.println("The error was:");
						} else
						{
							for (int i = 0; i < 5; i++)
							{
								System.out.println();
							}
							System.out.println("Deobfuscation failed. Please open a ticket on GitHub and provide the following error:");
						}
						e1.getTargetException().printStackTrace();
					}
				} catch (Throwable e1)
				{
					JFrame newFrame1 = new JFrame();
					newFrame1.setTitle("Error");
					newFrame1.setBounds(100, 100, 500, 400);
					newFrame1.setResizable(true);
					newFrame1.getContentPane().setLayout(new GridBagLayout());

					JLabel yourConfiguration = new JLabel("An error occured while running deobfuscator.");
					GridBagConstraints gbc_yourConfiguration = new GridBagConstraints();
					gbc_yourConfiguration.anchor = GridBagConstraints.PAGE_START;
					gbc_yourConfiguration.insets = new Insets(15, 5, 5, 5);
					gbc_yourConfiguration.gridx = 0;
					gbc_yourConfiguration.gridy = 0;
					newFrame1.getContentPane().add(yourConfiguration, gbc_yourConfiguration);

					JScrollPane scrollPane = new JScrollPane();
					JTextPane textPane = new JTextPane();
					textPane.setEditable(false);
					scrollPane.setViewportView(textPane);
					GridBagConstraints gbc_scrollPane = new GridBagConstraints();
					gbc_scrollPane.insets = new Insets(2, 10, 5, 10);
					gbc_scrollPane.gridx = 0;
					gbc_scrollPane.gridy = 1;
					gbc_scrollPane.weightx = 1;
					gbc_scrollPane.weighty = 1;
					gbc_scrollPane.fill = GridBagConstraints.BOTH;
					newFrame1.getContentPane().add(scrollPane, gbc_scrollPane);
					StringWriter stringWriter = new StringWriter();
					PrintWriter writer = new PrintWriter(stringWriter);
					e1.printStackTrace(writer);
					textPane.setText(stringWriter.toString());
					Toolkit toolkit = Toolkit.getDefaultToolkit();
					Dimension screenSize = toolkit.getScreenSize();
					newFrame1.setLocation((screenSize.width - newFrame1.getWidth()) / 2, (screenSize.height - newFrame1.getHeight()) / 2);
					newFrame1.setVisible(true);
				}
				deob.clearClasses();
			});
			thread.start();
			newFrame.addWindowListener(new WindowAdapter()
			{
				@Override
				public void windowClosing(WindowEvent e)
				{
					print.flush();
					area.setText(null);
					run.setEnabled(true);
					if (thread.isAlive())
					{
						thread.stop();
						deob.clearClasses();
					}
					e.getWindow().dispose();
				}
			});
		});

		frame.getContentPane().add(run, gbl_run);
		frame.setVisible(true);
	}

	private static void loadWrappers()
	{
		WrapperFactory.setupJarLoader(false);
		deob = WrapperFactory.getDeobfuscator();
		trans = WrapperFactory.getTransformers();
		try
		{
			config = deob.getConfig();
			transformers = trans.getTransformers();
			for (Class<?> clazz : transformers)
			{
				TRANSFORMER_TO_NAME.put(clazz, toShortName(clazz));
				NAME_TO_TRANSFORMER.put(toShortName(clazz), clazz);
				NAME_TO_TRANSFORMER.put(toShortNameLegacy(clazz), clazz);
			}
		} catch (FallbackException e)
		{
			config = null;
			transformers = null;
			fallbackLoad(e.path);
		}
	}

	private static void fallbackLoad(String path)
	{
		try
		{
			File file = new File(path);
			if (!file.exists())
				throw new FallbackException("Loading error", "Path specified does not exist.");
			try
			{
				WrapperFactory.setupJarLoader(file);
			} catch (IOException e)
			{
				throw new FallbackException("Loading error", "IOException while reading file.");
			} catch (InvalidJarException e)
			{
				throw new FallbackException("Loading error", "Invaild JAR selected. Note that old versions of deobfuscator are not supported!");
			}
			deob = WrapperFactory.getDeobfuscator();
			trans = WrapperFactory.getTransformers();
			config = deob.getConfig();
			transformers = trans.getTransformers();
			for (Class<?> clazz : transformers)
			{
				TRANSFORMER_TO_NAME.put(clazz, toShortName(clazz));
				NAME_TO_TRANSFORMER.put(toShortName(clazz), clazz);
				NAME_TO_TRANSFORMER.put(toShortNameLegacy(clazz), clazz);
			}
		} catch (FallbackException e)
		{
			config = null;
			transformers = null;
			fallbackLoad(e.path);
		}
	}

	private static String toShortName(Class<?> clazz)
	{
		return clazz.getName().replace("com.javadeobfuscator.deobfuscator.transformers.", "")
				.replace("Transformer", "")
				.replace("general.peephole.", "peephole.")
				.replace("general.removers.", "removers.");
	}

	private static String toShortNameLegacy(Class<?> clazz)
	{
		return clazz.getName().replace("com.javadeobfuscator.deobfuscator.transformers.", "");
	}

	private static class DeobfuscatorOutputStream extends OutputStream
	{

		private final PrintStream sysOut;
		private final JTextArea console;

		public DeobfuscatorOutputStream(PrintStream sysOut, JTextArea console)
		{
			this.console = console;
			this.sysOut = sysOut;
		}

		@Override
		public void write(int b) throws IOException
		{
			sysOut.write(b);
			console.append(String.valueOf((char) b));
			if (shouldLimitLines.isSelected() && console.getLineCount() > 100)
			{
				try
				{
					console.replaceRange("", 0, console.getLineEndOffset(0));
				} catch (Exception e)
				{
					e.printStackTrace(sysOut);
				}
			}
		}
	}
}
