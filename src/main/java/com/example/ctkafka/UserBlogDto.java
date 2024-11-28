package com.example.ctkafka;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserBlogDto {
    private Long blog_id;
    private String user_id;
    private String nick_name;
    private String profile_url;

    public UserBlogDto(Long blog_id, String user_id, String nick_name, String profile_url) {
        this.blog_id = blog_id;
        this.user_id = user_id;
        this.nick_name = nick_name;
        this.profile_url = profile_url;
    }
}
