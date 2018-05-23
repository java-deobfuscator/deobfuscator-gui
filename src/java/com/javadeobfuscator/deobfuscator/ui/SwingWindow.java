package com.javadeobfuscator.deobfuscator.ui;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.swing.*;
import javax.swing.border.TitledBorder;

import com.javadeobfuscator.deobfuscator.ui.component.SwingConfiguration;
import com.javadeobfuscator.deobfuscator.ui.component.SwingConfiguration.ConfigItem;
import com.javadeobfuscator.deobfuscator.ui.util.FallbackException;
import com.javadeobfuscator.deobfuscator.ui.util.InvalidJarException;
import com.javadeobfuscator.deobfuscator.ui.wrap.Config;
import com.javadeobfuscator.deobfuscator.ui.wrap.Deobfuscator;
import com.javadeobfuscator.deobfuscator.ui.wrap.Transformers;
import com.javadeobfuscator.deobfuscator.ui.wrap.WrapperFactory;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;

public class SwingWindow
{
	private static Deobfuscator deob;
	private static Transformers trans;
	private static Config config;
	private static List<Class<?>> transformers;
	
	public static void main(String[] args)
	{
		try
		{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}catch(ClassNotFoundException | InstantiationException
			| IllegalAccessException | UnsupportedLookAndFeelException e)
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
		
		//Deobfuscator Input
		GridBagConstraints gbc_IPanel = new GridBagConstraints();
		gbc_IPanel.fill = GridBagConstraints.HORIZONTAL;
		gbc_IPanel.anchor = GridBagConstraints.FIRST_LINE_START;
		gbc_IPanel.insets = new Insets(15, 10, 0, 10);
		gbc_IPanel.gridwidth = 2;
		gbc_IPanel.weightx = 1;
		JPanel inputPnl = new JPanel();
		inputPnl.setBorder(new TitledBorder("Deobfuscator Input"));
		frame.getContentPane().add(inputPnl, gbc_IPanel);
		inputPnl.setLayout(new GridBagLayout());
		
		int gridy = 0;
		for(ConfigItem i : fields)
		{
			if(i.type != SwingConfiguration.ItemType.FILE)
				continue;
			GridBagConstraints gbc_Label = new GridBagConstraints();
			gbc_Label.anchor = GridBagConstraints.PAGE_START;
		    gbc_Label.insets = new Insets(5, 2, 2, 2);
		    gbc_Label.gridx = 0;
		    gbc_Label.gridy = gridy;
		    inputPnl.add(new JLabel(i.getDisplayName() + ":"), gbc_Label);
		    GridBagConstraints gbc_Text = new GridBagConstraints();
		    gbc_Text.insets = new Insets(5, 2, 2, 2);
		    gbc_Text.gridx = 1;
		    gbc_Text.gridy = gridy;
		    gbc_Text.weightx = 1;
		    gbc_Text.fill = GridBagConstraints.HORIZONTAL;
		    JTextField textField = new JTextField();
		    i.component = textField;
		    inputPnl.add(textField, gbc_Text);
		    GridBagConstraints gbc_Select = new GridBagConstraints();
		    gbc_Select.insets = new Insets(5, 7, 2, 2);
		    gbc_Select.gridx = 2;
		    gbc_Select.gridy = gridy;
		    gbc_Select.ipadx = 15;
		    JButton button = new JButton("Select");
		    inputPnl.add(button, gbc_Select);
		    gridy++;
		}
		for(ConfigItem i : fields)
		{
			if(i.type != SwingConfiguration.ItemType.BOOLEAN)
				continue;
		    GridBagConstraints gbc_box = new GridBagConstraints();
		    gbc_box.anchor = GridBagConstraints.FIRST_LINE_START;
		    gbc_box.insets = new Insets(5, 2, 2, 2);
		    gbc_box.gridx = 0;
		    gbc_box.gridy = gridy;
		    JCheckBox checkBox = new JCheckBox(i.getDisplayName());
		    i.component = checkBox;
		    inputPnl.add(checkBox, gbc_box);
		    gridy++;
		}
		
		//Other Options
		GridBagConstraints gbc_OPanel = new GridBagConstraints();
		gbc_OPanel.fill = GridBagConstraints.BOTH;
		gbc_OPanel.anchor = GridBagConstraints.NORTHWEST;
		gbc_OPanel.insets = new Insets(15, 10, 20, 10);
		gbc_OPanel.gridwidth = 2;
		gbc_OPanel.gridy = 1;
		gbc_OPanel.weightx = 1;
		gbc_OPanel.weighty = 1;
		JPanel optionsPnl = new JPanel();
		optionsPnl.setBorder(new TitledBorder("Other Options"));
		optionsPnl.setLayout(new GridBagLayout());
		frame.getContentPane().add(optionsPnl, gbc_OPanel);
		
		//The tabbed pane
		JTabbedPane tabbedPane = new JTabbedPane(SwingConstants.TOP);
		GridBagConstraints gbc_TabbedPane = new GridBagConstraints();
		gbc_TabbedPane.fill = GridBagConstraints.BOTH;
		gbc_TabbedPane.weightx = 1;
		gbc_TabbedPane.weighty = 1;
		optionsPnl.add(tabbedPane, gbc_TabbedPane);
		
		//Transformers
		JPanel transformersPanel = new JPanel();
		transformersPanel.setLayout(new GridBagLayout());
		//First list (available)
		JScrollPane transformerListScroll = new JScrollPane();
		DefaultListModel<String> transformerList = new DefaultListModel<>();
		for(Class<?> clazz : transformers)
			transformerList.addElement(clazz.getName().replace("com.javadeobfuscator.deobfuscator.transformers.", ""));
		JList<String> transformerJList = new JList<>(transformerList);
		transformerJList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		transformerJList.setModel(transformerList);
		transformerListScroll.setViewportView(transformerJList);
		GridBagConstraints gbc_TransformerList = new GridBagConstraints();
		gbc_TransformerList.gridx = 0;
		gbc_TransformerList.gridy = 0;
		gbc_TransformerList.gridheight = 4;
		gbc_TransformerList.anchor = GridBagConstraints.FIRST_LINE_START;
		gbc_TransformerList.fill = GridBagConstraints.BOTH;
		gbc_TransformerList.insets = new Insets(20, 20, 20, 10);
		gbc_TransformerList.weightx = 0.5;
		gbc_TransformerList.weighty = 1;
		transformersPanel.add(transformerListScroll, gbc_TransformerList);
		//Second list (selected)
		JScrollPane transformerSelectedScroll = new JScrollPane();
		DefaultListModel<String> transformerSelected = new DefaultListModel<>();
		JList<String> selectedJList = new JList<>(transformerSelected);
		selectedJList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		selectedJList.setModel(transformerSelected);
		transformerSelectedScroll.setViewportView(selectedJList);
		GridBagConstraints gbc_TransformerSelected = new GridBagConstraints();
		gbc_TransformerSelected.gridy = 0;
		gbc_TransformerSelected.gridx = 2;
		gbc_TransformerSelected.gridheight = 4;
		gbc_TransformerSelected.anchor = GridBagConstraints.LAST_LINE_START;
		gbc_TransformerSelected.fill = GridBagConstraints.BOTH;
		gbc_TransformerSelected.insets = new Insets(20, 10, 20, 20);
		gbc_TransformerSelected.weightx = 0.5;
		gbc_TransformerSelected.weighty = 1;
		transformersPanel.add(transformerSelectedScroll, gbc_TransformerSelected);
		//Buttons
		//4 panels to position buttons correctly
		JPanel panel1 = new JPanel();
		GridBagConstraints gbc_panel1 = new GridBagConstraints();
		gbc_panel1.gridx = 1;
		gbc_panel1.weighty = 0.5;
		transformersPanel.add(panel1, gbc_panel1);
		JPanel panel2 = new JPanel();
		panel2.setLayout(new GridBagLayout());
		GridBagConstraints gbc_panel2 = new GridBagConstraints();
		gbc_panel2.gridx = 1;
		gbc_panel2.weighty = 0.5;
		transformersPanel.add(panel2, gbc_panel2);
		JPanel panel3 = new JPanel();
		panel3.setLayout(new GridBagLayout());
		GridBagConstraints gbc_panel3 = new GridBagConstraints();
		gbc_panel3.gridx = 1;
		gbc_panel3.weighty = 0.5;
		transformersPanel.add(panel3, gbc_panel3);
		JPanel panel4 = new JPanel();
		GridBagConstraints gbc_panel4 = new GridBagConstraints();
		gbc_panel4.gridx = 1;
		gbc_panel4.weighty = 0.5;
		transformersPanel.add(panel4, gbc_panel4);
		JButton add = new JButton(">");
		GridBagConstraints gbc_add = new GridBagConstraints();
		gbc_add.anchor = GridBagConstraints.CENTER;
		gbc_add.insets = new Insets(5, 5, 5, 5);
		gbc_add.ipadx = 10;
		panel2.add(add, gbc_add);
		JButton remove = new JButton("<");
		GridBagConstraints gbc_remove = new GridBagConstraints();
		gbc_remove.anchor = GridBagConstraints.CENTER;
		gbc_remove.insets = new Insets(5, 5, 5, 5);
		gbc_remove.ipadx = 10;
		panel3.add(remove, gbc_remove);
		tabbedPane.addTab("Transformers", transformersPanel);
		
		for(ConfigItem i : fields)
		{
			if(i.type != SwingConfiguration.ItemType.FILELIST)
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
			GridBagConstraints gbl_libraries = new GridBagConstraints();
			gbl_libraries.gridx = 0;
			gbl_libraries.gridy = 0;
			gbl_libraries.gridheight = 4;
			gbl_libraries.anchor = GridBagConstraints.FIRST_LINE_START;
			gbl_libraries.fill = GridBagConstraints.BOTH;
			gbl_libraries.insets = new Insets(20, 20, 20, 20);
			gbl_libraries.weightx = 1;
			gbl_libraries.weighty = 1;
			libPanel.add(libListScroll, gbl_libraries);
			//Buttons
			//4 panels to position buttons correctly
			JPanel libPanel1 = new JPanel();
			GridBagConstraints gbc_libPanel1 = new GridBagConstraints();
			gbc_libPanel1.gridx = 1;
			gbc_libPanel1.weighty = 0.5;
			libPanel.add(libPanel1, gbc_libPanel1);
			JPanel libPanel2 = new JPanel();
			libPanel2.setLayout(new GridBagLayout());
			GridBagConstraints gbc_libPanel2 = new GridBagConstraints();
			gbc_libPanel2.gridx = 1;
			gbc_libPanel2.weighty = 0.5;
			libPanel.add(libPanel2, gbc_libPanel2);
			JPanel libPanel3 = new JPanel();
			libPanel3.setLayout(new GridBagLayout());
			GridBagConstraints gbc_libPanel3 = new GridBagConstraints();
			gbc_libPanel3.gridx = 1;
			gbc_libPanel3.weighty = 0.5;
			libPanel.add(libPanel3, gbc_libPanel3);
			JPanel libPanel4 = new JPanel();
			GridBagConstraints gbc_libPanel4 = new GridBagConstraints();
			gbc_libPanel4.gridx = 1;
			gbc_libPanel4.weighty = 0.5;
			libPanel.add(libPanel4, gbc_libPanel4);
			JButton addLib = new JButton("  Add  ");
			GridBagConstraints gbc_addLib = new GridBagConstraints();
			gbc_addLib.anchor = GridBagConstraints.CENTER;
			gbc_addLib.insets = new Insets(5, 5, 5, 20);
			libPanel2.add(addLib, gbc_addLib);
			JButton removeLib = new JButton("Remove");
			GridBagConstraints gbc_removeLib = new GridBagConstraints();
			gbc_removeLib.anchor = GridBagConstraints.CENTER;
			gbc_removeLib.insets = new Insets(5, 5, 5, 20);
			libPanel3.add(removeLib, gbc_removeLib);
			tabbedPane.addTab(i.getDisplayName(), libPanel);
		}
		
		for(ConfigItem i : fields)
		{
			if(i.type != SwingConfiguration.ItemType.STRINGLIST)
				continue;
			JPanel stringPanel = new JPanel();
			stringPanel.setLayout(new GridBagLayout());
			JScrollPane stringListScroll = new JScrollPane();
			DefaultListModel<String> stringList = new DefaultListModel<>();
			i.component = stringList;
			JList<String> stringJList = new JList<>(stringList);
			stringJList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			stringJList.setModel(stringList);
			stringListScroll.setViewportView(stringJList);
			GridBagConstraints gbl_string = new GridBagConstraints();
			gbl_string.gridx = 0;
			gbl_string.gridy = 0;
			gbl_string.gridheight = 4;
			gbl_string.anchor = GridBagConstraints.FIRST_LINE_START;
			gbl_string.fill = GridBagConstraints.BOTH;
			gbl_string.insets = new Insets(0, 20, 0, 20);
			gbl_string.weightx = 1;
			gbl_string.weighty = 1;
			stringPanel.add(stringListScroll, gbl_string);
			//Buttons
			//4 panels to position buttons correctly
			JPanel stringPanel1 = new JPanel();
			GridBagConstraints gbc_stringPanel1 = new GridBagConstraints();
			gbc_stringPanel1.gridx = 1;
			gbc_stringPanel1.weighty = 0.5;
			stringPanel.add(stringPanel1, gbc_stringPanel1);
			JPanel stringPanel2 = new JPanel();
			stringPanel2.setLayout(new GridBagLayout());
			GridBagConstraints gbc_stringPanel2 = new GridBagConstraints();
			gbc_stringPanel2.gridx = 1;
			gbc_stringPanel2.weighty = 0.5;
			stringPanel.add(stringPanel2, gbc_stringPanel2);
			JPanel stringPanel3 = new JPanel();
			stringPanel3.setLayout(new GridBagLayout());
			GridBagConstraints gbc_stringPanel3 = new GridBagConstraints();
			gbc_stringPanel3.gridx = 1;
			gbc_stringPanel3.weighty = 0.5;
			stringPanel.add(stringPanel3, gbc_stringPanel3);
			JPanel stringPanel4 = new JPanel();
			GridBagConstraints gbc_stringPanel4 = new GridBagConstraints();
			gbc_stringPanel4.gridx = 1;
			gbc_stringPanel4.weighty = 0.5;
			stringPanel.add(stringPanel4, gbc_stringPanel4);
			JButton addString = new JButton("  Add  ");
			GridBagConstraints gbc_addString = new GridBagConstraints();
			gbc_addString.anchor = GridBagConstraints.CENTER;
			gbc_addString.insets = new Insets(5, 5, 5, 20);
			stringPanel2.add(addString, gbc_addString);
			JButton removeString = new JButton("Remove");
			GridBagConstraints gbc_removeString = new GridBagConstraints();
			gbc_removeString.anchor = GridBagConstraints.CENTER;
			gbc_removeString.insets = new Insets(5, 5, 5, 20);
			stringPanel3.add(removeString, gbc_removeString);
			//Text pane
			JTextField textPane = new JTextField();
			GridBagConstraints gbl_text = new GridBagConstraints();
			gbl_text.gridx = 0;
			gbl_text.gridy = 4;
			gbl_text.anchor = GridBagConstraints.FIRST_LINE_START;
			gbl_text.fill = GridBagConstraints.HORIZONTAL;
			gbl_text.insets = new Insets(0, 20, 20, 20);
			gbl_text.weightx = 1;
			stringPanel.add(textPane, gbl_text);
			tabbedPane.addTab(i.getDisplayName(), stringPanel);
		}
		
		//Config and Run buttons
		GridBagConstraints gbl_loadConfig = new GridBagConstraints();
		gbl_loadConfig.gridy = 2;
		gbl_loadConfig.anchor = GridBagConstraints.FIRST_LINE_START;
		gbl_loadConfig.insets = new Insets(0, 20, 20, 10);
		JButton load = new JButton("Load Config");
		frame.getContentPane().add(load, gbl_loadConfig);
		GridBagConstraints gbl_saveConfig = new GridBagConstraints();
		gbl_saveConfig.gridx = 1;
		gbl_saveConfig.gridy = 2;
		gbl_saveConfig.anchor = GridBagConstraints.FIRST_LINE_START;
		gbl_saveConfig.insets = new Insets(0, 10, 20, 20);
		JButton save = new JButton("Save Config");
		frame.getContentPane().add(save, gbl_saveConfig);
		GridBagConstraints gbl_run = new GridBagConstraints();
		gbl_run.anchor = GridBagConstraints.FIRST_LINE_END;
		gbl_run.gridx = 1;
		gbl_run.gridy = 2;
		gbl_run.ipadx = 15;
		gbl_run.insets = new Insets(0, 10, 20, 20);
		JButton run = new JButton("Run");
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
		}catch(FallbackException e)
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
			if(!file.exists())
				throw new FallbackException("Loading error", "Path specified does not exist.");
			try
			{
				WrapperFactory.setupJarLoader(file);
			}catch(IOException e)
			{
				throw new FallbackException("Loading error", "IOException while reading file.");
			}catch(InvalidJarException e)
			{
				throw new FallbackException("Loading error", "Invaild JAR selected. Note that old versions of deobfuscator are not supported!");
			}
			deob = WrapperFactory.getDeobfuscator();
			trans = WrapperFactory.getTransformers();
			config = deob.getConfig();
			transformers = trans.getTransformers();
		}catch(FallbackException e)
		{
			config = null;
			transformers = null;
			fallbackLoad(e.path);
		}
	}
}
