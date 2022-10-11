package ru.qoqqi.qcraft.util;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class ReflectionUtils {
	
	public static boolean setDeclaredFinalStatic(Class<?> cls, String fieldName, Object newValue) {
		return setDeclaredFinal(cls, null, fieldName, newValue);
	}
	
	public static <T> boolean setDeclaredFinal(Class<T> cls, T instance, String fieldName, Object newValue) {
		try {
			Field field = cls.getDeclaredField(fieldName);
			
			field.setAccessible(true);
			
			Field modifiersField = Field.class.getDeclaredField("modifiers");
			modifiersField.setAccessible(true);
			modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
			
			field.set(instance, newValue);
			
		} catch (Exception e) {
			throw new Error(e);
//			return false;
		}
		
		return true;
	}
	
	public static boolean setFinalStatic(Class<?> cls, String fieldName, Object newValue) {
		return setFinal(cls, null, fieldName, newValue);
	}
	
	public static <T> boolean setFinal(Class<T> cls, T instance, String fieldName, Object newValue) {
		try {
			Field field = cls.getField(fieldName);
			
			field.setAccessible(true);
			
			Field modifiersField = Field.class.getDeclaredField("modifiers");
			modifiersField.setAccessible(true);
			modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
			
			field.set(instance, newValue);
			
		} catch (Exception e) {
			throw new Error(e);
//			return false;
		}
		
		return true;
	}
}
