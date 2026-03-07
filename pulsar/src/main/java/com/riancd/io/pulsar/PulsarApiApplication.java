package com.riancd.io.pulsar;

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

	// O Spring vai executar este método automaticamente quando a aplicação ligar
	@Bean
	public CommandLineRunner runTest(AiProcessingService aiService) {
		return args -> {
			System.out.println("\n=======================================================");
			System.out.println("Iniciando teste da Inteligência Artificial (Llama 3)...");
			System.out.println("Aguarde, a IA está lendo e processando a notícia...");
			System.out.println("=======================================================\n");

			// Nossa notícia de mentira (Mock)
			String mockNews = """
                    A OpenAI anunciou nesta quinta-feira uma nova atualização para os seus modelos de linguagem,
                    prometendo reduzir as 'alucinações' geradas pela Inteligência Artificial em até 40%.
                    Especialistas em tecnologia celebraram o avanço, afirmando que a precisão dos dados
                    é fundamental para a adoção corporativa. No entanto, críticos apontam que o custo
                    de processamento continua sendo um desafio significativo para pequenas empresas.
                    """;

			// Chamamos o serviço que construímos na etapa anterior
			String aiResponse = aiService.processNewsAndGetSummary(mockNews);

			System.out.println(">>> RESPOSTA DA IA:\n");
			System.out.println(aiResponse);
			System.out.println("\n=======================================================\n");

			System.out.println("Gerando vetor matemático (Embedding) para a noticía...");
			List<Float> embedding = aiService.generateEmbedding(mockNews);

			System.out.println("Embedding gerado com sucesso!");
			System.out.println("Tamanho do vetor: " + embedding.size() + " dimensões");
			System.out.println("Amostra dos primeiros 5 números: " + embedding.subList(0, 5));
		};
	}

}
