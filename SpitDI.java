import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Resource;

@SuppressWarnings("rawtypes")
public class SpitDI {
	private Map<String, Object> container = new HashMap<>();

	public SpitDI bindByName(Class clas, String name, Object instance) {
		container.put(toKey(clas, name), instance);
		return this;
	}

	public SpitDI bindByType(Class clas, Object instance) {
		container.put(toKey(clas), instance);
		return this;
	}

	public SpitDI bindStatic(Class clas) {
		container.put(toKey(clas), null);
		return this;
	}

	public SpitDI inject(Object instance) {
		if (instance != null)
			container.put(toKey(instance.getClass()), instance);
		Class targetClass = null;
		String sourceKey = null;
		try {
			for (Entry<String, Object> target : container.entrySet()) {
				Object targetInstance = target.getValue();
				targetClass = getTypeBinding(target);
				for (Entry<String, Object> source : container.entrySet()) {
					sourceKey = source.getKey();
					if (sourceKey == target.getKey())
						continue;
					Class sourceClass = getTypeBinding(source);
					Object sourceInstance = source.getValue();
					String name = getNameBinding(source);
					if (!injectByName(targetInstance, targetClass, name, sourceClass, sourceInstance))
						injectByType(targetClass, targetInstance, sourceClass, sourceInstance);
				}
			}
		} catch (Exception e) {
			throw new RuntimeException("Error binding '" + sourceKey + "' to '" + targetClass + "'.", e);
		}
		return this;
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

	private boolean injectByName(Object targetInstance, Class targetClass, String targetFieldName, Class sourceClass, Object sourceInstance) throws IllegalArgumentException, IllegalAccessException {
		if (targetFieldName.length() == 0)
			return false;
		Field targetField = null;
		try {
			targetField = targetClass.getField(targetFieldName);
		} catch (NoSuchFieldException ignoreInstanceCheck) {
			try {
				targetField = targetClass.getDeclaredField(targetFieldName);
			} catch (NoSuchFieldException | SecurityException ignoreStaticCheck) {
			}
		}
		if (targetField == null)
			return false;
		else
			return injectResource(targetInstance, targetField, sourceClass, sourceInstance);
	}

	private boolean injectByType(Class targetClass, Object targetInstance, Class sourceClass, Object sourceInstance) throws IllegalArgumentException, IllegalAccessException {
		boolean atLeastOneSet = false;
		for (Field targetField : targetClass.getFields())
			atLeastOneSet |= injectResource(targetInstance, targetField, sourceClass, sourceInstance);
		for (Field targetField : targetClass.getDeclaredFields())
			atLeastOneSet |= injectResource(targetInstance, targetField, sourceClass, sourceInstance);
		return atLeastOneSet;
	}

	@SuppressWarnings("unchecked")
	private boolean injectResource(Object targetInstance, Field targetField, Class sourceClass, Object sourceInstance) throws IllegalArgumentException, IllegalAccessException {
		if (targetField.getAnnotation(Resource.class) == null)
			return false;
		if (!sourceClass.isAssignableFrom(targetField.getType()))
			return false;
		targetField.setAccessible(true);
		if (Modifier.isStatic(targetField.getModifiers()))
			targetField.set(null, sourceInstance);
		else
			targetField.set(targetInstance, sourceInstance);
		return true;
	}

	private String toKey(Class clas, String... name) {
		String key = clas.getName() + "|" + (name.length == 0 ? "" : name[0]);
		if (container.containsKey(key))
			throw new IllegalArgumentException("Duplicate binding for '" + key + "'.");
		return key;
	}
}