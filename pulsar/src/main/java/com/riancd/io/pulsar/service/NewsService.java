package com.riancd.io.pulsar.service;

import com.riancd.io.pulsar.client.NewsApiClient;
import com.riancd.io.pulsar.dto.MediastackArticle;
import com.riancd.io.pulsar.dto.MediastackResponse;
import com.riancd.io.pulsar.dto.NewsResponse;
import com.riancd.io.pulsar.model.News;
import com.riancd.io.pulsar.repository.NewsRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class NewsService {

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
        System.out.println("\n=======================================================");
        System.out.println("Iniciando Pipeline Automática do Pulsar (Agendamento)...");
        System.out.println("=======================================================\n");

        System.out.println("1. Buscando notícias de tecnologia na Mediastack...");
        MediastackResponse response = apiClient.fetchRecentTechnolyNews();

        if (response == null || response.data().isEmpty()) {
            System.out.println("Nenhuma notícia encontrada ou erro na API");
            return;
        }

        System.out.println("Foram encontradas " + response.data().size() + " notícias. Iniciando processamento...\n");

        for (MediastackArticle article : response.data()) {
            System.out.println("Processando: " + article.title());
            String rawText = article.title() + " - " + article.description();

            System.out.println("Gerando resumo com Llama 3...");
            String aiSummaryAndSentiment = aiService.processNewsAndGetSummary(rawText);

            System.out.println("Gerando vetor de embedding com All-MiniLM...");
            float[] embeddingVector = aiService.generateEmbeddingAsFloatArray(rawText);

            System.out.println("Salvando no supabase...");
            News news = new News();
            news.setTitle(article.title());
            news.setRawContent(rawText);
            news.setSummary(aiSummaryAndSentiment);
            news.setSentiment("Processado");
            news.setEmbedding(embeddingVector);

            repository.save(news);
            System.out.println("Notícia processada e salva com sucesso!\n");
        }

        System.out.println("=======================================================");
        System.out.println("Pipeline Finalizada! Os novos dados já estão na nuvem.");
        System.out.println("=======================================================\n");
    }
}
