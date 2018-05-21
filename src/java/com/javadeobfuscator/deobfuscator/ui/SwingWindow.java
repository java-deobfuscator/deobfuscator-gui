package com.javadeobfuscator.deobfuscator.ui;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.swing.*;
import javax.swing.border.TitledBorder;

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
		//Initial frame
		JFrame frame = new JFrame();
		frame.setTitle("Deobfuscator GUI");
		frame.setBounds(100, 100, 580, 560);
		frame.getContentPane().setLayout(new GridBagLayout());
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		
		//Deobfuscator Input
		GridBagConstraints gbc_IPanel = new GridBagConstraints();
		gbc_IPanel.fill = GridBagConstraints.HORIZONTAL;
		gbc_IPanel.anchor = GridBagConstraints.FIRST_LINE_START;
		gbc_IPanel.insets = new Insets(15, 10, 0, 10);
		gbc_IPanel.weightx = 1;
		JPanel inputPnl = new JPanel();
		inputPnl.setBorder(new TitledBorder("Deobfuscator Input"));
		frame.getContentPane().add(inputPnl, gbc_IPanel);
		inputPnl.setLayout(new GridBagLayout());
		//TODO Options are loaded below
		int gridy = 0;
		for(gridy = 0; gridy < 2; gridy++)
		{
			GridBagConstraints gbc_Label = new GridBagConstraints();
			gbc_Label.anchor = GridBagConstraints.PAGE_START;
		    gbc_Label.insets = new Insets(5, 2, 2, 2);
		    gbc_Label.gridx = 0;
		    gbc_Label.gridy = gridy;
		    inputPnl.add(new JLabel(gridy == 0 ? "Input:" : "Output:"), gbc_Label);
		    GridBagConstraints gbc_Text = new GridBagConstraints();
		    gbc_Text.insets = new Insets(5, 2, 2, 2);
		    gbc_Text.gridx = 1;
		    gbc_Text.gridy = gridy;
		    gbc_Text.weightx = 1;
		    gbc_Text.fill = GridBagConstraints.HORIZONTAL;
		    inputPnl.add(new JTextField(), gbc_Text);
		    GridBagConstraints gbc_Select = new GridBagConstraints();
		    gbc_Select.insets = new Insets(5, 7, 2, 2);
		    gbc_Select.gridx = 2;
		    gbc_Select.gridy = gridy;
		    gbc_Select.ipadx = 15;
		    JButton button = new JButton("Select");
		    inputPnl.add(button, gbc_Select);
		}
		for(gridy = 2; gridy < 4; gridy++)
		{
		    GridBagConstraints gbc_box = new GridBagConstraints();
		    gbc_box.anchor = GridBagConstraints.FIRST_LINE_START;
		    gbc_box.insets = new Insets(5, 2, 2, 2);
		    gbc_box.gridx = 0;
		    gbc_box.gridy = gridy;
		    inputPnl.add(new JCheckBox(gridy == 2 ? "Verify" : "Detect"), gbc_box);
		}
		
		//Other Options
		GridBagConstraints gbc_OPanel = new GridBagConstraints();
		gbc_OPanel.fill = GridBagConstraints.BOTH;
		gbc_OPanel.anchor = GridBagConstraints.NORTHWEST;
		gbc_OPanel.insets = new Insets(15, 10, 20, 10);
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
		gbc_TransformerList.anchor = GridBagConstraints.FIRST_LINE_START;
		gbc_TransformerList.fill = GridBagConstraints.BOTH;
		gbc_TransformerList.insets = new Insets(20, 20, 20, 10);
		gbc_TransformerList.weightx = 0.5;
		gbc_TransformerList.weighty = 1;
		transformersPanel.add(transformerListScroll, gbc_TransformerList);
		//Buttons
		JButton add = new JButton(">");
		GridBagConstraints gbc_add = new GridBagConstraints();
		gbc_add.gridx = 1;
		gbc_add.ipadx = 10;
		transformersPanel.add(add, gbc_add);
		JButton remove = new JButton("<");
		GridBagConstraints gbc_remove = new GridBagConstraints();
		gbc_remove.gridx = 1;
		gbc_remove.ipadx = 10;
		transformersPanel.add(remove, gbc_remove);
		//Second list (selected)
		JScrollPane transformerSelectedScroll = new JScrollPane();
		DefaultListModel<String> transformerSelected = new DefaultListModel<>();
		JList<String> selectedJList = new JList<>(transformerSelected);
		selectedJList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		selectedJList.setModel(transformerSelected);
		transformerSelectedScroll.setViewportView(selectedJList);
		GridBagConstraints gbc_TransformerSelected = new GridBagConstraints();
		gbc_TransformerSelected.anchor = GridBagConstraints.LAST_LINE_START;
		gbc_TransformerSelected.fill = GridBagConstraints.BOTH;
		gbc_TransformerSelected.insets = new Insets(20, 10, 20, 20);
		gbc_TransformerSelected.gridx = 2;
		gbc_TransformerSelected.weightx = 0.5;
		gbc_TransformerSelected.weighty = 1;
		transformersPanel.add(transformerSelectedScroll, gbc_TransformerSelected);
		tabbedPane.addTab("Transformers", transformersPanel);
		
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
