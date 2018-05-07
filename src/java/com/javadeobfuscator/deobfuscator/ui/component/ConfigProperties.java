package com.javadeobfuscator.deobfuscator.ui.component;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.controlsfx.control.PropertySheet;
import org.controlsfx.property.editor.PropertyEditor;


import com.javadeobfuscator.deobfuscator.ui.util.Reflect;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableValue;

/**
 * Reflection powered PropertySheet. Loads values from an instance.
 */
public class ConfigProperties extends PropertySheet {
	/**
	 * Ignored field names.
	 */
	@SuppressWarnings("serial")
	private final static Set<String> IGNORED_VALUES = new HashSet<String>() {
		{
			add("transformers");
		}
	};

	/**
	 * Create a PropertySheet by parsing fields of a class.
	 * 
	 * @param instances
	 *            Instances of classes to populate the property table.
	 */
	public ConfigProperties(Object... instances) {
		setModeSwitcherVisible(false);
		setSearchBoxVisible(false);
		for (Object instance : instances)
			setupItems(instance);
	}

	/**
	 * Setup items of PropertySheet based on annotated fields in the given {@link #instance}
	 * class.
	 */
	protected void setupItems(Object instance) {
		for (Field field : Reflect.fields(instance.getClass())) {
			// skip ignored values.
			if (IGNORED_VALUES.contains(field.getName())) {
				continue;
			}
			field.setAccessible(true);
			// Setup item & add to list
			getItems().add(new ReflectiveItem(instance, field, "options", field.getName()));
		}
	}

	/**
	 * Reflection-powered PropertySheet item.
	 */
	public static class ReflectiveItem implements Item {
		private final String category, name;
		private final Field field;
		private final Supplier<Object> getter;
		private final Consumer<Object> setter;
		private final Object owner;

		public ReflectiveItem(Object owner, Field field, String categoryKey, String translationKey) {
			this.category = categoryKey;
			this.name = translationKey;
			this.owner = owner;
			this.field = field;
			// TODO: swap out these and instead use a proper observable wrapper so that
			// listeners can be used.
			getter = () -> Reflect.get(owner, field);
			setter = (value) -> Reflect.set(owner, field, value);
		}

		/**
		 * @return Object instance of class that contains the {@link #getField() field}.
		 */
		protected Object getOwner() {
			return owner;
		}

		/**
		 * @return Field this item represents.
		 */
		protected Field getField() {
			return field;
		}

		/**
		 * @return Type of value with generic information included.
		 */
		protected ParameterizedType getGenericType() {
			Type type = field.getGenericType();
			if (type instanceof ParameterizedType) {
				return (ParameterizedType) type;
			}
			return null;
		}

		@Override
		public Class<?> getType() {
			return field.getType();
		}

		@Override
		public String getCategory() {
			return category;
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public String getDescription() {
			return "";
		}

		@Override
		public Object getValue() {
			return getter.get();
		}

		@Override
		public void setValue(Object value) {
			setter.accept(value);
		}

		@Override
		public Optional<ObservableValue<? extends Object>> getObservableValue() {
			// It would be proper to have this be a field getter, and change
			// get/setter methods but it works as-is.
			return optionalObserved(getValue());
		}

		/**
		 * @param clazz 
		 * @return Type of editor for the represented value of this item.
		 */
		protected <T> Class<? extends PropertyEditor<?>> getEditorType() {
			if (getType().equals(File.class)) {
				return FileEditor.class;
			} else if (getType().equals(List.class)) {
				Type[] args =  getGenericType().getActualTypeArguments();
				if (args.length > 0 && args[0].getTypeName().contains("File")) {
					return FileListEditor.class;
				} else {
					return StringListEditor.class;
				}
				
			}
			return null;
		}

		@Override
		public final Optional<Class<? extends PropertyEditor<?>>> getPropertyEditorClass() {
			// Check if there is a custom editor for this item.
			Class<? extends PropertyEditor<?>> type = getEditorType();
			if (type == null) {
				// call default implmentation in Item.
				return Item.super.getPropertyEditorClass();
			}
			return optional(type);
		}

		/**
		 * Wrap some value in an optional & observable value.
		 * 
		 * @param value
		 *            Value to wrap.
		 * @return Wrapped value.
		 */
		public static <T> Optional<ObservableValue<? extends T>> optionalObserved(T value) {
			return optional(observable(value));
		}

		/**
		 * Wrap some value in an observable value.
		 * 
		 * @param value
		 *            Value to wrap.
		 * @return Wrapped value.
		 */
		public static <T> ObservableValue<T> observable(T value) {
			return new ReadOnlyObjectWrapper<>(value);
		}

		/**
		 * Wrap some value in an optional value.
		 * 
		 * @param value
		 *            Value to wrap.
		 * @return Wrapped value.
		 */
		public static <T> Optional<T> optional(T value) {
			return Optional.of(value);
		}
	}

	/**
	 * Custom editor for a reflective item for non-standard property types.
	 *
	 * @param <T>
	 *            Type of value being modified.
	 */
	public static abstract class CustomEditor<T> implements PropertyEditor<T> {
		protected final ReflectiveItem item;

		public CustomEditor(Item item) {
			this.item = (ReflectiveItem) item;
		}

		@SuppressWarnings("unchecked")
		@Override
		public T getValue() {
			return (T) item.getValue();
		}

		@Override
		public void setValue(T value) {
			item.setValue(value);
		}
	}
}