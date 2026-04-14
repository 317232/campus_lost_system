package com.campus.lostfound.common.utils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailUtils {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    /**
     * 发送验证码邮件
     */
    public void sendVerifyCode(String toEmail, String code) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("【校园失物招领系统】验证码确认");
            
            String text = String.format("您的验证码是：【 %s 】，有效期5分钟。请勿泄露。", code);
            message.setText(text);

            mailSender.send(message);
            log.info("邮件已发送至: {}", toEmail);
        } catch (Exception e) {
            log.error("邮件发送异常", e);
            throw new RuntimeException("邮件服务异常，请稍后重试");
        }
    }
}
