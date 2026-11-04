package com.yudillo.api.global.mail;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * 개발용. 메일을 보내지 않고 콘솔에 내용을 찍는다. SMTP 설정 없이 인증 흐름을 확인할 수 있다.
 * prod 를 제외한 모든 프로필(local, test)에서 쓰인다.
 */
@Slf4j
@Component
@Profile("!prod")
public class LoggingEmailSender implements EmailSender {

    @Override
    public void send(String to, String subject, String body) {
        log.info("""

                ===== MAIL (발송하지 않음) =====
                To      : {}
                Subject : {}
                {}
                ================================""", to, subject, body);
    }
}
