package com.example.group56.common;

import com.example.group56.annotation.JsonCompatible;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

import java.lang.reflect.Field;
import java.util.*;

// Custom helper class that provides static methods to aid with converting entities into raw data.
public class EntityUtils {
    // Check whether a class has the entity annotation
    public static boolean isEntity(Class<?> clazz) {
        return clazz.isAnnotationPresent(Entity.class);
    }

    // Shorthand for checking if an object's class is an entity
    public static boolean isEntity(Object object) {
        return object != null && isEntity(object.getClass());
    }

    // Iterates through each field of an entity class to check whether it is that entity's @Id.
    public static Field getPrimaryKey(Class<?> clazz) {
        if (!isEntity(clazz)) return null;

        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(Id.class)) {
                return field;
            }
        }
        return null;
    }

    // Gets the @Id of an object by getting the primary key of the class
    public static Object getPrimaryKeyValue(Object object) {
        if (!isEntity(object)) return null;

        Field field = getPrimaryKey(object.getClass());
        if (field != null) {
            field.setAccessible(true);
            try {
                return field.get(object);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }

        }

        return null;
    }

    // Intelligently converts an entity object into a mapping of raw data, with field names as keys and field values as values. Behaviour is outlined below:
        // All fields attached to the entity are included, including private, static and final ones.
        // Although JSON allows null values, it often leads to undesired behaviour. Therefore, null values are replaced with a sensible alternative of their field type
        // References to other entities are replaced with their primary key
        // Does not currently support smart-behaviour with Map fields.
    @JsonCompatible
    @SuppressWarnings({"unchecked","rawtypes"})
    public static Map<String, Object> getFields(Object entity) {
        if (entity == null) return null;
        Map<String, Object> data = new HashMap<>();
        for (Field field : entity.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            try {
                Object value = field.get(entity);

                if (value == null) {
                    value = getNullValue(field.getType());
                } else if (isEntity(value)) {
                    value = getPrimaryKeyValue(field.get(entity));
                } else if (value instanceof List list) {
                    list.replaceAll(EntityUtils::getPrimaryKeyValue);
                }
                data.put(field.getName(), value);

            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        return data;
    }

    // Returns sensible null alternatives for each field of class
    @JsonCompatible
    public static Map<String, Object> getNullFields(Class<?> entityClass) {
        if (entityClass == null) return null;

        Map<String, Object> data = new HashMap<>();
        for (Field field : entityClass.getDeclaredFields()) {
            data.put(field.getName(), getNullValue(field.getType()));
        }

        return data;
    }

    // Hardcoded null alternatives for classes. If the class is an entity, the null alternative of its primary key is used instead.
    private static Object getNullValue(Class<?> clazz) {
        if (clazz.equals(String.class)) {
            return "";
        } else if (clazz.equals(int.class)) {
            return 0;
        } else if (clazz.equals(boolean.class)) {
            return false;
        } else if (isEntity(clazz)) {
            Field primaryKey = getPrimaryKey(clazz);
            if (primaryKey != null) {
                return getNullValue(primaryKey.getType());
            }
        } else if (Collection.class.isAssignableFrom(clazz)) {
            return Collections.emptyList();
        }
        return null;
    }
}
