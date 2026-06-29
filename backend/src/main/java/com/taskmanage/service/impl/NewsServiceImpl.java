package com.taskmanage.service.impl;

import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import com.taskmanage.model.dto.NewsDTO;
import com.taskmanage.service.NewsService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class NewsServiceImpl implements NewsService {

    @Value("${news.rss-url:https://www.oschina.net/news}")
    private String rssUrl;

    private final RestTemplate restTemplate = new RestTemplate();
    private List<NewsDTO> cachedNews = new ArrayList<>();
    private LocalDateTime lastRefresh;

    @PostConstruct
    public void init() {
        refresh();
    }

    @Override
    public List<NewsDTO> getNews(String keyword) {
        List<NewsDTO> source = cachedNews.isEmpty() ? refresh() : cachedNews;
        if (keyword == null || keyword.isBlank()) {
            return source;
        }
        return source.stream()
                .filter(n -> n.getTitle().toLowerCase().contains(keyword.toLowerCase())
                        || n.getDescription().toLowerCase().contains(keyword.toLowerCase()))
                .collect(Collectors.toList());
    }

    @Override
    public List<NewsDTO> refresh() {
        try {
            cachedNews = fetchFromRss();
            lastRefresh = LocalDateTime.now();
            log.info("资讯刷新成功，共 {} 条", cachedNews.size());
        } catch (Exception e) {
            log.warn("RSS抓取失败，使用模拟数据。原因: {}", e.getMessage());
            if (cachedNews.isEmpty()) {
                cachedNews = getMockNews();
            }
        }
        return cachedNews;
    }

    private List<NewsDTO> fetchFromRss() throws Exception {
        SyndFeedInput input = new SyndFeedInput();
        SyndFeed feed = input.build(new XmlReader(URI.create(rssUrl).toURL()));

        List<NewsDTO> newsList = new ArrayList<>();
        for (SyndEntry entry : feed.getEntries()) {
            newsList.add(new NewsDTO(
                    entry.getTitle(),
                    entry.getDescription() != null ? entry.getDescription().getValue() : "",
                    entry.getLink(),
                    feed.getTitle(),
                    entry.getPublishedDate() != null
                            ? LocalDateTime.ofInstant(entry.getPublishedDate().toInstant(), ZoneId.systemDefault())
                            : LocalDateTime.now()
            ));
        }
        return newsList.isEmpty() ? getMockNews() : newsList;
    }

    @Override
    public List<NewsDTO> searchRealtime(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return List.of();
        }
        try {
            SyndFeed feed = new SyndFeedInput().build(new XmlReader(URI.create(rssUrl).toURL()));
            return feed.getEntries().stream()
                    .filter(e -> e.getTitle() != null && e.getTitle().toLowerCase().contains(keyword.toLowerCase())
                            || (e.getDescription() != null && e.getDescription().getValue().toLowerCase().contains(keyword.toLowerCase())))
                    .map(e -> new NewsDTO(
                            e.getTitle(),
                            e.getDescription() != null ? e.getDescription().getValue() : "",
                            e.getLink(),
                            feed.getTitle(),
                            e.getPublishedDate() != null
                                    ? LocalDateTime.ofInstant(e.getPublishedDate().toInstant(), ZoneId.systemDefault())
                                    : LocalDateTime.now()
                    ))
                    .limit(3)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.warn("实时搜索 RSS 失败: {}", e.getMessage());
            return List.of();
        }
    }

    @Override
    public List<NewsDTO> searchByTaskKeyword(String keyword) {
        System.out.println("===== searchByTaskKeyword, keyword: '" + keyword + "' =====");
        if (keyword == null || keyword.isBlank()) return List.of();

        String english = keyword.replaceAll("[^a-zA-Z\\s]", " ").trim().replaceAll("\\s+", " ");

        if (!english.isBlank()) {
            System.out.println("===== 英文关键词: '" + english + "' → HackerNews =====");
            try {
                String url = "https://hn.algolia.com/api/v1/search?query="
                        + java.net.URLEncoder.encode(english, "UTF-8") + "&tags=story&hitsPerPage=8";
                @SuppressWarnings("unchecked")
                Map<String, Object> body = restTemplate.getForObject(url, Map.class);
                if (body != null) {
                    List<Map<String, Object>> hits = (List<Map<String, Object>>) body.get("hits");
                    if (hits != null && !hits.isEmpty()) {
                        List<NewsDTO> results = hits.stream().map(h -> {
                            Object titleObj = h.get("title");
                            String title = titleObj != null ? titleObj.toString() : "";
                            Object authorObj = h.get("author");
                            String author = authorObj != null ? authorObj.toString() : "";
                            Object pointsObj = h.get("points");
                            int points = pointsObj instanceof Number ? ((Number) pointsObj).intValue() : 0;
                            Object urlObj = h.get("url");
                            String articleUrl = urlObj != null ? urlObj.toString() : "";
                            LocalDateTime published = LocalDateTime.now();
                            Object createdAt = h.get("created_at");
                            if (createdAt != null) {
                                try {
                                    published = LocalDateTime.parse(
                                            createdAt.toString().replace("Z", ""),
                                            java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                                } catch (Exception ignored) {}
                            }
                            return new NewsDTO(title, "作者: " + author + " | " + points + " 赞", articleUrl, "HackerNews", published);
                        }).collect(Collectors.toList());
                        System.out.println("===== HackerNews 返回 " + results.size() + " 条 =====");
                        return results;
                    }
                }
            } catch (Exception e) {
                System.out.println("===== HackerNews异常: " + e.getMessage() + " =====");
            }
        }

        System.out.println("===== 降级到中文RSS多源匹配 =====");
        Set<String> tokens = new java.util.HashSet<>();
        for (String t : keyword.toLowerCase().split("\\s+")) {
            if (t.length() >= 2) tokens.add(t);
        }
        for (int i = 0; i < keyword.length(); i++) {
            for (int len = 2; len <= 4 && i + len <= keyword.length(); len++) {
                String sub = keyword.substring(i, i + len).toLowerCase();
                if (sub.matches("[\\u4e00-\\u9fff/\\\\w]+")) tokens.add(sub);
            }
        }
        System.out.println("===== 匹配词: " + tokens + " =====");

        for (String source : List.of(
                "https://www.oschina.net/news/rss",
                "https://36kr.com/feed",
                "https://juejin.cn/rss",
                "https://blog.csdn.net/rss")) {
            try {
                SyndFeed feed = new SyndFeedInput().build(new XmlReader(URI.create(source).toURL()));
                List<NewsDTO> results = feed.getEntries().stream()
                        .map(entry -> {
                            String title = entry.getTitle() != null ? entry.getTitle().toLowerCase() : "";
                            String desc = entry.getDescription() != null ? entry.getDescription().getValue().toLowerCase() : "";
                            int score = 0;
                            for (String t : tokens) {
                                if (title.contains(t)) score += 2;
                                else if (desc.contains(t)) score += 1;
                            }
                            NewsDTO dto = new NewsDTO(
                                    entry.getTitle(),
                                    entry.getDescription() != null ? entry.getDescription().getValue() : "",
                                    entry.getLink(),
                                    feed.getTitle(),
                                    entry.getPublishedDate() != null
                                            ? LocalDateTime.ofInstant(entry.getPublishedDate().toInstant(), ZoneId.systemDefault())
                                            : LocalDateTime.now()
                            );
                            return Map.entry(dto, score);
                        })
                        .filter(e -> e.getValue() > 0)
                        .sorted((a, b) -> Integer.compare(b.getValue(), a.getValue()))
                        .map(Map.Entry::getKey)
                        .limit(8)
                        .collect(Collectors.toList());
                if (!results.isEmpty()) {
                    System.out.println("===== " + source + " 返回 " + results.size() + " 条 =====");
                    return results;
                }
            } catch (Exception e) {
                System.out.println("===== " + source + " 失败: " + e.getMessage() + " =====");
            }
        }
        System.out.println("===== 所有源均无匹配 =====");
        return List.of();
    }

    private List<NewsDTO> getMockNews() {
        List<NewsDTO> mock = new ArrayList<>();
        mock.add(new NewsDTO("Spring Boot 3.3 正式发布，带来全新特性",
                "Spring Boot 3.3 版本带来了多项改进，包括增强的 GraalVM 原生镜像支持、改进的 Docker 镜像构建等",
                "https://spring.io/blog", "Spring官方", LocalDateTime.now().minusHours(2)));
        mock.add(new NewsDTO("Vue 3.5 代号 'Rising Sun' 发布",
                "Vue 3.5 版本带来了性能优化和新的组合式 API 功能",
                "https://vuejs.org", "Vue官方", LocalDateTime.now().minusHours(5)));
        mock.add(new NewsDTO("Java 21 LTS 新特性在实际项目中的应用",
                "虚拟线程、模式匹配、Record Pattern 等新特性正在改变 Java 开发方式",
                "https://openjdk.org", "开源中国", LocalDateTime.now().minusDays(1)));
        mock.add(new NewsDTO("MyBatis-Plus 3.5.7 发布",
                "新增多数据源事务支持、优化分页插件性能",
                "https://baomidou.com", "MyBatis-Plus", LocalDateTime.now().minusDays(2)));
        mock.add(new NewsDTO("Element Plus 2.8 发布",
                "新增多个组件，优化暗黑模式兼容性",
                "https://element-plus.org", "Element Plus", LocalDateTime.now().minusDays(3)));
        mock.add(new NewsDTO("AI 辅助编程工具 2026 年发展趋势",
                "Cursor、Copilot 等 AI 编程助手正在重新定义开发效率",
                "https://github.com", "36氪", LocalDateTime.now().minusDays(4)));
        return mock;
    }
}
