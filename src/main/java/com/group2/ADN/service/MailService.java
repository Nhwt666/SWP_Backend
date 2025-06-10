package com.group2.ADN.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender mailSender;

    public void sendOtpEmail(String to, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Mã Xác Thực");
        message.setText("Kính gửi Quý khách,\n\nMã OTP của bạn là: " + otp + ". Vui lòng không chia sẻ mã này với bất kỳ ai để đảm bảo an toàn tài khoản.\n\nTrân trọng,\nĐội ngũ Hỗ trợ");        mailSender.send(message);
    }
}
