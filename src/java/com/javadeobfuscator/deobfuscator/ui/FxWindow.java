package com.javadeobfuscator.deobfuscator.ui;

import java.io.PrintStream;

import javax.swing.JOptionPane;

import org.controlsfx.control.ListSelectionView;
import org.controlsfx.control.Notifications;

import com.javadeobfuscator.deobfuscator.ui.component.ConfigProperties;
import com.javadeobfuscator.deobfuscator.ui.wrap.Deobfuscator;
import com.javadeobfuscator.deobfuscator.ui.wrap.Transformers;
import com.javadeobfuscator.deobfuscator.ui.wrap.WrapperFactory;

import javafx.application.Application;
import javafx.application.Platform;
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
		stage.setTitle("Deobfuscator GUI");
		VBox root = new VBox();
		ConfigProperties props = new ConfigProperties(deob.getConfig().get());
		TitledPane wrapper1 = new TitledPane("Configuration options", props);
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
		// log
		ListView<String> logging = new ListView<>();
		TitledPane wrapper3 = new TitledPane("Logging", logging);
		int size = 140;
		// wrapper3.setMaxHeight(size);
		logging.setPrefHeight(size);
		logging.setCellFactory(p -> new ListCell<String>() {
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
		// It cant fill to the bottom by itself...
		// This is "good enough"
		stage.heightProperty().addListener(i -> {
			double y = logging.getLayoutY();
			double end = stage.getHeight();
			double diff = (end - y) / 3.4;
			logging.setPrefHeight(diff);
			wrapper3.setPrefHeight(diff);
		});
		PrintStream ps = new PrintStream(System.out, true) {
			@Override
			public void println(String line) {
				if (line.contains(" - ")) {
					line = line.substring(line.indexOf(" - ") + 3);
				}
				String newValue = line;
				// ensure updates are done on the JavaFX thread
				Platform.runLater(() -> {
					logging.getItems().add(newValue);
					int size = logging.getItems().size();
					logging.scrollTo(size - 1);
					if (size > 100) {
						logging.getItems().remove(0);
					}
				});
				super.println(line);
			}
		};
		deob.hookLogging(ps);
		// button to run the deobfuscator
		HBox hbox = new HBox();
		Button btnRun = new Button("Run deobfuscator");
		btnRun.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				logging.getItems().clear();
				new Thread() {
					@Override
					public void run() {
						try {
							deob.getConfig().setTransformers(trans, selectedTransformers.getTargetItems());
							deob.run();
						} catch (Exception e) {
							e.printStackTrace();
							fatalFX("Failed execution", e.toString());
						}
					}
				}.start();
			}
		});
		hbox.getChildren().add(btnRun);
		btnRun.setMaxWidth(Double.MAX_VALUE);
		btnRun.getStyleClass().add("click");
		HBox.setHgrow(btnRun, Priority.ALWAYS);
		root.getChildren().add(wrapper1);
		root.getChildren().add(wrapper2);
		root.getChildren().add(hbox);
		root.getChildren().add(wrapper3);
		Scene scene = new Scene(root, 700, 820);
		scene.getStylesheets().add("style.css");
		stage.setScene(scene);
		stage.show();
	}

	/**
	 * Load wrappers
	 */
	private void loadWrappers() {
		WrapperFactory.setupJarLoader(/* recursive */ false);
		deob = WrapperFactory.getDeobfuscator();
		trans = WrapperFactory.getTransformers();
		if (deob == null || trans == null) {
			fatalSwing("Failed to locate Deobfuscator jar",
					"Please ensure that JavaDeobfuscator is located adjacent to this program.");
		}
	}

	/**
	 * Display error message notification.
	 * 
	 * @param title
	 * @param text
	 */
	public static void fatalSwing(String title, String text) {
		text += "\nEnsure that you have JavaDeobfuscator in the same directory as this tool.";
		// Reverting back to Swing since JavaFX isn't up and running when this is called.
		JOptionPane.showMessageDialog(null, text, title, JOptionPane.ERROR_MESSAGE);
		System.exit(0);
	}

	/**
	 * Display error message notification.
	 * 
	 * @param title
	 * @param text
	 */
	public static void fatalFX(String title, String text) {
		//@formatter:off
		Duration time = Duration.seconds(5);
		Notifications.create()
			.title("Error: " + title)
			.text(text)
			.hideAfter(time).showError();
		//@formatter:on
	}
}
