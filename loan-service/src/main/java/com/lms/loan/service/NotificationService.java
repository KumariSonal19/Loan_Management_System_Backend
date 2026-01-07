package com.lms.loan.service;

import com.lms.loan.dto.NotificationDTO;
import com.lms.loan.config.NotificationConfig;
import lombok.extern.slf4j.Slf4j; // Import Slf4j
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j // Enable logging
public class NotificationService {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void sendNotification(Long userId, Long loanId, String userEmail, String type, String title, String message) {
        try {
            NotificationDTO notification = NotificationDTO.builder()
                    .userId(userId)
                    .loanId(loanId)
                    .userEmail(userEmail)
                    .type(type)
                    .title(title)
                    .message(message)
                    .build();

            rabbitTemplate.convertAndSend(
                    NotificationConfig.EXCHANGE_NAME,
                    NotificationConfig.ROUTING_KEY,
                    notification
            );

            log.info("Notification sent to RabbitMQ for User: {}", userId);

        } catch (Exception e) {
            
            log.error("Failed to send RabbitMQ notification (Service might be down). Error: {}", e.getMessage());
            
        }
    }
}