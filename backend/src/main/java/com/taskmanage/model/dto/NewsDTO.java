package com.taskmanage.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NewsDTO {
    private String title;
    private String description;
    private String url;
    private String source;
    private LocalDateTime publishTime;
}
