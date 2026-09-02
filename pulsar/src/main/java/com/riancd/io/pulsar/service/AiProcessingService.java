package com.riancd.io.pulsar.service;

import com.pgvector.PGvector;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AiProcessingService {

    private static final Logger log = LoggerFactory.getLogger(AiProcessingService.class);
    private final ChatLanguageModel chatModel;
    private final EmbeddingModel embeddingModel;

    public AiProcessingService(ChatLanguageModel chatModel, EmbeddingModel embeddingModel) {
        this.chatModel = chatModel;
        this.embeddingModel = embeddingModel;
    }

    public String processNewsAndGetSummary(String rawNewsText) {
        log.debug("Generating summary and sentiment analysis via Llama 3...");
        //Prompt de instruções para a IA
        String prompt = """
                Você é um assistente de curadoria de conteúdo.
                Leia a notícia abaixo, faça um resumo em no máximo 3 tópicos curtos e
                classifique o sentimento geral do texto informando apenas: POSITIVO, NEGATIVO ou NEUTRO.
                
                Notícia:
                """ + rawNewsText;

        // Gerar a resposta usando o modelo de linguagem Llma3
        String response = chatModel.generate(prompt);
        log.debug("Summary and sentiment generated successfully.");
        return response;
    }

    public List<Float> generateEmbedding(String text) {
        log.debug("Generating embeddings via All-MiniLM...");
        List<Float> embedding = embeddingModel.embed(text).content().vectorAsList();
        log.debug("Embeddings generated successfully.");
        return embedding;
    }
    
    public float[] generateEmbeddingAsFloatArray(String text) {
        List<Float> floatList = generateEmbedding(text);
        float[] floatArray = new float[floatList.size()];
        for (int i = 0; i < floatList.size(); i++) {
            floatArray[i] = floatList.get(i);
        }
        return floatArray;
    }
}
