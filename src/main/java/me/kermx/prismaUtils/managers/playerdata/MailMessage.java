package me.kermx.prismaUtils.managers.playerdata;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class MailMessage {
    private final UUID sender;
    private final String senderName;
    private final String message;
    private final LocalDateTime timestamp;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public MailMessage(UUID sender, String senderName, String message) {
        this.sender = sender;
        this.senderName = senderName;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }

    public UUID getSender() {
        return sender;
    }

    public String getSenderName() {
        return senderName;
    }

    public String getMessage() {
        return message;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getFormattedTimestamp() {
        return timestamp.format(FORMATTER);
    }
}
