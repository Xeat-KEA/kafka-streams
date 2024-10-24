package com.example.ctkafka;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Data
@Slf4j
public class ElasticArticleDto {
    private Integer article_id;
    private String nickname;
    private String profile_url;
    private String title;
    private String content;
    private String created_date;
    private Integer like_count;
    private Integer reply_count;
    private Integer view_count;

    public ElasticArticleDto(Integer article_id, String nickname, String profile_url, String title,
                             String content, Long created_date, Integer like_count, Integer reply_count, Integer view_count) {
        this.article_id = article_id;
        this.nickname = nickname;
        this.profile_url = profile_url;
        this.title = title;
        this.content = content;
        this.created_date = Instant.ofEpochMilli(created_date/1000).atZone(ZoneId.of("UTC")).toLocalDateTime().toString();
        log.info(this.created_date.toString());
        this.like_count = like_count;
        this.reply_count = reply_count;
        this.view_count = view_count;
    }
}
