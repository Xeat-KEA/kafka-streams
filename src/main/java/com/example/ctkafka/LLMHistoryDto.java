package com.example.ctkafka;

import lombok.Data;

@Data
public class LLMHistoryDto {
    private Long chatHistoryId;
    private String question;
    private String answer;
    private Long chatId;
}
