package com.taskmanage;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.taskmanage.mapper")
public class TaskManageApplication {

    public static void main(String[] args) {
        SpringApplication.run(TaskManageApplication.class, args);
    }
}
