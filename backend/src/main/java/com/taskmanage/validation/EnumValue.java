package com.taskmanage.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = EnumValueValidator.class)
@Documented
public @interface EnumValue {

    String message() default "参数值不在合法枚举范围内";

    Class<? extends Enum<?>> enumClass();

    boolean required() default false;

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
