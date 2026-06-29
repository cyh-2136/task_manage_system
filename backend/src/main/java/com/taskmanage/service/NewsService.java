package com.taskmanage.service;

import com.taskmanage.model.news.dto.NewsDTO;

import java.util.List;

public interface NewsService {
    List<NewsDTO> getNews(String keyword);
    List<NewsDTO> refresh();
    List<NewsDTO> searchRealtime(String keyword);
    List<NewsDTO> searchByTaskKeyword(String keyword);
}
