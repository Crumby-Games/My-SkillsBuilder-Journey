package com.example.group56.annotation;

import com.example.group56.validation.UniqueFieldValuesValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// Enforces unique fields for a DTO before the entity itself is even added to the database.
// The DTO is temporarily treated as the entityClass and then is validated by checking if all the values for its fieldNames are unique in the database.

@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = UniqueFieldValuesValidator.class) // Specify the validator class
public @interface UniqueFieldValues {
    /* Necessary fields for validator annotations */
    String message() default "{field} is already in database.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};

    // Fields to keep unique
    String[] fieldNames();

    // Allow entity class to be specified, or it will be inferred from the class of the annotated field. In the usage cases of this project, it would infer that it's a DTO.
    Class<?> entityClass() default Object.class;

    // Identifier of self to prevent detecting self as duplicate
    String primaryKeyName() default "id";
}
