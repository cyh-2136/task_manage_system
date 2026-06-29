package com.taskmanage.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class EnumValueValidator implements ConstraintValidator<EnumValue, String> {

    private Set<String> validValues = new HashSet<>();
    private boolean required;

    @Override
    public void initialize(EnumValue annotation) {
        this.required = annotation.required();
        Enum<?>[] enumConstants = annotation.enumClass().getEnumConstants();
        if (enumConstants != null) {
            try {
                Method getValue = annotation.enumClass().getMethod("getValue");
                for (Enum<?> e : enumConstants) {
                    validValues.add((String) getValue.invoke(e));
                }
            } catch (Exception ignored) {
                Arrays.stream(enumConstants).map(Enum::name).forEach(validValues::add);
            }
        }
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return !required;
        }
        return validValues.contains(value);
    }
}
