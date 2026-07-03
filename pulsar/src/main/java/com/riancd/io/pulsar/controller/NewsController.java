package com.riancd.io.pulsar.controller;

import com.riancd.io.pulsar.dto.NewsResponse;
import com.riancd.io.pulsar.service.NewsService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/news")
@CrossOrigin(origins = "*") // Permite requisições de qualquer origem (CORS)
public class NewsController {
    private final NewsService newsService;

    public NewsController(NewsService newsService) {
        this.newsService = newsService;
    }

    @GetMapping // Define que requisições do tipo GET vão acionar este método
    public List<NewsResponse> listAll() {
        return newsService.getAllProcessedNews();
    }

    @GetMapping("/search") // Define que a rota é /api/news/search
    public List<NewsResponse> search(@org.springframework.web.bind.annotation.RequestParam String query) {
        return newsService.searchSimilarNews(query);
    }
}
