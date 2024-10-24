package com.example.ctkafka;

import lombok.Data;

@Data
public class ArticleDto {
    private Integer article_id;
    private Integer blog_id;
    private String title;
    private String content;
    private Long created_date;
    private Integer like_count;
    private Integer reply_count;
    private Integer view_count;
}
