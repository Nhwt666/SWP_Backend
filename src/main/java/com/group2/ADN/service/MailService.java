package com.group2.ADN.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;
import org.springframework.mail.javamail.MimeMessageHelper;
import jakarta.mail.internet.MimeMessage;
import java.io.File;

@Service
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender mailSender;

    public void sendOtpEmail(String to, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Mã Xác Thực");
        message.setText("Kính gửi Quý khách,\n\nMã OTP của bạn là: " + otp + ". Vui lòng không chia sẻ mã này với bất kỳ ai để đảm bảo an toàn tài khoản.\n\nTrân trọng,\nĐội ngũ Hỗ trợ");
        mailSender.send(message);
    }

    // Gửi mail kèm file PDF kết quả
    public void sendResultWithAttachment(String to, String subject, String text, File pdfFile) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(text);
            helper.addAttachment("KetQuaXetNghiem.pdf", pdfFile);
            mailSender.send(message);
        } catch (Exception e) {
            throw new RuntimeException("Gửi mail thất bại", e);
        }
    }

    public void sendResultNotification(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        mailSender.send(message);
    }
}
