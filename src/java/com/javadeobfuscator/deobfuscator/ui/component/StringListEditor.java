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
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * PropertySheet editor for List<String> fields.
 */
public class StringListEditor extends CustomEditor<List<String>> {
	private static final String DEFAULT_TEXT = "Enter classes...";
	private final ListView<String> view = new ListView<>();
	private Stage stage;

	public StringListEditor(Item item) {
		super(item);
	}

	@Override
	public Node getEditor() {
		Button selector = new Button(DEFAULT_TEXT);
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
		// string view
		view.setCellFactory(p -> new ListCell<String>() {
			@Override
			protected void updateItem(String item, boolean empty) {
				super.updateItem(item, empty);
				if (empty || item == null) {
					setText(null);
				} else {
					setText(item);
				}
			}
		});
		// allow deletion via 'delete' key
		view.setOnKeyPressed(e -> {
			String selected = view.getSelectionModel().getSelectedItem();
			if (selected != null) {
				if (e.getCode().equals(KeyCode.DELETE)) {
					view.getItems().remove(selected);
				}
			}
		});
		// update field value when listview updates.
		view.getItems().addListener((ListChangeListener<String>) (c -> {
			setValue(view.getItems());
		}));
		bp.setCenter(view);
		// string adder
		TextField txt = new TextField();
		txt.setOnKeyPressed(k -> {
			String st = txt.getText();
			if (st.length() > 0 && k.getCode() == KeyCode.ENTER) {
				view.getItems().add(st);
				txt.setText("");
			}
		});
		bp.setBottom(txt);
		stage.show();
	}

}
