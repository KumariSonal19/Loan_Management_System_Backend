package com.lms.loan.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import com.lms.loan.config.NotificationConfig;
import com.lms.loan.dto.NotificationDTO;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @InjectMocks
    private NotificationService service;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Test
    void sendNotification_success() {
        service.sendNotification(
                1L, 2L, "a@b.com",
                "TYPE", "TITLE", "MSG"
        );

        verify(rabbitTemplate).convertAndSend(
                eq(NotificationConfig.EXCHANGE_NAME),
                eq(NotificationConfig.ROUTING_KEY),
                any(NotificationDTO.class)
        );
    }
}
