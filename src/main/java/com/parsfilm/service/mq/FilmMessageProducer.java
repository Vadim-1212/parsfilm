package com.parsfilm.service.mq;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.parsfilm.dto.FilmDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FilmMessageProducer {

    private final JmsTemplate jmsTemplate;
    private final ObjectMapper objectMapper;

    @Value("${film.queue.name}")
    private String queueName;

    public FilmMessageProducer(JmsTemplate jmsTemplate, ObjectMapper objectMapper) {
        this.jmsTemplate = jmsTemplate;
        this.objectMapper = objectMapper;
    }

    public void sendFilms(List<FilmDto> films) {
        try {
            String message = objectMapper.writeValueAsString(films);
            jmsTemplate.convertAndSend(queueName, message);
            System.out.println(">>> Отправлено в очередь " + films.size() + " фильмов");
        } catch (Exception e) {
            System.err.println("Ошибка отправки в очередь: " + e.getMessage());
        }
    }
}
