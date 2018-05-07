package com.javadeobfuscator.deobfuscator.ui;

import org.controlsfx.control.ListSelectionView;
import org.controlsfx.control.Notifications;

import com.javadeobfuscator.deobfuscator.ui.component.ConfigProperties;
import com.javadeobfuscator.deobfuscator.ui.wrap.Deobfuscator;
import com.javadeobfuscator.deobfuscator.ui.wrap.Transformers;
import com.javadeobfuscator.deobfuscator.ui.wrap.WrapperFactory;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.*;
import javafx.util.Duration;

public class FxWindow extends Application {
	public static void main(String[] args) {
		launch(args);
	}

	private Deobfuscator deob;
	private Transformers trans;

	@Override
	public void start(Stage stage) {
		loadWrappers();

		stage.setTitle("Deobfuscator UI!");
		VBox root = new VBox();
		ConfigProperties props = new ConfigProperties(deob.getConfig().get());
		TitledPane wrapper1 = new TitledPane("Configuration options", props);
		root.getChildren().add(wrapper1);
		// listview to display selected transformers
		ListSelectionView<Class<?>> selectedTransformers = new ListSelectionView<>();
		selectedTransformers.setCellFactory(p -> new ListCell<Class<?>>() {
			@Override
			protected void updateItem(Class<?> item, boolean empty) {
				super.updateItem(item, empty);
				if (empty || item == null) {
					setText(null);
				} else {
					String name = item.getName();
					int index = "com.javadeobfuscator.deobfuscator.transformers.".length();
					setText(name.substring(index));
				}
			}
		});
		selectedTransformers.getSourceItems().addAll(trans.getTransformers());
		TitledPane wrapper2 = new TitledPane("Transformers", selectedTransformers);
		root.getChildren().add(wrapper2);
		// button to run the deobfuscator
		Button btnRun = new Button("Run deobfuscator");
		btnRun.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				try {
					deob.getConfig().setTransformers(trans, selectedTransformers.getTargetItems());
					deob.run();
				} catch (Exception e) {
					fatal("Failed execution", e.toString());
				}
			}
		});
		root.getChildren().add(btnRun);
		stage.setScene(new Scene(root, 600, 600));
		stage.show();
	}

	/**
	 * Load wrappers
	 */
	private void loadWrappers() {
		WrapperFactory.setupJarLoader(true);
		deob = WrapperFactory.getDeobfuscator();
		trans = WrapperFactory.getTransformers();
		if (deob == null || trans == null) {
			fatal("Failed to locate Deobfuscator jar",
					"Please ensure that JavaDeobfuscator is located adjacent to this program.");
		}
	}

	/**
	 * Display error message notification.
	 * 
	 * @param title
	 * @param text
	 */
	public static void fatal(String title, String text) {
		System.err.println(text);
		System.exit(0);
		//@formatter:off
		Notifications.create()
			.title("Error: " + title)
			.text(text)
			.hideAfter(Duration.seconds(5)).showError();
		//@formatter:on
	}
}
