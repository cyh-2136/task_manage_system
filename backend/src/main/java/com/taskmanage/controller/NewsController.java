package com.taskmanage.controller;

import com.taskmanage.annotation.RequireRole;
import com.taskmanage.common.Result;
import com.taskmanage.dto.PageResult;
import com.taskmanage.model.dto.NewsDTO;
import com.taskmanage.service.NewsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "实时资讯")
@RestController
@RequestMapping("/api/news")
@RequiredArgsConstructor
public class NewsController {

    private final NewsService newsService;

    @Operation(summary = "获取资讯列表（支持分页和关键词搜索）")
    @RequireRole({"导师", "实习生"})
    @GetMapping
    public Result<PageResult<NewsDTO>> getNews(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = "10") int pageSize) {
        List<NewsDTO> all = newsService.getNews(keyword);
        int total = all.size();
        int from = (page - 1) * pageSize;
        int to = Math.min(from + pageSize, total);
        List<NewsDTO> records = from >= total ? List.of() : all.subList(from, to);
        return Result.success(new PageResult<>(records, total, page, pageSize));
    }

    @Operation(summary = "手动刷新资讯")
    @RequireRole({"导师", "实习生"})
    @GetMapping("/refresh")
    public Result<List<NewsDTO>> refresh() {
        return Result.success(newsService.refresh());
    }

    @Operation(summary = "实时搜索资讯（不缓存，实时抓取RSS）")
    @RequireRole({"导师", "实习生"})
    @GetMapping("/search")
    public Result<List<NewsDTO>> searchRealtime(@RequestParam String keyword) {
        return Result.success(newsService.searchRealtime(keyword));
    }

    @Operation(summary = "按任务关键词搜索百度新闻（实时）")
    @RequireRole({"导师", "实习生"})
    @GetMapping("/search-by-task")
    public Result<List<NewsDTO>> searchByTaskKeyword(@RequestParam String keyword) {
        System.out.println("========== 收到新闻搜索请求, keyword: " + keyword + " ==========");
        return Result.success(newsService.searchByTaskKeyword(keyword));
    }
}
