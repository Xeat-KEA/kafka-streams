package com.example.ctkafka;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.time.ZoneId;

@Data
@Slf4j
public class ElasticArticleDto {
    private Integer article_id;
    private String nick_name;
    private String profile_url;
    private String title;
    private String content;
    private String created_date;
    private Integer like_count;
    private Integer reply_count;
    private Integer view_count;
    private Long blog_id;
    private Long child_category_id;
    private Long parent_category_id;

    public ElasticArticleDto(Integer article_id, String nick_name, String profile_url, String title,
                             String content, Long created_date, Integer like_count, Integer reply_count, Integer view_count, Long blog_id) {
        this.article_id = article_id;
        this.nick_name = nick_name;
        this.profile_url = profile_url;
        this.title = title;
        this.content = content.replaceAll("<img[^>]*>", "");
        this.created_date = Instant.ofEpochMilli(created_date/1000).atZone(ZoneId.of("UTC")).toLocalDateTime().toString();
        this.like_count = like_count;
        this.reply_count = reply_count;
        this.view_count = view_count;
        this.blog_id = blog_id;
    }

    public ElasticArticleDto(Integer article_id) {
        this.article_id = article_id;
        this.nick_name = null;
        this.profile_url = null;
        this.title = null;
        this.content = null;
        this.created_date = null;
        this.like_count = null;
        this.reply_count = null;
        this.view_count = null;
    }
}
