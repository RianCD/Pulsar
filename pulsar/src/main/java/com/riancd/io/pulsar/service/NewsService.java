package com.riancd.io.pulsar.service;

import com.riancd.io.pulsar.client.NewsApiClient;
import com.riancd.io.pulsar.dto.MediastackArticle;
import com.riancd.io.pulsar.dto.MediastackResponse;
import com.riancd.io.pulsar.dto.NewsResponse;
import com.riancd.io.pulsar.model.News;
import com.riancd.io.pulsar.repository.NewsRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class NewsService {

    private static final Logger log = LoggerFactory.getLogger(NewsService.class);
    private final NewsRepository repository;
    private final NewsApiClient apiClient;
    private final AiProcessingService aiService;

    public NewsService(NewsRepository repository, NewsApiClient apiClient, AiProcessingService aiService) {
        this.repository = repository;
        this.apiClient = apiClient;
        this.aiService = aiService;
    }

    public List<NewsResponse> getAllProcessedNews() {
        return repository.findAll().stream()
                .map(news -> new NewsResponse(
                        news.getId(),
                        news.getTitle(),
                        news.getSummary(),
                        news.getSentiment()
                ))
                .collect(Collectors.toList());
    }

    public List<NewsResponse> searchSimilarNews(String query) {
        float[] queryVector = aiService.generateEmbeddingAsFloatArray(query);
        String vectorString = java.util.Arrays.toString(queryVector);
        
        return repository.findSimilarNews(vectorString, 5).stream()
                .map(news -> new NewsResponse(
                        news.getId(),
                        news.getTitle(),
                        news.getSummary(),
                        news.getSentiment()
                ))
                .collect(Collectors.toList());
    }

    // Executa a cada 1 hora (3600000 milissegundos)
    @Scheduled(fixedRate = 3600000)
    public void fetchAndProcessNewsScheduled() {
        log.info("Iniciando Pipeline Automática do Pulsar (Agendamento)...");

        log.info("1. Buscando notícias de tecnologia na Mediastack...");
        try {
            MediastackResponse response = apiClient.fetchRecentTechnolyNews();

            if (response == null || response.data().isEmpty()) {
                log.warn("Nenhuma notícia encontrada ou erro na API");
                return;
            }

            log.info("Foram encontradas {} notícias. Iniciando processamento...", response.data().size());

            for (MediastackArticle article : response.data()) {
                log.info("Processando: {}", article.title());
                String rawText = article.title() + " - " + article.description();

                log.info("Gerando resumo com Llama 3...");
                String aiSummaryAndSentiment = aiService.processNewsAndGetSummary(rawText);

                log.info("Gerando vetor de embedding com All-MiniLM...");
                float[] embeddingVector = aiService.generateEmbeddingAsFloatArray(rawText);

                log.info("Salvando no supabase...");
                News news = new News();
                news.setTitle(article.title());
                news.setRawContent(rawText);
                news.setSummary(aiSummaryAndSentiment);
                news.setSentiment("Processado");
                news.setEmbedding(embeddingVector);

                repository.save(news);
                log.info("Notícia '{}' processada e salva com sucesso!", article.title());
            }

            log.info("Pipeline Finalizada! Os novos dados já estão na nuvem.");
        } catch (Exception e) {
            log.error("Erro durante o processamento do pipeline: {}", e.getMessage(), e);
        }
    }
}
