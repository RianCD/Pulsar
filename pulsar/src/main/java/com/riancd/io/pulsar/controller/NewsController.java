package com.riancd.io.pulsar.controller;

import com.riancd.io.pulsar.dto.NewsResponse;
import com.riancd.io.pulsar.service.NewsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@RestController
@RequestMapping("/api/news")
@CrossOrigin(origins = "*") // Permite requisições de qualquer origem (CORS)
@Tag(name = "Notícias", description = "Endpoints para listagem e busca semântica de notícias processadas por IA")
public class NewsController {
    private static final Logger log = LoggerFactory.getLogger(NewsController.class);
    private final NewsService newsService;

    public NewsController(NewsService newsService) {
        this.newsService = newsService;
    }

    @Operation(summary = "Listar todas as notícias", description = "Retorna uma lista contendo todas as notícias previamente processadas e persistidas.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de notícias retornada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autorizado a acessar este recurso"),
            @ApiResponse(responseCode = "403", description = "Acesso proibido a este recurso"),
            @ApiResponse(responseCode = "500", description = "Erro interno no servidor")
    })
    @GetMapping
    public List<NewsResponse> listAll() {
        log.info("Receiving request to list all news.");
        return newsService.getAllProcessedNews();
    }

    @Operation(summary = "Busca semântica de notícias", description = "Gera o embedding para o texto buscado e retorna as notícias mais relevantes usando distância de cosseno.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Busca realizada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autorizado a acessar este recurso"),
            @ApiResponse(responseCode = "403", description = "Acesso proibido a este recurso"),
            @ApiResponse(responseCode = "500", description = "Erro interno no servidor ou na API de IA")
    })
    @GetMapping("/search")
    public List<NewsResponse> search(@RequestParam String query) {
        log.info("Receiving request to search news with query: {}", query);
        return newsService.searchSimilarNews(query);
    }
}
