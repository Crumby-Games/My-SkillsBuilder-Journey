package com.example.group56.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// Purely informative annotation that conveys that a method returns a Map<String, Object>,
// where object is a collection or map of basic types or nested collections or maps.

// Use clazz.isAnnotationPresent(JsonCompatible.class) to check if a method returns JSON-ready data
@Target(ElementType.METHOD)  // Applied to methods
@Retention(RetentionPolicy.RUNTIME)
public @interface JsonCompatible {
    String value() default "This method returns a value that is reflective of JSON";
}
