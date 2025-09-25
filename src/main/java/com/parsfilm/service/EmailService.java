package com.parsfilm.service;

import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Files;

@Service
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    // Данный метод считается рабочим когда на имейл пришло письмо с заголовком, с зип файлом, или выбросить исключение о том что ошибка при отправке + лог об ошибке
    public void sendReportByEmail(String toEmail, File zipFile) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(toEmail);
            helper.setSubject("Отчет по фильмам");
            helper.setText("Во вложении находится отчёт в формате ZIP.");

            // Добавляем ZIP файл как вложение, чтобы отправка не зависела от существования временного файла
            byte[] zipBytes = Files.readAllBytes(zipFile.toPath());
            helper.addAttachment(zipFile.getName(), new ByteArrayResource(zipBytes));

            mailSender.send(message);

        } catch (Exception e) {
            log.error("Не удалось отправить письмо с отчётом", e);
            throw new RuntimeException("Ошибка отправки email: " + e.getMessage());
        }
    }
}