package com.yudillo.api.global.mail;

public interface EmailSender {

    void send(String to, String subject, String body);
}
