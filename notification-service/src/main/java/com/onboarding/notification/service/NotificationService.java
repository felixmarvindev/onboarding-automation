package com.onboarding.notification.service;

import com.onboarding.notification.config.EmailProperties;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Map;

@Service
public class NotificationService {
    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    private final JavaMailSender mailSender;
    private final EmailProperties emailProperties;
    private final ErrorTriggerService errorTriggerService;

    public NotificationService(JavaMailSender mailSender, EmailProperties emailProperties, ErrorTriggerService errorTriggerService) {
        this.mailSender = mailSender;
        this.emailProperties = emailProperties;
        this.errorTriggerService = errorTriggerService;
    }

    @Retryable(
            value = {RuntimeException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public String sendNotification(String requestId, String accountId, Map<String, Object> accountDetails) {
        logger.info("Sending notification for requestId: {}, accountId: {}", requestId, accountId);

        try {
            String customerEmail = (String) accountDetails.get("customerEmail");
            if (!StringUtils.hasText(customerEmail)) {
                throw new IllegalArgumentException("Customer email is required for notification");
            }

            // Check for failure triggers
            if (errorTriggerService.shouldFail(customerEmail)) {
                logger.warn("Failure trigger detected for email: {}", customerEmail);
                throw new RuntimeException("Email notification failed: bounce email detected");
            }

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            String customerName = (String) accountDetails.getOrDefault("customerName", "Customer");
            String customerId = (String) accountDetails.getOrDefault("customerId", "");
            String status = (String) accountDetails.getOrDefault("status", "ACTIVE");
            String accountType = (String) accountDetails.getOrDefault("accountType", "STANDARD");

            helper.setFrom(emailProperties.getFrom(), emailProperties.getFromName());
            helper.setTo(customerEmail);
            helper.setSubject("Welcome! Your Account Has Been Created");

            String htmlContent = buildEmailContent(customerName, accountId, customerId, status, accountType);
            helper.setText(htmlContent, true);

            mailSender.send(message);

            logger.info("Email notification sent successfully for requestId: {}, accountId: {}", requestId, accountId);
            return "DELIVERED";
        } catch (MessagingException e) {
            logger.error("Failed to send email notification for requestId: {}", requestId, e);
            throw new RuntimeException("Email notification failed: " + e.getMessage(), e);
        }
    }

    private String buildEmailContent(String customerName, String accountId, String customerId, 
                                    String status, String accountType) {
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "<meta charset='UTF-8'>" +
                "<style>" +
                "body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; max-width: 600px; margin: 0 auto; padding: 20px; }" +
                ".header { background-color: #4CAF50; color: white; padding: 20px; text-align: center; border-radius: 5px 5px 0 0; }" +
                ".content { background-color: #f9f9f9; padding: 20px; border: 1px solid #ddd; border-top: none; border-radius: 0 0 5px 5px; }" +
                ".account-details { background-color: white; padding: 15px; margin: 15px 0; border-left: 4px solid #4CAF50; }" +
                ".account-id { font-size: 24px; font-weight: bold; color: #4CAF50; margin: 10px 0; }" +
                ".footer { text-align: center; margin-top: 20px; color: #666; font-size: 12px; }" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<div class='header'><h1>Welcome to Our Platform!</h1></div>" +
                "<div class='content'>" +
                "<p>Dear " + customerName + ",</p>" +
                "<p>Your onboarding process has been completed successfully. We're excited to have you on board!</p>" +
                "<div class='account-details'>" +
                "<h2>Your Account Details</h2>" +
                "<div class='account-id'>Account ID: " + accountId + "</div>" +
                "<p><strong>Customer ID:</strong> " + customerId + "</p>" +
                "<p><strong>Status:</strong> " + status + "</p>" +
                "<p><strong>Account Type:</strong> " + accountType + "</p>" +
                "</div>" +
                "<p>You can now start using our services. If you have any questions, please don't hesitate to contact our support team.</p>" +
                "<p>Best regards,<br>Onboarding Team</p>" +
                "</div>" +
                "<div class='footer'><p>This is an automated message. Please do not reply to this email.</p></div>" +
                "</body>" +
                "</html>";
    }
}
