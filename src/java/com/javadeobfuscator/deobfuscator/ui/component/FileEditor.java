package com.javadeobfuscator.deobfuscator.ui.component;

import java.io.File;

import org.controlsfx.control.PropertySheet.Item;
import com.javadeobfuscator.deobfuscator.ui.component.ConfigProperties.CustomEditor;

import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.stage.FileChooser;

/**
 * PropertySheet editor for file fields.
 */
public class FileEditor extends CustomEditor<File> {
	private static final String DEFAULT_TEXT = "Select file...";
	private final FileChooser chooser = new FileChooser();

	public FileEditor(Item item) {
		super(item);
		// initial location is current directory
		chooser.setInitialDirectory(new File(System.getProperty("user.dir")));
	}

	@Override
	public Node getEditor() {
		Button selector = new Button(DEFAULT_TEXT);
		selector.setOnAction(e -> {
			File value;
			if (item.getName().equals("input")) {
				setValue(value = chooser.showOpenDialog(null));
			} else {
				setValue(value = chooser.showSaveDialog(null));
			}
			if (value != null) {
				selector.setText(value.getName());
			} else {
				selector.setText(DEFAULT_TEXT);
			}
		});
		return selector;
	}
}
