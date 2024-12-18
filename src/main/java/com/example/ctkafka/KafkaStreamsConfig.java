package com.example.ctkafka;

import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.errors.LogAndContinueExceptionHandler;
import org.apache.kafka.streams.kstream.*;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.RedisVectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerde;
import org.springframework.kafka.support.serializer.JsonSerializer;

@Configuration
@EnableKafka
@Slf4j
@RequiredArgsConstructor
public class KafkaStreamsConfig {
    private final RedisVectorStore redisVectorStore;
    public final ObjectMapper objectMapper;
    public static String USER_TOPIC = "ct.user_service.users";
    public static String BLOG_TOPIC = "ct.blog_service.blog";
    public static String ARTICLE_TOPIC = "ct.blog_service.article";
    public static String CODE_ARTICLE_TOPIC = "ct.blog_service.code_article";
    public static String CHILD_TOPIC = "ct.blog_service.child_category";
    public static String JOINED_TOPIC = "article";
    public static String ELASTIC_USER_TOPIC = "user";
    public static String REDIS_TOPIC = "ct.llm_service.llm_history";
    public String BOOTSTRAP_SERVERS = "localhost:9092";
    public String APPLICATION_ID = "ctKafka2";

    @Bean
    public KafkaStreams initBoardDetailStream() {
        // Kafka Streams의 속성을 설정하는 객체
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, APPLICATION_ID);
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        // 문자열 데이터를 직렬화하고 역직렬화
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        props.put(StreamsConfig.DEFAULT_DESERIALIZATION_EXCEPTION_HANDLER_CLASS_CONFIG, LogAndContinueExceptionHandler.class);

        // Kafka Streams의 토폴로지를 정의하는 객체
        StreamsBuilder builder = new StreamsBuilder();

        // Member 역직렬화 설정
        JsonDeserializer<UserDto> userDtoJsonDeserializer = new JsonDeserializer<>(UserDto.class);
        userDtoJsonDeserializer.addTrustedPackages("com.example.*");

        // "member" 토픽에서 문자열 데이터를 읽어서 KTable로 변환
        KTable<String, UserDto> userKtable = builder.table(
                USER_TOPIC,
                Consumed.with(
                        Serdes.String(),
                        Serdes.serdeFrom(new JsonSerializer<>(), userDtoJsonDeserializer)
                )
        );
        userKtable.mapValues(value -> {
            log.info(value.toString());
            return value;
        });

        // Board 역직렬화 설정
        JsonDeserializer<BlogDto> blogDtoJsonDeserializer = new JsonDeserializer<>(BlogDto.class);
        blogDtoJsonDeserializer.addTrustedPackages("com.example.*");

        // 스트림 토픽에서 문자열 데이터를 읽어서 KStream으로 변환
        KStream<String, BlogDto> blogKstream = builder.stream(
                BLOG_TOPIC,
                Consumed.with(
                        Serdes.String(),
                        Serdes.serdeFrom(new JsonSerializer<>(), blogDtoJsonDeserializer)
                )
        );
        blogKstream.mapValues(value -> {
            log.info(value.toString());
            return value;
        });

        // KTable과 KStream을 inner join
        KTable<String, UserBlogDto> userblogJoin = blogKstream
                // board stream의 키를 user_id 변경
                // blog stream과 user table을 user_id 키 값으로 조인
                .selectKey((key, value) -> {
                    log.info("셀렉트키들어옴");
                    return "{\"user_id\":\"" + value.getUser_id() + "\"}";
                }).peek((key, value) -> log.info("키={}", key))
                .join(userKtable, (blogStreamValue, userTableValue) -> {
                    log.info("조인들어옴");
                    if (userTableValue == null || blogStreamValue == null) {
                        return null;  // 삭제된 데이터를 null로 반환하여 tombstone 메시지를 보냄
                    } else if (blogStreamValue.getUser_id().equals(userTableValue.getUser_id())) {
                        log.info("이프들어옴");
                        log.info(blogStreamValue.toString());
                        log.info(userTableValue.toString());
                        return new UserBlogDto(blogStreamValue.getBlog_id(), blogStreamValue.getUser_id(), userTableValue.getNick_name(), userTableValue.getProfile_url(), userTableValue.getProfile_message());
                    } else {
                        log.info("엘스로옴");
                        log.info(blogStreamValue.toString());
                        log.info(userTableValue.toString());
                        return null;
                    }
                }).selectKey((key, value) -> {
                    log.info("셀렉트키2들어옴");
                    return "{\"blog_id\":" + value.getBlog_id().toString() + "}";
                }).toTable(Materialized.with(Serdes.String(), new UserBlogDtoSerde()));
        userblogJoin.toStream().selectKey((key, value) -> "{\"id\":" + value.getBlog_id() + "}")
                .to(ELASTIC_USER_TOPIC, Produced.with(Serdes.String(), new JsonSerde<>(UserBlogDto.class)));

        //CHILD 역직렬화 설정
        JsonDeserializer<ChildDto> childDtoJsonDeserializer = new JsonDeserializer<>(ChildDto.class);
        childDtoJsonDeserializer.addTrustedPackages("com.example.*");

        // 스트림 토픽에서 문자열 데이터를 읽어서 KStream으로 변환
        KTable<String, ChildDto> childKstream = builder.table(
                CHILD_TOPIC,
                Consumed.with(
                        Serdes.String(),
                        Serdes.serdeFrom(new JsonSerializer<>(), childDtoJsonDeserializer)
                )
        );

        // article 역직렬화 설정
        JsonDeserializer<ArticleDto> articleDtoJsonDeserializer = new JsonDeserializer<>(ArticleDto.class);
        articleDtoJsonDeserializer.addTrustedPackages("com.example.*");

        // 스트림 토픽에서 문자열 데이터를 읽어서 KStream으로 변환
        KStream<String, ArticleDto> articleKstream = builder.stream(
                ARTICLE_TOPIC,
                Consumed.with(
                        Serdes.String(),
                        Serdes.serdeFrom(new JsonSerializer<>(), articleDtoJsonDeserializer)
                )
        );
        articleKstream.map((key, value) -> {
            log.info(key);
            log.info(String.valueOf(value));
            log.info("아티클 맵 들어옴");
            if (value == null) {
                key = key.replace("article_", "");
                articleKstream.to(JOINED_TOPIC,
                        Produced.with(
                                Serdes.String(),
                                new JsonSerde<>(ArticleDto.class)
                        ));
                return new KeyValue<>(key, null);
            }
            return new KeyValue<>(key, value);
        });

        // 조인된 스트림을 joined 토픽으로 전송
        articleKstream.selectKey(((key, value) -> {
                    log.info("아티클 셀렉트키 들어옴");
                    return "{\"blog_id\":" + value.getBlog_id().toString() + "}";
                })).join(userblogJoin, (articleValue, userblogJoinValue) -> {
                    log.info("아티클조인들어옴");
                    if (articleValue.get__deleted()) {
                        return new ElasticArticleDto(articleValue.getArticle_id());
                    } else if (articleValue.getBlog_id() == userblogJoinValue.getBlog_id().intValue()) {
                        log.info("아티클이프들어옴");
                        log.info(articleValue.toString());
                        log.info(userblogJoinValue.toString());
                        return new ElasticArticleDto(articleValue.getArticle_id(), userblogJoinValue.getNick_name(),
                                userblogJoinValue.getProfile_url(), articleValue.getTitle(), articleValue.getContent(),
                                articleValue.getCreated_date(), articleValue.getLike_count(), articleValue.getReply_count(),
                                articleValue.getView_count(), userblogJoinValue.getBlog_id(), articleValue.getChild_category_id(), null);
                    } else {
                        log.info("아티클엘스로옴");
                        return null;
                    }
                }).selectKey(((key, value) -> {
                    log.info("엘라아티클 셀렉트키 들어옴");
                    if (value.getChild_category_id() != null) {
                        return "{\"child_category_id\":" + value.getChild_category_id().toString() + "}";
                    }
                    return "{\"child_category_id\":0}";
                })).repartition(Repartitioned.<String, ElasticArticleDto>as("elastic-article-repartition")
                        .withKeySerde(Serdes.String())
                        .withValueSerde(new JsonSerde<>(ElasticArticleDto.class)))
                .join(childKstream, (elasticArticleDto, childDto) -> {
                    log.info("엘라아티클,차일드조인들어옴");
                    elasticArticleDto.setParent_category_id(childDto.getParent_category_id());
                    return elasticArticleDto;
                })
                .map(((key, value) -> {
                    log.info("엘라아티클 셀렉트키2 들어옴");
                    log.info("바꾸기전 키={}", key);
                    if (value.getNick_name() == null && value.getTitle() == null) {
                        return new KeyValue<>("{\"id\":" + value.getArticle_id().toString() + "}", null);
                    }
                    return new KeyValue<>("{\"id\":" + value.getArticle_id().toString() + "}", value);
                }))
                .to(
                        JOINED_TOPIC,
                        Produced.with(
                                Serdes.String(),
                                new JsonSerde<>(ElasticArticleDto.class)
                        )
                );

        // code_article 역직렬화 설정
        JsonDeserializer<CodeArticleDto> codeArticleDtoJsonDeserializer = new JsonDeserializer<>(CodeArticleDto.class);
        codeArticleDtoJsonDeserializer.addTrustedPackages("com.example.*");

        // 스트림 토픽에서 문자열 데이터를 읽어서 KStream으로 변환
        KStream<String, CodeArticleDto> codeArticleKstream = builder.stream(
                CODE_ARTICLE_TOPIC,
                Consumed.with(Serdes.String(), Serdes.serdeFrom(new JsonSerializer<>(), codeArticleDtoJsonDeserializer)));
        codeArticleKstream.peek((key, value) -> {
            log.info("코드아티클 키={}", key);
            log.info(value.toString());
        }).selectKey(((key, value) -> {
            log.info("코드아티클 셀렉트키 들어옴");
            if (value.getArticle_id() == null) {
                return "{\"id\":" + 1 + "}";
            }
            return "{\"id\":" + value.getArticle_id() + "}";
        })).to(JOINED_TOPIC, Produced.with(Serdes.String(), new JsonSerde<>(CodeArticleDto.class)));

        // LLMHistory 역직렬화 설정
        JsonDeserializer<LLMHistoryDto> LLMHistoryDtoJsonDeserializer = new JsonDeserializer<>(LLMHistoryDto.class);
        LLMHistoryDtoJsonDeserializer.addTrustedPackages("com.example.*");
        // 스트림 토픽에서 문자열 데이터를 읽어서 KStream으로 변환
        KStream<String, LLMHistoryDto> LLMKstream = builder.stream(
                REDIS_TOPIC,
                Consumed.with(
                        Serdes.String(),
                        Serdes.serdeFrom(new JsonSerializer<>(), LLMHistoryDtoJsonDeserializer)
                )
        );
        LLMKstream.peek((key, value) -> {
            if (value.getChatId() != null) {
                List<Document> cdcDocuments = List.of(
                        new Document(value.getQuestion(), Map.of("type", "question", "chatId", value.getChatId(), "chatHistoryId", value.getChatHistoryId())),
                        new Document(value.getAnswer(), Map.of("type", "answer", "chatId",value.getChatId(), "chatHistoryId", value.getChatHistoryId()))
                );
                redisVectorStore.doAdd(cdcDocuments);
            }
        });

        // 토폴로지를 빌드하여 Kafka Streams 객체 생성
        //3. 스트림즈 생성
        KafkaStreams streams = new KafkaStreams(builder.build(), props);
        streams.start();
        // 애플리케이션 종료 시 Kafka Streams를 정지
        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
        return streams;

    }

}