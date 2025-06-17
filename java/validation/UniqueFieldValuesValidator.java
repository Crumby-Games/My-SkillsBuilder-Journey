package com.example.group56.validation;

import com.example.group56.annotation.UniqueFieldValues;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/* Guarantees unique fields among entities of a class, for even DTOs that are not added to the database. Implicit behaviour for @UniqueFieldValues annotation. */

@Component
public class UniqueFieldValuesValidator implements ConstraintValidator<UniqueFieldValues, Object> {
    @Autowired
    private EntityManager entityManager;

    private String[] fieldNames;
    private String primaryKeyName;
    private Class<?> entityClass;
    private String message;

    @Override
    public void initialize(UniqueFieldValues constraintAnnotation) {
        this.fieldNames = constraintAnnotation.fieldNames();
        this.primaryKeyName = constraintAnnotation.primaryKeyName();
        this.entityClass = constraintAnnotation.entityClass().equals(Object.class) ?
                null :
                constraintAnnotation.entityClass();
        this.message = constraintAnnotation.message();
    }

    // Find validation issues
    @Override
    public boolean isValid(Object bean, ConstraintValidatorContext context) {
        if (bean == null || entityClass == null || fieldNames.length == 0) {
            return false;
        }

        Object primaryKey = getFieldValue(bean, primaryKeyName);
        boolean isValid = true;
        for (String fieldName : fieldNames) {
            Object fieldValue = getFieldValue(bean, fieldName);

            if (fieldValue == null) {
                continue; // Skip null values
            }

            // Generate SQL query that finds all entities that would cause a field to not be unique
            String queryStr = "SELECT COUNT(e) FROM " + entityClass.getSimpleName() +
                    " e WHERE e." + fieldName + " = :fieldValue";

            // In the case that a DTO is being modified instead of being created, ignore itself as a duplicate.
            if (primaryKey != null) {
                queryStr += " AND e." + primaryKeyName + " != :primaryKey";
            }

            // Using the SQL query, get all entities that would cause a field to not be unique
            Query query = entityManager.createQuery(queryStr);
            query.setParameter("fieldValue", fieldValue);
            if (primaryKey != null) {
                query.setParameter("primaryKey", primaryKey);
            }

            // If there are any entities that would cause a problem, flag it as a constraint violation.
            long count = (long) query.getSingleResult();
            if (count > 0) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(message.replace("{field}",(Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1))))
                        .addPropertyNode(fieldName)
                        .addConstraintViolation();
                isValid = false;

                // (does not break since all relevant constraint violations should be caught, instead of only the first)
            }
        }

        return isValid;
    }

    // Generates and uses the getter method for a field based on its name. Requires consistent naming conventions.
    private Object getFieldValue(Object bean, String fieldName) {
        try {
            String getterMethod = "get" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
            return bean.getClass().getMethod(getterMethod).invoke(bean);
        } catch (Exception e) {
            return null;
        }
    }


}
