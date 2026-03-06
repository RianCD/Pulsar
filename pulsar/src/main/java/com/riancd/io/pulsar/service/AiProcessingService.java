package com.riancd.io.pulsar.service;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import org.springframework.stereotype.Service;

@Service
public class AiProcessingService {

    private final ChatLanguageModel chatModel;
    private final EmbeddingModel embeddingModel;

    public AiProcessingService(ChatLanguageModel chatModel, EmbeddingModel embeddingModel) {
        this.chatModel = chatModel;
        this.embeddingModel = embeddingModel;
    }

    public String processNewsAndgetSummary(String rawNewsText) {
        //Prompt de instruções para a IA
        String prompt = """
                Você é um assistente de curadoria de conteúdo.
                Leia a notícia abaixo, faça um resumo em no máximo 3 tópicos curtos e
                classifique o sentimento geral do texto informando apenas: POSITIVO, NEGATIVO ou NEUTRO.
                
                Notícia:
                """ + rawNewsText;

        // Gerar a resposta usando o modelo de linguagem Llma3
        return chatModel.generate(prompt);
    }

}
