package com.lexsecura.application.port;

public interface EmailPort {

    void send(String to, String subject, String htmlBody);

    boolean isAvailable();
}
