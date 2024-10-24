package com.example.ctkafka;

import org.apache.kafka.common.serialization.Serdes;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

public class UserBlogDtoSerde extends Serdes.WrapperSerde<UserBlogDto> {
    public UserBlogDtoSerde() {
        super(new JsonSerializer<>(), new JsonDeserializer<>(UserBlogDto.class));
    }
}