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

import java.io.UnsupportedEncodingException;
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
        return sendNotification(requestId, accountId, accountDetails, EmailType.ACCOUNT_CREATED);
    }

    @Retryable(
            value = {RuntimeException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public String sendNotification(String requestId, String accountId, Map<String, Object> accountDetails, EmailType emailType) {
        logger.info("Sending {} notification for requestId: {}, accountId: {}", emailType, requestId, accountId);

        try {
            String customerEmail = (String) accountDetails.get("customerEmail");
            if (!StringUtils.hasText(customerEmail)) {
                throw new IllegalArgumentException("Customer email is required for notification");
            }

            // Check for failure triggers (only for account created emails to avoid false failures during workflow)
            if (emailType == EmailType.ACCOUNT_CREATED && errorTriggerService.shouldFail(customerEmail)) {
                logger.warn("Failure trigger detected for email: {}", customerEmail);
                throw new RuntimeException("Email notification failed: bounce email detected");
            }

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            String customerName = (String) accountDetails.getOrDefault("customerName", "Customer");
            String customerId = (String) accountDetails.getOrDefault("customerId", "");

            helper.setFrom(emailProperties.getFrom(), emailProperties.getFromName());
            helper.setTo(customerEmail);

            String htmlContent;
            String subject;

            switch (emailType) {
                case KYC_INITIATED:
                    subject = "KYC Verification Started";
                    htmlContent = buildKycInitiatedEmailContent(customerName, customerId);
                    break;
                case KYC_SUCCESSFUL:
                    subject = "KYC Verification Successful";
                    htmlContent = buildKycSuccessfulEmailContent(customerName, customerId);
                    break;
                case ACCOUNT_CREATED:
                    String status = (String) accountDetails.getOrDefault("status", "ACTIVE");
                    String accountType = (String) accountDetails.getOrDefault("accountType", "STANDARD");
                    subject = "Welcome! Your Account Has Been Created";
                    htmlContent = buildAccountCreatedEmailContent(customerName, accountId, customerId, status, accountType);
                    break;
                default:
                    subject = "Onboarding Update";
                    htmlContent = buildKycInitiatedEmailContent(customerName, customerId);
            }

            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);

            logger.info("{} email notification sent successfully for requestId: {}", emailType, requestId);
            return "DELIVERED";
        } catch (MessagingException e) {
            logger.error("Failed to send {} email notification for requestId: {}", emailType, requestId, e);
            throw new RuntimeException("Email notification failed: " + e.getMessage(), e);
        } catch (UnsupportedEncodingException e) {
            logger.error("Failed to send {} email notification for requestId: {}", emailType, requestId, e);
            throw new RuntimeException("Email notification failed: Unsupported encoding");
        }
    }

    private String buildKycInitiatedEmailContent(String customerName, String customerId) {
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head><meta charset='UTF-8'><style>" +
                "body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; max-width: 600px; margin: 0 auto; padding: 20px; }" +
                ".header { background-color: #2196F3; color: white; padding: 20px; text-align: center; border-radius: 5px 5px 0 0; }" +
                ".content { background-color: #f9f9f9; padding: 20px; border: 1px solid #ddd; border-top: none; border-radius: 0 0 5px 5px; }" +
                ".footer { text-align: center; margin-top: 20px; color: #666; font-size: 12px; }" +
                "</style></head>" +
                "<body>" +
                "<div class='header'><h1>KYC Verification Started</h1></div>" +
                "<div class='content'>" +
                "<p>Dear " + customerName + ",</p>" +
                "<p>Thank you for starting your onboarding process. Your KYC (Know Your Customer) verification has been initiated.</p>" +
                "<p>We are now reviewing your documents and information. You will receive an update once the verification is complete.</p>" +
                "<p>Customer ID: <strong>" + customerId + "</strong></p>" +
                "<p>If you have any questions, please don't hesitate to contact our support team.</p>" +
                "<p>Best regards,<br>Onboarding Team</p>" +
                "</div>" +
                "<div class='footer'><p>This is an automated message. Please do not reply to this email.</p></div>" +
                "</body></html>";
    }

    private String buildKycSuccessfulEmailContent(String customerName, String customerId) {
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head><meta charset='UTF-8'><style>" +
                "body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; max-width: 600px; margin: 0 auto; padding: 20px; }" +
                ".header { background-color: #4CAF50; color: white; padding: 20px; text-align: center; border-radius: 5px 5px 0 0; }" +
                ".content { background-color: #f9f9f9; padding: 20px; border: 1px solid #ddd; border-top: none; border-radius: 0 0 5px 5px; }" +
                ".footer { text-align: center; margin-top: 20px; color: #666; font-size: 12px; }" +
                "</style></head>" +
                "<body>" +
                "<div class='header'><h1>KYC Verification Successful</h1></div>" +
                "<div class='content'>" +
                "<p>Dear " + customerName + ",</p>" +
                "<p>Great news! Your KYC verification has been completed successfully.</p>" +
                "<p>Your documents and information have been verified. We are now proceeding with the next steps of your onboarding process.</p>" +
                "<p>Customer ID: <strong>" + customerId + "</strong></p>" +
                "<p>You will receive another notification once your account has been created.</p>" +
                "<p>Best regards,<br>Onboarding Team</p>" +
                "</div>" +
                "<div class='footer'><p>This is an automated message. Please do not reply to this email.</p></div>" +
                "</body></html>";
    }

    private String buildAccountCreatedEmailContent(String customerName, String accountId, String customerId,
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
