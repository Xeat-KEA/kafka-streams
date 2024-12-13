package com.example.ctkafka;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.ai.document.MetadataMode;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.openai.OpenAiEmbeddingOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.vectorstore.RedisVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.DefaultJedisClientConfig;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisPooled;
import redis.clients.jedis.search.Document;
import redis.clients.jedis.search.Schema;

import java.util.List;

@Configuration
@AllArgsConstructor
@NoArgsConstructor
public class RedisVectorStoreConfiguration {

    @Value("${spring.data.redis.host}") String host;

    @Bean
    public RedisVectorStore.RedisVectorStoreConfig redisVectorStoreConfig() {
        return RedisVectorStore.RedisVectorStoreConfig.builder()
                .withIndexName("vector-store")
                .withPrefix("vector:")
                .withContentFieldName("content")
                .withEmbeddingFieldName("embedding")
                .withVectorAlgorithm(RedisVectorStore.Algorithm.HSNW)
                .withMetadataFields(
                        new RedisVectorStore.MetadataField("type", Schema.FieldType.TEXT),
                        new RedisVectorStore.MetadataField("chatId", Schema.FieldType.NUMERIC),
                        new RedisVectorStore.MetadataField("chatHistoryId", Schema.FieldType.NUMERIC)
                )
                .build();
    }

    @Bean
    public JedisPooled jedisPooled() {
        return new JedisPooled(host, 6379);
    }

    @Bean
    public EmbeddingModel embeddingModel(@Value("${spring.ai.openai.base-url}") String baseUrl, @Value("${spring.ai.openai.api-key}") String apiKey) {
        return new OpenAiEmbeddingModel(new OpenAiApi(baseUrl, apiKey),
                MetadataMode.EMBED,
                OpenAiEmbeddingOptions.builder()
                        .withModel("text-embedding-ada-002")
                        .build());
    }

    @Bean
    public RedisVectorStore redisVectorStore(@Value("${spring.ai.openai.base-url}") String baseUrl, @Value("${spring.ai.openai.api-key}") String apiKey) {
        return new RedisVectorStore(redisVectorStoreConfig(), embeddingModel(baseUrl, apiKey), jedisPooled(), true);
    }
}