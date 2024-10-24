package com.example.ctkafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

@Configuration
public class StreamsFilter {
//    /** SASL 인증을 위한 JAAS Template */
//    private static final String JAAS_TEMPLATE = "org.apache.kafka.common.security.scram.ScramLoginModule required username=\"admin\" password=\"admin-secret\";";
//
//    @Bean
//    public KafkaStreams kafkaStreams() {
//        // 1. 설정 세팅
//        Properties prop = new Properties();
//        prop.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "123.143.255.255:9092"); // kafka host 및 server 설정
//        prop.put(StreamsConfig.APPLICATION_ID_CONFIG, "streams-test");
//        prop.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
//        prop.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());
//
//        // 1-1) SASL 설정 추가 (프로듀서 생성 전에 추가해야 함)
//        prop.put("security.protocol", "SASL_PLAINTEXT");
//        prop.put("sasl.mechanism", "SCRAM-SHA-256");
//        String jaasConfig = String.format(JAAS_TEMPLATE, "admin", "admin-secret");
//        prop.put("sasl.jaas.config", jaasConfig);
//
//        // 2. 스트림즈 빌더 생성
//        final StreamsBuilder streamsBuilder = new StreamsBuilder();
//
//        // 2-1) 소스 프로세서 생성
//        KStream<String, String> streamLog = streamsBuilder.stream("stream-all-test");
//        // 2-2) 스트림 프로세서 생성
//        streamLog.map((key, value) -> {
//                    try {
//                        ObjectMapper objectMapper = new ObjectMapper();
//
//                        // JSON 문자열을 StreamsTestDto 객체로 변환
//                        StreamsTestDto dto = objectMapper.readValue(value, StreamsTestDto.class);
//
//                        // dto의 name 필드를 key-value 쌍으로 반환
//                        return KeyValue.pair("name", dto.getName());
//
//                    } catch (Exception e) {
//                        return KeyValue.pair("name", null); // 예외 발생 시 key는 "name", value는 null
//                    }
//                })
//                // 2-3) 싱크 프로세서 생성
//                .to("stream-name-test");
//
//        // 3. 스트림즈 생성
//        KafkaStreams kafkaStreams = new KafkaStreams(streamsBuilder.build(), prop);
//        kafkaStreams.start();
//
//        return kafkaStreams;
//    }
}