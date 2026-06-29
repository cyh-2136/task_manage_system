package com.taskmanage.model.enums;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public enum TaskPriorityEnum {

    HIGH("高"),
    MEDIUM("中"),
    LOW("低");

    private final String value;

    TaskPriorityEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static Set<String> allValues() {
        return Arrays.stream(values()).map(TaskPriorityEnum::getValue).collect(Collectors.toSet());
    }

    public static boolean isValid(String value) {
        return allValues().contains(value);
    }
}
