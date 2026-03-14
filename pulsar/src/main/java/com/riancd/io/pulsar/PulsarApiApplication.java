package com.riancd.io.pulsar;

import com.pgvector.PGvector;
import com.riancd.io.pulsar.client.NewsApiClient;
import com.riancd.io.pulsar.dto.MediastackArticle;
import com.riancd.io.pulsar.dto.MediastackResponse;
import com.riancd.io.pulsar.model.News;
import com.riancd.io.pulsar.repository.NewsRepository;
import com.riancd.io.pulsar.service.AiProcessingService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.List;

@SpringBootApplication
public class PulsarApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(PulsarApiApplication.class, args);
	}

	@Bean
	public CommandLineRunner runIntegrantionTest(NewsApiClient apiClient,
												 AiProcessingService aiService,
												 NewsRepository repository) {
		return args -> {
			System.out.println("\n=======================================================");
			System.out.println("Iniciando Pipeline Completo do Pulsar...");
			System.out.println("=======================================================\n");
			//1. Extração
			System.out.println("1. Buscando notícias de tecnologia na Mediastack...");
			MediastackResponse response = apiClient.fetchRecentTechnolyNews();

			if(response == null || response.data().isEmpty()) {
				System.out.println("Nenhuma notícia encontrada ou erro na API");
				return;
			}

			System.out.println("Foram encontradas " + response.data().size() + " notícias. Iniciando processamento...\n");

			//2. Transformação e carga
			for (MediastackArticle article : response.data()) {

				System.out.println("Processando: " + article.title());
				String rawText = article.title() + " - " + article.description();

				//2.1 resumo da IA
				System.out.println("Gerando resumo com Llama 3...");
				String aiSummaryAndSentiment = aiService.processNewsAndGetSummary(rawText);

				//2.2 Gerar vetor numerico (embedding)
				System.out.println("Gerando vetor de embedding com All-MiniLM...");
				float[] embeddingVector = aiService.generateEmbeddingAsFloatArray(rawText);

				//3 Salvar no banco de dados
				System.out.println("Salvando no supabase...");
				News news = new News();
				news.setTitle(article.title());
				news.setRawContent(rawText);
				news.setSummary(aiSummaryAndSentiment); //resumo e sentimento estão sendo retornados no mesmo texto
				news.setSentiment("Processado");
				news.setEmbedding(embeddingVector);

				repository.save(news);
				System.out.println("Notícia processada e salva com sucesso!\n");
			}

			System.out.println("=======================================================");
			System.out.println("Pipeline Finalizada! Os dados reais já estão na nuvem.");
			System.out.println("=======================================================\n");

		};
	}
}