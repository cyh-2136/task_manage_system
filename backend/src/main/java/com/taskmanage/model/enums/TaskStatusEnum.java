package com.taskmanage.model.enums;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public enum TaskStatusEnum {

    TODO("待办"),
    IN_PROGRESS("进行中"),
    DONE("已完成");

    private final String value;

    TaskStatusEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static Set<String> allValues() {
        return Arrays.stream(values()).map(TaskStatusEnum::getValue).collect(Collectors.toSet());
    }

    public static boolean isValid(String value) {
        return allValues().contains(value);
    }
}
