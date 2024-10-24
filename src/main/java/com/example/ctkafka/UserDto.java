package com.example.ctkafka;

import lombok.Data;

@Data
public class UserDto {
    private Long user_id;
    private String nickname;
    private String profile_url;
}
