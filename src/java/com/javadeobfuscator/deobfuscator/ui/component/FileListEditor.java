package com.javadeobfuscator.deobfuscator.ui.component;

import java.io.File;
import java.util.List;

import org.controlsfx.control.PropertySheet.Item;
import com.javadeobfuscator.deobfuscator.ui.component.ConfigProperties.CustomEditor;

import javafx.collections.ListChangeListener;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * PropertySheet editor for List<File> fields.
 */
public class FileListEditor extends CustomEditor<List<File>> {
	private static final String DEFAULT_TEXT_DISPLAY = "Select files...";
	private static final String DEFAULT_TEXT_WINDOW = "Select file...";
	private final FileChooser chooser = new FileChooser();
	private final ListView<File> view = new ListView<>();
	private Stage stage;

	public FileListEditor(Item item) {
		super(item);
		// initial location is current directory
		chooser.setInitialDirectory(new File(System.getProperty("user.dir")));
	}

	@Override
	public Node getEditor() {
		Button selector = new Button(DEFAULT_TEXT_DISPLAY);
		selector.setOnAction(e -> showEditorWindow());
		return selector;
	}

	public void showEditorWindow() {
		if (stage != null) {
			stage.show();
			stage.toFront();
			return;
		}
		BorderPane bp = new BorderPane();
		stage = new Stage();
		stage.setScene(new Scene(bp));
		stage.setOnCloseRequest(e -> stage.hide());
		stage.initStyle(StageStyle.UTILITY);
		// file view
		view.setCellFactory(p -> new ListCell<File>() {
			@Override
			protected void updateItem(File item, boolean empty) {
				super.updateItem(item, empty);
				if (empty || item == null) {
					setText(null);
				} else {
					setText(item.getName());
				}
			}
		});
		// allow deletion via 'delete' key
		view.setOnKeyPressed(e -> {
			File selected = view.getSelectionModel().getSelectedItem();
			if (selected != null) {
				if (e.getCode().equals(KeyCode.DELETE)) {
					view.getItems().remove(selected);
				}
			}
		});
		// update field value when listview updates.
		view.getItems().addListener((ListChangeListener<File>) (c -> {
			setValue(view.getItems());
		}));
		bp.setCenter(view);
		// file adder
		Button selector = new Button(DEFAULT_TEXT_WINDOW);
		selector.setOnAction(e -> {
			File value = chooser.showOpenDialog(null);
			if (value != null) {
				view.getItems().add(value);
			}
		});
		bp.setBottom(selector);
		stage.show();
	}
}
