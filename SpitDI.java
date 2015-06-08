import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.Resource;

@SuppressWarnings("rawtypes")
public class SpitDI {
	private Map<String, Object> container = new HashMap<>();

	public SpitDI bindByName(Class clas, String name, Object instance, boolean... allowBindingOverwrite) {
		container.put(toKey(allowBindingOverwrite, clas, name), instance);
		return this;
	}

	public SpitDI bindByType(Class clas, Object instance, boolean... allowBindingOverwrite) {
		container.put(toKey(allowBindingOverwrite, clas), instance);
		return this;
	}

	public SpitDI bindStatic(Class clas, boolean... allowBindingOverwrite) {
		container.put(toKey(allowBindingOverwrite, clas), null);
		return this;
	}

	public SpitDI inject(Object... instance) {
		if (instance.length > 0 && instance[0] != null)
			bindByType(instance[0].getClass(), instance[0]);
		Class targetClass = null;
		String sourceKey = null;
		try {
			for (Entry<String, Object> target : container.entrySet()) {
				Object targetInstance = target.getValue();
				targetClass = getTypeBinding(target);
				for (Entry<String, Object> source : container.entrySet()) {
					sourceKey = source.getKey();
					if (target.getKey().equals(sourceKey))
						continue;
					Class sourceClass = getTypeBinding(source);
					String sourceName = getNameBinding(source);
					Object sourceInstance = source.getValue();
					if (sourceName.length() == 0)
						injectByType(targetClass, targetInstance, sourceClass, sourceInstance);
					else
						injectByName(targetInstance, targetClass, sourceName, sourceClass, sourceInstance);
				}
			}
		} catch (Exception e) {
			throw new RuntimeException("Error binding '" + sourceKey + "' to '" + targetClass + "'.", e);
		}
		container.clear();
		return this;
	}

	private Set<Field> getAllFields(Class<?> type) {
		Set<Field> fields = new HashSet<>();
		for (Class<?> c = type; c != null; c = c.getSuperclass())
			fields.addAll(Arrays.asList(c.getDeclaredFields()));
		return fields;
	}

	private Field getFieldByName(Class<?> clas, String name) {
		for (Class<?> c = clas; c != null; c = c.getSuperclass()) {
			try {
				return c.getDeclaredField(name);
			} catch (NoSuchFieldException | SecurityException ignore) {
			}
		}
		return null;
	}

	private String getNameBinding(Entry<String, Object> entry) {
		String[] keyParts = entry.getKey().split("\\|");
		return keyParts.length > 1 ? keyParts[1] : "";
	}

	private Class getTypeBinding(Entry<String, Object> entry) throws ClassNotFoundException {
		Object instance = entry.getValue();
		if (instance == null) {
			return Class.forName(entry.getKey().split("\\|")[0]);
		}
		else {
			return instance.getClass();
		}
	}

	private boolean injectByName(Object targetInstance, Class targetClass, String sourceName, Class sourceClass, Object sourceInstance) throws IllegalArgumentException, IllegalAccessException {
		if (sourceName.length() == 0)
			return false;
		Field targetField = getFieldByName(targetClass, sourceName);
		if (targetField == null)
			return false;
		else
			return injectResource(targetInstance, targetField, sourceClass, sourceInstance);
	}

	private boolean injectByType(Class targetClass, Object targetInstance, Class sourceClass, Object sourceInstance) throws IllegalArgumentException, IllegalAccessException {
		boolean atLeastOneSet = false;
		for (Field targetField : getAllFields(targetClass))
			atLeastOneSet |= injectResource(targetInstance, targetField, sourceClass, sourceInstance);
		return atLeastOneSet;
	}

	private boolean injectResource(Object targetInstance, Field targetField, Class sourceClass, Object sourceInstance) throws IllegalArgumentException, IllegalAccessException {
		if (targetField.getAnnotation(Resource.class) == null)
			return false;
		if (!targetField.getType().isAssignableFrom(sourceClass))
			return false;
		targetField.setAccessible(true);
		if (Modifier.isStatic(targetField.getModifiers()))
			targetField.set(null, sourceInstance);
		else
			targetField.set(targetInstance, sourceInstance);
		return true;
	}

	private String toKey(boolean[] allowBindingOverwrite, Class clas, String... name) {
		String key = clas.getName() + "|" + (name.length == 0 ? "" : name[0]);
		if ((allowBindingOverwrite.length == 0 || !allowBindingOverwrite[0]) && container.containsKey(key))
			throw new IllegalArgumentException("Duplicate binding for '" + key + "'.");
		return key;
	}
}