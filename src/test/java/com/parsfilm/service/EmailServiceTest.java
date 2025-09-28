package com.parsfilm.service;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import java.io.File;

import java.nio.file.Files;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class EmailServiceTest {

    @InjectMocks
    private EmailService emailService;

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private MimeMessage  mimeMessage;

    @Test
    void sendReportByEmail_success() throws Exception {
        // given
        File tempFile = File.createTempFile("report", ".zip");
        Files.write(tempFile.toPath(), "test data".getBytes());

        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        // when
        emailService.sendReportByEmail("test@example.com", tempFile);

        // then
        verify(mailSender).send(mimeMessage);

        tempFile.delete();
    }


}
