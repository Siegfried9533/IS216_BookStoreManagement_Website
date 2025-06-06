package com.rgbunny.security.services;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl {
    @Autowired
    private JavaMailSender mailSender;

    @Value("${from.email}")
    private String fromEmail;

    @Value("${app.reset-password-url}")
    private String resetPasswordBaseUrl;

    public static class EmailSendingException extends RuntimeException {
        public EmailSendingException(String message) {
            super(message);
        }
    }

    public void sendPasswordResetEmail(String toEmail, String token) throws EmailSendingException {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message);
            String resetLink = resetPasswordBaseUrl + "?token=" + token;
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Password Reset Request");
            helper.setText(buildEmailContent(resetLink), true);
            mailSender.send(message);
            System.out.println("Password reset email sent to: " + toEmail);
        } catch (MessagingException e) {
            System.out.println("Failed to send password reset email to: " + toEmail + e);
            throw new EmailSendingException("Failed to send password reset email");
        }
    }

    private String buildEmailContent(String resetLink) {
        return "<a style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;'>"
                + "<h2 style='color: #2d3748;'>Password Reset Request</h2>"
                + "<p>We received a request to reset your password. Click the link below to proceed:</p>"
                + "<p><a href='" + resetLink
                + "' style='background-color: #4299e1; color: white; padding: 10px 20px; text-decoration: none; border-radius: 4px; display: inline-block;'>Reset Password</a></p>"
                + "<p>If you didn't request this password reset, please ignore this email.</p>"
                + "<p style='color: #718096; font-size: 0.9em;'>This link will expire in 1 hour.</p>"
                + "</a>";
    }
}
