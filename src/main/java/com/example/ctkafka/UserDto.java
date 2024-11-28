package com.example.ctkafka;

import lombok.Data;

@Data
public class UserDto {
    private String user_id;
    private String nick_name;
    private String profile_url;
    private String profile_message;
}
